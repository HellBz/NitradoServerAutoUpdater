
import net.nitrado.server.autoupdater.utils.*
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.lang.System.getProperty
import java.util.*
import java.util.regex.Pattern

val jarPath: String = FileUntils::class.java.protectionDomain.codeSource.location.toURI().path
val jarName: String = jarPath.substring(jarPath.lastIndexOf("/") + 1)

val userDir: String = getProperty("user.dir")
const val mainDir = "server-autoupdater"

const val backupDir = "$mainDir/backups"
const val cacheDir = "$mainDir/cache"

const val modDownloadFile = "modpack-download.zip"
const val configFileName = "server-autoupdater.yaml"

val config = loadConfig()

var serverProcess: Process? = null

fun main() {

    var currentLoader = net.nitrado.server.autoupdater.api.Base()
    currentLoader.jobGreeting()

    val loader = config?.get("loader").toString()
    if ( doesClassExist("net.nitrado.server.autoupdater.api." + capitalize( loader ) ) ){

        when ( loader ) {
            "curse" -> currentLoader = net.nitrado.server.autoupdater.api.Curse()
            "bukkit" -> currentLoader = net.nitrado.server.autoupdater.api.Base()
            else -> {
                logError("Warning: $loader is not implemented yet")
                currentLoader.errorNoLoader()
            }
        }
    }else{
        currentLoader.errorNoLoader()
    }

    currentLoader.jobCreateMainConfig()
    currentLoader.jobGetLocalVersion()

    if (currentLoader.doupdate || config?.get("autoupdate") == true) {

        currentLoader.doupdate = false
        logInfo("Checking for latest Version of Modpack")

        currentLoader.jobGetCurrentVersion()

        logInfo("Last Build is " + currentLoader.localVersion)
        logInfo("Last Server Build is " + currentLoader.currentVersion)

        if (currentLoader.currentVersion != currentLoader.localVersion) {
            currentLoader.jobWriteLatestVersionToFile()
            currentLoader.doupdate = true
        } else {
            logInfo("Target-Build is: " + currentLoader.localVersion + " and Source-Build is: " + currentLoader.currentVersion)
            logInfo("Exit Auto-Updater, because Versions are the same.")
        }
    }

    if (currentLoader.doupdate) {

        currentLoader.jobBackUpFiles()

        currentLoader.jobGetDownloadFile()

        currentLoader.jobDownloadFiles()

        currentLoader.jobUnPackFiles()

        currentLoader.jobFindInstaller()

        //TODO overwrite new Files

        //TODO Copy Temp-Files to Main-Folder

        currentLoader.jobCopyTempToServer()

    }

    //TODO currentLoader.jobFindStartFile()
    currentLoader.jobFindStartFile()

    //println("START Server")

    val startFiles = listFilesinDirectory("$userDir/")

    startFiles["files"]?.forEach { file ->

        val matchForge = Pattern.compile("forge-([.0-9]+)-([.0-9]+).([jar|zip]+)").matcher(file)

        if (matchForge.find()) {

            logInfo("Find StartFile: $file")
            val curseVersionMinecraft = matchForge.group(1)
            val curseVersionForge = matchForge.group(2)
            logInfo("Minecraft-Version: $curseVersionMinecraft")
            logInfo("Forge-Version: $curseVersionForge")

            startServer("$userDir/$file")
        }

    }

    //TODO currentLoader.jobStartServer()
    currentLoader.jobStartServer()

    //TODO Start Server

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




/*
    println("What is your Name?")
    val name = readLine()
    println("Hello, $name")

    var projekt_id = 381671 // ATM-6
    projekt_id = config?.get("modpack-id") as Int
    const val curseArray = arrayOf("mods", projekt_id.toString() , "files", "?pageSize=50" )
    val curse = curseAPI( curseArray )
    //val curse = net.nitrado.server.autoupdater.api.curseAPI("mods/$projekt_id/files/?pageSize=50")

    println( curse["message"].toString() )

    if ( !curse["code"].equals("200") ) System.exit(0)

    val curseObj = JsonParser().parse(curse["data"].toString()).asJsonObject

    println(curseObj.isJsonObject)

    var entry = JsonObject()

    val entrys = curseObj["data"] as JsonArray

    for (j in 0 until entrys.size()) {
        val entry_obj = entrys[j] as JsonObject
        //System.out.println(  entry_obj.get("serverPackFileId").toString() );
        if (j == 0) {
            entry = entry_obj
        }
        if (entry_obj["serverPackFileId"].toString().isNotEmpty()) {
            entry = entry_obj

            break
        }
    }

    // Get Entry-Datas
    // println(entry)

    println("Last Build is " + entry.get("id"))
    println("Last Server Build is " + entry.get("serverPackFileId"))

    //config.setProperty("latest-build", entry.get("serverPackFileId").toString() )
    //saveConfig(config)
*/


    //downloadFile( "$curseServerFile", "$mainDir/$modDownloadFile" )

    //val zipContnet = listZipContents("$mainDir/$modDownloadFile")
    //println( zipContnet.toString() )

/*
    val pattern: Pattern = Pattern.compile("<title>(.*)</title>")
    val matcher: Matcher = pattern.matcher( zipContnet.toString() )
    println( matcher.matches().toString() )
*/



/*

/*

fun main(args: Array<String>) {
    println("Hello World!")

    // Try adding program arguments at Run/Debug configuration
    println("Program arguments: ${args.joinToString()}")
}

*/