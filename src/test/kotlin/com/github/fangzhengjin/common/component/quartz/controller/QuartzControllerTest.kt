package com.github.fangzhengjin.common.component.quartz.controller

import com.alibaba.fastjson.JSON
import com.github.fangzhengjin.common.core.entity.Result
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.util.AssertionErrors
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.util.Assert
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@RunWith(SpringRunner::class)
class QuartzControllerTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private val mockMvc: MockMvc by lazy {
        MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build()
    }

    @Test
    fun taskList() {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/task/list")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect {
                    val result = JSON.parseObject(it.response.contentAsString, Result::class.java)
                    AssertionErrors.assertEquals("code != 200", result.code, 200)
                    Assert.notNull(result.body, "body null")
                }
                .andDo(MockMvcResultHandlers.print()
                )
    }

    @Test
    fun jobExecList() {
    }

    @Test
    fun add() {
    }

    @Test
    fun removeTask() {
    }

    @Test
    fun pauseTask() {
    }

    @Test
    fun pauseAllTask() {
    }

    @Test
    fun resumeTask() {
    }

    @Test
    fun resumeAllTask() {
    }

    @Test
    fun addTrigger() {
    }

    @Test
    fun removeTrigger() {
    }

    @Test
    fun modifyTrigger() {
    }

    @Test
    fun pauseTrigger() {
    }

    @Test
    fun resumeTask1() {
    }
}