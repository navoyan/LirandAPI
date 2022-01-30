package lirand.api.extensions.inventory

import lirand.api.extensions.server.server
import lirand.api.nbt.NBTData
import lirand.api.nbt.nbtData
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.ItemMeta

fun ItemStack(material: Material, amount: Int = 1, nbtData: NBTData): ItemStack {
	return ItemStack(material, amount).apply {
		this.nbtData = nbtData
	}
}


fun <T : ItemMeta> ItemMeta(
	material: Material
): T {
	val meta = server.itemFactory.getItemMeta(material)
	return meta as T
}

@JvmName("typedMeta")
inline fun <reified T : ItemMeta> ItemStack.meta(builder: T.() -> Unit) = apply {
	itemMeta = (itemMeta as? T)?.apply(builder) ?: itemMeta
}

inline fun ItemStack.meta(builder: ItemMeta.() -> Unit) = meta<ItemMeta>(builder)


var ItemMeta.loreString: String?
	get() = lore?.joinToString("\n")
	set(value) {
		lore = value?.split("\n")
	}

var ItemMeta.name: String?
	get() = if (hasDisplayName()) displayName else null
	set(value) = setDisplayName(if (!value.isNullOrEmpty()) value else " ")

var ItemMeta.customModel: Int?
	get() = if (hasCustomModelData()) customModelData else null
	set(value) = setCustomModelData(value)

var ItemMeta.localName: String
	get() = localizedName
	set(value) = setLocalizedName(value)


val BookMeta.content: String
	get() = buildString {
		for (it in pages) {
			if (isNotEmpty())
				append('\n')
			append(it)
		}
	}


val ItemStack?.isEmpty: Boolean get() = this == null || type == Material.AIR
val ItemStack?.isNotEmpty: Boolean get() = !isEmpty

val Material.isPickaxe: Boolean get() = name.endsWith("_PICKAXE")
val Material.isSword: Boolean get() = name.endsWith("_SWORD")
val Material.isAxe: Boolean get() = name.endsWith("_AXE")
val Material.isSpade: Boolean get() = name.endsWith("_SPADE")
val Material.isHoe: Boolean get() = name.endsWith("_HOE")
val Material.isHelmet: Boolean get() = name.endsWith("_HELMET")
val Material.isChestplate: Boolean get() = name.endsWith("_CHESTPLATE")
val Material.isLeggings: Boolean get() = name.endsWith("_LEGGINGS")
val Material.isBoots: Boolean get() = name.endsWith("_BOOTS")
val Material.isOre: Boolean get() = name.endsWith("_ORE")
val Material.isIngot: Boolean get() = name.endsWith("_INGOT")
val Material.isDoor: Boolean get() = name.endsWith("_DOOR")
val Material.isMinecart: Boolean get() = name.endsWith("_MINECART")