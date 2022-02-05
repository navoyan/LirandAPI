package lirand.api.nbt

import lirand.api.extensions.server.nmsVersion
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

var Entity.nbtData: NBTData
	get() {
		val nbtTagCompound = NBTData.nbtCompoundConstructor.newInstance()

		val entity = getHandleMethod.invoke(this)

		minecraftEntityLoadMethod.invoke(entity, nbtTagCompound)

		return NBTData(nbtTagCompound)
	}
	set(value) {
		val entity = getHandleMethod.invoke(this)
		minecraftEntitySaveMethod.invoke(entity, value.nbtTagCompound)
	}


var ItemStack.nbtData: NBTData
	get() {
		return asNMSCopyMethod.invoke(null, this).let {
			if (minecraftItemStackHasTagMethod.invoke(it) as Boolean)
				NBTData(minecraftItemStackGetTagMethod.invoke(it))
			else
				NBTData()
		}
	}
	set(value) {
		val nmsItemStack = (asNMSCopyMethod.invoke(null, this)).apply {
			minecraftItemStackSetTagMethod.invoke(this, value.nbtTagCompound)
		}
		itemMeta = getItemMetaMethod.invoke(null, nmsItemStack) as ItemMeta
	}




private val craftItemStackClass =
	Class.forName("org.bukkit.craftbukkit.v$nmsVersion.inventory.CraftItemStack")

private val getHandleMethod =
	Class.forName("org.bukkit.craftbukkit.v$nmsVersion.entity.CraftEntity")
		.getMethod("getHandle")

private val asNMSCopyMethod =
	craftItemStackClass.getMethod("asNMSCopy", ItemStack::class.java)

private val getItemMetaMethod = craftItemStackClass.methods
	.find { it.name == "getItemMeta" && it.parameterTypes.let {
		it.size == 1 && it[0] == asNMSCopyMethod.returnType
	} }!!


private val minecraftEntityClass = getHandleMethod.returnType

private val minecraftEntityLoadMethod = minecraftEntityClass.methods
	.find { it.returnType == Void.TYPE && it.parameterTypes.let {
		it.size == 1 && it[0] == NBTData.nbtCompoundClass
	} }!!
private val minecraftEntitySaveMethod = minecraftEntityClass.methods
	.find { it.returnType == NBTData.nbtCompoundClass && it.parameterTypes.let {
		it.size == 1 && it[0] == NBTData.nbtCompoundClass
	} }!!


private val minecraftItemStackClass = asNMSCopyMethod.returnType

private val minecraftItemStackHasTagMethod = minecraftItemStackClass.methods
	.find { it.returnType == Boolean::class.java && it.parameterCount == 0 }!!
private val minecraftItemStackGetTagMethod = minecraftItemStackClass.methods
	.find { it.returnType == NBTData.nbtCompoundClass && it.parameterCount == 0 }!!
private val minecraftItemStackSetTagMethod = minecraftItemStackClass.methods
	.find { it.returnType == Void.TYPE && it.parameterTypes.let {
		it.size == 1 && it[0] == NBTData.nbtCompoundClass
	} }!!