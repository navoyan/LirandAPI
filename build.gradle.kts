import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.6.0"
	java
	idea

	`maven-publish`
}

val projectGroup = "lirand.api"
val projectVersion = "0.6.0"

group = projectGroup
version = projectVersion

repositories {
	mavenCentral()
	maven("https://repo.codemc.io/repository/maven-snapshots/")
	maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
	maven("https://libraries.minecraft.net")
}

dependencies {
	compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.6.0")
	compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

	compileOnly("org.spigotmc:spigot-api:1.16.1-R0.1-SNAPSHOT")
	compileOnly("com.mojang:brigadier:1.0.18")

	api("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:1.5.0")
	api("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:1.5.0")

	api("net.wesjd:anvilgui:1.5.3-SNAPSHOT")
}

java {
	withSourcesJar()
}

tasks.withType<KotlinCompile>().configureEach {
	kotlinOptions.jvmTarget = "1.8"
	kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

publishing {
	publications {
		create<MavenPublication>("main") {
			group = projectGroup
			version = projectVersion
			artifactId = "LirandAPI"

			from(components["kotlin"])
			artifact(tasks["sourcesJar"])
		}
	}
}

idea {
	module {
		isDownloadSources = true
		isDownloadJavadoc = true
	}
}