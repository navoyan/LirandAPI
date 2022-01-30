package lirand.api.extensions.chat

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun String.translateColorCode(code: Char = '&'): String =
	ChatColor.translateAlternateColorCodes(code, this)


fun String.reverseColorCode(code: Char = '&') =
	replace('§', code)


operator fun ChatColor.plus(text: String) = toString() + text
operator fun ChatColor.plus(other: ChatColor) = toString() + other


fun BaseComponent?.isNullOrEmpty() = this?.toPlainText().isNullOrEmpty()

fun translatedColor(default: String, colorChar: Char = '&') =
	TranslatedChatColorDelegate(default, colorChar)

class TranslatedChatColorDelegate(realValue: String, val colorChar: Char = '&') : ReadWriteProperty<Any?, String> {
	private var value: String = realValue.translateColorCode(colorChar)

	override operator fun getValue(thisRef: Any?, property: KProperty<*>) = value

	override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
		this.value = value.translateColorCode(colorChar)
	}
}


fun BaseComponent.toJson(): String =
	ComponentSerializer.toString(this)

fun Array<BaseComponent>.toJson(): String =
	ComponentSerializer.toString(*this)