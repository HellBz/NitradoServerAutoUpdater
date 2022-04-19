package net.nitrado.server.autoupdater

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.LinkedHashMap
import java.util.function.Predicate
import java.util.stream.Collectors

class Dumped {
}

/*
val files = listFilesUsingFileWalk("./", 1 )

println( files.toString() )


if ( files != null) {
    println( listOf(files.contains("gradlew")) )
}

try {
    BufferedInputStream(URL( curse_serverfile ).openStream()).use { `in` ->
        FileOutputStream("modpack-download.zip").use { fileOutputStream ->
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

val mod_content = listContents( "modpack-download.zip" )

if ( mod_content != null) {
    println( listOf(mod_content.contains("startserver.sh")) )
}

*/



fun getCurse( request : String ): LinkedHashMap<String, String> {

    var l_serv_build = ""
    var response_code = ""
    var response = ""

    try {
        //Download-File
        //URL url = new URL("https://api.curseforge.com/v1" + "/mods/1234/files/3661690/download-url" );
        //val slug = "/mods/381671/files/?pageSize=50"
        val url = URL("https://api.curseforge.com/v1$request")
        val http = url.openConnection() as HttpURLConnection
        http.requestMethod = "GET"
        http.setRequestProperty("Accept", "application/json")
        http.setRequestProperty("x-api-key", "$2a$10\$nA5YAQ2zhPP779caRJp.Bua.8X0j/oGJ7xPQgJTaRYQLgF6Ou4PAq")
        response_code = http.responseCode.toString()
        // System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
        val document = String()
        val reader: Reader = InputStreamReader(http.inputStream, StandardCharsets.UTF_8)

        if (!reader.toString().trim().isEmpty() && http.responseCode == 200) {
            val parser = JsonParser()
            val datas = parser.parse(reader) as JsonObject
            response = datas["data"].toString().trim()
            //response = datas.toString().trim()
            val entrys = datas["data"] as JsonArray
            for (j in 0 until entrys.size()) {
                val entry_obj = entrys[j] as JsonObject
                //System.out.println(  entry_obj.get("serverPackFileId").toString() );
                if (j == 0) l_serv_build = entry_obj["serverPackFileId"].toString()
                if (!entry_obj["serverPackFileId"].toString().isEmpty()) {
                    //System.out.println(  entrys.get(j).getAsJsonArray("hashes") );
                    l_serv_build = entry_obj["serverPackFileId"].toString()
                    break
                }
            }
        }
    } catch (e: IOException) {
        // e.printStackTrace();
    }

    val curse = LinkedHashMap<String, String>()
    curse["code"] = response_code
    curse["data"] = response
    curse["last_build"] = l_serv_build
    return curse

}

/*

val jsona = "{\"brand\":\"Jeep\", \"doors\": 3}"

//Read the JSON file
//Read the JSON file
val root = JsonParser().parse(jsona)

println(root.asJsonObject)

val json = "{\"brand\":\"Jeep\", \"doors\": 3}"

val gson = Gson()

val car = gson.fromJson(json, Car::class.java)

println(car.brand)

val fileName = "sample.json"
System.out.println("getResourceAsStream : " + fileName);

val inputStream: InputStream = getResourceAsStream(fileName)
val textBuilder = StringBuilder()
BufferedReader(InputStreamReader(inputStream, Charset.forName(StandardCharsets.UTF_8.name()))).use { reader ->
    var c = 0
    while (reader.read().also { c = it } != -1) {
        textBuilder.append(c.toChar())
    }
}

// for static access
// URL resource = JavaClassName.class.getClassLoader().getResource("fileName");
//println(textBuilder)


val map: Map<String, String> =
    Gson().fromJson<Map<*, *>>(textBuilder.toString(), MutableMap::class.java) as Map<String, String>

println(map)
*/


/*

    val fileNamea = "sample.json"

    //Source: https://www.youtube.com/watch?v=_kHKp_CuVQI
    val parser = JsonParser()
    val inputStreama: InputStream = getResourceAsStream(fileNamea)
    val reader: Reader = InputStreamReader(inputStreama)
    val rootElement = parser.parse(reader)
    val rootObject = rootElement.asJsonObject
    val pages = rootObject.getAsJsonObject("query").getAsJsonObject("pages").getAsJsonObject("12359849").getAsJsonArray("revisions")

    for (i in 0 until pages.size() ) {
        System.out.println( pages.get(i).asJsonObject.get("user") )
    }


    println( pages.size() )

    */

@Throws(IOException::class)
fun listFilesUsingFileWalk(dir: String?, depth: Int): MutableSet<Any>? {
    Files.walk(Paths.get(dir), depth).use { stream ->
        return stream
            .filter(Predicate { file: Path? -> !Files.isDirectory(file) })
            .map<Any>(Path::getFileName)
            .map<Any> { it.toString() }
            .collect(Collectors.toSet<Any>())
    }
}