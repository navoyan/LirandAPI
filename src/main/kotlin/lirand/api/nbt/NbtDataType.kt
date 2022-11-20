package lirand.api.nbt

import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KClass

interface NbtDataType<T> {

	val typeId: Int

	fun decode(nbt: Any): T?

	fun encode(data: T): Any

}


abstract class AbstractNbtDataType<T>(override val typeId: Int) : NbtDataType<T> {
	final override fun decode(nbt: Any): T? {
		return if ((nbtBaseGetTypeIdMethod.invoke(nbt) as Byte).toInt() == typeId)
			decodeCorrectlyTyped(nbt)
		else
			null
	}

	protected abstract fun decodeCorrectlyTyped(nbt: Any): T

	override fun toString(): String {
		return javaClass.name
	}
}


object NbtCompoundType : AbstractNbtDataType<NbtData>(10) {
	override fun encode(data: NbtData): Any {
		return data.nbtTagCompound
	}

	override fun decodeCorrectlyTyped(nbt: Any): NbtData {
		return NbtData(nbt)
	}
}

object NbtStringType : AbstractNbtDataType<String>(8) {
	override fun encode(data: String): Any {
		return stringTagFactoryMethod.invoke(null, data)
	}

	override fun decodeCorrectlyTyped(nbt: Any): String {
		return (nbtStringAsStringMethod.invoke(nbt) as String).removeSurrounding("\"")
	}
}

object NbtByteType : AbstractNbtDataType<Byte>(1) {
	override fun encode(data: Byte): Any {
		return byteTagFactoryMethod.invoke(null, data)
	}

	override fun decodeCorrectlyTyped(nbt: Any): Byte {
		return nbtNumberAsByteMethod.invoke(nbt) as Byte
	}
}

object NbtShortType : AbstractNbtDataType<Short>(2) {
	override fun encode(data: Short): Any {
		return shortTagFactoryMethod.invoke(null, data)
	}

	override fun decodeCorrectlyTyped(nbt: Any): Short {
		return nbtNumberAsShortMethod.invoke(nbt) as Short
	}
}

object NbtIntType : AbstractNbtDataType<Int>(3) {
	override fun encode(data: Int): Any {
		return intTagFactoryMethod.invoke(null, data)
	}

	override fun decodeCorrectlyTyped(nbt: Any): Int {
		return nbtNumberAsIntMethod.invoke(nbt) as Int
	}
}

object NbtLongType : AbstractNbtDataType<Long>(4) {
	override fun encode(data: Long): Any {
		return longTagFactoryMethod.invoke(null, data)
	}

	override fun decodeCorrectlyTyped(nbt: Any): Long {
		return nbtNumberAsLongMethod.invoke(nbt) as Long
	}
}

object NbtFloatType : AbstractNbtDataType<Float>(5) {
	override fun encode(data: Float): Any {
		return floatTagFactoryMethod.invoke(null, data)
	}

	override fun decodeCorrectlyTyped(nbt: Any): Float {
		return nbtNumberAsFloatMethod.invoke(nbt) as Float
	}
}

object NbtDoubleType : AbstractNbtDataType<Double>(6) {
	override fun encode(data: Double): Any {
		return doubleTagFactoryMethod.invoke(null, data)
	}

	override fun decodeCorrectlyTyped(nbt: Any): Double {
		return nbtNumberAsDoubleMethod.invoke(nbt) as Double
	}

}

object NbtByteArrayType : AbstractNbtDataType<ByteArray>(7) {
	override fun encode(data: ByteArray): Any {
		return byteArrayTagConstructor.newInstance(data)
	}

	override fun decodeCorrectlyTyped(nbt: Any): ByteArray {
		return nbtByteArrayGetBytesMethod.invoke(nbt) as ByteArray
	}
}

object NbtIntArrayType : AbstractNbtDataType<IntArray>(11) {
	override fun encode(data: IntArray): Any {
		return intArrayTagConstructor.newInstance(data)
	}

	override fun decodeCorrectlyTyped(nbt: Any): IntArray {
		return nbtIntArrayGetIntsMethod.invoke(nbt) as IntArray
	}
}

