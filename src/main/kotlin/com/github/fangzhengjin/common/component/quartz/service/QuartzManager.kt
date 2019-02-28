package com.github.fangzhengjin.common.component.quartz.service

import com.github.fangzhengjin.common.autoconfigure.quartz.QuartzManagerProperties
import com.github.fangzhengjin.common.component.quartz.annotation.QuartzJobDescription
import com.github.fangzhengjin.common.component.quartz.exception.QuartzManagerException
import com.github.fangzhengjin.common.component.quartz.vo.QuartzJobInfo
import org.quartz.*
import org.quartz.core.jmx.JobDataMapSupport
import org.quartz.impl.matchers.GroupMatcher
import org.reflections.Reflections
import org.slf4j.LoggerFactory
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.util.StringUtils
import java.util.*

/**
 * QuartzManager
 */
object QuartzManager {
    /**
     * 日志对象实例
     */
    @JvmStatic
    private val logger = LoggerFactory.getLogger(this::class.java)
    @JvmStatic
    private lateinit var schedulerFactory: SchedulerFactoryBean
    @JvmStatic
    private lateinit var quartzManagerProperties: QuartzManagerProperties
    @JvmStatic
    private lateinit var scheduler: Scheduler

    @JvmStatic
    fun init(schedulerFactory: SchedulerFactoryBean, quartzManagerProperties: QuartzManagerProperties): QuartzManager {
        this.schedulerFactory = schedulerFactory
        this.quartzManagerProperties = quartzManagerProperties
        this.scheduler = schedulerFactory.scheduler
        return this
    }

    @JvmStatic
    fun getSchedulerFactory(): SchedulerFactoryBean = schedulerFactory

    @JvmStatic
    fun getQuartzManagerProperties(): QuartzManagerProperties = quartzManagerProperties

    @JvmStatic
    fun getScheduler(): Scheduler = scheduler


    @JvmStatic
    @JvmOverloads
    fun checkExists(triggerKey: TriggerKey, mustBeExists: Boolean = true, throwException: Boolean = true): Boolean {
        val exists = scheduler.checkExists(triggerKey)
        if (!exists && mustBeExists && throwException) throw QuartzManagerException("触发器【$triggerKey】不存在")
        if (exists && !mustBeExists && throwException) throw QuartzManagerException("触发器【$triggerKey】已存在")
        return exists
    }

    @JvmStatic
    @JvmOverloads
    fun checkExists(jobKey: JobKey, mustBeExists: Boolean = true, throwException: Boolean = true): Boolean {
        val exists = scheduler.checkExists(jobKey)
        if (!exists && mustBeExists && throwException) throw QuartzManagerException("任务【$jobKey】不存在")
        if (exists && !mustBeExists && throwException) throw QuartzManagerException("任务【$jobKey】已存在")
        return exists
    }

    /**
     * 扫描可使用的任务执行器
     */
    @JvmStatic
    fun jobExecList(): HashMap<String, String> {
        try {
            val jobDesc = hashMapOf<String, String>()
            val subTypes =
                Reflections(quartzManagerProperties.scanExecJobPackages.split(","))
                    .getTypesAnnotatedWith(QuartzJobDescription::class.java)
            subTypes.forEach {
                it.annotations.forEach { annotation ->
                    if (annotation is QuartzJobDescription) {
                        jobDesc[it.name] = annotation.description
                    }
                }
            }
            return jobDesc
        } catch (e: Exception) {
            logger.error(e.message, e)
            if (e is QuartzManagerException) throw e
            throw QuartzManagerException("扫描可使用的执行器失败")
        }
    }

