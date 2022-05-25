package net.nitrado.server.autoupdater.api

import mainDir
import net.nitrado.server.autoupdater.utils.*
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.PrintWriter

//println( config.getProperty("host")
//([.0-9]*)-([.0-9]*)-([installer|universal]*).([jar|zip]*)

open class Base {

    open var doupdate = false

    open var name: String = "Curse-Loader"

    open var localVersion: String? = null

    open var currentVersion: String? = null

    open var downloadFile: String? = null

    open var installFile: String? = null

    open var startFile: String? = null

    fun errorNoLoader() {
        // loader: String

        logWarn("No Valid Loader Found")
        logWarn("From which Launcher is the Pack")
        logWarn("curse|technic|ftblauncher|atlauncher")
        logWarn("Or for Server-Side Scripting")
        logWarn("vanilla|spigot|bikkit|paper|purpur|verlocity|mohist|spongevanilla")
        logWarn("Or for Server-Side Modding")
        logWarn("forge|magma|spongeforge")
        System.exit(0)

    }

    open fun jobGreeting() {

        logInfo("-----------------------------------------------")
        logInfo("$TXT_YELLOW|\\| o _|_ __ _  _| _    __  _ _|_$TXT_RESET")
        logInfo("$TXT_YELLOW| | |  |_ | (_|(_|(_) o | |(/_ |_$TXT_RESET")
        logInfo("-----------------------------------------------")

    }

    open fun jobCreateMainConfig() {
        if (!copyConfigFromResource("server-autoupdater.yaml", "$mainDir/server-autoupdater.yaml")) {
            logWarn("Created Main-Config: $mainDir/sample_test.json")
            logWarn("Please Setup your Configuration-File, Exit Program.")
            System.exit(0)
        }
    }

    open fun jobGetLocalVersion() {
        val latestYaml = loadYAMLConfig("$mainDir/latest.yaml")
        if( latestYaml != null ) this.localVersion = latestYaml.get("latest").toString()

        if ( this.localVersion == null ) {
            logInfo("Latest Version cannot be found, checking for Updates.")
            this.doupdate = true
        }else{
            logInfo("Found latest Version of Modpack: " + this.localVersion )
        }
    }

    open fun jobGetCurrentVersion(): Any = Unit

    fun jobWriteLatestVersionToFile() {
        val yamlConfigData: MutableMap<String, Int> = HashMap()
        yamlConfigData["latest"] = Integer.parseInt( this.currentVersion )
        val options = DumperOptions()
        options.indent = 2
        options.isPrettyFlow = true
        options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        val configFile = File("$mainDir/latest.yaml")
        val writer = PrintWriter(configFile)
        val yaml = Yaml(options)
        yaml.dump(yamlConfigData, writer)
    }

    open fun jobBackUpFiles(): Any = Unit

    open fun jobGetDownloadFile(): Any = Unit

    open fun jobDownloadFiles(): Any = Unit

    open fun jobUnPackFiles(): Any = Unit

    open fun jobFindInstaller(): Any = Unit

    open fun jobCleanUpTemp(): Any = Unit

    open fun jobCleanUpServer(): Any = Unit

    open fun jobCopyTempToServer(): Any = Unit

    open fun jobFindStartFile(): Any = Unit

    open fun jobStartServer(): Any = Unit

    open fun api(): Any = Unit

}