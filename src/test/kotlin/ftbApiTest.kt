import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.nitrado.server.autoupdater.utils.downloadFile
import net.nitrado.server.autoupdater.utils.mainDir
import java.io.File
import java.nio.file.Files

fun main() {

    val currentLoader = net.nitrado.server.autoupdater.api.Base()

    currentLoader.baseurl = "https://api.modpacks.ch/"

    currentLoader.cache = "ftb"

    val ftbArray = arrayOf( "public", "modpack", "89", "2063" )

    println( ftbArray );

    var response = currentLoader.api( ftbArray )

    val curseObj = JsonParser().parse(response["data"].toString()).asJsonObject

    if (curseObj.isJsonObject) {

        val curseFiles = curseObj["files"] as JsonArray

        for (j in 0 until curseFiles.size() ) {

            val entry_obj = curseFiles[j] as JsonObject
            var path =  entry_obj.get("path").toString().substring(1, entry_obj.get("path").toString().length - 1  )
            var url =   entry_obj.get("url").toString().substring(1, entry_obj.get("url").toString().length - 1  )
            var name =  entry_obj.get("name").toString().substring(1, entry_obj.get("name").toString().length - 1  )

            println(path)
            println(url)
            println(name)

            var toDir = "$mainDir/testtesttest/"+ path
            toDir = "G:\\GIT\\NitradoServerAutoUpdater\\test-kopie\\test\\" + path

            var toFile = name
            println( toDir + toFile  )


            val theDir = File(toDir)
            if (!theDir.exists()) {
                theDir.mkdirs()
                println("CreateDirectory before File: " + toDir )
            }


            downloadFile( url  , toDir +  toFile )
            println( "----------------------------------" )
        }

    }

}




