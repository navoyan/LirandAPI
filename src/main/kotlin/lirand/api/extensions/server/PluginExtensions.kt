package lirand.api.extensions.server

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import org.bukkit.NamespacedKey
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import java.io.File
import java.net.URL
import java.net.URLDecoder
import java.util.jar.JarFile

fun Plugin.registerEvents(
	vararg listeners: Listener
) = listeners.forEach { server.pluginManager.registerEvents(it, this) }

fun Plugin.registerSuspendingEvents(
	vararg listeners: Listener
) = listeners.forEach { server.pluginManager.registerSuspendingEvents(it, this) }


fun Plugin.getKey(name: String) = NamespacedKey(this, name)


fun Plugin.getResourcesNames(path: String): List<String>? {
	val directoryURL: URL = this::class.java.classLoader.getResource(path) ?: return null

	val result = mutableListOf<String>()

	val jarPath: String = directoryURL.path.substring(5, directoryURL.path.indexOf("!"))

	val jar: JarFile = try {
		JarFile(URLDecoder.decode(jarPath, "UTF-8"));
	} catch (exception: Exception) {
		return null
	}

	val entries = jar.entries()

	while (entries.hasMoreElements()) {
		val name = entries.nextElement().name;
		if (name.startsWith(path)) {
			val entry: String = name.substring(path.length + 1);
			val last: String = name.substring(name.length - 1);

			if (last != File.separator) {
				if (entry.matches(Regex(".*[a-zA-Z0-9].*"))) {
					result.add(entry);
				}
			}
		}
	}

	return result
}