apply<MavenPublishPlugin>()

//GAV坐标
val groupName: String by extra
val artifactId: String by extra
val versionCode: String by extra

val githubRepo: String by extra
val repoUrl: String by extra
val gitUrl: String by extra
val issueUrl: String by extra
val pomPackaging: String by extra
val pomDescription: String by extra
val pomLicenceName: String by extra
val pomLicenceUrl: String by extra
val pomLicenceDist: String by extra
val pomDeveloperId: String by extra
val pomDeveloperName: String by extra
val pomDeveloperEmail: String by extra
val pomDeveloperUrl: String by extra

tasks.register<Jar>("sourcesJar") {
    //    from(sourceSets.main.get().allJava)
    from(project.the<SourceSetContainer>()["main"].allJava)
    archiveClassifier.set("sources")
}
tasks.withType<Javadoc> {
    options {
        encoding = "UTF-8"
        val charSet = "UTF-8"
        //忽略自定义标注
        val tags = arrayOf(
            "title:a:title",
            "package:a:package",
            "description:a:description",
            "date:a:date"
        )
    }
}
tasks.register<Jar>("javadocJar") {
    //    from(tasks.javadoc)
    from(tasks["javadoc"])
    archiveClassifier.set("javadoc")
}
tasks.withType<GenerateMavenPom>().all {
    destination = File("$buildDir/libs/$artifactId-$versionCode.pom")
}

configure<PublishingExtension> {
    publications {
        create("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            //GAV配置
            afterEvaluate {
                groupId = groupName
                artifactId = artifactId
                version = versionCode
            }
            //将动态依赖版本解析为实际引用版本 如 1.2.+ -> 1.2.10
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            //pom相关信息
            pom {
                name.set(artifactId)
                description.set(pomDescription)
                url.set(repoUrl)
                scm {
                    url.set(repoUrl)
                    connection.set(gitUrl)
                    developerConnection.set(gitUrl)
                }
                issueManagement {
                    system.set("github")
                    url.set(issueUrl)
                }
                developers {
                    developer {
                        id.set(pomDeveloperId)
                        name.set(pomDeveloperName)
                        email.set(pomDeveloperEmail)
                        url.set(pomDeveloperUrl)
                    }
                }
                licenses {
                    license {
                        name.set(pomLicenceName)
                        url.set(pomLicenceUrl)
                        distribution.set(pomLicenceDist)
                    }
                }
            }
        }
    }
}

artifacts {
    add("archives", tasks["sourcesJar"])
    add("archives", tasks["javadocJar"])
}

tasks["build"].dependsOn(tasks["generatePomFileForMavenJavaPublication"])