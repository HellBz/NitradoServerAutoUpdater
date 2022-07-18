import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.nitrado.server.autoupdater.utils.downloadFile
import net.nitrado.server.autoupdater.utils.installCurseLoader
import net.nitrado.server.autoupdater.utils.logInfo
import net.nitrado.server.autoupdater.utils.mainDir
import java.io.File
import kotlin.system.exitProcess


fun main() {

    val installPath = "G:\\GIT\\NitradoServerAutoUpdater\\test-kopie\\test\\"

    val currentLoader = net.nitrado.server.autoupdater.api.Base()

    currentLoader.baseurl = "https://api.modpacks.ch/"

    currentLoader.cache = "ftb"

    currentLoader.currentVersion = "96"

    val ftbVersionArray = arrayOf( "public", "modpack", currentLoader.currentVersion.toString() )

    var ftbVersionData = currentLoader.api( ftbVersionArray )

    val ftbVersionObj = JsonParser().parse(ftbVersionData["data"].toString()).asJsonObject

    if (ftbVersionObj.isJsonObject) {

        val ftbVersion = ftbVersionObj["versions"] as JsonArray

        for (j in 0 until ftbVersion.size() ) {

            val ftbGetVersion = ftbVersion[j] as JsonObject
            val type = ftbGetVersion.get("type").toString().substring(1, ftbGetVersion.get("type").toString().length - 1  ).toLowerCase()
            val id =   ftbGetVersion.get("id")

            logInfo( ftbGetVersion.toString() )

            if ( type == "release" ){

                //logInfo( type )

                //logInfo( id.toString() )

                currentLoader.currentBuild = id.toString()

                break
            }


        }

    }

    val ftbArray = arrayOf( "public", "modpack", currentLoader.currentVersion.toString() , currentLoader.currentBuild.toString() )

    var response = currentLoader.api( ftbArray )

    val curseObj = JsonParser().parse(response["data"].toString()).asJsonObject

    if (curseObj.isJsonObject) {

        val curseFiles = curseObj["files"] as JsonArray

        for (j in 0 until curseFiles.size() ) {

            val entry_obj = curseFiles[j] as JsonObject
            var path =  entry_obj.get("path").toString().substring(1, entry_obj.get("path").toString().length - 1  )
            var url =   entry_obj.get("url").toString().substring(1, entry_obj.get("url").toString().length - 1  )
            var name =  entry_obj.get("name").toString().substring(1, entry_obj.get("name").toString().length - 1  )

            //println(path)
            //println(url)
            //println(name)

            var toDir = "$mainDir/testtesttest/"+ path
            toDir = installPath + path

            var toFile = name


            logInfo( "╥► Count Files: " + j.toString() + "/" + curseFiles.size().toString() )

            val theDir = File(toDir)
            if (!theDir.exists()) {
                theDir.mkdirs()
                logInfo( "╠► Create Directory $toDir")
            }


            logInfo( "╠► Download File: $name"  )
            logInfo( "╠► To Folder: $path")
            logInfo( "╚► From URL: $url")

            downloadFile( url  , toDir +  toFile )
            //println( "----------------------------------" )
        }

        val gettargets = curseObj.get("targets").asJsonArray

        val get_first = JsonParser().parse( gettargets.get(0).toString() ).asJsonObject
        val get_second = JsonParser().parse( gettargets.get(1).toString() ).asJsonObject
        val get_third = JsonParser().parse( gettargets.get(2).toString() ).asJsonObject

        var loader_version = get_first.get("version").toString().substring(1, get_first.get("version").toString().length - 1  )
        var loader_name = get_first.get("name").toString().substring(1, get_first.get("name").toString().length - 1  )
        var mc_version = get_second.get("version").toString().substring(1, get_second.get("version").toString().length - 1  )
        var java_version_full = get_third.get("version").toString().substring(1, get_third.get("version").toString().length - 1  )

        var java_version = java_version_full.replace(  java_version_full.substring( java_version_full.indexOf(".") )  , "" )

        logInfo( "Loader Version: $loader_version" )
        logInfo( "Loader Name: $loader_name" )
        logInfo( "Minecraft Version: $mc_version" )
        logInfo( "Java Version: $java_version_full" )

        if ( loader_name == "forge" ){

            logInfo("Download forge-$mc_version-$loader_version-installer.jar")

            downloadFile( "https://maven.minecraftforge.net/net/minecraftforge/forge/$mc_version-$loader_version/forge-$mc_version-$loader_version-installer.jar"  , installPath + "forge-$mc_version-$loader_version-installer.jar"  )

            installCurseLoader( installPath + "forge-$mc_version-$loader_version-installer.jar")

        }

        logInfo( "force_java" + java_version )

    }

}





