package lirand.api.extensions.inventory

import lirand.api.extensions.server.server
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

fun Inventory(
	owner: InventoryHolder? = null,
	type: InventoryType,
	title: String? = null
): Inventory = Inventory<Inventory>(owner, type, title)

@JvmName("TypedInventory")
fun <I : Inventory> Inventory(
	owner: InventoryHolder? = null,
	type: InventoryType,
	title: String? = null
): I {
	return if (title != null)
		(server.createInventory(owner, type, title) as I)
	else
		(server.createInventory(owner, type) as I)
}

fun Inventory(
	owner: InventoryHolder? = null,
	size: Int,
	title: String? = null
): Inventory {
	return if (title != null)
		server.createInventory(owner, size, title)
	else
		server.createInventory(owner, size)
}

val Inventory.hasSpace: Boolean
	get() = contents.any { it == null || it.type == Material.AIR }

fun Inventory.hasSpace(
	item: ItemStack,
	amount: Int = item.amount
): Boolean = getSpaceOf(item) >= amount

fun Inventory.getSpaceOf(item: ItemStack): Int {
	return contents.filterNotNull().map {
		if (it.amount < it.maxStackSize && it.isSimilar(item))
			it.maxStackSize - it.amount
		else 0
	}.count()
}

operator fun Inventory.get(slot: Int): ItemStack? = getItem(slot)

operator fun Inventory.set(slot: Int, itemStack: ItemStack?) {
	setItem(slot, itemStack)
}