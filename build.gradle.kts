import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.8.21"
	java
	idea

	`maven-publish`
}

val projectGroup = "lirand.api"
val projectVersion = "0.8.0"

group = projectGroup
version = projectVersion

repositories {
	mavenCentral()
	maven("https://repo.codemc.io/repository/maven-snapshots/")
	maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
	maven("https://libraries.minecraft.net")
}

dependencies {
	compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.8.21")
	compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-RC")
	compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.0")
	compileOnly("org.jetbrains.kotlin:kotlin-reflect:1.8.21")

	compileOnly("org.spigotmc:spigot-api:1.16.1-R0.1-SNAPSHOT")
	compileOnly("com.mojang:brigadier:1.0.18")

	api("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.11.0")
	api("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.11.0")

	api("net.wesjd:anvilgui:1.6.4-SNAPSHOT")
}

java {
	withSourcesJar()

	val javaVersion = JavaVersion.toVersion(8)
	sourceCompatibility = javaVersion
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