import java.text.SimpleDateFormat
import java.util.*

val groupName: String by extra
val artifactId: String by extra
val versionCode: String by extra

val bintrayUser: String by extra
val pomDescription: String by extra
val repoUrl: String by extra
val issueUrl: String by extra
val gitUrl: String by extra
val pomLicenceName: String by extra

val packageDir = "${groupName.replace('.', '/')}/$artifactId"

tasks.register("writeTravisDescriptor") {
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    val description = """
{
    "package": {
        "name": "$artifactId",
        "repo": "maven",
        "subject": "$bintrayUser",
        "desc": "$pomDescription",
        "website_url": "$repoUrl",
        "issue_tracker_url": "$issueUrl",
        "vcs_url": "$gitUrl",
        "github_use_tag_release_notes": true,
        "github_release_notes_file": "README.md",
        "licenses": "$pomLicenceName",
        "public_download_numbers": true
    },
    "version": {
        "name": "$versionCode",
        "desc": "$pomDescription",
        "released": "${simpleDateFormat.format(Date())}",
        "vcs_tag": "$versionCode",
        "gpgSign": true
    },
    "publish": true,
    "files": [
        {
            "includePattern": "build/libs/(.*\\.(jar|pom))",
            "uploadPattern": "$packageDir/$versionCode/$1",
            "matrixParams": {
                "override": 1
            }
        }
    ]
}
    """.trimIndent()
    val descriptionFile = File("$buildDir/descriptor.json")
    if (!descriptionFile.parentFile.exists()) descriptionFile.parentFile.mkdirs()
    descriptionFile.writeText(description)
}

tasks["build"].dependsOn(tasks["writeTravisDescriptor"])