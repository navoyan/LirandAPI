@file:Suppress("MemberVisibilityCanBePrivate")

package lirand.api.nbt

import lirand.api.extensions.server.nmsNumberVersion
import lirand.api.extensions.server.nmsVersion

class NbtData internal constructor(nbtTagCompound: Any?) {

	internal val nbtTagCompound: Any = nbtTagCompound ?: nbtCompoundConstructor.newInstance()
	private val map: MutableMap<String, Any> = nbtCompoundMapField.get(this.nbtTagCompound) as MutableMap<String, Any>

	constructor() : this(nbtCompoundConstructor.newInstance())
	constructor(nbtString: String) : this(mojangParseMethod.invoke(null, nbtString))


	/**
	 * Returns a [Set] of all keys in this nbt.
	 */
	val keys: Set<String> get() = map.keys


	/**
	 * This method gets the value
	 * at the given [key]. The returned [dataType]
	 * must be specified.
	 * The returned value is null, if it
	 * was not possible to find any value at
	 * the specified location, or if the type
	 * is not the one which was specified.
	 */
	operator fun <T> get(key: String, dataType: NbtDataType<T>): T? {
		val value = map[key] ?: return null

		return dataType.decode(value)
	}

	/**
	 * This method gets the value
	 * at the given [key]. The returned [dataType]
	 * must be specified.
	 * If it was not possible to find any value at
	 * the specified location, or if the type
	 * is not the one which was specified,
	 * the result of calling [defaultValue] was put into specified location.
	 */
	inline fun <T> getOrSet(key: String, dataType: NbtDataType<T>, defaultValue: () -> T): T {
		return get(key, dataType) ?: defaultValue().also {
			set(key, dataType, it)
		}
	}

	/**
	 * This method gets the value
	 * at the given [key]. The returned [dataType]
	 * must be specified.
	 * The returned value is [defaultValue], if it
	 * was not possible to find any value at
	 * the specified location, or if the type
	 * is not the one which was specified.
	 */
	inline fun <T> getOrDefault(key: String, dataType: NbtDataType<T>, defaultValue: () -> T): T {
		return get(key, dataType) ?: defaultValue()
	}

	/**
	 * This method sets some [value]
	 * at the position of the given [key].
	 * The [dataType] of the given [value]
	 * must be specified.
	 */
	operator fun <T> set(key: String, dataType: NbtDataType<T>, value: T) {
		map[key] = dataType.encode(value)
	}

	/**
	 * This method removes the
	 * given [key] from the NBTTagCompound.
	 * Its value will be lost.
	 */
	fun remove(key: String) {
		map.remove(key)
	}

	/**
	 * @see remove
	 */
	operator fun minusAssign(key: String) = remove(key)


	fun getTypeId(key: String): Int? {
		if (key !in this) return null

		return (nbtBaseGetTypeIdMethod.invoke(map[key]) as Byte).toInt()
	}


	/**
	 * Returns `true` if the nbt contains the specified [key].
	 */
	operator fun contains(key: String): Boolean {
		return map.containsKey(key)
	}

	/**
	 * Returns `true` if the nbt contains the specified [key] of [type].
	 */
	fun containsKeyOfType(key: String, type: NbtDataType<*>): Boolean {
		return getTypeId(key) == type.typeId
	}

	/**
	 * Returns `true` if the nbt contains the specified [key] of [typeId].
	 */
	fun containsKeyOfType(key: String, typeId: Int): Boolean {
		return getTypeId(key) == typeId
	}


	override fun toString(): String {
		return nbtTagCompound.toString()
	}


	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as NbtData

		if (nbtTagCompound != other.nbtTagCompound) return false

		return true
	}

	override fun hashCode(): Int {
		return nbtTagCompound.hashCode()
	}
}




internal val nmsPackage = run {
	if (nmsNumberVersion < 17)
		"net.minecraft.server.v$nmsVersion"
	else
		"net.minecraft.nbt"
}

internal val nbtCompoundClass = Class.forName("$nmsPackage.NBTTagCompound")

private val mojangParseMethod = Class.forName("$nmsPackage.MojangsonParser").methods
	.find { it.returnType == nbtCompoundClass && it.parameterTypes.let {
		it.size == 1 && it[0] == String::class.java
	} }!!
internal val nbtCompoundConstructor = nbtCompoundClass.getConstructor()

private val nbtCompoundMapField = nbtCompoundClass.declaredFields
	.find { it.type == MutableMap::class.java }!!
	.apply { isAccessible = true }