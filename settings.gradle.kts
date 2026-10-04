import java.io.File

pluginManagement {
	repositories {
		gradlePluginPortal()
		maven {
			name = "eagler-teavm"
			url = uri("https://eaglercraft-teavm-fork.github.io/maven/")
		}
		maven {
			name = "eagler-local"
			url = uri(File(rootDir, "gradle/local-libs"))
		}
		mavenCentral()
	}
}

// Set to PREFER_PROJECT so sub-projects can define their own flatDir & custom repos
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
	repositories {
		mavenCentral()
		maven {
			name = "eagler-teavm"
			url = uri("https://eaglercraft-teavm-fork.github.io/maven/")
		}
		maven {
			name = "eagler-local"
			url = uri(File(rootDir, "gradle/local-libs"))
		}
	}
}

buildCache {
	local {
		isEnabled = true
		removeUnusedEntriesAfterDays = 7
	}
}

rootProject.name = "eaglercraft-workspace"

include("target_lwjgl_desktop")
include("target_teavm_javascript")
include("target_teavm_wasm_gc")