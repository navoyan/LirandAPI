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
import org.bukkit.Particle
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

/**
 * A [Particle] type.
 */
open class ParticleType(
	open val allowedParticles: (sender: Player?) -> Map<Particle, Message?> = Instance.allowedParticles,
	open val notFoundExceptionType: ChatCommandExceptionType = Instance.notFoundExceptionType
) : WordType<Particle> {

	/**
	 * Returns a [Particle] from the result of the [allowedParticles]
	 * which name matches the string returned by the given [reader].
	 *
	 * @param reader the reader
	 * @return a [Particle] with the given name
	 * @throws ChatCommandSyntaxException if a [Particle] with the given name does not exist
	 */
	override fun parse(reader: StringReader): Particle {
		val name = reader.readUnquoted().lowercase()

		return allParticles[name]?.takeIf { it in allowedParticles(null) }
			?: throw notFoundExceptionType.createWithContext(reader, name)
	}

	/**
	 * Returns the [Particle] names from the result of the [allowedParticles]
	 * that start with the remaining input of the given [builder].
	 *
	 * @param S the type of the source
	 * @param context the context
	 * @param builder the builder
	 * @return the [Particle] names that start with the remaining input
	 */
	override fun <S> listSuggestions(
		context: CommandContext<S>,
		builder: SuggestionsBuilder
	): CompletableFuture<Suggestions> {
		val sender = context.source as? Player? ?: return builder.buildFuture()

		allowedParticles(sender).mapKeys { (particle, _) -> particle.name.lowercase() }
			.filterKeys { it.startsWith(builder.remaining, true) }
			.forEach { (particleName, tooltip) ->
				if (tooltip != null)
					builder.suggest(particleName, tooltip)
				else
					builder.suggest(particleName)
			}

		return builder.buildFuture()
	}

	override fun getExamples(): List<String> = listOf("barrier", "bubble_column_up")



	companion object Instance : ParticleType(
		allowedParticles = { Particle.values().associateWith { null } },
		notFoundExceptionType = ChatCommandExceptionType {
			TranslatableComponent("particle.notFound", it[0].toString().lowercase())
		}
	) {
		val allParticles = Particle.values()
			.associateBy { it.name.lowercase() }
	}
}