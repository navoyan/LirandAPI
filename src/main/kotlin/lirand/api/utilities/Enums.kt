package lirand.api.utilities

import com.google.common.collect.Sets
import java.util.*

inline fun <reified E : Enum<E>> enumSetOf(vararg elements: E): EnumSet<E> {
	return Sets.newEnumSet(elements.toHashSet(), E::class.java)
}

inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String) =
	try {
		enumValueOf<T>(name)
	} catch (exception: IllegalArgumentException) {
		null
	}