package lirand.api.nbt

import java.lang.reflect.Method
import kotlin.reflect.KClass

interface NBTDataType<T> {

	fun decodeNMS(nbtBase: Any): T?

	fun writeToCompound(key: String, data: T, compound: Any)

	companion object {
		val COMPOUND = nbtDataType<NBTData>(
			10,
			{ NBTData(it) },
			{ key, data, compound -> nbtCompoundSetMethod.invoke(compound, key, data) }
		)
		val BYTE = nbtDataType<Byte>(
			1,
			{ nbtNumberAsByteMethod.invoke(it) as Byte },
			{ key, data, compound -> nbtCompoundSetByteMethod.invoke(compound, key, data) }
		)
		val BYTE_ARRAY = nbtDataType<ByteArray>(
			7,
			{ nbtByteArrayGetBytesMethod.invoke(it) as ByteArray },
			{ key, data, compound -> nbtCompoundSetByteArrayMethod.invoke(compound, key, data) }
		)
		val DOUBLE = nbtDataType<Double>(
			6,
			{ nbtNumberAsDoubleMethod.invoke(it) as Double },
			{ key, data, compound -> nbtCompoundSetDoubleMethod.invoke(compound, key, data) }
		)
		val FLOAT = nbtDataType<Float>(
			5,
			{ nbtNumberAsFloatMethod.invoke(it) as Float },
			{ key, data, compound -> nbtCompoundSetFloatMethod.invoke(compound, key, data) }
		)
		val INT = nbtDataType<Int>(
			3,
			{ nbtNumberAsIntMethod.invoke(it) as Int },
			{ key, data, compound -> nbtCompoundSetIntMethod.invoke(compound, key, data) }
		)
		val INT_ARRAY = nbtDataType<IntArray>(
			11,
			{ nbtIntArrayGetIntsMethod.invoke(it) as IntArray },
			{ key, data, compound -> nbtCompoundSetIntArrayMethod.invoke(compound, key, data) }
		)
		val LONG = nbtDataType<Long>(
			4,
			{ nbtNumberAsLongMethod.invoke(it) as Long },
			{ key, data, compound -> nbtCompoundSetLongMethod.invoke(compound, key, data) }
		)
		val LONG_ARRAY = nbtDataType<LongArray>(
			12,
			{ nbtLongArrayGetLongsMethod.invoke(it) as LongArray },
			{ key, data, compound -> nbtCompoundSetLongArrayMethod.invoke(compound, key, data) }
		)
		val SHORT = nbtDataType<Short>(
			2,
			{ nbtNumberAsShortMethod.invoke(it) as Short },
			{ key, data, compound -> nbtCompoundSetShortMethod.invoke(compound, key, data) }
		)
		val STRING = nbtDataType<String>(
			8,
			{ nbtStringAsStringMethod.invoke(it) as String },
			{ key, data, compound -> nbtCompoundSetStringMethod.invoke(compound, key, data) }
		)



		private val nbtCompoundSetMethod = NBTData.nbtCompoundClass.methods
			.find {
				it.name == "set" && it.parameterTypes.let {
					it.size >= 2 && it[0] == String::class.java && it[1].simpleName == "NBTBase"
				}
			}!!

		private val nbtCompoundSetByteMethod = getSetMethod(Byte::class)
		private val nbtCompoundSetByteArrayMethod = getSetMethod(ByteArray::class)
		private val nbtCompoundSetDoubleMethod = getSetMethod(Double::class)
		private val nbtCompoundSetFloatMethod = getSetMethod(Float::class)
		private val nbtCompoundSetIntMethod = getSetMethod(Int::class)
		private val nbtCompoundSetIntArrayMethod = getSetMethod(IntArray::class)
		private val nbtCompoundSetLongMethod = getSetMethod(Long::class)
		private val nbtCompoundSetLongArrayMethod = getSetMethod(LongArray::class)
		private val nbtCompoundSetShortMethod = getSetMethod(Short::class)
		private val nbtCompoundSetStringMethod = getSetMethod(String::class)

		private fun getSetMethod(clazz: KClass<*>): Method {
			return NBTData.nbtCompoundClass.methods.find {
				it.returnType == Void.TYPE && it.parameterTypes.let {
					it.size == 2 && it[0] == String::class.java && it[1] == clazz.java
				}
			}!!
		}


		private val nbtNumberClass = Class.forName("${NBTData.nmsPackage}.NBTNumber")

		private val nbtNumberAsByteMethod = nbtNumberClass.getMethod("asByte")
		private val nbtNumberAsDoubleMethod = nbtNumberClass.getMethod("asDouble")
		private val nbtNumberAsFloatMethod = nbtNumberClass.getMethod("asFloat")
		private val nbtNumberAsIntMethod = nbtNumberClass.getMethod("asInt")
		private val nbtNumberAsLongMethod = nbtNumberClass.getMethod("asLong")
		private val nbtNumberAsShortMethod = nbtNumberClass.getMethod("asShort")


		private val nbtByteArrayGetBytesMethod = Class.forName("${NBTData.nmsPackage}.NBTTagByteArray")
			.getMethod("getBytes")
		private val nbtIntArrayGetIntsMethod = Class.forName("${NBTData.nmsPackage}.NBTTagIntArray")
			.getMethod("getInts")
		private val nbtLongArrayGetLongsMethod = Class.forName("${NBTData.nmsPackage}.NBTTagLongArray")
			.getMethod("getLongs")


		private val nbtStringAsStringMethod = Class.forName("${NBTData.nmsPackage}.NBTTagString")
			.getMethod("asString")



		private val nbtBaseGetTypeIdMethod = Class.forName("${NBTData.nmsPackage}.NBTBase")
			.getMethod("getTypeId")

		private inline fun <T> nbtDataType(
			typeId: Byte,
			crossinline decodeNMS: (Any) -> T,
			crossinline writeToCompound: (key: String, data: T, compound: Any) -> Unit,
		): NBTDataType<T> {
			return object : NBTDataType<T> {
				override fun decodeNMS(nbtBase: Any): T? {
					return if (nbtBaseGetTypeIdMethod.invoke(nbtBase) as Byte == typeId)
						decodeNMS.invoke(nbtBase)
					else
						null
				}
				override fun writeToCompound(key: String, data: T, compound: Any) {
					writeToCompound(key, data, compound)
				}
			}
		}
	}
}