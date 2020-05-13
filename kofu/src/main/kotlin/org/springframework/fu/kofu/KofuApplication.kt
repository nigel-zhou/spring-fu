package org.springframework.fu.kofu

import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext

/**
 * Kofu application that can be run parameterized with Spring profiles and/or command line arguments.
 *
 * @see application
 * @see webApplication
 * @see reactiveWebApplication
 * @author Sebastien Deleuze
 */
abstract class KofuApplication(private val initializer: AbstractDsl) {

	private var customizer: (ApplicationDsl.() -> Unit)? = null

	/**
	 * Run the current application
	 * @param profiles [ApplicationContext] profiles separated by commas.
	 * @param args the application arguments (usually passed from a Java main method)
	 * @param lazy Configure if beans are created only when needed (`true` by default)
	 * @return The application context of the application
	 */
	fun run(args: Array<String> = emptyArray(), profiles: String = "", lazy: Boolean = true): ConfigurableApplicationContext {
		val app = object: SpringApplication(KofuApplication::class.java) {
			override fun load(context: ApplicationContext?, sources: Array<out Any>?) {
				// We don't want the annotation bean definition reader
			}

			override fun createApplicationContext(): ConfigurableApplicationContext {
				return createContext()
			}
		}
		if (profiles.isNotEmpty()) {
			app.setAdditionalProfiles(*profiles.split(",").map { it.trim() }.toTypedArray())
		}
		app.addInitializers(initializer.toInitializer())
		if (customizer != null) app.addInitializers(ApplicationDsl(customizer!!).toInitializer())
		System.setProperty("spring.backgroundpreinitializer.ignore", "true")
		System.setProperty("spring.main.lazy-initialization", "$lazy")
		return app.run(*args)
	}

	/**
	 * Customize an existing application for testing, mocking, etc. `bean(isPrimary = true) { ... }` can be used
	 * to override existing beans.
	 */
	fun customize(customizer: ApplicationDsl.() -> Unit) {
		this.customizer = customizer
	}

	protected abstract fun createContext(): ConfigurableApplicationContext

}
