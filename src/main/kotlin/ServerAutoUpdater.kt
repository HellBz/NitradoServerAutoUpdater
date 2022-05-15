import net.nitrado.server.autoupdater.utils.*
import net.nitrado.server.autoupdater.utils.ZipFileExample.zip
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.regex.Pattern


const val mainDir = "server-autoupdater"
const val modDownloadFile = "modpack-download.zip"
const val configFileName = "server-autoupdater.yaml"

val jarPath: String = FileUntils::class.java.protectionDomain.codeSource.location.toURI().path
val jarName: String = jarPath.substring(jarPath.lastIndexOf("/") + 1)

val userDir: String = System.getProperty("user.dir")
val backupDir = "$mainDir/backups"
val cacheDir = "$mainDir/cache"

val config = loadConfig()
val latest = loadYAMLConfig("$mainDir/latest.yaml")

var serverProcess: Process? = null


fun main() {

    var currentLoader = net.nitrado.server.autoupdater.api.Base()
    currentLoader.jobGreeting()

    val loader = config?.get("loader") as String

    //currentLoader = Class.forName("net.nitrado.server.autoupdater.api.Curse").newInstance() as Base

    when (loader) {
        "curse" -> currentLoader = net.nitrado.server.autoupdater.api.Curse()
        "bukkit" -> currentLoader = net.nitrado.server.autoupdater.api.Base()
        else -> {
            currentLoader.errorNoLoader(loader)
        }
    }
    logInfo( currentLoader.name )
    currentLoader.name = "Test"
    logInfo( currentLoader.name )

    //println( config.getProperty("host")
    //([.0-9]*)-([.0-9]*)-([installer|universal]*).([jar|zip]*)

    if ( !copyConfigFromResource("server-autoupdater.yaml","$mainDir/server-autoupdater.yaml") ) {
        logWarn("Created Main-Config: $mainDir/sample_test.json")
        logWarn("Please Setup your Configuration-File, Exit Program.")
        System.exit(0)
    }

    var doupdate = false

    if ( latest == null ) {
        //TODO Update
        logInfo("Latest Version cannot be found, checking for Updates.")
        doupdate = true
    }

    if( latest != null ){

        latest.get("latest")
        logInfo("Found latest Version of Modpack: " + latest.get("latest").toString() )
        doupdate = false
    }

    if( doupdate || config.get("autoupdate") == true ){

        doupdate = false
        logInfo("Checking for latest Version of Modpack")

        val latest_version = currentLoader.latestVersion()



        logInfo("Last Build is $latest_version" )
        logInfo("Last Server Build is " + currentLoader.latestServer() )


        val yamlConfigData: MutableMap<String, Int> = HashMap()
        yamlConfigData["latest"] = Integer.parseInt(latest_version.toString())
        val options = DumperOptions()
        options.indent = 2
        options.isPrettyFlow = true
        options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        val configFile = File("$mainDir/latest.yaml")
        val writer = PrintWriter(configFile)
        val yaml = Yaml(options)
        yaml.dump(yamlConfigData, writer)

        if ( latest_version.toString() != latest?.get("latest").toString() )
            doupdate = true
        else
            logInfo("Target-Build is: " + latest?.get("latest").toString() + " and Source-Build is: $latest_version" )
        logInfo("Exit Auto-Updater, because Versions are the same.")

    }

    if ( doupdate ) {

        //TODO Backup current Files
        logInfo("Start creating Backup of existing Files to:")
        var backup_zip = "$backupDir/curse_" + latest?.get("latest").toString() + ".zip"
        logInfo("$backup_zip")
        try {
            val directory: File = File( "$userDir/$backupDir/" )
            if (!directory.exists()) directory.mkdirs()
            zip(  "$userDir/", "$backup_zip" )
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var curseServerFile = currentLoader.latestFile()
        logInfo("Download Server-Files from:")
        logInfo("$curseServerFile")

        //TODO Download newest Version
        downloadFile( "$curseServerFile", "$mainDir/$modDownloadFile" )

        //TODO unpack newest Version
        logInfo("Unpack new Pack to Temp-Folder:")
        logInfo("$mainDir/$modDownloadFile")
        try {

            val zipFile: String = "$mainDir/$modDownloadFile" //your zip file location

            val unzipLocation: String = "$mainDir/temp/" // unzip location

            val df = DecompressFast(zipFile, unzipLocation)
            df.unzip()

        } catch (ex: Exception) {
            // some errors occurred
            ex.printStackTrace()
        }

        val tempDataInstaller = listFilesinDirectory("$mainDir/temp/")
        tempDataInstaller["files"]?.forEach {
                file -> file

            val matchForge = Pattern.compile("forge-([.0-9]+)-([.0-9]+)-([universal|installer]+).([jar|zip]+)").matcher(file)

            if (matchForge.find()) {

                logInfo( "Find Installer: $file")
                val curseVersionMinecraft = matchForge.group(1)
                val curseVersionForge = matchForge.group(2)
                logInfo( "Minecraft-Version: $curseVersionMinecraft")
                logInfo( "Forge-Version: $curseVersionForge")

                val installer = installCurseLoader("$userDir/$mainDir/temp/$file")
            }

        }

        //TODO overwrite new Files



        //TODO Copy Temp-Files to Main-Folder

        val serverFiles = listFilesinDirectory("$userDir/")
        val tempDatas = listFilesinDirectory("$mainDir/temp/")

        tempDatas["files"]?.forEach {
                file -> file
            if  (
                getExt(file).toString() != "bat" &&
                getExt(file).toString() != "sh" &&
                /* serverFiles.get("files")?.contains( file ) == true &&  */
                jarName != file
            ) {

                logInfo( "Cleanup & Copy File: $file" )

                val toFile: File = File("$userDir/$file" )
                val fromFile: File = File("$mainDir/temp/$file" )
                if( toFile.exists() ) Files.delete( toFile.toPath() )
                if ( file.equals("server-icon.png") )
                    copyConfigFromResource( "server-icon.png" , "$userDir/server-icon.png" )
                else
                    if( !toFile.exists() ) Files.copy( fromFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);


            }

        }
        tempDatas["dirs"]?.forEach {
                dir -> dir
            if  (
            /* serverFiles.get("dirs")?.contains( dir )!! && */
                ( serverFiles.get("dirs")?.contains( "world" )!! && dir.equals("world") )
            ) {
                println( "Found World: $dir, SKIP" )
            }else{

                logInfo( "Cleanup & Copy Directory: $dir" )

                val toDir: File = File("$userDir/$dir" )
                if( toDir.exists() )  deleteDirectory( toDir )
                if( !toDir.exists() )  copyDirectory( "$mainDir/temp/$dir" ,"$userDir/$dir" )
            }
        }

    }

    //TODO Start Server

    println("START Server")

    val startFiles = listFilesinDirectory("$userDir/")

    startFiles["files"]?.forEach {
            file -> file

        println( file )

        val matchForge = Pattern.compile("forge-([.0-9]+)-([.0-9]+).([jar|zip]+)").matcher(file)

        if (matchForge.find()) {

            logInfo( "Find StartFile: $file")
            val curseVersionMinecraft = matchForge.group(1)
            val curseVersionForge = matchForge.group(2)
            logInfo( "Minecraft-Version: $curseVersionMinecraft")
            logInfo( "Forge-Version: $curseVersionForge")

            startServer("$userDir/$file")
        }

    }

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
            } catch  (e: IOException) {
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






/*
    println("What is your Name?")
    val name = readLine()
    println("Hello, $name")

    var projekt_id = 381671 // ATM-6
    projekt_id = config?.get("modpack-id") as Int
    val curseArray = arrayOf("mods", projekt_id.toString() , "files", "?pageSize=50" )
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

}

/*

/*

fun main(args: Array<String>) {
    println("Hello World!")

    // Try adding program arguments at Run/Debug configuration
    println("Program arguments: ${args.joinToString()}")
}

*/