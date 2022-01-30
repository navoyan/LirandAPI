package lirand.api.utilities

interface Initializable {
	fun initialize() {}
}

fun initialize(vararg initializables: Initializable) {
	initializables.forEach {
		it.initialize()
	}
}