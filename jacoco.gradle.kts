apply<JacocoPlugin>()

configure<JacocoPluginExtension> {
    closureOf<JacocoReportsContainer> {
        xml.isEnabled = true
        html.isEnabled = false
        csv.isEnabled = false
    }
}

tasks["check"].dependsOn(tasks["jacocoTestReport"])