package com.github.fangzhengjin.common.component.quartz.controller

import com.github.fangzhengjin.common.component.quartz.service.QuartzManager
import com.github.fangzhengjin.common.component.quartz.vo.QuartzJobInfo
import com.github.fangzhengjin.common.component.quartz.vo.QuartzTrigger
import com.github.fangzhengjin.common.core.entity.HttpResult
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.quartz.JobKey
import org.quartz.TriggerKey
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*
import kotlin.collections.ArrayList


@Api(tags = ["定时任务"])
@ConditionalOnProperty(value = ["customize.common.quartz.enableController"], matchIfMissing = true)
@RestController
@RequestMapping(value = ["\${customize.common.quartz.baseUrl:/task}"], produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
class QuartzController {

    @ApiOperation("任务详情")
    @GetMapping("/{jobGroupName}/{jobName}")
    fun jobInfo(
            @PathVariable("jobName") jobName: String,
            @PathVariable("jobGroupName") jobGroupName: String
    ): HttpResult<QuartzJobInfo> {
        return HttpResult.ok(QuartzManager.getJobInfo(jobName, jobGroupName))
    }

    @ApiOperation("任务列表")
    @GetMapping("/list")
    fun taskList(): HttpResult<ArrayList<QuartzJobInfo>> {
        return HttpResult.ok(QuartzManager.jobList())
    }

    @ApiOperation("任务执行器列表")
    @PostMapping("/jobExec/list")
    fun jobExecList(@RequestBody scanExecJobPackages: List<String> = ArrayList()): HttpResult<HashMap<String, String>> {
        return HttpResult.ok(QuartzManager.jobExecList(*scanExecJobPackages.toTypedArray()))
    }

    @ApiOperation("添加任务")
    @PostMapping("/add")
    fun add(@Validated @RequestBody quartzJobInfo: QuartzJobInfo): HttpResult<String> {
        QuartzManager.addJob(quartzJobInfo)
        return HttpResult.ok("操作成功")
    }

    @ApiOperation("删除任务")
    @PostMapping("/delete")
    fun removeTask(@RequestBody jobKey: JobKey): HttpResult<String> {
        QuartzManager.remove(jobKey)
        return HttpResult.ok("操作成功")
    }

    @ApiOperation("暂停任务")
    @PostMapping("/pause")
    fun pauseTask(@RequestBody jobKey: JobKey): HttpResult<String> {
        QuartzManager.pause(jobKey)
        return HttpResult.ok("操作成功")
    }

    @ApiOperation("暂停所有任务")
    @PostMapping("/all/pause")
    fun pauseAllTask(): HttpResult<String> {
        QuartzManager.pauseAll()
        return HttpResult.ok("操作成功")
    }

    @ApiOperation("恢复任务")
    @PostMapping("/resume")
    fun resumeTask(@RequestBody jobKey: JobKey): HttpResult<String> {
        QuartzManager.resume(jobKey)
        return HttpResult.ok("操作成功")
    }

    @ApiOperation("恢复所有任务")
    @PostMapping("/all/resume")
    fun resumeAllTask(): HttpResult<String> {
        QuartzManager.resumeAll()
        return HttpResult.ok("操作成功")
    }

    @ApiOperation("新增触发器")
    @PostMapping("/trigger/add")
    fun addTrigger(@Validated @RequestBody quartzJobInfo: QuartzJobInfo): HttpResult<String> {
        QuartzManager.addTrigger(quartzJobInfo)
        return HttpResult.ok("操作成功")
    }

    @ApiOperation("删除触发器")
    @PostMapping("/trigger/delete")
    fun removeTrigger(@RequestBody triggerKey: TriggerKey): HttpResult<String> {
        QuartzManager.remove(triggerKey)
        return HttpResult.ok("操作成功")
    }

    @ApiOperation("修改触发器")
    @PostMapping("/trigger/modify")
    fun modifyTrigger(@Validated @RequestBody quartzTrigger: QuartzTrigger): HttpResult<String> {
        QuartzManager.modifyTrigger(quartzTrigger)
        return HttpResult.ok("操作成功")
    }

    @ApiOperation("暂停触发器")
    @PostMapping("/trigger/pause")
    fun pauseTrigger(@RequestBody triggerKey: TriggerKey): HttpResult<String> {
        QuartzManager.pause(triggerKey)
        return HttpResult.ok("操作成功")
    }

    @ApiOperation("恢复触发器")
    @PostMapping("/trigger/resume")
    fun resumeTask(@RequestBody triggerKey: TriggerKey): HttpResult<String> {
        QuartzManager.resume(triggerKey)
        return HttpResult.ok("操作成功")
    }
}