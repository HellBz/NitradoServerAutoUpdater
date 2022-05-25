package net.nitrado.server.autoupdater.api

import backupDir
import cacheDir
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import config
import jarName
import mainDir
import modDownloadFile
import net.nitrado.server.autoupdater.utils.*
import userDir
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.regex.Pattern

class Curse () : Base() {

    override var name: String = "Base-Loader"

    override fun jobGetCurrentVersion() {
        var entry = JsonObject()

        //Check Version for Curse-Pack
        var modpackID = config?.get("modpack-id") as Int
        val curseArray = arrayOf("mods", modpackID.toString(), "files", "?pageSize=50")
        val curse = this.api(curseArray)
        val curseObj = JsonParser().parse(curse["data"].toString()).asJsonObject

        // println(curseObj.isJsonObject)

        if (curseObj.isJsonObject) {

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
        }
        this.currentVersion = entry.get("id").toString()
    }

    override fun jobBackUpFiles() {

        logInfo("Start creating Backup of existing Files to:")
        var backup_zip = "$backupDir/curse_" + this.localVersion + ".zip"
        logInfo("$backup_zip")
        try {
            val directory: File = File( "$userDir/$backupDir/" )
            if (!directory.exists()) directory.mkdirs()
            ZipFileExample.zip("$userDir/", "$backup_zip")
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun jobGetDownloadFile() {
        val projektId = config?.get("modpack-id") as Int
        val curseServerArray = arrayOf("mods",  projektId.toString() , "files", this.latestServer() ,  "download-url" )

        val curseServer = this.api(curseServerArray)
        val curseServerObj = JsonParser().parse(curseServer["data"].toString()).asJsonObject
        val curseServerFile = curseServerObj["data"].toString().replace("\"", "").trim()

        this.downloadFile = curseServerFile
    }

    override fun jobDownloadFiles() {

        logInfo("Download Server-Files from:")
        logInfo( this.downloadFile.toString() )

        val downloadThisFile = this.downloadFile
        downloadFile( "$downloadThisFile" , "$mainDir/$modDownloadFile" )

    }

    override fun jobUnPackFiles() {
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
    }

    override fun jobFindInstaller() {

        val tempDataInstaller = listFilesinDirectory("$mainDir/temp/")
        tempDataInstaller["files"]?.forEach { file ->
            file

            val matchForge =
                Pattern.compile("forge-([.0-9]+)-([.0-9]+)-([universal|installer]+).([jar|zip]+)").matcher(file)

            if (matchForge.find()) {

                logInfo("Find Installer: $file")
                val curseVersionMinecraft = matchForge.group(1)
                val curseVersionForge = matchForge.group(2)
                logInfo("Minecraft-Version: $curseVersionMinecraft")
                logInfo("Forge-Version: $curseVersionForge")

                val installer = installCurseLoader("$userDir/$mainDir/temp/$file")
            }
        }
    }

    override fun jobCopyTempToServer() {

        val serverFiles = listFilesinDirectory("$userDir/")
        val tempDatas = listFilesinDirectory("$mainDir/temp/")

        tempDatas["files"]?.forEach { file ->
            file
            if (
                getExt(file).toString() != "bat" &&
                getExt(file).toString() != "sh" &&
                /* serverFiles.get("files")?.contains( file ) == true &&  */
                jarName != file
            ) {

                logInfo("Cleanup & Copy File: $file")

                val toFile: File = File("$userDir/$file")
                val fromFile: File = File("$mainDir/temp/$file")
                if (toFile.exists()) Files.delete(toFile.toPath())
                if (file.equals("server-icon.png"))
                    copyConfigFromResource("server-icon.png", "$userDir/server-icon.png")
                else
                    if (!toFile.exists()) Files.copy(
                        fromFile.toPath(),
                        toFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                    );


            }

        }
        tempDatas["dirs"]?.forEach { dir ->
            dir
            if (
            /* serverFiles.get("dirs")?.contains( dir )!! && */
                (serverFiles.get("dirs")?.contains("world")!! && dir.equals("world"))
            ) {
                println("Found World: $dir, SKIP")
            } else {

                logInfo("Cleanup & Copy Directory: $dir")

                val toDir: File = File("$userDir/$dir")
                if (toDir.exists()) deleteDirectory(toDir)
                if (!toDir.exists()) copyDirectory("$mainDir/temp/$dir", "$userDir/$dir")
            }
        }

    }

    fun api(requestArray: Array<String>): LinkedHashMap<String, String> {

        var sendRequest = requestArray.joinToString("/")
        var cacheString = requestArray.joinToString("_")
        var cacheStringUrl = URLEncoder.encode(cacheString, StandardCharsets.UTF_8.toString())

        val curse = LinkedHashMap<String, String>()

        var responseCode = ""
        var responseMessage = ""
        var responseData = ""

        val directory: File = File(cacheDir)
        if (!directory.exists()) directory.mkdirs()

        var curseCacheFile = "$cacheDir/curse_$cacheStringUrl.json"

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

                val url = URL("https://api.curseforge.com/v1/$sendRequest")
                val http = url.openConnection() as HttpURLConnection
                http.requestMethod = "GET"
                http.setRequestProperty("Accept", "application/json")
                http.setRequestProperty("x-api-key", "$2a$10\$nA5YAQ2zhPP779caRJp.Bua.8X0j/oGJ7xPQgJTaRYQLgF6Ou4PAq")

                responseCode = http.responseCode.toString()
                responseMessage = http.responseMessage

                if (http.responseCode == 200) {

                    val reader: Reader = InputStreamReader(http.inputStream, StandardCharsets.UTF_8)

                    if (!reader.toString().trim().isEmpty()) {

                        val datas = JsonParser().parse(reader) as JsonObject
                        responseData = datas.toString().trim()

                        val writer = BufferedWriter(FileWriter("$cacheDir/curse_$cacheStringUrl.json"))
                        writer.write(responseData)

                        writer.close()
                    }
                }

            } catch (exception: FileNotFoundException) {
                responseCode = "404"
                responseMessage = exception.printStackTrace().toString()
            }
        }

        curse["code"] = responseCode
        curse["message"] = responseMessage
        curse["data"] = responseData
        return curse
    }

    private fun latestGet(): JsonObject {

        var entry = JsonObject()

        //Check Version for Curse-Pack
        var modpackID = config?.get("modpack-id") as Int
        val curseArray = arrayOf("mods", modpackID.toString(), "files", "?pageSize=50")
        val curse = this.api(curseArray)
        val curseObj = JsonParser().parse(curse["data"].toString()).asJsonObject

        // println(curseObj.isJsonObject)

        if (curseObj.isJsonObject) {

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
        }
        return entry
    }

    private fun latestVersion(): String {

        this.name = "Test"

        var version: String? = null

        var entry = this.latestGet()

        version = entry.get("id").toString()

        return version
    }

    private fun latestServer(): String {

        var version: String? = null

        var entry = this.latestGet()

        version = entry.get("serverPackFileId").toString()

        return version
    }


}