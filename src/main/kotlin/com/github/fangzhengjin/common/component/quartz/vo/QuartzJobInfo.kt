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
import javax.validation.Valid
import javax.validation.constraints.NotBlank

/**
 * @param jobName               任务名
 * @param jobGroupName          任务组名
 * @param jobClassName          任务执行
 * @param jobDescription        任务描述
 * @param jobDataMap            任务参数
 */
@ApiModel("Quartz定时任务")
class QuartzJobInfo {

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

    @ApiModelProperty("任务名", required = true)
    @NotBlank(message = "任务名称不能为空")
    var jobName: String? = null

    @ApiModelProperty("任务组名", required = true)
    @NotBlank(message = "任务组名称不能为空")
    var jobGroupName: String? = null

    @ApiModelProperty("任务执行器", required = true)
    @NotBlank(message = "任务执行器不能为空")
    var jobClassName: String? = null
        set(value) {
            if (value != null) {
                jobExecCheck(value)
            }
            field = value
        }
    var jobDescription: String? = null
    var jobDataMap: Map<String, Any> = hashMapOf()

    @ApiModelProperty(name = "触发器")
    @Valid
    var triggers: MutableList<QuartzTrigger> = mutableListOf()

    /**
     * 任务执行
     */
    val jobClass: Class<out Job>
        @ApiModelProperty(name = "任务执行实体类", hidden = true, readOnly = true, accessMode = ApiModelProperty.AccessMode.READ_ONLY)
        @JSONField(serialize = false)
        @JsonIgnore
        get() {
            val clazz = Class.forName(jobClassName)
//            jobExecCheck(clazz)
            @Suppress("UNCHECKED_CAST")
            return clazz as Class<out Job>
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