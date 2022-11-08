package lirand.api.nbt

import org.bukkit.block.TileState
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
	get() = tagNbtData.getOrSet("PublicBukkitValues", NbtCompoundType) { NbtData() }
	set(value) {
		val nbt = tagNbtData
		nbt["PublicBukkitValues", NbtCompoundType] = value

		tagNbtData = nbt
	}


var TileState.persistentData: NbtData
	get() = nbtData.getOrSet("PublicBukkitValues", NbtCompoundType) { NbtData() }
	set(value) {
		val nbt = nbtData
		nbt["PublicBukkitValues", NbtCompoundType] = value

		nbtData = nbt
	}