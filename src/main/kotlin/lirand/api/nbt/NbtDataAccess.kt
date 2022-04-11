package lirand.api.nbt

val NbtData.compound get() = NbtDataAccessor(this, NbtCompoundType)
val NbtData.string get() = NbtDataAccessor(this, NbtStringType)
val NbtData.byte get() = NbtDataAccessor(this, NbtByteType)
val NbtData.short get() = NbtDataAccessor(this, NbtShortType)
val NbtData.int get() = NbtDataAccessor(this, NbtIntType)
val NbtData.long get() = NbtDataAccessor(this, NbtLongType)
val NbtData.float get() = NbtDataAccessor(this, NbtFloatType)
val NbtData.double get() = NbtDataAccessor(this, NbtDoubleType)
val NbtData.byteArray get() = NbtDataAccessor(this, NbtByteArrayType)
val NbtData.intArray get() = NbtDataAccessor(this, NbtIntArrayType)
val NbtData.longArray get() = NbtDataAccessor(this, NbtLongArrayType)

fun <T> NbtData.list(type: NbtDataType<T>) = NbtDataAccessor(this, NbtListType(type))
fun <T> NbtData.list(accessor: NbtDataAccessor<T>) = NbtDataAccessor(this, NbtListType(accessor.dataType))



/**
 * Gives access to change [NbtData] values of a certain [dataType].
 */
class NbtDataAccessor<T>(
	private val nbtData: NbtData,
	internal val dataType: NbtDataType<T>
) {
	/**
	 * Gets the value at the given [key].
	 * The returned value is null, if it
	 * was not possible to find any value at
	 * the specified location, or if the type
	 * is not the [dataType].
	 */
	operator fun get(key: String): T? {
		return nbtData[key, dataType]
	}

	/**
	 * Gets the value at the given [key].
	 * If it was not possible to find any value at
	 * the specified location, or if the type
	 * is not the [dataType],
	 * the result of calling [defaultValue] was put into specified location.
	 */
	inline fun getOrSet(key: String, defaultValue: () -> T): T {
		return get(key) ?: defaultValue().also {
			set(key, it)
		}
	}

	/**
	 * Gets the value at the given [key].
	 * The returned value is [defaultValue], if it
	 * was not possible to find any value at
	 * the specified location, or if the type
	 * is not the [dataType].
	 */
	inline fun getOrDefault(key: String, defaultValue: () -> T): T {
		return get(key) ?: defaultValue()
	}


	/**
	 * Sets some [value]
	 * at the position of the given [key].
	 */
	operator fun set(key: String, value: T) {
		nbtData[key, dataType] = value
	}
}