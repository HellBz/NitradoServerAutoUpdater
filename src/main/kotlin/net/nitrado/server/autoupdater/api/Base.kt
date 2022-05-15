package net.nitrado.server.autoupdater.api

import net.nitrado.server.autoupdater.utils.*

open class Base {

    open var name: String = "Holmes, Sherlock"

    open var version: String? = null


    open fun jobGreeting() {

        logInfo("-----------------------------------------------");
        logInfo(TXT_YELLOW + "|\\| o _|_ __ _  _| _    __  _ _|_" + TXT_RESET);
        logInfo(TXT_YELLOW + "| | |  |_ | (_|(_|(_) o | |(/_ |_" + TXT_RESET);
        logInfo("-----------------------------------------------")

    }

    open fun API(){}

    open fun latestVersion(): Any = Unit

    open fun latestFile(): Any = Unit

    open fun latestGet(): Any = Unit

    open fun latestServer(): Any = Unit

    fun errorNoLoader(loader: String) {

        logError("Warning: $loader is not implemented yet")

        logWarn("No Valid Loader Found")
        logWarn("From which Launcher is the Pack")
        logWarn("curse|technic|ftblauncher|atlauncher")
        logWarn("Or for Server-Side Scripting")
        logWarn("vanilla|spigot|bikkit|paper|purpur|verlocity|mohist|spongevanilla")
        logWarn("Or for Server-Side Modding")
        logWarn("forge|magma|spongeforge")
        System.exit(0)

    }
}