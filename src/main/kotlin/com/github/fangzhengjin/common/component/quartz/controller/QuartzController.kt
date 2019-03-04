package com.github.fangzhengjin.common.component.quartz.controller

import com.github.fangzhengjin.common.component.quartz.service.QuartzManager
import com.github.fangzhengjin.common.component.quartz.vo.QuartzJobInfo
import com.github.fangzhengjin.common.component.quartz.vo.QuartzTrigger
import com.github.fangzhengjin.common.core.entity.Result
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.quartz.JobKey
import org.quartz.TriggerKey
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.*
import java.util.*


@Api(tags = ["定时任务"])
@ConditionalOnProperty(value = ["customize.common.quartz.enableController"], matchIfMissing = true)
@RestController
@RequestMapping("\${customize.common.quartz.baseUrl:/task}")
class QuartzController {
    @ApiOperation("任务列表")
    @GetMapping("/list")
    fun taskList(): Result<ArrayList<QuartzJobInfo>> {
        return Result.ok(QuartzManager.jobList())
    }

    @ApiOperation("任务执行器列表")
    @GetMapping("/jobExec/list")
    fun jobExecList(): Result<HashMap<String, String>> {
        return Result.ok(QuartzManager.jobExecList())
    }

    @ApiOperation("添加任务")
    @PostMapping("/add")
    fun add(@RequestBody quartzJobInfo: QuartzJobInfo): Result<String> {
        QuartzManager.addJob(quartzJobInfo)
        return Result.ok("操作成功")
    }

    @ApiOperation("删除任务")
    @PostMapping("/delete")
    fun removeTask(@RequestBody jobKey: JobKey): Result<String> {
        QuartzManager.remove(jobKey)
        return Result.ok("操作成功")
    }

    @ApiOperation("暂停任务")
    @PostMapping("/pause")
    fun pauseTask(@RequestBody jobKey: JobKey): Result<String> {
        QuartzManager.pause(jobKey)
        return Result.ok("操作成功")
    }

    @ApiOperation("暂停所有任务")
    @PostMapping("/all/pause")
    fun pauseAllTask(): Result<String> {
        QuartzManager.pauseAll()
        return Result.ok("操作成功")
    }

    @ApiOperation("恢复任务")
    @PostMapping("/resume")
    fun resumeTask(@RequestBody jobKey: JobKey): Result<String> {
        QuartzManager.resume(jobKey)
        return Result.ok("操作成功")
    }

    @ApiOperation("恢复所有任务")
    @PostMapping("/all/resume")
    fun resumeAllTask(): Result<String> {
        QuartzManager.resumeAll()
        return Result.ok("操作成功")
    }

    @ApiOperation("新增触发器")
    @PostMapping("/trigger/add")
    fun addTrigger(@RequestBody quartzJobInfo: QuartzJobInfo): Result<String> {
        QuartzManager.addTrigger(quartzJobInfo)
        return Result.ok("操作成功")
    }

    @ApiOperation("删除触发器")
    @PostMapping("/trigger/delete")
    fun removeTrigger(@RequestBody triggerKey: TriggerKey): Result<String> {
        QuartzManager.remove(triggerKey)
        return Result.ok("操作成功")
    }

    @ApiOperation("修改触发器")
    @PostMapping("/trigger/modify")
    fun modifyTrigger(@RequestBody quartzTrigger: QuartzTrigger): Result<String> {
        QuartzManager.modifyTrigger(quartzTrigger)
        return Result.ok("操作成功")
    }

    @ApiOperation("暂停触发器")
    @PostMapping("/trigger/pause")
    fun pauseTrigger(@RequestBody triggerKey: TriggerKey): Result<String> {
        QuartzManager.pause(triggerKey)
        return Result.ok("操作成功")
    }

    @ApiOperation("恢复触发器")
    @PostMapping("/trigger/resume")
    fun resumeTask(@RequestBody triggerKey: TriggerKey): Result<String> {
        QuartzManager.resume(triggerKey)
        return Result.ok("操作成功")
    }
}