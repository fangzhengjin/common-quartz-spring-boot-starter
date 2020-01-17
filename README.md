# common-quartz-spring-boot-starter

[![Codecov branch](https://img.shields.io/codecov/c/github/fangzhengjin/common-quartz-spring-boot-starter/master.svg?logo=codecov&style=flat-square)](https://codecov.io/gh/fangzhengjin/common-quartz-spring-boot-starter)
[![Build Status](https://img.shields.io/travis/com/fangzhengjin/common-quartz-spring-boot-starter/master.svg?style=flat-square)](https://travis-ci.com/fangzhengjin/common-quartz-spring-boot-starter)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.fangzhengjin/common-quartz-spring-boot-starter.svg?style=flat-square&color=brightgreen)](https://maven-badges.herokuapp.com/maven-central/com.github.fangzhengjin/common-quartz-spring-boot-starter/)
[![Bintray](https://img.shields.io/bintray/v/fangzhengjin/maven/common-quartz-spring-boot-starter.svg?style=flat-square&color=blue)](https://bintray.com/fangzhengjin/maven/common-quartz-spring-boot-starter/_latestVersion)
[![License](https://img.shields.io/github/license/fangzhengjin/common-quartz-spring-boot-starter.svg?style=flat-square&color=blue)](https://www.gnu.org/licenses/gpl-3.0.txt)
[![SpringBootVersion](https://img.shields.io/badge/SpringBoot-2.2.3-heightgreen.svg?style=flat-square)](https://spring.io/projects/spring-boot)

```groovy
dependencies {
    implementation "com.github.fangzhengjin:common-quartz-spring-boot-starter:version"
}
```

使用前需配置quartz
配置样例
```yaml
spring:
  #QuartZ定时任务
  quartz:
    job-store-type: jdbc
    jdbc:
      #手动初始化数据库
      #脚本位置：org.quartz.impl.jdbcjobstore
      initialize-schema: never
    #相关属性配置
    properties:
      org:
        quartz:
          scheduler:
            #调度器实例名称
            instanceName: clusteredScheduler
            #调度器实例编号自动生成
            instanceId: AUTO
          jobStore:
            class: org.quartz.impl.jdbcjobstore.JobStoreTX
            #数据库
            #MYSQL - org.quartz.impl.jdbcjobstore.StdJDBCDelegate
            #PostgreSQL - org.quartz.impl.jdbcjobstore.PostgreSQLDelegate
            driverDelegateClass: org.quartz.impl.jdbcjobstore.oracle.OracleDelegate
            #数据库表前缀
            tablePrefix: QRTZ_
            #是否开启集群模式
            isClustered: true
            #集群监测间隔
            clusterCheckinInterval: 10000
            #是否使用quartz.properties文件配置
            useProperties: false
          threadPool:
            #ThreadPool 实现的类名
            class: org.quartz.simpl.SimpleThreadPool
            #线程数量
            threadCount: 10
            #线程优先级
            threadPriority: 5
            #自创建父线程
            threadsInheritContextClassLoaderOfInitializingThread: true
```

自定义Job需要使用@QuartzJobDescription注解修饰并描述用途

# 配置说明 - application.yml

样例均为默认值

```yaml
customize:
  common:
    quartz:
      #内置Controller基础地址
      baseUrl: /task
      #是否开启内置Controller
      enableController: true
      #是否在Swagger中展示QuartzControllerApi
      showInSwagger: false
      #是否使用内置异常处理器处理QuartzManagerException异常
      catchQuartzManagerException: true
      #任务执行器扫描路径，如不配置则默认扫描@SpringBootApplication修饰的启动类下的子包
      scanExecJobPackages: youBasePackage
```
因组件使用reflections进行包扫描，启动过程中会出现大量reflections警告，如需屏蔽警告请添加以下配置项
```yaml
logging:
  level:
    org.reflections: error
```
