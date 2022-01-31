package lirand.api.architecture

import com.github.shynixn.mccoroutine.SuspendingJavaPlugin
import lirand.api.LirandAPI
import lirand.api.dsl.menu.dynamic.chest.chestMenu
import lirand.api.dsl.menu.dynamic.chest.slot
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

abstract class KotlinPlugin : SuspendingJavaPlugin() {

	open suspend fun onPluginLoad() {}
	open suspend fun onPluginEnable() {}
	open suspend fun onPluginDisable() {}

	final override suspend fun onLoadAsync() {
		onPluginLoad()
	}

	final override suspend fun onEnableAsync() {
		try {
			LirandAPI.register(this)
		} catch (_: IllegalStateException) {}

		onPluginEnable()
	}

	final override suspend fun onDisableAsync() {
		onPluginDisable()
	}

}