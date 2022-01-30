package lirand.api.extensions.events

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

val PlayerMoveEvent.displaced: Boolean
	get() = this.from.x != this.to?.x || this.from.y != this.to?.y || this.from.z != this.to?.z

/**
 * Returns the item used in the interaction
 * with the use of the [EquipmentSlot] returned
 * by the value [PlayerInteractEntityEvent.hand].
 */
val PlayerInteractEntityEvent.interactItem: ItemStack?
	get() {
		return when (hand) {
			EquipmentSlot.HAND -> player.inventory.itemInMainHand
			EquipmentSlot.OFF_HAND -> player.inventory.itemInOffHand
			else -> null
		}
	}

/**
 * @return True, if the action was a left mouse button click.
 */
val Action.isLeftClick get() = this == Action.LEFT_CLICK_BLOCK || this == Action.LEFT_CLICK_AIR

/**
 * @return True, if the action was a right mouse button click.
 */
val Action.isRightClick get() = this == Action.RIGHT_CLICK_BLOCK || this == Action.RIGHT_CLICK_AIR

val PlayerInteractEvent.clickedBlockExceptAir: Block?
	get() {
		return clickedBlock ?: run {
			return@run if (this.action == Action.RIGHT_CLICK_AIR) {
				val p: Player = this.player
				// check for sight blocking entities
				for (nearbyEntity: Entity in p.getNearbyEntities(5.0, 5.0, 5.0))
					if (p.hasLineOfSight(nearbyEntity)) return@run null
				// get first block in line of sight which is not air
				p.getLineOfSight(null, 5).find { block -> !block.type.isAir }
			}
			else null
		}
	}


/**
 * Checks if the event is "cancelled"
 * by returning if the material of
 * the result is equal to [Material.AIR].
 */
val PrepareItemCraftEvent.isCancelled: Boolean
	get() = inventory.result?.type == Material.AIR

/**
 * "Cancels" this event by
 * setting the result to [Material.AIR].
 */
fun PrepareItemCraftEvent.cancel() {
	inventory.result = ItemStack(Material.AIR)
}