    @JvmStatic
    fun jobList(): ArrayList<QuartzJobInfo> {
        var cacheJobKey: JobKey? = null
        try {
            val list = arrayListOf<QuartzJobInfo>()
            for (jobGroupName in scheduler.jobGroupNames) {
                // 根据任务组名称查询所有任务
                for (jobKey in scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroupName))) {
                    cacheJobKey = jobKey
                    val jobName = jobKey.name
                    val jobDetail = scheduler.getJobDetail(jobKey)
                    list.add(
                        QuartzJobInfo(
                            jobName = jobName,
                            jobGroupName = jobGroupName,
                            jobClassName = jobDetail.jobClass.name,
                            jobDescription = jobDetail.description,
                            jobDataMap = jobDetail.jobDataMap
                            //                        triggers = QuartzJobInfo.QuartzTrigger.generator(jobKey, scheduler)
                        ).also {
                            it.triggers = QuartzJobInfo.QuartzTrigger.generator(jobKey, scheduler)
                        })
                }
            }
            return list
        } catch (e: Exception) {
            logger.error(e.message, e)
            if (e is QuartzManagerException) throw e
            if (e.cause is ClassNotFoundException) throw QuartzManagerException("任务【${cacheJobKey}】的执行器【${e.cause?.message}】未找到")
            throw QuartzManagerException("列表查询失败")
        }
    }

    /**
     * @Description: 添加一个定时任务
     * @param quartzJobInfo 任务信息
     */
    @JvmStatic
    fun addJob(quartzJobInfo: QuartzJobInfo) {
        try {
            Class.forName(quartzJobInfo.jobClassName)
            checkExists(quartzJobInfo.jobKey, false)
            quartzJobInfo.triggers.forEach { checkExists(it.key, false) }
            // 任务执行类
            val jobBuilder = JobBuilder.newJob(quartzJobInfo.jobClass)
            if (!StringUtils.isEmpty(quartzJobInfo.jobName) && !StringUtils.isEmpty(quartzJobInfo.jobGroupName)) {
                // 任务名 任务组
                jobBuilder.withIdentity(quartzJobInfo.jobKey)
            }
            if (!StringUtils.isEmpty(quartzJobInfo.jobDescription)) {
                // 任务描述
                jobBuilder.withDescription(quartzJobInfo.jobDescription)
            }
            if (!quartzJobInfo.jobDataMap.isNullOrEmpty()) {
                jobBuilder.setJobData(JobDataMapSupport.newJobDataMap(quartzJobInfo.jobDataMap))
            }
            val jobDetail = jobBuilder.build()
//            val trigger = quartzJobInfo.triggers[0].trigger
            // 调度容器设置JobDetail和Trigger
            scheduler.scheduleJob(jobDetail, quartzJobInfo.getCronTriggers().toMutableSet(), false)
            // 启动
            start()
        } catch (e: Exception) {
            logger.error(e.message, e)
            if (e is QuartzManagerException) throw e
            if (e is ClassNotFoundException) throw QuartzManagerException("任务执行器【${quartzJobInfo.jobClassName}】不存在")
            throw QuartzManagerException("任务创建失败")
        }
    }

    /**
     * 为任务添加触发器
     */
    @JvmStatic
    fun addTrigger(quartzJobInfo: QuartzJobInfo) {
        try {
            checkExists(quartzJobInfo.jobKey, true)
            val existsList =
                quartzJobInfo.triggers.filter { checkExists(it.key, false, false) }.map { it.key.toString() }
                    .toMutableList()
            if (existsList.isNotEmpty()) throw RuntimeException("触发器【${existsList.joinToString()}】已存在")
            val jobDetail = scheduler.getJobDetail(quartzJobInfo.jobKey)
            val oldTriggers = scheduler.getTriggersOfJob(quartzJobInfo.jobKey).toMutableList()
            if (oldTriggers.isNotEmpty()) {
                quartzJobInfo.triggers.addAll(QuartzJobInfo.QuartzTrigger.generator(oldTriggers))
            }
            scheduler.scheduleJob(jobDetail, quartzJobInfo.getCronTriggers().toMutableSet(), true)
        } catch (e: Exception) {
            logger.error(e.message, e)
            if (e is QuartzManagerException) throw e
            throw QuartzManagerException("添加触发器失败")
        }
    }

    /**
     * @Description: 修改触发器
     */
    @JvmStatic
    fun modifyTrigger(quartzTrigger: QuartzJobInfo.QuartzTrigger) {
        try {
            checkExists(quartzTrigger.key, true)
            var trigger: CronTrigger = scheduler.getTrigger(quartzTrigger.key) as CronTrigger
            val oldTime = trigger.cronExpression
            if (!oldTime.equals(quartzTrigger.cronExpression, ignoreCase = true)) {
                // 触发器
                val triggerBuilder = TriggerBuilder.newTrigger()
                // 触发器名,触发器组
                triggerBuilder.withIdentity(quartzTrigger.key)
                triggerBuilder.startNow()
                // 触发器时间设定
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(quartzTrigger.cronExpression))
                if (!StringUtils.isEmpty(quartzTrigger.triggerDescription)) {
                    // 触发器描述
                    triggerBuilder.withDescription(quartzTrigger.triggerDescription)
                }
                // 创建Trigger对象
                trigger = triggerBuilder.build() as CronTrigger
                // 修改一个任务的触发时间
                scheduler.rescheduleJob(quartzTrigger.key, trigger)
            }
        } catch (e: Exception) {
            logger.error(e.message, e)
            if (e is QuartzManagerException) throw e
            throw QuartzManagerException("触发器【${quartzTrigger.key}】更新失败")
        }

    }

    /**
     * @Description: 移除一个触发器
     */
    @JvmStatic
    fun remove(triggerKey: TriggerKey) {
        try {
            checkExists(triggerKey)
            pause(triggerKey)
            scheduler.unscheduleJob(triggerKey)
        } catch (e: Exception) {
            logger.error(e.message, e)
            if (e is QuartzManagerException) throw e
            throw QuartzManagerException("移除触发器【$triggerKey】失败")
        }
    }

    /**
     * @Description: 移除一个任务
     */
    @JvmStatic
    fun remove(jobKey: JobKey) {
        try {
            checkExists(jobKey)
//            val triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName)
//            scheduler.pause(triggerKey)// 停止触发器
//            scheduler.unscheduleJob(triggerKey)// 移除触发器
            for (trigger in scheduler.getTriggersOfJob(jobKey)) {
//                pause(trigger.key)
                remove(trigger.key)
            }
            scheduler.deleteJob(jobKey)// 删除任务
        } catch (e: Exception) {
            logger.error(e.message, e)
            if (e is QuartzManagerException) throw e
            throw QuartzManagerException("移除任务【$jobKey】失败")
        }
    }

    /**
     * @Description:暂停一个触发器
     * @param triggerKey
     */
    @JvmStatic
    fun pause(triggerKey: TriggerKey) {
        try {
            checkExists(triggerKey)
            scheduler.pauseTrigger(triggerKey)
        } catch (e: Exception) {
            logger.error(e.message, e)
            if (e is QuartzManagerException) throw e
            throw QuartzManagerException("暂停触发器【$triggerKey】失败")
        }
    }

    /**
     * @Description:恢复一个暂停的触发器
     * @param triggerKey
     */
    @JvmStatic
    fun resume(triggerKey: TriggerKey) {
        try {
            checkExists(triggerKey)
            scheduler.resumeTrigger(triggerKey)
        } catch (e: Exception) {
            logger.error(e.message, e)
            if (e is QuartzManagerException) throw e
            throw QuartzManagerException("恢复触发器【$triggerKey】失败")
        }
    }

    /**
     * @Description:暂停一个任务
     * @param jobKey
     */
    @JvmStatic
    fun pause(jobKey: JobKey) {
        try {
            checkExists(jobKey)
            scheduler.pauseJob(jobKey)
        } catch (e: Exception) {
            logger.error(e.message, e)
            if (e is QuartzManagerException) throw e
            throw QuartzManagerException("暂停任务【$jobKey】失败")
        }
    }

    /**
     * @Description:恢复一个暂停的任务
     * @param jobKey
     */
    @JvmStatic
    fun resume(jobKey: JobKey) {
        try {
            checkExists(jobKey)
            scheduler.resumeJob(jobKey)
        } catch (e: Exception) {
            logger.error(e.message, e)
            if (e is QuartzManagerException) throw e
            throw QuartzManagerException("恢复任务【$jobKey】失败")
        }
    }

    /**
     * 暂停所有任务
     */
    @JvmStatic
    fun pauseAll() {
        try {
            scheduler.pauseAll()
        } catch (e: Exception) {
            logger.error(e.message, e)
            throw QuartzManagerException("暂停所有任务失败")
        }
    }

    /**
     * 恢复所有暂停任务
     */
    @JvmStatic
    fun resumeAll() {
        try {
            scheduler.resumeAll()
        } catch (e: Exception) {
            logger.error(e.message, e)
            throw QuartzManagerException("恢复所有暂停任务失败")
        }
    }

    /**
     * @Description:启动所有定时任务
     */
    @JvmStatic
    fun start() {
        try {
            if (!scheduler.isStarted) {
                scheduler.start()
            }
        } catch (e: Exception) {
            logger.error(e.message, e)
            if (e is QuartzManagerException) throw e
            throw QuartzManagerException("启动任务调度器失败")
        }

    }

    /**
     * @Description:关闭所有定时任务
     */
    @JvmStatic
    fun shutdown() {
        try {
            if (!scheduler.isShutdown) {
                scheduler.shutdown()
            }
        } catch (e: Exception) {
            logger.error(e.message, e)
            if (e is QuartzManagerException) throw e
            throw QuartzManagerException("停止任务调度器失败")
        }
    }
}


