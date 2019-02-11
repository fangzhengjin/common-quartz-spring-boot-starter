package com.github.fangzhengjin.common.component.quartz.vo

import com.alibaba.fastjson.annotation.JSONField
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.fangzhengjin.common.component.quartz.annotation.QuartzJobDescription
import com.github.fangzhengjin.common.component.quartz.exception.QuartzManagerException
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.quartz.*
import org.quartz.Trigger.TriggerState
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.util.StringUtils
import java.util.*
import javax.validation.constraints.NotBlank

/**
 * @param jobName               任务名
 * @param jobGroupName          任务组名
 * @param jobClassName          任务执行
 * @param jobDescription        任务描述
 * @param jobDataMap            任务参数
 */
@ApiModel("Quartz定时任务")
class QuartzJobInfo(
    @field:[
    ApiModelProperty("任务名", required = true)
    NotBlank(message = "任务名称不能为空")
    ]
    var jobName: String,
    @field:[
    ApiModelProperty("任务组名", required = true)
    NotBlank(message = "任务组名称不能为空")
    ]
    var jobGroupName: String,
    @field:[
    ApiModelProperty("任务执行器", required = true)
    NotBlank(message = "任务执行器不能为空")
    ]
    var jobClassName: String,
    var jobDescription: String? = null,
    var jobDataMap: Map<String, Any>? = hashMapOf()
) {
    constructor(
        jobName: String,
        jobGroupName: String,
        jobClassName: String,
        jobDescription: String? = null,
        jobDataMap: Map<String, Any>? = hashMapOf(),
        triggers: MutableList<QuartzTrigger>? = mutableListOf()
    ) : this(jobName, jobGroupName, jobClassName, jobDescription, jobDataMap) {
        this.jobClassName = jobClassName
        this.triggers = triggers ?: mutableListOf()
        val clazz = Class.forName(jobClassName)
        if (clazz.interfaces.contains(Job::class.java) && clazz.isAnnotationPresent(QuartzJobDescription::class.java)) {
            @Suppress("UNCHECKED_CAST")
            this.jobClass = clazz as Class<out Job>
        } else {
            throw QuartzManagerException("所选执行器不是标准的执行器")
        }
    }

    constructor(
        jobName: String,
        jobGroupName: String,
        jobClass: Class<out Job>,
        jobDescription: String? = null,
        jobDataMap: Map<String, Any>? = hashMapOf(),
        triggers: MutableList<QuartzTrigger>? = mutableListOf()
    ) : this(jobName, jobGroupName, jobClass.name, jobDescription, jobDataMap) {
        this.triggers = triggers ?: mutableListOf()
        if (!jobClass.isAnnotationPresent(QuartzJobDescription::class.java)) throw QuartzManagerException("所选执行器不是标准的执行器")
        this.jobClass = jobClass
    }

    /**
     * 任务执行
     */
    @ApiModelProperty(
        name = "任务执行实体类",
        hidden = true,
        readOnly = true,
        accessMode = ApiModelProperty.AccessMode.READ_ONLY
    )
    @JSONField(serialize = false)
    @JsonIgnore
    var jobClass: Class<out Job>? = null
        get() {
            if (field == null) {
                val clazz = Class.forName(jobClassName)
                if (clazz.interfaces.contains(Job::class.java) && clazz.isAnnotationPresent(QuartzJobDescription::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    field = clazz as Class<out Job>
                } else {
                    throw QuartzManagerException("所选执行器不是标准的执行器")
                }
            }
            return field
        }
    /**
     * 触发器
     */
    @ApiModelProperty(name = "触发器")
    var triggers: MutableList<QuartzTrigger> = mutableListOf()

    val jobKey: JobKey
        @ApiModelProperty(hidden = true)
        @JSONField(serialize = false)
        @JsonIgnore
        get() = JobKey.jobKey(jobName, jobGroupName)

    @JSONField(serialize = false)
    @JsonIgnore
    fun getCronTriggers(): MutableList<CronTrigger> {
        return triggers.map { it.trigger as CronTrigger }.toMutableList()
    }


    /**
     * @param triggerName           触发器名
     * @param triggerGroupName      触发器组名
     * @param cronExpression        时间设置，参考quartz说明文档
     * @param triggerDescription    触发器描述
     */
    @ApiModel("Quartz定时任务触发器")
    class QuartzTrigger(
        @field:[
        ApiModelProperty("触发器名", required = true)
        ]
        var triggerName: String,
        @field:[
        ApiModelProperty("触发器组名", required = true)
        ]
        var triggerGroupName: String,
        @field:[
        ApiModelProperty("cron时间设置，参考quartz说明文档", required = true)
        ]
        var cronExpression: String,
        @field:[
        ApiModelProperty("触发器描述")
        ]
        var triggerDescription: String? = null
//            var triggerDataMap: Map<String, Any> = hashMapOf()
    ) {
        companion object {
            @JvmStatic
            fun generator(trigger: CronTrigger): QuartzTrigger {
                return QuartzTrigger(trigger.key.name, trigger.key.group, trigger.cronExpression, trigger.description)
            }

            @JvmStatic
            fun generator(triggers: MutableList<Trigger>, scheduler: Scheduler? = null): MutableList<QuartzTrigger> {
                val quartzTriggers: MutableList<QuartzTrigger> = mutableListOf()
                for (trigger in triggers) {
                    if (trigger is CronTrigger) {
                        if (scheduler != null) {
                            quartzTriggers.add(generator(trigger).refreshStatus(scheduler))
                        } else {
                            quartzTriggers.add(generator(trigger))
                        }
                    }
                }
                return quartzTriggers
            }

            @JvmStatic
            fun generator(triggers: MutableList<Trigger>): MutableList<QuartzTrigger> {
                val quartzTriggers: MutableList<QuartzTrigger> = mutableListOf()
                for (trigger in triggers) {
                    if (trigger is CronTrigger) {
                        quartzTriggers.add(generator(trigger))
                    }
                }
                return quartzTriggers
            }

            @JvmStatic
            fun generator(jobKey: JobKey, scheduler: Scheduler): MutableList<QuartzTrigger> {
                val triggers = scheduler.getTriggersOfJob(jobKey)
                val quartzTriggers: MutableList<QuartzTrigger> = mutableListOf()
                for (trigger in triggers) {
                    if (trigger is CronTrigger) {
                        quartzTriggers.add(generator(trigger).refreshStatus(scheduler))
                    }
                }
                return quartzTriggers
            }
        }

        val trigger: Trigger
            @ApiModelProperty(hidden = true)
            @JSONField(serialize = false)
            @JsonIgnore
            get() {
                // 触发器
                val triggerBuilder = TriggerBuilder.newTrigger()
                // 触发器名,触发器组
                if (!StringUtils.isEmpty(triggerName) && !StringUtils.isEmpty(triggerGroupName)) {
                    // 任务名 任务组
                    triggerBuilder.withIdentity(key)
                }
                if (!StringUtils.isEmpty(triggerDescription)) {
                    // 触发器描述
                    triggerBuilder.withDescription(triggerDescription)
                }
                triggerBuilder.startNow()
                if (!StringUtils.isEmpty(cronExpression)) {
                    // 触发器时间设定
                    triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                }
                // 创建Trigger对象
                return triggerBuilder.build() as CronTrigger
            }

        val key: TriggerKey
            @ApiModelProperty(hidden = true)
            @JSONField(serialize = false)
            @JsonIgnore
            get() = TriggerKey.triggerKey(triggerName, triggerGroupName)

        /**
         * 触发器状态枚举
         */
        @ApiModelProperty(name = "触发器状态枚举", readOnly = true, accessMode = ApiModelProperty.AccessMode.READ_ONLY)
        var triggerState: TriggerState? = null

        val triggerStateName: String
            @ApiModelProperty(name = "触发器状态说明", readOnly = true, accessMode = ApiModelProperty.AccessMode.READ_ONLY)
            get() {
                return when (this.triggerState) {
                    TriggerState.NONE -> "不存在"
                    TriggerState.NORMAL -> "正常"
                    TriggerState.PAUSED -> "暂停"
                    TriggerState.COMPLETE -> "完成"
                    TriggerState.ERROR -> "错误"
                    TriggerState.BLOCKED -> "阻塞"
                    null -> "未知"
                }
            }

//        /**
//         * 上一次执行时间
//         */
//        var previousExecTime: Date? = null
        /**
         * 下一次执行时间
         */
        val nextExecTime: Date
            @ApiModelProperty(name = "下一次执行时间", readOnly = true, accessMode = ApiModelProperty.AccessMode.READ_ONLY)
            @JSONField(name = "nextExecTime", format = "yyyy-MM-dd HH:mm:ss")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
            get() = CronExpression(cronExpression).getTimeAfter(Date())


        fun refreshStatus(scheduler: Scheduler): QuartzTrigger {
            triggerState = scheduler.getTriggerState(key)
            return this@QuartzTrigger
        }
    }
}