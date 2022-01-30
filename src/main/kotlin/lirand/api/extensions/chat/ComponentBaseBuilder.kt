package lirand.api.extensions.chat

import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent

inline fun chatComponent(builder: ComponentBaseBuilder.() -> Unit): BaseComponent {
	return ComponentBaseBuilder().apply(builder).build()
}

class ComponentBaseBuilder {

	@PublishedApi
	internal val siblingText = TextComponent("")

	/**
	 * Append text to the parent.
	 *
	 * @param text the raw text (without formatting)
	 * @param builder the builder which can be used to set the style and add child text components
	 */
	inline fun add(
		text: String = "",
		builder: TextComponent.() -> Unit = {}
	) {
		siblingText.addExtra(TextComponent(text).apply(builder))
	}

	/**
	 * Append text to the parent.
	 *
	 * @param component the component instance
	 * @param builder the builder which can be used to set the style and add child text components
	 */
	inline fun <C : BaseComponent> add(
		component: C,
		builder: C.() -> Unit = {}
	) {
		siblingText.addExtra(component.apply(builder))
	}

	/**
	 * Append the given legacy text to the parent. This
	 * allows you to use legacy color codes (e.g. `§c` for red).
	 * It is **not** recommended using this.
	 *
	 * @param text the text instance
	 * @param builder the builder which can be used to set the style and add child text components
	 */
	inline fun addLegacy(
		text: String,
		builder: TextComponent.() -> Unit = {}
	) {
		siblingText.addExtra(text.toComponent().apply(builder))
	}

	/**
	 * Adds a line break.
	 */
	fun addNewLine() {
		siblingText.addExtra(TextComponent("\n"))
	}

	/**
	 * Adds an empty line.
	 */
	fun addEmptyLine() {
		addNewLine()
		addNewLine()
	}


	fun build() = siblingText.duplicate()
}