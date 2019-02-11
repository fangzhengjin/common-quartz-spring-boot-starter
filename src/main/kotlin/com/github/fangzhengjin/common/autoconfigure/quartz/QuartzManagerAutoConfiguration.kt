package com.github.fangzhengjin.common.autoconfigure.quartz

import com.github.fangzhengjin.common.component.quartz.service.QuartzManager
import com.github.fangzhengjin.common.component.quartz.vo.QuartzJobInfo
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.SchedulerFactoryBean

/**
 * 当项目使用Quartz时，如果Spring容器中不存在QuartzManager则自动创建
 */
@Configuration
@ConditionalOnClass(SchedulerFactoryBean::class)
@EnableConfigurationProperties(QuartzManagerProperties::class)
@ComponentScan("com.github.fangzhengjin.common.component.quartz")
class QuartzManagerAutoConfiguration {

    @Bean
    @ConditionalOnClass(QuartzJobInfo::class)
    @ConditionalOnMissingBean(QuartzManager::class)
    fun createQuartzManager(
        @Suppress("SpringJavaInjectionPointsAutowiringInspection")
        schedulerFactory: SchedulerFactoryBean,
        quartzManagerProperties: QuartzManagerProperties
    ): QuartzManager {
        return QuartzManager(schedulerFactory, quartzManagerProperties)
    }
}