package com.github.fangzhengjin.common.component.quartz.interceptor

import com.github.fangzhengjin.common.component.quartz.exception.QuartzManagerException
import com.github.fangzhengjin.common.core.entity.HttpResult
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.servlet.http.HttpServletRequest

/**
 * @version V1.0
 * @title: QuartzManagerExceptionHandler
 * @package com.github.fangzhengjin.common.component.quartz.interceptor
 * @description: Quartz异常处理
 * @author fangzhengjin
 * @date 2019/1/30 17:19
 */
@ConditionalOnProperty(value = ["customize.common.quartz.catchQuartzManagerException"], matchIfMissing = true)
@RestControllerAdvice("com.github.fangzhengjin.common.component.quartz")
class QuartzManagerExceptionHandler {
    private companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(QuartzManagerExceptionHandler::class.java)
    }

    /**
     * 处理QuartzManagerException定义异常
     */
    @ExceptionHandler(QuartzManagerException::class)
    fun handleQuartzManagerException(
        e: QuartzManagerException,
        request: HttpServletRequest
    ): ResponseEntity<HttpResult<String>> {
        logger.error("QuartzManagerException message: ${e.message}", e)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON_UTF8
        val responseContext = HttpResult.fail<String>(code = HttpStatus.INTERNAL_SERVER_ERROR.value(), message = e.message)
        return ResponseEntity(responseContext, headers, HttpStatus.OK)
    }
}