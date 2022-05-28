package net.nitrado.server.autoupdater.api

import com.google.gson.annotations.Until
import config
import mainDir
import net.nitrado.server.autoupdater.utils.*
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import serverProcess
import userDir
import java.io.*
import java.util.*


//println( config.getProperty("host")
//([.0-9]*)-([.0-9]*)-([installer|universal]*).([jar|zip]*)

open class Base {

    open var doupdate = false

    open var name: String = "Base-Loader"

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

    open fun jobDoUpdateYesOrNo() {
        if (this.doupdate || config?.get("autoupdate") == true) {

            this.doupdate = false
            logInfo("Checking for latest Version of " + this.name )

            this.jobGetCurrentVersion()

            logInfo("Last Build is " + this.localVersion)
            logInfo("Last Server Build is " + this.currentVersion)

            if (this.currentVersion != this.localVersion) {
                this.jobWriteLatestVersionToFile()
                this.doupdate = true
            } else {
                logInfo("Target-Build is: " + this.localVersion + " and Source-Build is: " + this.currentVersion)
                logInfo("Exit Auto-Updater, because Versions are the same.")
            }
        }
    }

    open fun jobWriteLatestVersionToFile() {
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

    open fun jobSetEulaTrue(){

        val eulaFile = File("eula.txt")

        var eulaFileWrite = false

        if (eulaFile.exists()) {
            val eulaProps = Properties()
            val eulaReader = FileReader(eulaFile)

            // load the properties file and close
            eulaProps.load(eulaReader)
            eulaReader.close()
            if (eulaProps.getProperty("eula") == "false") {
                eulaFileWrite = true
                logInfo("Set Eula-File to true")
            } else {
                logInfo("Eula-File is already accepted.")
            }
        } else {
            if (eulaFile.createNewFile()) {
                eulaFileWrite = true
                logInfo("Creating a new Eula-File")
            }
        }

        if (eulaFileWrite) {
            val eulaWriter = FileWriter(eulaFile)
            val eulaPropsNew = Properties()
            eulaPropsNew.setProperty("eula", "true")
            eulaPropsNew.store(
                eulaWriter,
                "By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula)."
            )
            eulaWriter.close()
        }

    }

    open fun jobStartServer() {

        startServer("$userDir/" + this.startFile )

        //TODO REMOVE THIS

        logInfo("Server-Starter - LOG-Printer is started")
        logInfo("-----------------------------------------------")
        val serverLog = Scanner(serverProcess!!.inputStream)
        val logger = Thread {
            while (serverLog.hasNextLine()) {
                val println = serverLog.nextLine()
                println(println)
            }
        }

        logger.start()
        logInfo("Server-Starter - Console-Scanner is started")
        logInfo("-----------------------------------------------")
        val stdin = serverProcess!!.outputStream // <- Eh?
        val writer = BufferedWriter(OutputStreamWriter(stdin))
        val scanner = Scanner(System.`in`)
        val console = Thread {
            while (true) {
                val input = scanner.nextLine()
                if (input == "") break
                //System.out.println( input );
                try {
                    writer.write(input + System.lineSeparator())
                    writer.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            try {
                writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        console.start()

    }
    open fun jobServerStopped(){
        logWarn("Oh Snap, Server is stopped.")
    }

    open fun api(): Any = Unit

}