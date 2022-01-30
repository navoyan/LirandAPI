package lirand.api.extensions.inventory

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import lirand.api.extensions.other.set
import lirand.api.extensions.other.toId
import lirand.api.nbt.NBTData
import lirand.api.nbt.nbtData
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

object Serializer {
	private val jsonParser = JsonParser()

	fun serialize(item: ItemStack): String {
		return """
        	{"id":"${item.type.toId()}","Count":${item.amount},"tag":"${item.nbtData.serialize()}"}
    	""".trimIndent()
	}

	fun deserializeItem(itemNbt: String): ItemStack? {
		return try {
			val asJsonObject = jsonParser.parse(itemNbt).asJsonObject

			val id = asJsonObject.getAsJsonPrimitive("id").asString
			val amount = asJsonObject.getAsJsonPrimitive("Count").asInt
			val nbt = asJsonObject.getAsJsonPrimitive("tag").asString

			ItemStack(
				Material.matchMaterial(id)!!,
				amount,
				NBTData.deserialize(nbt)
			)
		} catch (exception: Throwable) {
			null
		}
	}

	fun serialize(inventory: Inventory, title: String? = null): String {
		val items = JsonObject().apply {
			for ((index, item) in inventory.withIndex())
				this["$index"] = jsonParser.parse(item?.let { serialize(it) } ?: continue)
		}

		val serializedSize = if (inventory.type == InventoryType.CHEST)
			"\"size\":${inventory.size},"
		else ""

		val serializedTitle = if (title != null)
			"\"title\":\"$title\","
		else ""

		return """
			{"type":"${inventory.type.toString().lowercase()}",$serializedSize$serializedTitle"Items":$items}
		""".trimIndent()
	}

	fun deserializeInventory(
		owner: InventoryHolder,
		inventoryNbt: String,
		title: String? = null
	): Inventory? {
		return try {
			val jsonObject = jsonParser.parse(inventoryNbt).asJsonObject

			val type = InventoryType.valueOf(
				jsonObject.getAsJsonPrimitive("type").asString.uppercase()
			)
			val size = if (type == InventoryType.CHEST)
				jsonObject.getAsJsonPrimitive("size").asInt
			else -1
			val resultTitle = if (title == null && jsonObject.has("title"))
				jsonObject.getAsJsonPrimitive("title").asString
			else title

			val result = if (type == InventoryType.CHEST)
				Inventory(owner, size, resultTitle)
			else
				Inventory(owner, type, resultTitle)

			val items = jsonObject.getAsJsonObject("Items")
			result.apply {
				for ((slot, item) in items.entrySet()) {
					setItem(slot.toInt(), deserializeItem(item.toString()))
				}
			}
		} catch (exception: Throwable) {
			null
		}
	}
}