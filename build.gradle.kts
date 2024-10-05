@file:Suppress("ConstPropertyName")

import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import java.util.*

val localProperty = Properties().apply {
    val local = project.rootProject.file("local.properties")
    if (local.exists()) {
        local.inputStream().use { this.load(it) }
    }
}

val buildVersion = System.getenv("buildVersion") ?: project.properties["buildVersion"]
val buildSnapshot = (System.getenv("buildSnapshot") ?: project.properties["buildSnapshot"] as String).toBoolean()

object Ver {
    const val cleverVersion = "3.3.4.0-SNAPSHOT"
    const val springBootVersion = "3.3.4"
    const val springCloudVersion = "2023.0.3"
    const val kotlinVersion = "2.0.20"
    const val jettyVersion = "11.0.24"
    const val jmhVersion = "1.37"
}

buildscript {
    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/central")
        mavenCentral()
    }
    dependencies {
        classpath("org.apache.commons:commons-lang3:3.17.0")
        classpath("commons-io:commons-io:2.16.1")
    }
}

plugins {
    idea
    `java-library`
    `maven-publish`
    id("io.spring.dependency-management")
}

idea {
    project {
        jdkName = "17"
        languageLevel = IdeaLanguageLevel("17")
    }
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

allprojects {
    apply(plugin = "java-library")

    group = "org.clever"
    version = "${buildVersion}-${if (buildSnapshot) "SNAPSHOT" else "RELEASE"}"

    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://maven.aliyun.com/repository/public/")
        maven(url = "http://all.msvc.top:30005/api/packages/clever/maven/") {
            isAllowInsecureProtocol = true
        }
    }

    // 默认是24小时，gradle会检查一次依赖，可以设置每次build都进行检查
    configurations.all {
        // resolutionStrategy.cacheChangingModulesFor(0, "seconds")
    }

    java.sourceCompatibility = JavaVersion.VERSION_17
    java.targetCompatibility = JavaVersion.VERSION_17

    tasks.compileJava {
        options.encoding = "UTF-8"
        // options.isWarnings = false
        // options.isDeprecation = true
        options.compilerArgs.add("-parameters")
    }
    tasks.compileTestJava {
        options.encoding = "UTF-8"
        // options.isWarnings = false
        // options.isDeprecation = true
        options.compilerArgs.add("-parameters")
    }

    // 让Spring使用相同的Kotlin版本
    extra["kotlin.version"] = Ver.kotlinVersion
}

subprojects {
    apply(plugin = "idea")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "io.spring.dependency-management")

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${Ver.springBootVersion}")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${Ver.springCloudVersion}")
            mavenBom("org.eclipse.jetty:jetty-bom:${Ver.jettyVersion}")
        }

        dependencies {
            // clever
            dependency("org.clever:clever-spring:${Ver.cleverVersion}")
            dependency("org.clever:clever-core:${Ver.cleverVersion}")
            dependency("org.clever:clever-data-jdbc:${Ver.cleverVersion}")
            dependency("org.clever:clever-data-redis:${Ver.cleverVersion}")
            dependency("org.clever:clever-web:${Ver.cleverVersion}")
            dependency("org.clever:clever-security:${Ver.cleverVersion}")
            dependency("org.clever:clever-task:${Ver.cleverVersion}")
            dependency("org.clever:clever-task-ext:${Ver.cleverVersion}")
            // performance test
            dependency("org.openjdk.jmh:jmh-core:${Ver.jmhVersion}")
            dependency("org.openjdk.jmh:jmh-generator-annprocess:${Ver.jmhVersion}")
            // jakarta
            dependency("org.glassfish:jakarta.el:4.0.2")
            // time
            dependency("joda-time:joda-time:2.12.7")
            dependency("org.joda:joda-convert:2.2.3")
            // jdbc
            dependency("p6spy:p6spy:3.9.1")
            dependency("org.postgresql:postgresql:42.5.6")
            dependency("mysql:mysql-connector-java:8.0.33")
            dependency("com.mysql:mysql-connector-j:8.0.33")
            dependency("com.microsoft.sqlserver:mssql-jdbc:11.2.3.jre8")
            dependency("com.oracle.database.jdbc:ojdbc8:21.6.0.0.1")
            dependency("com.oracle.database.nls:orai18n:21.3.0.0")
            // apache commons
            dependency("commons-io:commons-io:2.16.1")
            dependency("org.apache.commons:commons-text:1.12.0")
            // http相关
            dependency("com.squareup.okhttp3:okhttp:4.12.0")
            dependency("com.squareup.retrofit2:converter-jackson:2.11.0")
            dependency("com.squareup.retrofit2:retrofit:2.11.0")
            // json、xml相关
            dependency("org.json:json:20240303")
            dependency("com.alibaba.fastjson2:fastjson2:2.0.53")
            dependency("com.thoughtworks.xstream:xstream:1.4.20")
            // 反射相关
            dependency("net.jodah:typetools:0.6.3")
            dependency("cglib:cglib:3.3.0")
            dependency("org.reflections:reflections:0.10.2")
            // kotlin
            dependency("org.jetbrains.kotlin:kotlin-stdlib-common:${Ver.kotlinVersion}")
            dependency("org.jetbrains.kotlin:kotlin-stdlib:${Ver.kotlinVersion}")
            dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Ver.kotlinVersion}")
            dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Ver.kotlinVersion}")
            dependency("org.jetbrains.kotlin:kotlin-reflect:${Ver.kotlinVersion}")
            // 其他工具包
            dependency("com.google.guava:guava:33.2.1-jre")
            dependency("org.apache.commons:commons-math3:3.6.1")
        }
    }

    dependencies {
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        testCompileOnly("org.projectlombok:lombok")
        testAnnotationProcessor("org.projectlombok:lombok")
        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testImplementation("org.openjdk.jmh:jmh-core")
        testImplementation("org.openjdk.jmh:jmh-generator-annprocess")
    }

    tasks.withType<Copy>().all {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    tasks.withType<Jar>().all {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    java {
        // withJavadocJar()
        withSourcesJar()
    }

    tasks.javadoc {
        options.encoding = "UTF-8"
        (options as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    tasks.jar {
        manifest {
            attributes["provider"] = "gradle"
        }
    }

    // tasks.test {
    //     useJUnitPlatform()
    // }

    publishing {
        repositories {
            maven {
                // setUrl("https://nexus.msvc.top/repository/maven-${if (buildSnapshot) "snapshots" else "releases"}/")
                setUrl("http://all.msvc.top:30005/api/packages/clever/maven")
                isAllowInsecureProtocol = true
                credentials(HttpHeaderCredentials::class) {
                    name = "Authorization"
                    value = "token ${localProperty["GITEA_TOKEN"]}"
                }
                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
                // credentials(PasswordCredentials::class) {
                //     username = "${localProperty["NEXUS_USERNAME"]}"
                //     password = "${localProperty["NEXUS_PASSWORD"]}"
                // }
            }
        }

        publications {
            create<MavenPublication>("mavenJava") {
                versionMapping {
                    usage("java-api") {
                        fromResolutionOf("runtimeClasspath")
                    }
                    usage("java-runtime") {
                        fromResolutionResult()
                    }
                }
                from(components["java"])
                pom {
                    name.set("clever-app library")
                    description.set("clever-app library")
                }
            }
        }
    }
}

tasks.jar {
    enabled = false
}
