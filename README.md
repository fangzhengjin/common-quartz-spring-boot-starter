# common-quartz

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
      #是否使用内置异常处理器处理QuartzManagerException异常
      cacheQuartzManagerException: true
```