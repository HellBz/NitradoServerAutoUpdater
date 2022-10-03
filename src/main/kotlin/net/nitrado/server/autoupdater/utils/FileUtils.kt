package net.nitrado.server.autoupdater.utils

import org.yaml.snakeyaml.Yaml
import java.io.*
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.util.*


class FileUtils {
}

fun downloadFile(dowloadFile: String, toFile: String? = null ){

    /*
    println("------------------------------------------")
    if (config != null) {
        println( config.get("launcher").toString().trim() )
    }
    println("------------------------------------------")
*/
    //config.setProperty("blah","Test")
    //saveConfig(config)

    val toLocalFile: String?

    if (toFile != null)
        toLocalFile = toFile
    else{
        toLocalFile = File(dowloadFile).name
    }

    if (!File(toLocalFile).exists()) {
        try {
            BufferedInputStream( URL(dowloadFile).openStream() ).use { `in` ->
                FileOutputStream(toLocalFile).use { fileOutputStream ->
                    val dataBuffer = ByteArray(1024)
                    var bytesRead: Int
                    while (`in`.read(dataBuffer, 0, 1024).also { bytesRead = it } != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead)
                    }
                }
            }
        } catch (e: IOException) {
            // handle exception
        }
    } else{
        println( "File Already exist!" )
    }
}

fun loadYAMLConfig(yamlFilePath : String ): Map<String, Any>? {

    val yamlConfig: File = File( yamlFilePath )

    if (!yamlConfig.exists()) {
        val yaml = Yaml()
        return yaml.load<Map<String, Any>?>("")
    }

    val configStream: InputStream = FileInputStream( yamlFilePath )
    val yaml = Yaml()
    return yaml.load<Map<String, Any>?>(configStream)

}

fun copyConfigFromResource(localfile :String, copyToFile : String ): Boolean {

    val configFile = File( copyToFile )
    val configFilePath = File( copyToFile.replace( configFile.name.toString() , "" ) )

    if (!configFilePath.exists()) configFilePath.mkdirs()

    if (!configFile.exists()) {
        try {
            FileUtils::class.java.classLoader.getResourceAsStream( localfile ).use { `is` ->
                Files.copy(`is`, Paths.get( copyToFile ))
            }
        } catch (e: IOException) {
            // An error occurred copying the resource
        }finally {
            return false
        }
    }else return true
}



fun saveConfig(config: Properties? = null) {

    val configFile = File("server-autoupdater.conf")

    if ( configFile.exists() ){

        val writer = FileWriter(configFile)
        if (config != null) {
            config.store(writer, "Server-Autoupdater")
        }
    }
}

fun cacheFile(cacheFile : String , cacheTime : Int = 3600 ): Boolean {

    val cacheFile: File = File( cacheFile )

    val nowTime = (  Instant.now().toEpochMilli() / 1000 )

    if ( cacheFile.exists() ){

        val cachePath = cacheFile.toPath()
        val fileTime = ( Files.getLastModifiedTime(cachePath).toMillis() / 1000 )

        return ( fileTime + cacheTime ) >= nowTime

    }else{

        return false

    }
}

@Throws(IOException::class)
fun listFilesinDirectory(dir: String?): LinkedHashMap<String, MutableSet<String>> {
    val fileList: MutableSet<String> = HashSet()
    val dirList: MutableSet<String> = HashSet()
    val completeList = LinkedHashMap<String, MutableSet<String> >()

    Files.newDirectoryStream(Paths.get(dir)).use { stream ->
        for (path in stream) {
            if (!Files.isDirectory(path)) {
                fileList.add(path.fileName.toString())
            }else{
                dirList.add(path.fileName.toString())
            }
        }
    }
    completeList["files"] = fileList
    completeList["dirs"] = dirList
    return completeList
}

fun deleteDirectory(directoryToBeDeleted: File): Boolean {
    val allContents = directoryToBeDeleted.listFiles()
    if (allContents != null) {
        for (file in allContents) {
            deleteDirectory(file)
        }
    }
    return directoryToBeDeleted.delete()
}

