package lirand.api.dsl.menu.exposed.fixed

import java.util.WeakHashMap

class MenuTypedDataMap : WeakHashMap<String, Any?>() {

	fun <T> getTyped(key: String): T {
		return get(key) as T
	}

}