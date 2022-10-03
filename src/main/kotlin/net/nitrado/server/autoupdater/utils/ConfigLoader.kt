package net.nitrado.server.autoupdater.utils

import org.yaml.snakeyaml.Yaml
import java.awt.GraphicsEnvironment
import java.awt.HeadlessException
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.system.exitProcess

val jarPath: String = FileUtils::class.java.protectionDomain.codeSource.location.toURI().path
val jarName: String = jarPath.substring(jarPath.lastIndexOf("/") + 1)
val OS = System.getProperty("os.name").lowercase(Locale.getDefault())

val userDir: String = System.getProperty("user.dir")
const val mainDir = "server-autoupdater"

const val backupDir = "$mainDir/backups"
const val cacheDir = "$mainDir/cache"

const val modDownloadFile = "modpack-download.zip"
const val configFileName = "configuration.yaml"

var serverProcess: Process? = null

fun loadConfig(): Map<String, Any>? {

    val directory = File( mainDir )
    if (!directory.exists()) directory.mkdirs()

    val configFile = File("$mainDir/$configFileName")

    if (!configFile.exists()) {

        var currentLoader = net.nitrado.server.autoupdater.api.Base()
        currentLoader.jobGreeting()

        val timezone = getTimeZone()
        //Write Config
        try {
            val writer = BufferedWriter(FileWriter("$mainDir/$configFileName" ))
            FileUtils::class.java.classLoader.getResourceAsStream("server-autoupdater.yaml").use { inputStream ->
                InputStreamReader(inputStream, StandardCharsets.UTF_8).use { streamReader ->
                    BufferedReader(streamReader).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val currentLine = line.toString().replace("timezone: UTC", "timezone: $timezone" )
                            //logInfo( currentLine )
                            writer.write(currentLine + System.lineSeparator() )
                        }
                        writer.close()
                    }
                }
            }
        } catch (e: IOException) {
            //e.printStackTrace()
        }

            logWarn("Your Standard-Config has generated now.")
            logWarn("Please go to /$mainDir/$configFileName and set it up.")
            logWarn("Exit process now. ;)")
            exitProcess(0)

    }

    val configStream: InputStream = FileInputStream("$mainDir/$configFileName")
    val yaml = Yaml()
    return yaml.load<Map<String, Any>?>(configStream)
}

val isReallyHeadless: Boolean
    get() = if (GraphicsEnvironment.isHeadless()) {
        true
    } else try {
        val screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices
        screenDevices == null || screenDevices.size == 0
    } catch (e: HeadlessException) {
        e.printStackTrace()
        true
    }

fun getTimeZone(): String {
    var IP = "127.0.0.1"
    var timezone = "UTC"

    //Get IP From Internet
    val ipv4Services = arrayOf(
        "http://ip-api.com/csv/?fields=query",
        "http://checkip.amazonaws.com/",
        "https://ipv4.icanhazip.com/",
        "http://myexternalip.com/raw",
        "http://ipecho.net/plain",
        "https://myexternalip.com/raw",
    )

    //Get IP From Config
    try {
        FileInputStream("server.properties").use { input ->
            val prop = Properties()
            prop.load(input)
            if ( prop.getProperty("server-ip").isNotEmpty() ){
                IP = prop.getProperty("server-ip")
                logInfo( "Config-IP is: $IP" )
            }
        }
    } catch (ex: IOException) {} catch (ex: FileNotFoundException) {}

    //GET IP / WEBSITE TIMEZONE
    if( IP == "127.0.0.1" ) {

        val ipv4Service = ipv4Services.get(((Math.random() * ipv4Services.size).toInt()))
        logInfo("Use following IP-Service: $ipv4Service")
        try {
            IP = BufferedReader(InputStreamReader(URL(ipv4Service).openStream())).readLine()
        } catch (ex: IOException) {} catch (ex: FileNotFoundException) {}

        logInfo("Public-IP is: $IP")
    }

    if( IP != "127.0.0.1" ){

        try {
            val URL = URL("http://ip-api.com/csv/$IP?fields=status,timezone&lang=en")
            val bufferedURL = BufferedReader(InputStreamReader(URL.openStream()))
            val line = bufferedURL.readLine()
            if ( line.contains("success,") ) {
                timezone = line.replace("success,","" )
            }
            bufferedURL.close()
        } catch (me: MalformedURLException) {} catch (ioe: IOException) {}

    }

    logInfo( "Set Timezone to: $timezone" )

    return timezone

}
