package com.github.fangzhengjin.common.component.quartz.controller

import com.github.fangzhengjin.common.component.quartz.service.QuartzManager
import com.github.fangzhengjin.common.component.quartz.vo.QuartzJobInfo
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
class QuartzController(
    private val quartzManager: QuartzManager
) {
//    @ApiAuthSkip
//    @ApiOperation("启动调度")
//    @PostMapping("/start")
//    fun start(): Result<String> {
//        quartzManager.start()
//        return Result.ok("操作成功")
//    }
//
//    /**
//     * 危险操作，停止后同一实例无法再次启动
//     */
//    @ApiAuthSkip
//    @ApiOperation("停止调度")
//    @PostMapping("/shutdown")
//    fun shutdown(): Result<String> {
//        quartzManager.shutdown()
//        return Result.ok("操作成功")
//    }

    @ApiOperation("任务列表")
    @GetMapping("/list")
    fun taskList(): Result<ArrayList<QuartzJobInfo>> {
        return Result.ok(quartzManager.jobList())
    }

    @ApiOperation("任务执行器列表")
    @GetMapping("/jobExec/list")
    fun jobExecList(): Result<HashMap<String, String>> {
        return Result.ok(quartzManager.jobExecList())
    }

    @ApiOperation("添加任务")
    @PostMapping("/add")
    fun add(@RequestBody quartzJobInfo: QuartzJobInfo): Result<String> {
        quartzManager.addJob(quartzJobInfo)
        return Result.ok("操作成功")
    }

    @ApiOperation("删除任务")
    @PostMapping("/delete")
    fun removeTask(@RequestBody jobKey: JobKey): Result<String> {
        quartzManager.remove(jobKey)
        return Result.ok("操作成功")
    }

    @ApiOperation("暂停任务")
    @PostMapping("/pause")
    fun pauseTask(@RequestBody jobKey: JobKey): Result<String> {
        quartzManager.pause(jobKey)
        return Result.ok("操作成功")
    }

    @ApiOperation("暂停所有任务")
    @PostMapping("/all/pause")
    fun pauseAllTask(): Result<String> {
        quartzManager.pauseAll()
        return Result.ok("操作成功")
    }

    @ApiOperation("恢复任务")
    @PostMapping("/resume")
    fun resumeTask(@RequestBody jobKey: JobKey): Result<String> {
        quartzManager.resume(jobKey)
        return Result.ok("操作成功")
    }

    @ApiOperation("恢复所有任务")
    @PostMapping("/all/resume")
    fun resumeAllTask(): Result<String> {
        quartzManager.resumeAll()
        return Result.ok("操作成功")
    }

    @ApiOperation("新增触发器")
    @PostMapping("/trigger/add")
    fun addTrigger(@RequestBody quartzJobInfo: QuartzJobInfo): Result<String> {
        quartzManager.addTrigger(quartzJobInfo)
        return Result.ok("操作成功")
    }

    @ApiOperation("删除触发器")
    @PostMapping("/trigger/delete")
    fun removeTrigger(@RequestBody triggerKey: TriggerKey): Result<String> {
        quartzManager.remove(triggerKey)
        return Result.ok("操作成功")
    }

    @ApiOperation("修改触发器")
    @PostMapping("/trigger/modify")
    fun modifyTrigger(@RequestBody quartzTrigger: QuartzJobInfo.QuartzTrigger): Result<String> {
        quartzManager.modifyTrigger(quartzTrigger)
        return Result.ok("操作成功")
    }

    @ApiOperation("暂停触发器")
    @PostMapping("/trigger/pause")
    fun pauseTrigger(@RequestBody triggerKey: TriggerKey): Result<String> {
        quartzManager.pause(triggerKey)
        return Result.ok("操作成功")
    }

    @ApiOperation("恢复触发器")
    @PostMapping("/trigger/resume")
    fun resumeTask(@RequestBody triggerKey: TriggerKey): Result<String> {
        quartzManager.resume(triggerKey)
        return Result.ok("操作成功")
    }
}