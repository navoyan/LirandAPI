package lirand.api.dsl.command.builders

import com.github.shynixn.mccoroutine.minecraftDispatcher
import com.mojang.brigadier.RedirectModifier
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.RootCommandNode
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
import com.mojang.brigadier.Command as BrigadierCommand

private typealias CommandExecutor<S> = BrigadierCommandContext<S>.(scope: CoroutineScope) -> Unit

@NodeBuilderDSLMarker
abstract class NodeDSLBuilder<B : ArgumentBuilder<CommandSender, B>>(
	val plugin: Plugin
) {

	private val rootNode = RootCommandNode<CommandSender>()

	val arguments: Collection<CommandNode<CommandSender>>
		get() = rootNode.children


	protected val scope = CoroutineScope(
		plugin.minecraftDispatcher + SupervisorJob() +
				CoroutineExceptionHandler { _, exception -> exception.printStackTrace() }
	)

	var completeExecutor: BrigadierCommand<CommandSender>? = null
		private set

	var defaultExecutor: CommandExecutor<CommandSender>? = null
		private set
	var playerExecutor: CommandExecutor<Player>? = null
		private set
	var consoleExecutor: CommandExecutor<ConsoleCommandSender>? = null
		private set


	var completeRequirement: Predicate<CommandSender> = Predicate { sender -> requirements.all { it.test(sender) } }
		private set

	private val _requirements = mutableListOf<Predicate<CommandSender>>()
	val requirements: List<Predicate<CommandSender>>
		get() = _requirements


	var redirect: CommandNode<CommandSender>? = null
		private set

	var redirectModifier: RedirectModifier<CommandSender>? = null
		private set

	var isFork: Boolean = false
		private set



	inline fun literal(
		name: String,
		crossinline builder: LiteralDSLBuilder.() -> Unit
	): BrigadierLiteral<CommandSender> {
		val childNode = LiteralDSLBuilder(plugin, name)
			.apply(builder).build()

		addChild(childNode)

		return childNode
	}

	inline fun <A : ArgumentType<T>, T> argument(
		name: String,
		type: A,
		crossinline builder: ArgumentDSLBuilder<T>.(argument: ArgumentDefinition<A, T>) -> Unit
	): BrigadierArgument<CommandSender, T> {
		val childNode = ArgumentDSLBuilder(plugin, name, type).apply {
			builder(ArgumentDefinition(name, type))
		}.build()

		addChild(childNode)

		return childNode
	}



	fun executes(block: CommandExecutor<CommandSender>) {
		if (completeExecutor == null) {
			setupExecutor()
		}
		defaultExecutor = block
	}

	fun executesPlayer(block: CommandExecutor<Player>) {
		if (completeExecutor == null) {
			setupExecutor()
		}
		playerExecutor = block
	}

	fun executesConsole(block: CommandExecutor<ConsoleCommandSender>) {
		if (completeExecutor == null) {
			setupExecutor()
		}
		consoleExecutor = block
	}



	fun requires(predicate: (sender: CommandSender) -> Boolean) {
		_requirements.add(predicate)
	}

	fun requiresPermissions(permission: String, vararg permissions: String) {
		_requirements.add { sender -> sender.allPermissions(permission, *permissions) }
	}



	fun redirect(target: CommandNode<CommandSender>) {
		_requirements.add(target.requirement)
		forward(target.redirect, target.redirectModifier, target.isFork)
		completeExecutor = target.command

		for (child in target.children) {
			addChild(child)
		}
	}

	fun fork(
		target: CommandNode<CommandSender>,
		modifier: RedirectModifier<CommandSender>? = null
	) {
		forward(target, modifier, true)
	}

	fun forward(
		target: CommandNode<CommandSender>,
		modifier: RedirectModifier<CommandSender>? = null,
		isFork: Boolean
	) {
		check(rootNode.children.isEmpty()) { "Cannot forward a node with children." }
		redirect = target
		redirectModifier = modifier
		this.isFork = isFork
	}



	abstract fun build(): CommandNode<CommandSender>



	@PublishedApi
	internal fun addChild(node: CommandNode<CommandSender>) {
		check(redirect == null) { "Cannot add children to a redirected node." }
		rootNode.addChild(node)
	}

	private fun setupExecutor() {
		completeExecutor = BrigadierCommand { context ->

			val brigadierContext = BrigadierCommandContext(context)

			invokeCatching(brigadierContext, defaultExecutor)

			if (context.source is Player) {
				invokeCatching(
					brigadierContext as BrigadierCommandContext<Player>,
					playerExecutor
				)
			}

			if (context.source is ConsoleCommandSender) {
				invokeCatching(
					brigadierContext as BrigadierCommandContext<ConsoleCommandSender>,
					consoleExecutor
				)
			}
			
			BrigadierCommand.SINGLE_SUCCESS
		}
	}

	private fun <S : CommandSender> invokeCatching(
		context: BrigadierCommandContext<S>,
		executor: CommandExecutor<S>?
	) {
		if (executor == null) return

		scope.launch {
			try {
				executor(context, this)
			} catch (exception: CommandFailException) {
				val message = exception.failMessage

				if (message != null) {
					context.source.sendMessage(message)
				}
			}
		}
	}
}