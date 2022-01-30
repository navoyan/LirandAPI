package lirand.api.dsl.command.builders

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import lirand.api.dsl.command.implementation.tree.nodes.BrigadierLiteral
import lirand.api.dsl.command.implementation.tree.nodes.createAlias
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin

class LiteralDSLBuilder(
	plugin: Plugin,
	override val actualBuilder: LiteralArgumentBuilder<CommandSender>
) : NodeDSLBuilder<LiteralArgumentBuilder<CommandSender>>(plugin, actualBuilder) {

	val literal: String
		get() = actualBuilder.literal

	private val _aliases = mutableListOf<String>()
	val aliases: List<String> get() = _aliases


	fun alias(vararg aliases: String) {
		_aliases.addAll(aliases)
	}

	override fun build(): BrigadierLiteral<CommandSender> {
		val literal = with(actualBuilder) {
			BrigadierLiteral(
				literal, mutableListOf(), false,
				command, requirement, redirect,
				redirectModifier, isFork
			)
		}

		for (child in arguments) {
			literal.addChild(child)
		}

		for (alias in aliases) {
			literal.createAlias(alias)
		}

		return literal
	}
}