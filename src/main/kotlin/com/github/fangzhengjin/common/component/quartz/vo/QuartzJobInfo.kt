package com.github.fangzhengjin.common.component.quartz.vo

import com.alibaba.fastjson.annotation.JSONField
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.fangzhengjin.common.component.quartz.annotation.QuartzJobDescription
import com.github.fangzhengjin.common.component.quartz.exception.QuartzManagerException
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.quartz.CronTrigger
import org.quartz.Job
import org.quartz.JobKey
import javax.validation.constraints.NotBlank

/**
 * @param jobName               任务名
 * @param jobGroupName          任务组名
 * @param jobClassName          任务执行
 * @param jobDescription        任务描述
 * @param jobDataMap            任务参数
 */
@ApiModel("Quartz定时任务")
class QuartzJobInfo @JvmOverloads constructor(
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
    var jobDataMap: Map<String, Any>? = hashMapOf(),
    @field:[
    ApiModelProperty(name = "触发器")
    ]
    var triggers: MutableList<QuartzTrigger> = mutableListOf()
) {

    companion object {
        @JvmStatic
        fun jobExecCheck(jobClass: Class<*>) {
            if (!(jobClass.interfaces.contains(Job::class.java) && jobClass.isAnnotationPresent(QuartzJobDescription::class.java))) throw QuartzManagerException(
                "所选执行器不是标准的执行器"
            )
        }

        @JvmStatic
        fun jobExecCheck(jobClassName: String) {
            val clazz = Class.forName(jobClassName)
            jobExecCheck(clazz)
        }
    }

    /**
     * 任务执行
     */
    @get:[
    ApiModelProperty(
        name = "任务执行实体类",
        hidden = true,
        readOnly = true,
        accessMode = ApiModelProperty.AccessMode.READ_ONLY
    )
    JSONField(serialize = false)
    JsonIgnore
    ]
    val jobClass: Class<out Job> by lazy {
        val clazz = Class.forName(jobClassName)
        jobExecCheck(clazz)
        @Suppress("UNCHECKED_CAST")
        clazz as Class<out Job>
    }

    @JvmOverloads
    constructor(
        jobName: String,
        jobGroupName: String,
        jobClass: Class<out Job>,
        jobDescription: String? = null,
        jobDataMap: Map<String, Any>? = hashMapOf(),
        triggers: MutableList<QuartzTrigger> = mutableListOf()
    ) : this(jobName, jobGroupName, jobClass.name, jobDescription, jobDataMap, triggers) {
        jobExecCheck(jobClass)
    }

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
}