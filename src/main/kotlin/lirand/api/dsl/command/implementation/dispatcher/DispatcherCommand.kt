package lirand.api.dsl.command.implementation.dispatcher

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException
import lirand.api.dsl.command.types.exceptions.ChatCommandSyntaxException
import lirand.api.extensions.chat.SuggestCommandClickEvent
import lirand.api.extensions.chat.sendMessage
import lirand.api.extensions.chat.toComponent
import lirand.api.utilities.isOverridden
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.TranslatableComponent
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginIdentifiableCommand
import org.bukkit.plugin.Plugin
import java.lang.reflect.Method
import kotlin.math.max
import kotlin.math.min

/**
 * A [Command] subclass that forwards execution to a underlying `CommandDispatcher`.
 *
 * **Note:**
 * This class was adapted from Spigot's `VanillaCommand`.
 */
class DispatcherCommand(
	name: String,
	private val plugin: Plugin,
	val dispatcher: CommandDispatcher<CommandSender>,
	usage: String,
	aliases: List<String>
) : Command(name, "", usage, aliases), PluginIdentifiableCommand {

	/**
	 * Forwards execution with the rejoined label and arguments to the underlying
	 * [CommandDispatcher] if the [sender] has sufficient permission.
	 *
	 * @param sender the sender
	 * @param label the label
	 * @param arguments the arguments
	 * @return true
	 */
	override fun execute(sender: CommandSender, label: String, vararg arguments: String): Boolean {
		if (!testPermission(sender)) {
			return true
		}

		val reader = StringReader(join(label, arguments))

		if (reader.canRead() && reader.peek() == '/') {
			reader.skip()
		}

		try {
			dispatcher.execute(reader, sender)
		} catch (exception: CommandSyntaxException) {
			if (exception is ChatCommandSyntaxException) {
				sender.sendMessage(exception.chatMessage.apply {
					color = ChatColor.RED
				})
			}
			else {
				sender.sendMessage(exception.rawMessage.toComponent().apply {
					color = ChatColor.RED
				})
			}

			report(sender, exception.input, exception.cursor)
		}

		return true
	}

	override fun getPlugin(): Plugin {
		return plugin
	}



	private fun report(sender: CommandSender, input: String?, cursor: Int) {
		if (input == null || cursor < 0) return

		val index = min(input.length, cursor)
		val errorStart = input.lastIndexOf(' ', index - 1) + 1

		val failedCommandMessage = TextComponent().apply {
			color = ChatColor.GRAY
			clickEvent = SuggestCommandClickEvent(input)
		}

		if (errorStart > 10) {
			failedCommandMessage.addExtra("...")
		}
		failedCommandMessage.addExtra(input.substring(max(0, errorStart - 10), errorStart))

		if (errorStart < input.length) {
			val error = TextComponent(input.substring(errorStart, cursor)).apply {
				color = ChatColor.RED
				isUnderlined = true
			}
			failedCommandMessage.addExtra(error)
		}

		val context = TranslatableComponent("command.context.here").apply {
			color = ChatColor.RED
			isItalic = true
		}
		failedCommandMessage.addExtra(context)


		sender.sendMessage(failedCommandMessage)
	}

	private fun Message.toComponent(): BaseComponent {
		val messageClass = this::class.java

		return if (messageClass.simpleName == "ChatMessage") {
			verifyChatMessageClass(messageClass)
			val key = chatMessageGetKeyMethod.invoke(this) as String
			val args = chatMessageGetArgsMethod.invoke(this) as Array<*>
			TranslatableComponent(key, *args)
		}
		else {
			string.toComponent()
		}
	}

	private fun join(name: String, arguments: Array<out String>): String {
		var command = "/$name"
		if (arguments.isNotEmpty()) {
			command += " ${arguments.joinToString(" ")}"
		}
		return command
	}


	private companion object {
		lateinit var chatMessageGetKeyMethod: Method
			private set

		lateinit var chatMessageGetArgsMethod: Method
			private set

		fun verifyChatMessageClass(chatMessageClass: Class<*>) {
			if (::chatMessageGetKeyMethod.isInitialized) return

			chatMessageGetKeyMethod = chatMessageClass.methods
				.find { it.returnType == String::class.java && !it.isOverridden }!!
			chatMessageGetArgsMethod = chatMessageClass.methods
				.find { it.returnType == Array<Any>::class.java && !it.isOverridden }!!
		}
	}
}