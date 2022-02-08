package lirand.api.extensions.chat

import lirand.api.extensions.other.toId
import lirand.api.nbt.nbtData
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.ItemTag
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Item
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import net.md_5.bungee.api.chat.hover.content.Entity as HoverEntity

fun String.toComponent() =
	TextComponent(*TextComponent.fromLegacyText(this))

fun Array<String>.toComponent() =
	TextComponent(*map { it.toComponent() }.toTypedArray())

fun Collection<String>.toComponent() =
	TextComponent(*map { it.toComponent() }.toTypedArray())



fun HoverTextEvent(vararg components: BaseComponent): HoverEvent {
	return HoverEvent(
		HoverEvent.Action.SHOW_TEXT,
		Text(components)
	)
}

inline fun HoverTextEvent(builder: ComponentBaseBuilder.() -> Unit): HoverEvent {
	return HoverEvent(
		HoverEvent.Action.SHOW_TEXT,
		Text(arrayOf(ComponentBaseBuilder().apply(builder).build()))
	)
}

fun HoverItemEvent(itemStack: ItemStack): HoverEvent {
	return HoverEvent(
		HoverEvent.Action.SHOW_ITEM,
		Item(
			itemStack.type.toId(),
			itemStack.amount,
			ItemTag.ofNbt(itemStack.nbtData.toString())
		)
	)
}

fun HoverEntityEvent(entity: Entity): HoverEvent {
	return HoverEvent(
		HoverEvent.Action.SHOW_ENTITY,
		HoverEntity(
			entity.type.toId(),
			entity.uniqueId.toString(),
			entity.customName?.toComponent() ?: entity.name.toComponent()
		)
	)
}



fun RunCommandClickEvent(command: String): ClickEvent {
	return ClickEvent(
		ClickEvent.Action.RUN_COMMAND,
		command
	)
}

fun SuggestCommandClickEvent(command: String): ClickEvent {
	return ClickEvent(
		ClickEvent.Action.SUGGEST_COMMAND,
		command
	)
}

fun CopyTextClickEvent(text: String): ClickEvent {
	return ClickEvent(
		ClickEvent.Action.COPY_TO_CLIPBOARD,
		text
	)
}

fun OpenUrlClickEvent(url: String): ClickEvent {
	return ClickEvent(
		ClickEvent.Action.OPEN_URL,
		url
	)
}

fun ChangePageClickEvent(page: Int): ClickEvent {
	return ClickEvent(
		ClickEvent.Action.CHANGE_PAGE,
		page.toString()
	)
}