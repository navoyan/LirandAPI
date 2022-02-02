# Lirand API

Lirand API allows you to develop Spigot plugins faster, easier 
and more Kotlin-like way.

It includes a lot of cool features like:
 - Command builders based on the [Mojang Brigadier](https://github.com/Mojang/brigadier)
 - Three types of inventory-based menus and their builders
 - Scoreboard builder
 - Easy way to change the NBT of items and entities
 - Items and inventories serialization/deserialization
 - Event flow
 - Online player collections
 - A bunch of useful extensions for working with chat, events, items, inventories, etc.

And most importantly **it is backward compatible via 1.16+ Minecraft versions**.


## Declaring a dependency via Gradle

Add the following to your build script:
```kotlin
repositories { 
    // ...
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://libraries.minecraft.net")
    // ...
}

dependencies {
    // ... 
    implementation("com.github.dyam0:LirandAPI:VERSION")
    compileOnly("com.mojang:brigadier:1.0.18")
    // ...
}
```
Replace `VERSION` with the version of Lirand API you need. 
Also you can build the latest snapshot on the [JitPack](https://jitpack.io/#dyam0/LirandAPI) and use it as a dependency.

I highly recommend you to shade this dependency into your Jar file 
as well as some dependencies of Lirand API 
([MCCoroutine](https://github.com/Shynixn/MCCoroutine) and [AnvilGUI](https://github.com/WesJD/AnvilGUI)).