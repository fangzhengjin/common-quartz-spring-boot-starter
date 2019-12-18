pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.1.1"
}

rootProject.name = "common-quartz-spring-boot-starter"