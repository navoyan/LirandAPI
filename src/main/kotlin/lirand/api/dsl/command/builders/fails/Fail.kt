package lirand.api.dsl.command.builders.fails

import lirand.api.dsl.command.builders.BrigadierCommandContext
import lirand.api.extensions.chat.ComponentBaseBuilder
import lirand.api.extensions.chat.toComponent
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent


fun BrigadierCommandContext<*>.fail(
	message: BaseComponent? = null
): Nothing = throw CommandFailException(message, source)

fun BrigadierCommandContext<*>.fail(
	message: BaseComponent,
	vararg messages: BaseComponent
): Nothing = throw CommandFailException(TextComponent(message, *messages), source)

inline fun BrigadierCommandContext<*>.fail(
	crossinline builder: ComponentBaseBuilder.() -> Unit
): Nothing = throw CommandFailException(ComponentBaseBuilder().apply(builder).build(), source)

fun BrigadierCommandContext<*>.fail(
	message: String
): Nothing = fail(message.toComponent())