package com.github.fangzhengjin.common.component.quartz.service

import com.github.fangzhengjin.common.component.quartz.annotation.QuartzJobDescription
import com.github.fangzhengjin.common.component.quartz.vo.QuartzJobInfo
import com.github.fangzhengjin.common.component.quartz.vo.QuartzTrigger
import org.junit.Test
import org.junit.runner.RunWith
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.Trigger
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.Assert

@SpringBootTest
@RunWith(SpringRunner::class)
class QuartzManagerTest {

    private val quartzJobInfo = QuartzJobInfo(
        "jobName",
        "jobGroupName",
        TestJob::class.java,
        "jobDescription",
        hashMapOf(),
        mutableListOf(
            QuartzTrigger(
                "triggerName",
                "triggerGroupName",
                "0 0/30 * * * ?"
            )
        )
    )

    private val quartzTrigger = QuartzTrigger(
        "triggerName2",
        "triggerGroupName2",
        "0 0/30 * * * ?"
    )

    @Test
    fun quartzManagerTest() {
        QuartzManager.addJob(quartzJobInfo)
        Assert.notEmpty(QuartzManager.jobList(), "未能查出任务列表")

        QuartzManager.addTrigger(quartzJobInfo.jobKey, quartzTrigger)
        var jobInfo = QuartzManager.getJobInfo(quartzJobInfo.jobKey)
        org.junit.Assert.assertEquals(jobInfo.triggers.size, 2)

        quartzTrigger.cronExpression = "0 0/50 * * * ?"
        QuartzManager.modifyTrigger(quartzTrigger)
        jobInfo = QuartzManager.getJobInfo(quartzJobInfo.jobKey)
        org.junit.Assert.assertEquals(jobInfo.triggers.filter { it.cronExpression == "0 0/50 * * * ?" }.size, 1)

        Assert.isTrue(QuartzManager.jobExecList().containsValue("测试任务执行器"), "未扫描到任务执行器")

        QuartzManager.pause(quartzJobInfo.jobKey)
        jobInfo = QuartzManager.getJobInfo(quartzJobInfo.jobKey)
        org.junit.Assert.assertEquals(
            jobInfo.triggers.filter { it.triggerState == Trigger.TriggerState.PAUSED }.size,
            2
        )

        QuartzManager.resume(quartzJobInfo.jobKey)
        jobInfo = QuartzManager.getJobInfo(quartzJobInfo.jobKey)
        org.junit.Assert.assertEquals(
            jobInfo.triggers.filter { it.triggerState == Trigger.TriggerState.NORMAL }.size,
            2
        )

        QuartzManager.pause(quartzTrigger.key)
        jobInfo = QuartzManager.getJobInfo(quartzJobInfo.jobKey)
        org.junit.Assert.assertEquals(
            jobInfo.triggers.filter { it.triggerState == Trigger.TriggerState.PAUSED }.size,
            1
        )

        QuartzManager.resume(quartzTrigger.key)
        jobInfo = QuartzManager.getJobInfo(quartzJobInfo.jobKey)
        org.junit.Assert.assertEquals(
            jobInfo.triggers.filter { it.triggerState == Trigger.TriggerState.NORMAL }.size,
            2
        )

        QuartzManager.pauseAll()
        jobInfo = QuartzManager.getJobInfo(quartzJobInfo.jobKey)
        org.junit.Assert.assertEquals(
            jobInfo.triggers.filter { it.triggerState == Trigger.TriggerState.PAUSED }.size,
            2
        )

        QuartzManager.resumeAll()
        jobInfo = QuartzManager.getJobInfo(quartzJobInfo.jobKey)
        org.junit.Assert.assertEquals(
            jobInfo.triggers.filter { it.triggerState == Trigger.TriggerState.NORMAL }.size,
            2
        )
        QuartzManager.removeAll()
    }
}

@QuartzJobDescription("测试任务执行器")
class TestJob : Job {
    override fun execute(context: JobExecutionContext?) {
        println("我执行了")
    }
}