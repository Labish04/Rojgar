pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // ✅ Correct Kotlin DSL syntax
        maven { url = uri("https://storage.zego.im/maven") }
        maven { url = uri("https://www.jitpack.io") }
    }
}

rootProject.name = "Rojgar"
include(":app")
