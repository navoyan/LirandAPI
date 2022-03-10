package lirand.api.serialization

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import lirand.api.extensions.inventory.Inventory
import lirand.api.extensions.inventory.ItemStack
import lirand.api.extensions.other.set
import lirand.api.extensions.other.toId
import lirand.api.nbt.NbtData
import lirand.api.nbt.nbtData
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

object InventoryJsonSerializer {
	private val jsonParser = JsonParser()

	fun serialize(item: ItemStack): String {
		return """
        	{"id":"${item.type.toId()}","Count":${item.amount},"tag":"${item.nbtData}"}
    	""".trimIndent()
	}

	fun deserializeItemStack(itemStackNbt: String): ItemStack? = try {
		val nbtJsonObject = jsonParser.parse(itemStackNbt).asJsonObject

		val id = nbtJsonObject.getAsJsonPrimitive("id").asString
		val amount = nbtJsonObject.getAsJsonPrimitive("Count").asInt
		val nbt = nbtJsonObject.getAsJsonPrimitive("tag").asString

		ItemStack(
			Material.matchMaterial(id)!!,
			amount,
			NbtData(nbt)
		)
	} catch (exception: Throwable) {
		null
	}

	fun serialize(inventory: Inventory, title: String? = null): String {
		val itemsNbtJsonObject = JsonObject().apply {
			for ((index, item) in inventory.withIndex()) {
				this["$index"] = jsonParser.parse(item?.let { serialize(it) } ?: continue)
			}
		}

		val serializedSize = if (inventory.type == InventoryType.CHEST)
			"\"size\":${inventory.size},"
		else ""
		val serializedTitle = if (title != null)
			"\"title\":\"$title\","
		else ""
		val serializedType = "\"type\":\"${inventory.type.toString().lowercase()}\","

		return """
			{$serializedType$serializedSize$serializedTitle"Items":$itemsNbtJsonObject}
		""".trimIndent()
	}

	fun deserializeInventory(
		inventoryNbt: String,
		owner: InventoryHolder? = null,
		title: String? = null
	): Inventory? = try {
		val nbtJsonObject = jsonParser.parse(inventoryNbt).asJsonObject

		val type = InventoryType.valueOf(
			nbtJsonObject.getAsJsonPrimitive("type").asString.uppercase()
		)
		val size = if (type == InventoryType.CHEST)
			nbtJsonObject.getAsJsonPrimitive("size").asInt
		else -1
		val resultTitle = if (title == null && nbtJsonObject.has("title"))
			nbtJsonObject.getAsJsonPrimitive("title").asString
		else title

		val resultInventory = if (type == InventoryType.CHEST)
			Inventory(owner, size, resultTitle)
		else
			Inventory(owner, type, resultTitle)

		val items = nbtJsonObject.getAsJsonObject("Items")

		resultInventory.apply {
			for ((slot, item) in items.entrySet()) {
				setItem(slot.toInt(), deserializeItemStack(item.toString()))
			}
		}
	} catch (exception: Throwable) {
		null
	}
}