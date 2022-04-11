package lirand.api.nbt

import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KClass

interface NbtDataType<T> {

	val typeId: Int

	fun decode(nbtBase: Any): T?

	fun encode(data: T): Any

}


val NbtCompoundType = createNbtType<NbtData>(
	10,
	{ tag -> NbtData(tag) },
	{ data -> data.nbtTagCompound }
)

val NbtStringType = createNbtType<String>(
	8,
	{ tag -> nbtStringAsStringMethod.invoke(tag) as String },
	{ data -> stringTagFactoryMethod.invoke(null, data) }
)

val NbtByteType = createNbtType<Byte>(
	1,
	{ tag -> nbtNumberAsByteMethod.invoke(tag) as Byte },
	{ data -> byteTagFactoryMethod.invoke(null, data) }
)

val NbtShortType = createNbtType<Short>(
	2,
	{ tag -> nbtNumberAsShortMethod.invoke(tag) as Short },
	{ data -> shortTagFactoryMethod.invoke(null, data) }
)

val NbtIntType = createNbtType<Int>(
	3,
	{ tag -> nbtNumberAsIntMethod.invoke(tag) as Int },
	{ data -> intTagFactoryMethod.invoke(null, data) }
)

val NbtLongType = createNbtType<Long>(
	4,
	{ tag -> nbtNumberAsLongMethod.invoke(tag) as Long },
	{ data -> longTagFactoryMethod.invoke(null, data) }
)

val NbtFloatType = createNbtType<Float>(
	5,
	{ tag -> nbtNumberAsFloatMethod.invoke(tag) as Float },
	{ data -> floatTagFactoryMethod.invoke(null, data) }
)

val NbtDoubleType = createNbtType<Double>(
	6,
	{ tag -> nbtNumberAsDoubleMethod.invoke(tag) as Double },
	{ data -> doubleTagFactoryMethod.invoke(null, data) }
)

val NbtByteArrayType = createNbtType<ByteArray>(
	7,
	{ tag -> nbtByteArrayGetBytesMethod.invoke(tag) as ByteArray },
	{ data -> byteArrayTagConstructor.newInstance(data) }
)

val NbtIntArrayType = createNbtType<IntArray>(
	11,
	{ tag -> nbtIntArrayGetIntsMethod.invoke(tag) as IntArray },
	{ data -> intArrayTagConstructor.newInstance(data) }
)

val NbtLongArrayType = createNbtType<LongArray>(
	12,
	{ tag -> nbtLongArrayGetLongsMethod.invoke(tag) as LongArray },
	{ data -> longArrayTagConstructor.newInstance(data) }
)

fun <T> NbtListType(type: NbtDataType<T>): NbtDataType<List<T>> {
	return createNbtType<List<T>>(
		9,
		{ tag ->
			val list = tag as MutableList<Any>

			list.map { type.decode(it)!! }
		},
		{ data ->
			(nbtListConstructor.newInstance() as MutableList<Any>).apply {
				addAll(data.map { type.encode(it) })
			}
		}
	)
}



private inline fun <T> createNbtType(
	typeId: Int,
	crossinline decode: (Any) -> T,
	crossinline encode: (data: T) -> Any
): NbtDataType<T> {
	return object : NbtDataType<T> {
		override val typeId: Int = typeId

		override fun decode(nbtBase: Any): T? {
			return if ((nbtBaseGetTypeIdMethod.invoke(nbtBase) as Byte).toInt() == typeId)
				decode.invoke(nbtBase)
			else
				null
		}
		override fun encode(data: T): Any {
			return encode(data)
		}
	}
}




private val byteTagFactoryMethod = getTagFactoryMethod("NBTTagByte")
private val shortTagFactoryMethod = getTagFactoryMethod("NBTTagShort")
private val intTagFactoryMethod = getTagFactoryMethod("NBTTagInt")
private val longTagFactoryMethod = getTagFactoryMethod("NBTTagLong")
private val floatTagFactoryMethod = getTagFactoryMethod("NBTTagFloat")
private val doubleTagFactoryMethod = getTagFactoryMethod("NBTTagDouble")
private val stringTagFactoryMethod = getTagFactoryMethod("NBTTagString")

private fun getTagFactoryMethod(className: String): Method {
	val clazz = Class.forName("${nmsPackage}.$className")

	return clazz.methods.find { it.returnType == clazz && Modifier.isStatic(it.modifiers) }!!
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