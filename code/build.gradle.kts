import org.gradle.configurationcache.extensions.capitalized

plugins {
    java
}

allprojects {
    apply<JavaPlugin>()

    java {
        toolchain {
            //languageVersion = JavaLanguageVersion.of(19)
            languageVersion.set(JavaLanguageVersion.of(19))

        }
    }

    repositories {
        mavenCentral()
    }

    group = "it.unibo.ise"
}

subprojects {
    sourceSets {
        main {
            resources {
                srcDir("src/main/asl")
            }
        }
    }

    dependencies {
        implementation("io.github.jason-lang:interpreter:3.2.0")
        testImplementation("junit", "junit", "4.13.2")
    }

    tasks.register<JavaExec>("runConfigWindow") {
        group = "run"
        mainClass.set("env.ConfigWindow")
        classpath = sourceSets["main"].runtimeClasspath
    }


    file(projectDir).listFiles().filter { it.extension == "mas2j" }.forEach { mas2jFile ->
        task<JavaExec>("run${mas2jFile.nameWithoutExtension.capitalized()}Mas") {
            group = "run"
            classpath = sourceSets.getByName("main").runtimeClasspath
            mainClass.set("jason.infra.centralised.RunCentralisedMAS")
            args(mas2jFile.path)
            standardInput = System.`in`
            javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
        }
    }
}