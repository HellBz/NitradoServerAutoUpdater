package net.nitrado.server.autoupdater.api

import cacheDir
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import config

import net.nitrado.server.autoupdater.utils.cacheFile

import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class Curse () : Base() {

    fun API(requestArray: Array<String>): LinkedHashMap<String, String> {

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

    override fun latestGet(): JsonObject {

        var entry = JsonObject()

        //Check Version for Curse-Pack
        var modpackID = config?.get("modpack-id") as Int
        val curseArray = arrayOf("mods", modpackID.toString(), "files", "?pageSize=50")
        val curse = this.API(curseArray)
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

    override fun latestVersion(): String {

        var version: String? = null

        var entry = this.latestGet()

        version = entry.get("id").toString()

        return version
    }

    override fun latestServer(): String {

        var version: String? = null

        var entry = this.latestGet()

        version = entry.get("serverPackFileId").toString()

        return version
    }

    override fun latestFile(): String {

        val projektId = config?.get("modpack-id") as Int
        val curseServerArray = arrayOf("mods",  projektId.toString() , "files", this.latestServer() ,  "download-url" )

        val curseServer = this.API(curseServerArray)
        val curseServerObj = JsonParser().parse(curseServer["data"].toString()).asJsonObject
        val curseServerFile = curseServerObj["data"].toString().replace("\"", "").trim()

        return curseServerFile

    }
}