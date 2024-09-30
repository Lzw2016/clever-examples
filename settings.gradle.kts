pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        gradlePluginPortal()
    }
    plugins {
        id("io.spring.dependency-management").version("1.1.6")
        id("org.springframework.boot").version("3.3.4")
        id("org.jetbrains.kotlin.jvm").version("2.0.20")
    }
}

rootProject.name = "clever-examples"
include("clever-examples-javalin")
include("clever-examples-spring")
