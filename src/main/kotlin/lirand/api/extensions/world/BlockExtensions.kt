package lirand.api.extensions.world

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player

operator fun Block.component1() = x
operator fun Block.component2() = y
operator fun Block.component3() = z


fun Block.equalsType(block: Block) = type == block.type
fun Block.equalsType(data: BlockData) = type == data.material && blockData == data
fun Block.equalsType(material: Material) = type == material


fun Block.sendBlockChange(
	blockData: BlockData,
	players: List<Player>
) {
	players.filter { it.world.name == world.name }.forEach {
		it.sendBlockChange(location, blockData)
	}
}

fun Block.sendBlockChange(
	material: Material,
	players: List<Player>
) = sendBlockChange(material.createBlockData(), players)