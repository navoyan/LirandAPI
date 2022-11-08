package lirand.api.nbt

import lirand.api.extensions.inventory.Inventory
import lirand.api.extensions.inventory.ItemStack
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

fun Inventory.getSerializableNbtData(title: String? = null): NbtData {
	return nbtData {
		string["type"] = type.toString().lowercase()

		if (type == InventoryType.CHEST)
			int["size"] = size

		if (title != null)
			string["title"] = title

		list(compound)["Items"] = mapIndexedNotNull { index, itemStack ->
			itemStack?.let {
				nbtData {
					byte["Slot"] = index.toByte()
					compound["ItemStack"] = itemStack.nbtData
				}
			}
		}
	}
}


fun NbtData.deserializeItemStack(): ItemStack? {
	return ItemStack(
		Material.matchMaterial(string["id"] ?: error("Invalid material id")) ?: error("Invalid material id"),
		byte["Count"]?.toInt() ?: error("Invalid item count"),
		compound["tag"] ?: error("Invalid item tag")
	)
}

fun NbtData.deserializeInventory(
	owner: InventoryHolder? = null,
	title: String? = null
): Inventory {
	val type = InventoryType.valueOf(string["type"]?.uppercase() ?: error("Invalid inventory type"))

	val size = if (type == InventoryType.CHEST) int["size"] ?: error("Invalid size") else -1

	val resultTitle = title ?: string["title"]

	val resultInventory = if (type == InventoryType.CHEST)
		Inventory(size, owner, resultTitle)
	else
		Inventory(type, owner, resultTitle)

	val itemStacksNbt = list(compound)["Items"] ?: error("Invalid items")

	return resultInventory.apply {
		for (itemStackNbt in itemStacksNbt) {
			setItem(
				itemStackNbt.byte["Slot"]?.toInt() ?: error("Invalid slot of $itemStackNbt"),
				itemStackNbt.compound["ItemStack"]?.deserializeItemStack() ?: error("Invalid ItemStack of $itemStackNbt")
			)
		}
	}
}

