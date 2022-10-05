package lirand.api.dsl.command.types

import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import lirand.api.dsl.command.types.exceptions.ChatCommandExceptionType
import lirand.api.dsl.command.types.exceptions.ChatCommandSyntaxException
import lirand.api.dsl.command.types.extensions.readUnquoted
import net.md_5.bungee.api.chat.TranslatableComponent
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

/**
 * A [Material] type. Legacy materials are **not** supported.
 */
open class MaterialType(
	open val allowedMaterials: (sender: Player?) -> Map<Material, Message?> = Instance.allowedMaterials,
	open val notFoundExceptionType: ChatCommandExceptionType = Instance.notFoundExceptionType
) : WordType<Material> {

	/**
	 * Returns a [Material] from the result of the [allowedMaterials]
	 * which key matches the string returned by the given [reader].
	 *
	 * @param reader the reader
	 * @return a [Material] with the given key
	 * @throws ChatCommandSyntaxException if a [Material] with the given key does not exist
	 */
	override fun parse(reader: StringReader): Material {
		val name = reader.readUnquoted().lowercase()

		return allMaterials[name]?.takeIf { it in allowedMaterials(null) }
			?: throw notFoundExceptionType.createWithContext(reader, name)
	}

	/**
	 * Returns the [Material] names from the result of the [allowedMaterials]
	 * that start with the remaining input of the given [builder].
	 *
	 * @param S the type of the source
	 * @param context the context
	 * @param builder the builder
	 * @return the [Material] names that start with the remaining input
	 */
	override fun <S> listSuggestions(
		context: CommandContext<S>,
		builder: SuggestionsBuilder
	): CompletableFuture<Suggestions> {
		val sender = context.source as? Player? ?: return builder.buildFuture()

		allowedMaterials(sender).mapKeys { (material, _) -> material.key.key }
			.filterKeys { it.startsWith(builder.remaining, true) }
			.forEach { (materialName, tooltip) ->
				if (tooltip != null)
					builder.suggest(materialName, tooltip)
				else
					builder.suggest(materialName)
			}

		return builder.buildFuture()
	}

	override fun getExamples(): List<String> = listOf("flint_and_steel", "tnt")

	companion object Instance : MaterialType(
		allowedMaterials = { Material.values().filter { !it.isLegacy }.associateWith { null } },
		notFoundExceptionType = ChatCommandExceptionType {
			TranslatableComponent("argument.id.unknown", it[0])
		}
	) {
		val allMaterials = Material.values()
			.filter { !it.isLegacy }
			.associateBy { it.key.key }
	}
}