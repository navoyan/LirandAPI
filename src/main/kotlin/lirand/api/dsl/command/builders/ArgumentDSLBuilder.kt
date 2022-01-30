package lirand.api.dsl.command.builders

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import lirand.api.dsl.command.implementation.tree.nodes.BrigadierArgument
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin

class ArgumentDSLBuilder<T>(
	plugin: Plugin,
	override val actualBuilder: RequiredArgumentBuilder<CommandSender, T>
) : NodeDSLBuilder<RequiredArgumentBuilder<CommandSender, T>>(plugin, actualBuilder) {

	val name: String
		get() = actualBuilder.name

	val type: ArgumentType<T>
		get() = actualBuilder.type

	val suggestionsProvider: SuggestionProvider<CommandSender>
		get() = actualBuilder.suggestionsProvider



	@NodeBuilderDSLMarker
	fun suggests(provider: BrigadierCommandContext<CommandSender>.(builder: SuggestionsBuilder) -> Unit) {
		actualBuilder.suggests { context, builder ->
			BrigadierCommandContext<CommandSender>(context).provider(builder)
			builder.buildFuture()
		}
	}

	override fun build(): BrigadierArgument<CommandSender, T> {
		val argument = with(actualBuilder) {
			BrigadierArgument<CommandSender, T>(
				name, type, command, requirement, suggestionsProvider,
				redirect, redirectModifier, isFork
			)
		}
		for (child in arguments) {
			argument.addChild(child)
		}

		return argument
	}
}