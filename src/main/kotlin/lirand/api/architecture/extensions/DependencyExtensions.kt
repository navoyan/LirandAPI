package lirand.api.architecture.extensions

import org.bukkit.plugin.Plugin
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Delegate that returns the plugin dependency if the plugin is installed in the server
 * otherwise, returns null
 */
inline fun <reified T : Plugin> Plugin.softDepend(
	pluginName: String
) = softDepend(T::class, pluginName)

fun <T : Plugin> Plugin.softDepend(
	type: KClass<T>,
	pluginName: String
): SoftDependencyDelegate<T> =
	SoftDependencyDelegate(
		pluginName,
		type
	)

/**
 * Delegate that returns the plugin dependency, disable the plugin if the plugin
 * is not available.
 */
inline fun <reified T : Plugin> Plugin.depend(
	pluginName: String
) = depend(T::class, pluginName)

fun <T : Plugin> Plugin.depend(
	type: KClass<T>,
	pluginName: String
): DependencyDelegate<T> =
	DependencyDelegate(pluginName, type)


class DependencyDelegate<T : Plugin>(
	val pluginName: String,
	val type: KClass<T>
) : ReadOnlyProperty<Plugin, T> {

	private var isDisabled: Boolean = false
	private var cache: T? = null

	override operator fun getValue(
		thisRef: Plugin,
		property: KProperty<*>
	): T {
		if (cache == null) {
			val plugin = thisRef.server.pluginManager.getPlugin(pluginName)

			if (plugin != null) {
				if (type.isInstance(plugin)) {
					cache = plugin as T
				}
				else {
					thisRef.server.pluginManager.disablePlugin(thisRef)
					error(
						"Invalid plugin dependency with the name $pluginName: " +
								"The plugin do not match main class with ${type.qualifiedName}."
					)
				}
			}
			else {
				thisRef.server.pluginManager.disablePlugin(thisRef)
				error("Missing plugin dependency: $pluginName")
			}
		}

		return cache!!
	}
}

class SoftDependencyDelegate<T : Plugin>(
	val pluginName: String,
	val type: KClass<T>
) : ReadOnlyProperty<Plugin, T?> {

	private var alreadySearch: Boolean = false
	private var cache: T? = null

	override operator fun getValue(
		thisRef: Plugin,
		property: KProperty<*>
	): T? {
		if (!alreadySearch) {
			val plugin = thisRef.server.pluginManager.getPlugin(pluginName) ?: return null

			alreadySearch = true

			if (type.isInstance(plugin)) {
				cache = plugin as T
			}
			else {
				thisRef.server.pluginManager.disablePlugin(thisRef)
				error(
					"Invalid plugin dependency with the name $pluginName: " +
							"The plugin do not match main class with ${type.qualifiedName}."
				)
			}
		}

		return cache
	}
}