//open class QuartzManager(
//    private val schedulerFactory: SchedulerFactoryBean,
//    private val quartzManagerProperties: QuartzManagerProperties
//) {
//    companion object {
//        /**
//         * 日志对象实例
//         */
//        @JvmStatic
//        private val logger = LoggerFactory.getLogger(QuartzManager::class.java)
//    }
//
//    private val scheduler: Scheduler by lazy { schedulerFactory.scheduler }
//
//    @JvmOverloads
//    fun checkExists(triggerKey: TriggerKey, mustBeExists: Boolean = true, throwException: Boolean = true): Boolean {
//        val exists = scheduler.checkExists(triggerKey)
//        if (!exists && mustBeExists && throwException) throw QuartzManagerException("触发器【$triggerKey】不存在")
//        if (exists && !mustBeExists && throwException) throw QuartzManagerException("触发器【$triggerKey】已存在")
//        return exists
//    }
//
//    @JvmOverloads
//    fun checkExists(jobKey: JobKey, mustBeExists: Boolean = true, throwException: Boolean = true): Boolean {
//        val exists = scheduler.checkExists(jobKey)
//        if (!exists && mustBeExists && throwException) throw QuartzManagerException("任务【$jobKey】不存在")
//        if (exists && !mustBeExists && throwException) throw QuartzManagerException("任务【$jobKey】已存在")
//        return exists
//    }
//
//    /**
//     * 扫描可使用的任务执行器
//     */
//    fun jobExecList(): HashMap<String, String> {
//        try {
//            val jobDesc = hashMapOf<String, String>()
//            val subTypes =
//                Reflections(quartzManagerProperties.scanExecJobPackages.split(","))
//                    .getTypesAnnotatedWith(QuartzJobDescription::class.java)
//            subTypes.forEach {
//                it.annotations.forEach { annotation ->
//                    if (annotation is QuartzJobDescription) {
//                        jobDesc[it.name] = annotation.description
//                    }
//                }
//            }
//            return jobDesc
//        } catch (e: Exception) {
//            logger.error(e.message, e)
//            if (e is QuartzManagerException) throw e
//            throw QuartzManagerException("扫描可使用的执行器失败")
//        }
//    }
//
//    fun jobList(): ArrayList<QuartzJobInfo> {
//        var cacheJobKey: JobKey? = null
//        try {
//            val list = arrayListOf<QuartzJobInfo>()
//            for (jobGroupName in scheduler.jobGroupNames) {
//                // 根据任务组名称查询所有任务
//                for (jobKey in scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroupName))) {
//                    cacheJobKey = jobKey
//                    val jobName = jobKey.name
//                    val jobDetail = scheduler.getJobDetail(jobKey)
//                    list.add(
//                        QuartzJobInfo(
//                            jobName = jobName,
//                            jobGroupName = jobGroupName,
//                            jobClassName = jobDetail.jobClass.name,
//                            jobDescription = jobDetail.description,
//                            jobDataMap = jobDetail.jobDataMap
//                            //                        triggers = QuartzJobInfo.QuartzTrigger.generator(jobKey, scheduler)
//                        ).also {
//                            it.triggers = QuartzJobInfo.QuartzTrigger.generator(jobKey, scheduler)
//                        })
//                }
//            }
//            return list
//        } catch (e: Exception) {
//            logger.error(e.message, e)
//            if (e is QuartzManagerException) throw e
//            if (e.cause is ClassNotFoundException) throw QuartzManagerException("任务【${cacheJobKey}】的执行器【${e.cause?.message}】未找到")
//            throw QuartzManagerException("列表查询失败")
//        }
//    }
//
//    /**
//     * @Description: 添加一个定时任务
//     * @param quartzJobInfo 任务信息
//     */
//    fun addJob(quartzJobInfo: QuartzJobInfo) {
//        try {
//            Class.forName(quartzJobInfo.jobClassName)
//            checkExists(quartzJobInfo.jobKey, false)
//            quartzJobInfo.triggers.forEach { checkExists(it.key, false) }
//            // 任务执行类
//            val jobBuilder = JobBuilder.newJob(quartzJobInfo.jobClass)
//            if (!StringUtils.isEmpty(quartzJobInfo.jobName) && !StringUtils.isEmpty(quartzJobInfo.jobGroupName)) {
//                // 任务名 任务组
//                jobBuilder.withIdentity(quartzJobInfo.jobKey)
//            }
//            if (!StringUtils.isEmpty(quartzJobInfo.jobDescription)) {
//                // 任务描述
//                jobBuilder.withDescription(quartzJobInfo.jobDescription)
//            }
//            if (!quartzJobInfo.jobDataMap.isNullOrEmpty()) {
//                jobBuilder.setJobData(JobDataMapSupport.newJobDataMap(quartzJobInfo.jobDataMap))
//            }
//            val jobDetail = jobBuilder.build()
////            val trigger = quartzJobInfo.triggers[0].trigger
//            // 调度容器设置JobDetail和Trigger
//            scheduler.scheduleJob(jobDetail, quartzJobInfo.getCronTriggers().toMutableSet(), false)
//            // 启动
//            start()
//        } catch (e: Exception) {
//            logger.error(e.message, e)
//            if (e is QuartzManagerException) throw e
//            if (e is ClassNotFoundException) throw QuartzManagerException("任务执行器【${quartzJobInfo.jobClassName}】不存在")
//            throw QuartzManagerException("任务创建失败")
//        }
//    }
//
//    /**
//     * 为任务添加触发器
//     */
//    fun addTrigger(quartzJobInfo: QuartzJobInfo) {
//        try {
//            checkExists(quartzJobInfo.jobKey, true)
//            val existsList =
//                quartzJobInfo.triggers.filter { checkExists(it.key, false, false) }.map { it.key.toString() }
//                    .toMutableList()
//            if (existsList.isNotEmpty()) throw RuntimeException("触发器【${existsList.joinToString()}】已存在")
//            val jobDetail = scheduler.getJobDetail(quartzJobInfo.jobKey)
//            val oldTriggers = scheduler.getTriggersOfJob(quartzJobInfo.jobKey).toMutableList()
//            if (oldTriggers.isNotEmpty()) {
//                quartzJobInfo.triggers.addAll(QuartzJobInfo.QuartzTrigger.generator(oldTriggers))
//            }
//            scheduler.scheduleJob(jobDetail, quartzJobInfo.getCronTriggers().toMutableSet(), true)
//        } catch (e: Exception) {
//            logger.error(e.message, e)
//            if (e is QuartzManagerException) throw e
//            throw QuartzManagerException("添加触发器失败")
//        }
//    }
//
//    /**
//     * @Description: 修改触发器
//     */
//    fun modifyTrigger(quartzTrigger: QuartzJobInfo.QuartzTrigger) {
//        try {
//            checkExists(quartzTrigger.key, true)
//            var trigger: CronTrigger = scheduler.getTrigger(quartzTrigger.key) as CronTrigger
//            val oldTime = trigger.cronExpression
//            if (!oldTime.equals(quartzTrigger.cronExpression, ignoreCase = true)) {
//                // 触发器
//                val triggerBuilder = TriggerBuilder.newTrigger()
//                // 触发器名,触发器组
//                triggerBuilder.withIdentity(quartzTrigger.key)
//                triggerBuilder.startNow()
//                // 触发器时间设定
//                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(quartzTrigger.cronExpression))
//                if (!StringUtils.isEmpty(quartzTrigger.triggerDescription)) {
//                    // 触发器描述
//                    triggerBuilder.withDescription(quartzTrigger.triggerDescription)
//                }
//                // 创建Trigger对象
//                trigger = triggerBuilder.build() as CronTrigger
//                // 修改一个任务的触发时间
//                scheduler.rescheduleJob(quartzTrigger.key, trigger)
//            }
//        } catch (e: Exception) {
//            logger.error(e.message, e)
//            if (e is QuartzManagerException) throw e
//            throw QuartzManagerException("触发器【${quartzTrigger.key}】更新失败")
//        }
//
//    }
//
//    /**
//     * @Description: 移除一个触发器
//     */
//    fun remove(triggerKey: TriggerKey) {
//        try {
//            checkExists(triggerKey)
//            pause(triggerKey)
//            scheduler.unscheduleJob(triggerKey)
//        } catch (e: Exception) {
//            logger.error(e.message, e)
//            if (e is QuartzManagerException) throw e
//            throw QuartzManagerException("移除触发器【$triggerKey】失败")
//        }
//    }
//
//    /**
//     * @Description: 移除一个任务
//     */
//    fun remove(jobKey: JobKey) {
//        try {
//            checkExists(jobKey)
////            val triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName)
////            scheduler.pause(triggerKey)// 停止触发器
////            scheduler.unscheduleJob(triggerKey)// 移除触发器
//            for (trigger in scheduler.getTriggersOfJob(jobKey)) {
////                pause(trigger.key)
//                remove(trigger.key)
//            }
//            scheduler.deleteJob(jobKey)// 删除任务
//        } catch (e: Exception) {
//            logger.error(e.message, e)
//            if (e is QuartzManagerException) throw e
//            throw QuartzManagerException("移除任务【$jobKey】失败")
//        }
//    }
//
//    /**
//     * @Description:暂停一个触发器
//     * @param triggerKey
//     */
//    fun pause(triggerKey: TriggerKey) {
//        try {
//            checkExists(triggerKey)
//            scheduler.pauseTrigger(triggerKey)
//        } catch (e: Exception) {
//            logger.error(e.message, e)
//            if (e is QuartzManagerException) throw e
//            throw QuartzManagerException("暂停触发器【$triggerKey】失败")
//        }
//    }
//
//    /**
//     * @Description:恢复一个暂停的触发器
//     * @param triggerKey
//     */
//    fun resume(triggerKey: TriggerKey) {
//        try {
//            checkExists(triggerKey)
//            scheduler.resumeTrigger(triggerKey)
//        } catch (e: Exception) {
//            logger.error(e.message, e)
//            if (e is QuartzManagerException) throw e
//            throw QuartzManagerException("恢复触发器【$triggerKey】失败")
//        }
//    }
//
//    /**
//     * @Description:暂停一个任务
//     * @param jobKey
//     */
//    fun pause(jobKey: JobKey) {
//        try {
//            checkExists(jobKey)
//            scheduler.pauseJob(jobKey)
//        } catch (e: Exception) {
//            logger.error(e.message, e)
//            if (e is QuartzManagerException) throw e
//            throw QuartzManagerException("暂停任务【$jobKey】失败")
//        }
//    }
//
//    /**
//     * @Description:恢复一个暂停的任务
//     * @param jobKey
//     */
//    fun resume(jobKey: JobKey) {
//        try {
//            checkExists(jobKey)
//            scheduler.resumeJob(jobKey)
//        } catch (e: Exception) {
//            logger.error(e.message, e)
//            if (e is QuartzManagerException) throw e
//            throw QuartzManagerException("恢复任务【$jobKey】失败")
//        }
//    }
//
//    /**
//     * 暂停所有任务
//     */
//    fun pauseAll() {
//        try {
//            scheduler.pauseAll()
//        } catch (e: Exception) {
//            logger.error(e.message, e)
//            throw QuartzManagerException("暂停所有任务失败")
//        }
//    }
//
//    /**
//     * 恢复所有暂停任务
//     */
//    fun resumeAll() {
//        try {
//            scheduler.resumeAll()
//        } catch (e: Exception) {
//            logger.error(e.message, e)
//            throw QuartzManagerException("恢复所有暂停任务失败")
//        }
//    }
//
//    /**
//     * @Description:启动所有定时任务
//     */
//    fun start() {
//        try {
//            if (!scheduler.isStarted) {
//                scheduler.start()
//            }
//        } catch (e: Exception) {
//            logger.error(e.message, e)
//            if (e is QuartzManagerException) throw e
//            throw QuartzManagerException("启动任务调度器失败")
//        }
//
//    }
//
//    /**
//     * @Description:关闭所有定时任务
//     */
//    fun shutdown() {
//        try {
//            if (!scheduler.isShutdown) {
//                scheduler.shutdown()
//            }
//        } catch (e: Exception) {
//            logger.error(e.message, e)
//            if (e is QuartzManagerException) throw e
//            throw QuartzManagerException("停止任务调度器失败")
//        }
//    }
//}