pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        gradlePluginPortal()
    }
    plugins {
        id("io.spring.dependency-management").version("1.1.7")
        id("org.springframework.boot").version("3.3.12")
        id("org.jetbrains.kotlin.jvm").version("2.1.21")
    }
}

rootProject.name = "clever-examples"
include("clever-example-javalin")
include("clever-example-spring")
include("clever-task-example")
