package com.github.fangzhengjin.common.autoconfigure.quartz

import com.github.fangzhengjin.common.component.quartz.service.QuartzManager
import com.github.fangzhengjin.common.component.quartz.vo.QuartzJobInfo
import org.reflections.Reflections
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.util.StringUtils

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
        // 如果用户未设置任务执行器扫描目录，自动扫描入口类下的包
        if (StringUtils.isEmpty(quartzManagerProperties.scanExecJobPackages) || quartzManagerProperties.scanExecJobPackages == "com.github.fangzhengjin") {
            quartzManagerProperties.scanExecJobPackages =
                try {
                    val typesAnnotatedWith = Reflections().getTypesAnnotatedWith(SpringBootApplication::class.java)
                    val `package` = typesAnnotatedWith.firstOrNull()?.`package`
                    if (`package` != null) `package`.name else "com.github.fangzhengjin"
                } catch (e: Exception) {
                    "com.github.fangzhengjin"
                }
        }
        return QuartzManager.init(schedulerFactory, quartzManagerProperties)
    }
}