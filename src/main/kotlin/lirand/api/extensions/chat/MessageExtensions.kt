@file:Suppress("MemberVisibilityCanBePrivate")

package lirand.api.extensions.chat

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.awt.Color

/**
 * Creates a new [ChatColor] instance from the provided
 * hex code.
 * Format example: `"#4BD6CB"`
 */
fun chatColor(hex: String): ChatColor = ChatColor.of(hex)

/**
 * Creates a new [ChatColor] instance from the provided
 * hex code.
 * Format example: `0x4BD6CB`
 */
fun chatColor(rgb: Int): ChatColor = ChatColor.of(Color(rgb))



/**
 * Sends a message built by [builder].
 *
 * @param builder the builder of the message
 */
inline fun CommandSender.sendMessage(
	crossinline builder: ComponentBaseBuilder.() -> Unit
) = sendMessage(ComponentBaseBuilder().apply(builder).build())

/**
 * Sends the given [component].
 */
fun CommandSender.sendMessage(component: BaseComponent) =
	spigot().sendMessage(component)

/**
 * Sends the given [components] as an action bar message.
 */
fun CommandSender.sendMessage(vararg components: BaseComponent) {
	spigot().sendMessage(*components)
}

/**
 * @param mainText title text
 * @param subText subtitle text
 * @param fadeIn time in ticks for titles to fade in
 * @param stay time in ticks for titles to stay
 * @param fadeOut time in ticks for titles to fade out
 */
fun Player.title(
	mainText: String? = null,
	subText: String? = null,
	fadeIn: Int = 10,
	stay: Int = 70,
	fadeOut: Int = 20,
) = sendTitle(mainText, subText, fadeIn, stay, fadeOut)

/**
 * Sends the given [message] as an action bar message.
 */
fun Player.actionBar(message: String) = actionBar(message.toComponent())

/**
 * Sends the given [message] as an action bar message.
 */
fun Player.actionBar(message: BaseComponent) {
	spigot().sendMessage(ChatMessageType.ACTION_BAR, message)
}

/**
 * Sends the given [messages] as an action bar message.
 */
fun Player.actionBar(vararg messages: BaseComponent) {
	spigot().sendMessage(ChatMessageType.ACTION_BAR, *messages)
}

/**
 * Sends a message built by [builder] as an action bar message.
 */
inline fun Player.actionBar(
	crossinline builder: ComponentBaseBuilder.() -> Unit
) = actionBar(ComponentBaseBuilder().apply(builder).build())