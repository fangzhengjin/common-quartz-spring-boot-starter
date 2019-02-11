package com.github.fangzhengjin.common.autoconfigure.quartz

import org.springframework.boot.context.properties.ConfigurationProperties

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
    var cacheQuartzManagerException: Boolean = true
    var enableController: Boolean = true
    var baseUrl: String = "/task"
    var scanExecJobPackages: String = "com.github.fangzhengjin"
}