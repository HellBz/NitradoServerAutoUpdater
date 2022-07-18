package net.nitrado.server.autoupdater.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import config
import net.nitrado.server.autoupdater.utils.*
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


//println( config.getProperty("host")
//([.0-9]*)-([.0-9]*)-([installer|universal]*).([jar|zip]*)

open class Base {

    open var doupdate = false

    open var name: String = "Base-Loader"

    open var cache: String?    = "base"

    open var baseurl: String?  = "https://mineversion.top"

    open var apiKey: String?   = "no_key"

    open var localVersion: String? = null

    open var localBuild: String? = null

    open var currentVersion: String? = null

    open var currentBuild: String? = null

    open var downloadFile: String? = null

    open var installFile: String? = null

    open var startFile: String? = null



    fun errorNoLoader() {
        // loader: String

        logInfo( mainDir )

        logWarn("No Valid Loader Found")
        logWarn("From which Launcher is the Pack")
        logWarn("curse|technic|ftblauncher|atlauncher")
        logWarn("Or for Server-Side Scripting")
        logWarn("vanilla|spigot|bikkit|paper|purpur|verlocity|mohist|spongevanilla")
        logWarn("Or for Server-Side Modding")
        logWarn("forge|magma|spongeforge")
        System.exit(1)

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


            val logger = Thread {
                val serverLog = Scanner(serverProcess!!.inputStream)
                logInfo("Server-Starter - LOG-Printer is started")
                logInfo("-----------------------------------------------")

                while (serverLog.hasNextLine()) {
                    val println = serverLog.nextLine()
                    println(println)
                }
            }

            val console = Thread {

                val stdin = serverProcess!!.outputStream // <- Eh?
                val writer = BufferedWriter(OutputStreamWriter(stdin))
                val scanner = Scanner(System.`in`)

                logInfo("Server-Starter - Console-Scanner is started")
                logInfo("-----------------------------------------------")

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

        val check_server = Thread {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(5)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
                if ( !(serverProcess?.isAlive)!! ) {
                    logger.stop()
                    console.stop()
                    Thread.currentThread().stop()
                }
            }
        }


        if( serverProcess != null  ) {
            logger.start()
            console.start()
            check_server.start()
        }

    }
    open fun jobServerStopped(){

        val exitCode: Int? = serverProcess?.waitFor()

        if ( exitCode == 0 ){
            logWarn("Server is successfully stopped.")
            exitProcess( 0 )
        }else{
            logError("Server is Crashed with Exit-Code: $exitCode ")
            exitProcess( 1 )
        }
    }

    open fun api(requestArray: Array<String>): LinkedHashMap<String, String> {

        var sendRequest = requestArray.joinToString("/")
        var cacheString = requestArray.joinToString("_")
        var cacheStringUrl = URLEncoder.encode(cacheString, StandardCharsets.UTF_8.toString())

        val api = LinkedHashMap<String, String>()

        var responseCode = ""
        var responseMessage = ""
        var responseData = ""

        val directory: File = File(cacheDir)
        if (!directory.exists()) directory.mkdirs()

        var curseCacheFile = "$cacheDir/" + this.cache + "_" + cacheStringUrl + ".json"

        if (cacheFile(curseCacheFile, 3600)) {

            try {
                val configFileContent = String(Files.readAllBytes(Paths.get(curseCacheFile)))

                val cacheObj = JsonParser().parse(configFileContent).asJsonObject
                if (cacheObj.isJsonObject) {

                    responseCode = "200"
                    responseMessage = "Fetch from Cache"
                    responseData = cacheObj.toString()
                }

            } catch (exception: IllegalStateException) {
                responseCode = "403"
                responseMessage = exception.printStackTrace().toString()
            } catch (exception: FileNotFoundException) {
                responseCode = "404"
                responseMessage = exception.printStackTrace().toString()
            }

        }

        if (responseCode.isEmpty()) {

            try {

                val url = URL(this.baseurl +"$sendRequest")
                val http = url.openConnection() as HttpURLConnection
                http.requestMethod = "GET"
                http.setRequestProperty("Accept", "application/json")
                http.setRequestProperty("x-api-key", this.apiKey )

                responseCode = http.responseCode.toString()
                responseMessage = http.responseMessage

                if (http.responseCode == 200) {

                    val reader: Reader = InputStreamReader(http.inputStream, StandardCharsets.UTF_8)

                    if (!reader.toString().trim().isEmpty()) {

                        val datas = JsonParser().parse(reader) as JsonObject
                        responseData = datas.toString().trim()

                        val writer = BufferedWriter(FileWriter("$cacheDir/" + this.cache + "_" + cacheStringUrl +".json"))
                        writer.write(responseData)

                        writer.close()
                    }
                }

            } catch (exception: FileNotFoundException) {
                responseCode = "404"
                responseMessage = exception.printStackTrace().toString()
            }
        }

        api["code"] = responseCode
        api["message"] = responseMessage
        api["data"] = responseData
        return api
    }

}