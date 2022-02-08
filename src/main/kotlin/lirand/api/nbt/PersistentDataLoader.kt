package lirand.api.nbt

import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack


var Entity.persistentData: NbtData
	get() = nbtData.getOrSet("BukkitValues", NbtCompoundType) { NbtData() }
	set(value) {
		val nbt = nbtData
		nbt["BukkitValues", NbtCompoundType] = value

		nbtData = nbt
	}


var ItemStack.persistentData: NbtData
	get() = nbtData.getOrSet("BukkitValues", NbtCompoundType) { NbtData() }
	set(value) {
		val nbt = nbtData
		nbt["BukkitValues", NbtCompoundType] = value

		nbtData = nbt
	}