@Throws(IOException::class)
fun copyDirectory(sourceDirectoryLocation: String, destinationDirectoryLocation: String?) {
    Files.walk(Paths.get(sourceDirectoryLocation))
        .forEach { source: Path ->
            val destination: Path = Paths.get(destinationDirectoryLocation, source.toString()
                .substring(sourceDirectoryLocation.length))
            try {
                Files.copy(source, destination)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
}

fun installCurseLoader( installerFile: String ): Boolean {

    try {

        val filename = File(installerFile).name
        val basePath = installerFile.replace( filename.toString() , "" )

        logInfo("Attempting to start Server $installerFile")
        logInfo("Filename: $filename")
        logInfo("Directory: $basePath")

        logInfo("Attempting to use installer from $basePath")

        logInfo("Starting installation of Loader, installer output incoming")
        logInfo("Check log from installer for more information")

        val installer = ProcessBuilder(
            "java",
            "-jar",
            filename,
            "nogui",
            "--installServer",
        )
            /* .inheritIO() */
            .directory( File( basePath ) )
            .start()

        val serverLog = Scanner(installer!!.inputStream)
        while (serverLog.hasNextLine()) {
            val println = serverLog.nextLine()
            logInfo(println)
        }

        installer.waitFor()

        logInfo("Done installing loader, deleting installer!")

        logInfo( "Delete: $installer ")



        val installerFile: File = File("$installerFile" )
        if( installerFile.exists() )  Files.delete( installerFile.toPath() )

        val installerFileLog: File = File("$installerFile.log" )
        if( installerFileLog.exists() )  Files.delete( installerFileLog.toPath() )

        val installerFileRunBat: File = File( basePath + "run.bat" )
        if( installerFileRunBat.exists() )  Files.delete( installerFileRunBat.toPath() )


        val installerFileRunSh: File = File( basePath + "run.sh" )
        if( installerFileRunSh.exists() )  Files.delete( installerFileRunSh.toPath() )

        val installerFileJavaArgs: File = File( basePath + "user_jvm_args.txt" )
        if( installerFileJavaArgs.exists() )  Files.delete( installerFileJavaArgs.toPath() )

        return true

    } catch (e: IOException) {
        logError("Problem while installing Loader from $installerFile $e")
        //throw DownloadLoaderException("Problem while installing Loader from $url", e)
        return false
    } catch (e: InterruptedException) {
        logError("Problem while installing Loader from $installerFile $e")
        //throw DownloadLoaderException("Problem while installing Loader from $url", e)#
        return false
    }


}

fun startServer( startFolder: String , startFile: String ): Boolean {

    println( File("$startFolder").absoluteFile )

    try {

        logInfo("Attempting to start Server $startFolder")
        logInfo("Filename: $startFile")
        logInfo("Directory: $startFolder")

        logInfo("Starting installation of Loader, installer output incoming")
        logInfo("Check log from installer for more information")

        val array = arrayOf("java", startFile , "nogui").toString()

        val vsArrays: MutableList<String> = ArrayList()
        vsArrays.add("java")
        if ( !startFile.startsWith("@") ) vsArrays.add("-jar")
        vsArrays.add( startFile )
        vsArrays.add("nogui")


        val installer = ProcessBuilder( vsArrays )
            .inheritIO()
            .directory( File("$startFolder") )
            .start()

        val serverLog = Scanner(installer!!.inputStream)
        while (serverLog.hasNextLine()) {
            val println = serverLog.nextLine()
            println( println )
        }

        installer.waitFor()

        logInfo("Done executing Server.")

        return true

    } catch (e: IOException) {
        logError("Problem while starting Server from $startFolder $e")
        //throw DownloadLoaderException("Problem while installing Loader from $url", e)
        return false
    } catch (e: InterruptedException) {
        logError("Problem while starting Server from $startFolder $e")
        //throw DownloadLoaderException("Problem while installing Loader from $url", e)#
        return false
    }


}
fun startServer_old( startFolder: String, startFile: String): Boolean {

    try {
        logInfo("Attempting to start Server $startFile @  $startFolder" )

        serverProcess = ProcessBuilder(
            "java",
            /*"java",*/
            "-Xmx5G",
            "-Xms1G",
            "-jar",
            startFile,
            "nogui",
        )
            /*.inheritIO()*/
            .directory( File( startFolder ) )
            .start()

        //logInfo("Server is Crashed")

        return true

    } catch (e: FileNotFoundException) {
        logError("The Start-File \"$startFile\" not exits. Error: $e")
        return false
    }catch (e: IOException) {
        logError("Problem while start Server from \"$startFile\" $e")
        return false
    } catch (e: InterruptedException) {
        logError("Problem while start Server from \"$startFile\" $e")
        return false
    }


}