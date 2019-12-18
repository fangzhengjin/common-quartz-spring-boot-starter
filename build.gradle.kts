import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val versionCode: String by extra
val fastJsonVersion: String by extra
val swaggerVersion: String by extra
val groupName: String by extra
val reflectionsVersion: String by extra

plugins {
    val kotlinVersion: String by System.getProperties()
    val springBootVersion: String by System.getProperties()
    val springDependencyManagementVersion: String by System.getProperties()
    kotlin("plugin.jpa") version kotlinVersion
    id("org.springframework.boot") version springBootVersion
    id("io.spring.dependency-management") version springDependencyManagementVersion
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("com.jfrog.bintray") version "1.8.4"
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}

apply {
    from("maven.gradle.kts")
    from("travis.gradle.kts")
    from("jacoco.gradle.kts")
    from("bintray.gradle")
}

group = groupName
version = versionCode
java.sourceCompatibility = JavaVersion.VERSION_1_8

tasks {
    bootJar {
        enabled = false
    }
    jar {
        enabled = true
    }
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }
    withType<Test> {
        useJUnitPlatform()
    }
}

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public/")
    mavenCentral()
}

dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    testCompile("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-quartz")
    api("org.reflections:reflections:$reflectionsVersion")
    api("com.github.fangzhengjin:common-core:0.0.5")
    //fastjson
//    implementation "com.alibaba:fastjson:${fastJsonVersion}"
    //Swagger2
    compileOnly("io.springfox:springfox-swagger2:$swaggerVersion")
    compileOnly("io.springfox:springfox-swagger-ui:$swaggerVersion")
    compileOnly("io.springfox:springfox-bean-validators:$swaggerVersion")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api("org.jetbrains.kotlin:kotlin-reflect")
    testRuntimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}