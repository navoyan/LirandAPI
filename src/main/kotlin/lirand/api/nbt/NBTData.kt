@file:Suppress("MemberVisibilityCanBePrivate")

package lirand.api.nbt

import lirand.api.extensions.server.nmsNumberVersion
import lirand.api.extensions.server.nmsVersion

class NBTData internal constructor(nbtTagCompound: Any?) {
	val nbtTagCompound: Any = nbtTagCompound ?: nbtCompoundConstructor.newInstance()

	constructor() : this(nbtCompoundConstructor.newInstance())
	constructor(nbtString: String) : this(mojangParseMethod.invoke(null, nbtString))

	fun serialize() = nbtTagCompound.toString()

	/**
	 * This method gets the value
	 * at the given [key]. The returned [dataType]
	 * must be specified.
	 * The returned value is null, if it
	 * was not possible to find any value at
	 * the specified location, or if the type
	 * is not the one which was specified.
	 */
	operator fun <T> get(key: String, dataType: NBTDataType<T>): T? {
		val value = nbtCompoundGetMethod.invoke(nbtTagCompound, key)
		return if (value != null) {
			dataType.decodeNMS(value)
		}
		else null
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
	inline fun <T> getOrSet(key: String, dataType: NBTDataType<T>, defaultValue: () -> T): T {
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
	fun <T> getOrDefault(key: String, dataType: NBTDataType<T>, defaultValue: T): T {
		return get(key, dataType) ?: defaultValue
	}

	/**
	 * This method sets some [value]
	 * at the position of the given [key].
	 * The [dataType] of the given [value]
	 * must be specified.
	 */
	operator fun <T> set(key: String, dataType: NBTDataType<T>, value: T) {
		dataType.writeToCompound(key, value, nbtTagCompound)
	}

	/**
	 * This method removes the
	 * given [key] from the NBTTagCompound.
	 * Its value will be lost.
	 */
	fun remove(key: String) {
		nbtCompoundRemoveMethod.invoke(nbtTagCompound, key)
	}

	/**
	 * @see remove
	 */
	operator fun minusAssign(key: String) = remove(key)



	companion object {
		fun deserialize(nbtString: String) = NBTData(nbtString)

		internal val nmsPackage = run {
			if (nmsNumberVersion < 17)
				"net.minecraft.server.v$nmsVersion"
			else
				"net.minecraft.nbt"
		}

		internal val mojangParseMethod = Class.forName("$nmsPackage.MojangsonParser").methods
			.find { it.returnType == nbtCompoundClass && it.parameterTypes.let {
				it.size == 1 && it[0] == String::class.java
			} }!!

		internal val nbtCompoundClass = Class.forName("$nmsPackage.NBTTagCompound")
		internal val nbtCompoundConstructor = nbtCompoundClass.getConstructor()
		internal val nbtCompoundGetMethod = nbtCompoundClass.methods
			.find { it.returnType.name == "$nmsPackage.NBTBase" && it.parameterTypes.let {
				it.size == 1 && it[0] == String::class.java
			} }!!
		internal val nbtCompoundRemoveMethod = nbtCompoundClass.methods
			.find { it.returnType == Void.TYPE && it.parameterTypes.let {
				it.size == 1 && it[0] == String::class.java
			} }!!
	}
}
