package com.github.fangzhengjin.common.autoconfigure.quartz

import org.reflections.Reflections
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.util.StringUtils

/**
 * @version V1.0
 * @title: QuartzManagerProperties
 * @package com.github.fangzhengjin.common.autoconfigure.quartz
 * @description:
 * @author fangzhengjin
 * @date 2019/1/30 17:49
 */
@ConfigurationProperties("customize.common.quartz")
class QuartzManagerProperties {
    private companion object {
        private val basePackage: String =
            try {
                val typesAnnotatedWith = Reflections().getTypesAnnotatedWith(SpringBootApplication::class.java)
                val `package` = typesAnnotatedWith.firstOrNull()?.`package`
                if (`package` != null) `package`.name else "com.github.fangzhengjin"
            } catch (e: Exception) {
                ""
            }
    }

    var cacheQuartzManagerException: Boolean = true
    var enableController: Boolean = true
    var baseUrl: String = "/task"
    var scanExecJobPackages: String = ""
        get() {
            if (StringUtils.isEmpty(field)) {
                field = basePackage
            }
            return field
        }
}