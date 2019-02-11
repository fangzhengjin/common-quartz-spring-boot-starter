package com.github.fangzhengjin.common.component.quartz.annotation

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class QuartzJobDescription(
        val description: String
)