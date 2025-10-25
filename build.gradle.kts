import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.axiom.release)
}

group = "com.pswidersk"

version = scmVersion.version

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation(libs.bundles.ktor)
    implementation(libs.logback.classic)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}

val buildId: String = "${project.version}-${System.currentTimeMillis()}"

tasks.processResources {
    filesMatching("static/index.html") {
        filter<ReplaceTokens>(
            "tokens" to mapOf(
                "buildId" to buildId
            )
        )
    }
}
