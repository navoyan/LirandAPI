package lirand.api.nbt

import lirand.api.extensions.server.nmsVersion
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

var Entity.nbtData: NbtData
	get() {
		val nbtTagCompound = nbtCompoundConstructor.newInstance()

		val entity = craftEntityGetHandleMethod.invoke(this)

		minecraftEntitySaveMethod.invoke(entity, nbtTagCompound)

		return NbtData(nbtTagCompound)
	}
	set(value) {
		val entity = craftEntityGetHandleMethod.invoke(this)

		minecraftEntityLoadMethod.invoke(entity, value.nbtTagCompound)
	}


var ItemStack.nbtData: NbtData
	get() {
		val itemStack = asNMSCopyMethod.invoke(null, this)

		return NbtData(minecraftItemStackTagField.get(itemStack))
	}
	set(value) {
		val nmsItemStack = (asNMSCopyMethod.invoke(null, this)).apply {
			minecraftItemStackSetTagMethod.invoke(this, value.nbtTagCompound)
		}
		itemMeta = getItemMetaMethod.invoke(null, nmsItemStack) as ItemMeta
	}




private val craftItemStackClass =
	Class.forName("org.bukkit.craftbukkit.v$nmsVersion.inventory.CraftItemStack")

private val craftEntityGetHandleMethod =
	Class.forName("org.bukkit.craftbukkit.v$nmsVersion.entity.CraftEntity")
		.getMethod("getHandle")

private val asNMSCopyMethod =
	craftItemStackClass.getMethod("asNMSCopy", ItemStack::class.java)

private val getItemMetaMethod = craftItemStackClass.methods
	.find {
		it.name == "getItemMeta" && it.parameterTypes.let {
			it.size == 1 && it[0] == asNMSCopyMethod.returnType
		}
	}!!


private val minecraftEntityClass = craftEntityGetHandleMethod.returnType

private val minecraftEntityLoadMethod = minecraftEntityClass.methods
	.find {
		it.returnType == Void.TYPE && it.parameterTypes.let {
			it.size == 1 && it[0] == nbtCompoundClass
		}
	}!!
private val minecraftEntitySaveMethod = minecraftEntityClass.methods
	.find {
		it.returnType == nbtCompoundClass && it.parameterTypes.let {
			it.size == 1 && it[0] == nbtCompoundClass
		}
	}!!


private val minecraftItemStackClass = asNMSCopyMethod.returnType

private val minecraftItemStackTagField = minecraftItemStackClass.declaredFields
	.find { it.type == nbtCompoundClass }!!
	.apply { isAccessible = true }

private val minecraftItemStackSetTagMethod = minecraftItemStackClass.methods
	.find {
		it.returnType == Void.TYPE && it.parameterTypes.let {
			it.size == 1 && it[0] == nbtCompoundClass
		}
	}!!