object NbtLongArrayType : AbstractNbtDataType<LongArray>(12) {
	override fun encode(data: LongArray): Any {
		return longArrayTagConstructor.newInstance(data)
	}

	override fun decodeCorrectlyTyped(nbt: Any): LongArray {
		return nbtLongArrayGetLongsMethod.invoke(nbt) as LongArray
	}
}

fun <T> NbtListType(type: NbtDataType<T>): NbtDataType<List<T>> {
	return object : AbstractNbtDataType<List<T>>(9) {
		override fun encode(data: List<T>): Any {
			return (nbtListConstructor.newInstance() as MutableList<Any>).apply {
				addAll(data.map { type.encode(it) })
			}
		}

		override fun decodeCorrectlyTyped(nbt: Any): List<T> {
			val list = nbt as MutableList<Any>

			return list.map { type.decode(it)!! }
		}
	}
}



private val byteTagFactoryMethod = getTagFactoryMethod("NBTTagByte", Byte::class)
private val shortTagFactoryMethod = getTagFactoryMethod("NBTTagShort", Short::class)
private val intTagFactoryMethod = getTagFactoryMethod("NBTTagInt", Int::class)
private val longTagFactoryMethod = getTagFactoryMethod("NBTTagLong", Long::class)
private val floatTagFactoryMethod = getTagFactoryMethod("NBTTagFloat", Float::class)
private val doubleTagFactoryMethod = getTagFactoryMethod("NBTTagDouble", Double::class)
private val stringTagFactoryMethod = getTagFactoryMethod("NBTTagString", String::class)

private fun getTagFactoryMethod(className: String, argumentType: KClass<*>): Method {
	val clazz = Class.forName("${nmsPackage}.$className")

	return clazz.methods.find {
		it.returnType == clazz && Modifier.isStatic(it.modifiers)
				&& it.parameterTypes.let { it.size == 1 && it[0] == argumentType.java }
	}!!
}


private val nbtListConstructor = Class.forName("${nmsPackage}.NBTTagList").getConstructor()

private val byteArrayTagConstructor = getTagConstructor("NBTTagByteArray", ByteArray::class)
private val intArrayTagConstructor = getTagConstructor("NBTTagIntArray", IntArray::class)
private val longArrayTagConstructor = getTagConstructor("NBTTagLongArray", LongArray::class)

private fun getTagConstructor(className: String, dataArgumentClass: KClass<*>): Constructor<*> {
	val clazz = Class.forName("${nmsPackage}.$className")

	return clazz.getConstructor(dataArgumentClass.java)
}



private val nbtNumberClass = Class.forName("${nmsPackage}.NBTNumber")

private val nbtNumberAsByteMethod = getDecodeMethod(nbtNumberClass, Byte::class)
private val nbtNumberAsDoubleMethod = getDecodeMethod(nbtNumberClass, Double::class)
private val nbtNumberAsFloatMethod = getDecodeMethod(nbtNumberClass, Float::class)
private val nbtNumberAsIntMethod = getDecodeMethod(nbtNumberClass, Int::class)
private val nbtNumberAsLongMethod = getDecodeMethod(nbtNumberClass, Long::class)
private val nbtNumberAsShortMethod = getDecodeMethod(nbtNumberClass, Short::class)
private val nbtByteArrayGetBytesMethod = getDecodeMethod("NBTTagByteArray", ByteArray::class)
private val nbtIntArrayGetIntsMethod = getDecodeMethod("NBTTagIntArray", IntArray::class)
private val nbtLongArrayGetLongsMethod = getDecodeMethod("NBTTagLongArray", LongArray::class)
private val nbtStringAsStringMethod = getDecodeMethod("NBTTagString", String::class)

private fun getDecodeMethod(clazz: Class<*>, returnType: KClass<*>): Method {
	return clazz.methods.find { it.returnType == returnType.java && it.parameterCount == 0 }!!
}

private fun getDecodeMethod(className: String, returnType: KClass<*>): Method {
	return getDecodeMethod(Class.forName("${nmsPackage}.$className"), returnType)
}


internal val nbtBaseGetTypeIdMethod = Class.forName("${nmsPackage}.NBTBase").methods
	.find { it.returnType == Byte::class.java && it.parameterCount == 0 }!!