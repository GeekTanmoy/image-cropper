pluginManagement {
    repositories {
        //Maven Local
        //mavenLocal()

        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()

        maven { url = uri("https://jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        //Maven Local
        //mavenLocal()

        google()
        mavenCentral()

        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Image Cropper"
include(":app")
include(":image-cropper")
