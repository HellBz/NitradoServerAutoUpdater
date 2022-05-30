package net.nitrado.server.autoupdater.utils

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

val jarPath: String = FileUtils::class.java.protectionDomain.codeSource.location.toURI().path
val jarName: String = jarPath.substring(jarPath.lastIndexOf("/") + 1)

val userDir: String = System.getProperty("user.dir")
const val mainDir = "server-autoupdater"

const val backupDir = "$mainDir/backups"
const val cacheDir = "$mainDir/cache"

const val modDownloadFile = "modpack-download.zip"
const val configFileName = "server-autoupdater.yaml"

var serverProcess: Process? = null

fun loadConfig(): Map<String, Any>? {

    val directory = File( mainDir )
    if (!directory.exists()) directory.mkdirs()

    val configFile = File("$mainDir/$configFileName")

    if (!configFile.exists()) {

        try {
            FileUtils::class.java.classLoader.getResourceAsStream(configFileName).use { `is` ->
                if (`is` != null) {
                    Files.copy(`is`, Paths.get("$mainDir/$configFileName"))
                }
            }
        } catch (e: IOException) {
            // An error occurred copying the resource
        }
        println("You Standard-Config has generated now. Exit.")
        exitProcess(0)

/*
        val yamlConfigData: MutableMap<String, Any> = HashMap()
        yamlConfigData["launcher"] = "curse"
        yamlConfigData["modpack-id"] = 507137
        yamlConfigData["latest-version"] = ""
        yamlConfigData["autoupdate"] = false

        val options = DumperOptions()
        options.indent = 2
        options.isPrettyFlow = true
        // Fix below - additional configuration
        // Fix below - additional configuration
        options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK

        val writer = PrintWriter(configFile)
        val yaml = Yaml(options)
        yaml.dump(yamlConfigData, writer)
*/
        //val reader = FileReader(configFile)
        //props.load(reader);

    }

    val configStream: InputStream = FileInputStream("$mainDir/$configFileName")
    val yaml = Yaml()
    return yaml.load<Map<String, Any>?>(configStream)
}
