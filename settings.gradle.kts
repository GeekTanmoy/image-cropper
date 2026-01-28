pluginManagement {
    repositories {
        //Maven Local
        mavenLocal()

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
        //Maven Local
        mavenLocal()

        google()
        mavenCentral()
    }
}

rootProject.name = "Image Cropper"
include(":app")
include(":image-cropper")
