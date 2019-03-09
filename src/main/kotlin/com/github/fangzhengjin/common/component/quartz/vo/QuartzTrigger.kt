package com.github.fangzhengjin.common.component.quartz.vo

import com.alibaba.fastjson.annotation.JSONField
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.quartz.*
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.util.StringUtils
import java.util.*

/**
 * @version V1.0
 * @title: QuartzTrigger
 * @package com.github.fangzhengjin.common.component.quartz.vo
 * @description:
 * @author fangzhengjin
 * @date 2019/3/4 16:22
 */

/**
 * @param triggerName           触发器名
 * @param triggerGroupName      触发器组名
 * @param cronExpression        时间设置，参考quartz说明文档
 * @param triggerDescription    触发器描述
 */
@ApiModel("Quartz定时任务触发器")
data class QuartzTrigger @JvmOverloads constructor(
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
        fun transformToQuartzTrigger(trigger: CronTrigger): QuartzTrigger {
            return QuartzTrigger(trigger.key.name, trigger.key.group, trigger.cronExpression, trigger.description)
        }

        @JvmStatic
        @JvmOverloads
        fun transformToQuartzTriggerAndFlushStatus(
            triggers: MutableList<Trigger>,
            scheduler: Scheduler? = null
        ): MutableList<QuartzTrigger> {
            val quartzTriggers: MutableList<QuartzTrigger> = mutableListOf()
            for (trigger in triggers) {
                if (trigger is CronTrigger) {
                    if (scheduler != null) {
                        // 转换触发器对象并刷新触发器状态
                        quartzTriggers.add(transformToQuartzTrigger(trigger).refreshStatus(scheduler))
                    } else {
                        quartzTriggers.add(transformToQuartzTrigger(trigger))
                    }
                }
            }
            return quartzTriggers
        }

        @JvmStatic
        fun getJobTriggersAndFlushStatus(jobKey: JobKey, scheduler: Scheduler): MutableList<QuartzTrigger> {
            val triggers = scheduler.getTriggersOfJob(jobKey)
            val quartzTriggers: MutableList<QuartzTrigger> = mutableListOf()
            for (trigger in triggers) {
                if (trigger is CronTrigger) {
                    quartzTriggers.add(transformToQuartzTrigger(trigger).refreshStatus(scheduler))
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
    var triggerState: Trigger.TriggerState? = null

    val triggerStateName: String
        @ApiModelProperty(name = "触发器状态说明", readOnly = true, accessMode = ApiModelProperty.AccessMode.READ_ONLY)
        get() {
            return when (this.triggerState) {
                Trigger.TriggerState.NONE -> "不存在"
                Trigger.TriggerState.NORMAL -> "正常"
                Trigger.TriggerState.PAUSED -> "暂停"
                Trigger.TriggerState.COMPLETE -> "完成"
                Trigger.TriggerState.ERROR -> "错误"
                Trigger.TriggerState.BLOCKED -> "阻塞"
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