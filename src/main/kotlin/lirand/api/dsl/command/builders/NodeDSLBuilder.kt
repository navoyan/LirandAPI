package lirand.api.dsl.command.builders

import com.github.shynixn.mccoroutine.launch
import com.mojang.brigadier.Command
import com.mojang.brigadier.RedirectModifier
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.tree.CommandNode
import lirand.api.dsl.command.builders.fails.CommandFailException
import lirand.api.dsl.command.implementation.tree.nodes.BrigadierArgument
import lirand.api.dsl.command.implementation.tree.nodes.BrigadierLiteral
import lirand.api.extensions.chat.sendMessage
import lirand.api.extensions.server.allPermissions
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.function.Predicate

private typealias CommandExecution<S> = suspend BrigadierCommandContext<S>.() -> Unit

@NodeBuilderDSLMarker
abstract class NodeDSLBuilder<B : ArgumentBuilder<CommandSender, B>>(
	val plugin: Plugin,
	@PublishedApi
	internal open val actualBuilder: ArgumentBuilder<CommandSender, B>
) {

	val arguments: Collection<CommandNode<CommandSender>>
		get() = actualBuilder.arguments

	val executor: Command<CommandSender>
		get() = actualBuilder.command

	private val _requirements = mutableListOf<Predicate<CommandSender>>()
	val requirements: List<Predicate<CommandSender>>
		get() = _requirements

	inline val completeRequirement: (CommandSender) -> Boolean
		get() = { sender -> requirements.all { it.test(sender) } }

	val redirect: CommandNode<CommandSender>
		get() = actualBuilder.redirect

	val redirectModifier: RedirectModifier<CommandSender>
		get() = actualBuilder.redirectModifier

	val isFork get() = actualBuilder.isFork

	private var isRequirementSetup = false
	private var isExecutorSetup = false

	var defaultExecution: CommandExecution<CommandSender>? = null
		private set
	var playerExecution: CommandExecution<Player>? = null
		private set
	var consoleExecution: CommandExecution<ConsoleCommandSender>? = null
		private set



	@NodeBuilderDSLMarker
	inline fun literal(
		name: String,
		builder: LiteralDSLBuilder.() -> Unit
	): BrigadierLiteral<CommandSender> {
		val childNode = LiteralDSLBuilder(
			plugin, LiteralArgumentBuilder.literal(name)
		).apply(builder).build()

		actualBuilder.then(childNode)

		return childNode
	}

	@NodeBuilderDSLMarker
	inline fun <A : ArgumentType<T>, T> argument(
		name: String,
		type: A,
		builder: ArgumentDSLBuilder<T>.(ArgumentDefinition<A, T>) -> Unit
	): BrigadierArgument<CommandSender, T> {
		val childNode = ArgumentDSLBuilder(
			plugin, RequiredArgumentBuilder.argument(name, type)
		).apply {
			builder(ArgumentDefinition(name, type))
		}.build()

		actualBuilder.then(childNode)

		return childNode
	}



	@NodeBuilderDSLMarker
	fun executes(block: suspend BrigadierCommandContext<CommandSender>.() -> Unit) {
		if (!isExecutorSetup) {
			setupExecutor()
		}
		defaultExecution = block
	}

	@NodeBuilderDSLMarker
	fun executesPlayer(block: suspend BrigadierCommandContext<Player>.() -> Unit) {
		if (!isExecutorSetup) {
			setupExecutor()
		}
		playerExecution = block
	}

	@NodeBuilderDSLMarker
	fun executesConsole(block: suspend BrigadierCommandContext<ConsoleCommandSender>.() -> Unit) {
		if (!isExecutorSetup) {
			setupExecutor()
		}
		consoleExecution = block
	}



	@NodeBuilderDSLMarker
	fun requires(predicate: (CommandSender) -> Boolean) {
		if (!isRequirementSetup) {
			actualBuilder.requires { completeRequirement(it) }
		}
		_requirements.add(predicate)
	}

	@NodeBuilderDSLMarker
	fun requiresPermissions(permission: String, vararg permissions: String) {
		if (!isRequirementSetup) {
			actualBuilder.requires { completeRequirement(it) }
		}
		_requirements.add { sender -> sender.allPermissions(permission, *permissions) }
	}



	fun redirect(target: CommandNode<CommandSender>) {
		fixedRedirect(target)
	}

	fun fork(
		target: CommandNode<CommandSender>,
		modifier: RedirectModifier<CommandSender>? = null
	) {
		actualBuilder.fork(target, modifier)
	}

	fun forward(
		target: CommandNode<CommandSender>,
		modifier: RedirectModifier<CommandSender>? = null,
		isFork: Boolean
	) {
		actualBuilder.forward(target, modifier, isFork)
	}



	abstract fun build(): CommandNode<CommandSender>



	private fun setupExecutor() {
		actualBuilder.executes { context ->

			val brigadierContext = BrigadierCommandContext(context)

			invokeCatching(brigadierContext, defaultExecution)

			if (context.source is Player) {
				invokeCatching(
					brigadierContext as BrigadierCommandContext<Player>,
					playerExecution
				)
			}

			if (context.source is ConsoleCommandSender) {
				invokeCatching(
					brigadierContext as BrigadierCommandContext<ConsoleCommandSender>,
					consoleExecution
				)
			}
			
			Command.SINGLE_SUCCESS
		}
		isExecutorSetup = true
	}

	private fun <S : CommandSender> invokeCatching(
		context: BrigadierCommandContext<S>,
		executor: CommandExecution<S>?
	) {
		if (executor == null) return

		plugin.launch {
			try {
				executor(context)
			} catch (exception: CommandFailException) {
				val message = exception.failMessage

				if (message != null) {
					context.source.sendMessage(message)
				}
			}
		}
	}

	private fun fixedRedirect(target: CommandNode<CommandSender>) {
		actualBuilder.requires(target.requirement)
		actualBuilder.forward(target.redirect, target.redirectModifier, target.isFork)
		actualBuilder.executes(target.command)

		for (child in target.children) {
			actualBuilder.then(child)
		}
	}
}