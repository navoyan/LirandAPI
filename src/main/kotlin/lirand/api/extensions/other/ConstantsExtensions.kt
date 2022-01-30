package lirand.api.extensions.other

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.EntityType

fun Material.toId() = "minecraft:${toString().lowercase()}"

fun EntityType.toId() = "minecraft:${toString().lowercase()}"

/**
 * @return Whether it is possible to take damage in this [GameMode].
 */
val GameMode.isDamageable: Boolean
	get() = when (this) {
		GameMode.SURVIVAL, GameMode.ADVENTURE -> true
		GameMode.SPECTATOR, GameMode.CREATIVE -> false
	}