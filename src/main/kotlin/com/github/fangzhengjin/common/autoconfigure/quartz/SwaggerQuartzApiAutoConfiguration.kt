package com.github.fangzhengjin.common.autoconfigure.quartz

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket

@Configuration
@ConditionalOnClass(name = ["org.springframework.scheduling.quartz.SchedulerFactoryBean", "springfox.documentation.spring.web.plugins.Docket"])
class SwaggerQuartzApiAutoConfiguration {
    /**
     * 注册SwaggerApi
     */
    @Bean
    @ConditionalOnProperty(value = ["customize.common.quartz.showInSwagger"], matchIfMissing = false)
    fun swaggerQuartzApi(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .apiInfo(ApiInfoBuilder().title("QuartzApi").version("0.0.6").build())
                .groupName("quartz")
                .useDefaultResponseMessages(false)
                .forCodeGeneration(true)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.github.fangzhengjin.common.component.quartz.controller"))
                .apis(RequestHandlerSelectors.any())
                .build()
    }
}