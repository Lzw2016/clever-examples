import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("org.jetbrains.kotlin.jvm")
    //id("org.springframework.boot")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.clever:clever-spring")
    api("org.clever:clever-core")
    api("org.clever:clever-data-jdbc")
    api("org.clever:clever-data-redis")
    api("org.clever:clever-web")
    api("org.clever:clever-security")
    api("org.clever:clever-task")
    api("org.clever:clever-task-ext")
    api("org.jetbrains.kotlin:kotlin-stdlib-common")
    api("org.jetbrains.kotlin:kotlin-stdlib")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk7")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("com.oracle.database.jdbc:ojdbc8")

    api("com.esotericsoftware:kryo:5.6.2")
    // api("net.openhft:chronicle-wire:2.27ea13")
    api("net.openhft:chronicle-queue:5.27ea11")
    api("org.rocksdb:rocksdbjni:10.2.1")

    api("commons-io:commons-io:2.21.0")
    api("org.apache.commons:commons-csv:1.14.1")
    api("com.konghq:unirest-java-core:4.4.12")
    api("com.konghq:unirest-modules-jackson:4.4.12")
    api("com.starrocks:starrocks-stream-load-sdk:1.0")

    api("co.elastic.clients:elasticsearch-java:8.19.10")

    api("com.github.luben:zstd-jni:1.5.7-6")
    api("org.apache.kafka:kafka-clients:4.1.1")

    // api("org.apache.arrow:arrow-vector:18.3.0")
    api("org.apache.flink:flink-streaming-java:2.2.0")
    api("org.apache.flink:flink-clients:2.2.0")
    api("org.apache.flink:flink-table-api-java:2.2.0")
    api("org.apache.flink:flink-table-runtime:2.2.0")
    api("org.apache.flink:flink-connector-datagen:2.2.0")
    api("org.apache.flink:flink-connector-files:2.2.0")

    // api("ch.qos.logback:logback-core:1.2.13")
    // api("ch.qos.logback:logback-classic:1.2.13")
    runtimeOnly("org.apache.doris:flink-doris-connector-1.20:25.1.0")

    api("io.projectreactor:reactor-core")
    testImplementation("io.projectreactor:reactor-core")

    implementation("io.debezium:debezium-connector-postgres:3.3.2.Final")
    implementation("io.debezium:debezium-connector-mysql:3.3.2.Final")
    implementation("io.debezium:debezium-embedded:3.3.2.Final")
}

//configurations.all {
//    resolutionStrategy {
//        force("ch.qos.logback:logback-core:1.2.13")
//        force("ch.qos.logback:logback-classic:1.2.13")
//    }
//}

sourceSets {
    main {
        // java {
        //     setSrcDirs(listOf<String>())
        // }
        // withConvention(GroovySourceSet::class) {
        //     groovy {
        //         setSrcDirs(listOf("src/main/java", "src/main/groovy"))
        //     }
        // }
    }
}

//tasks.compileJava {
//    enabled = false
//}

kotlin {
    compilerOptions {
        apiVersion = KotlinVersion.KOTLIN_2_0
        jvmTarget = JvmTarget.JVM_17
        javaParameters = true
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

// 在源码目录中引入资源(如: mybatis xml文件)
tasks.processResources {
    val includes = listOf("**/*.xml")
    from("src/main/java") {
        include(includes)
    }
    from("src/main/kotlin") {
        include(includes)
    }
}

// 拷贝lib文件
tasks.register("copyJar", Copy::class) {
    val libDir = layout.buildDirectory.dir("libs/lib").get()
    delete(libDir)
    from(configurations.runtimeClasspath)
    into(libDir)
}

// 拷贝配置文件
tasks.register("copyResources", Copy::class) {
    val configDir = layout.buildDirectory.dir("libs/config").get()
    delete(configDir)
    from("src/main/resources")
    into(configDir)
}

// 配置启动jar
tasks.jar {
    enabled = true
    manifest.attributes["Main-Class"] = "org.clever.app.StartTaskApp"
    // lib/jar 加入 classPath
    val classPaths = project.configurations.runtimeClasspath.get().files.map { file -> "lib/${file.name}" }.toMutableList()
    // // resources 资源加入 classPath (当没有把resources资源编译进jar包时很有用)
    // val resourcesFile = File(projectDir.absolutePath, "src/main/resources")
    // val resourcesPath = resourcesFile.absolutePath
    // resourcesFile.listFiles { file -> file.isFile }?.forEach { file ->
    //     var path = file.absolutePath
    //     if (path.startsWith(resourcesPath)) {
    //         path = path.substring(resourcesPath.length + 1)
    //         path = path.replace('\\', '/')
    //     }
    //     classPaths.add("config/$path")
    // }
    manifest.attributes["Class-Path"] = classPaths.joinToString(" ")
    // println("### [${classPaths.joinToString(" ")}]")
}

// 触发class热部署
tasks.getByName("classes") {
    doLast {
        File("./build").mkdirs()
        File("./build", ".hotReload").apply {
            if (this.exists()) {
                this.createNewFile()
            }
            this.appendText("${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date())}\n")
            println("> [触发class热部署] -> ${this.absolutePath}")
        }
    }
}

tasks.getByName("build") {
    dependsOn("copyJar")
    dependsOn("copyResources")
    dependsOn("jar")
}
