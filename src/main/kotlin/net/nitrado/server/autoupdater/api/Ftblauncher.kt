package net.nitrado.server.autoupdater.api

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.nitrado.server.autoupdater.utils.*
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.regex.Pattern


class Ftblauncher () : Base() {

    override var name: String = "FTB-Modpack"

    override var cache: String?    = "ftb"

    override var baseurl: String?  = "https://api.modpacks.ch/"


    override fun jobGetCurrentBuild() {

        val ftbVersionArray = arrayOf( "public", "modpack", this.localVersion.toString() )

        var ftbVersionData = this.api( ftbVersionArray )

        val ftbVersionObj = JsonParser().parse(ftbVersionData["data"].toString()).asJsonObject

        if (ftbVersionObj.isJsonObject) {

            val ftbVersion = ftbVersionObj["versions"] as JsonArray

            for (j in 0 until ftbVersion.size() ) {

                val ftbGetVersion = ftbVersion[j] as JsonObject
                val type = ftbGetVersion.get("type").toString().substring(1, ftbGetVersion.get("type").toString().length - 1  ).toLowerCase()
                val id =   ftbGetVersion.get("id")

                logInfo( ftbGetVersion.toString() )

                if ( type == "release" ){

                    logInfo( type )

                    logInfo( id.toString() )

                    this.currentBuild = id.toString()

                    break
                }


            }

        }

    }

    override fun jobDownloadFiles() {

        var entry = JsonObject()

        val ftbArray = arrayOf( "public", "modpack", this.localVersion.toString() , this.currentBuild.toString() )

        var response = this.api( ftbArray )

        val curseObj = JsonParser().parse(response["data"].toString()).asJsonObject

        if (curseObj.isJsonObject) {

            val curseFiles = curseObj["files"] as JsonArray

            for (j in 0 until curseFiles.size() ) {

                val entry_obj = curseFiles[j] as JsonObject
                var path =  entry_obj.get("path").toString().substring(1, entry_obj.get("path").toString().length - 1  )

                var url: Any? = null
                if ( entry_obj.has("curseforge") ){
                    val curseforge = entry_obj.get("curseforge") as JsonObject
                    //NEW-WAY
                    // url =  this.getCurseFile( curseforge.get("project").toString(), curseforge.get("file").toString() )

                    //Bit Hacky Old Way, baypassing the API
                    var name =  entry_obj.get("name").toString().substring(1, entry_obj.get("name").toString().length - 1  )
                    var file = curseforge.get("file").toString()
                    url = "https://mediafiles.forgecdn.net/files/" + file.substring( 0, 4 ) + "/" + file.substring( file.length -3 ) + "/" + name.replace("+","%2B")

                }else{
                    url =   entry_obj.get("url").toString().substring(1, entry_obj.get("url").toString().length - 1  )
                }

                var name =  entry_obj.get("name").toString().substring(1, entry_obj.get("name").toString().length - 1  )

                var toDir  = "$mainDir/temp/$path"

                var toFile = name

                logInfo( "╥► File-Count: " + j.toString() + "/" + curseFiles.size().toString() )

                val theDir = File(toDir)
                if (!theDir.exists()) {
                    theDir.mkdirs()
                    logInfo( "╠► Create Directory $toDir")
                }


                logInfo( "╠► Download File: $name"  )
                logInfo( "╠► To Folder: $path")
                logInfo( "╚► From URL: $url")

                //todo Reimplement
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

                println( "https://maven.minecraftforge.net/net/minecraftforge/forge/$mc_version-$loader_version/forge-$mc_version-$loader_version-installer.jar" )
                //todo Reimplement
                downloadFile( "https://maven.minecraftforge.net/net/minecraftforge/forge/$mc_version-$loader_version/forge-$mc_version-$loader_version-installer.jar"  , mainDir +"/temp/forge-$mc_version-$loader_version-installer.jar"  )

                installCurseLoader( "$mainDir/temp/forge-$mc_version-$loader_version-installer.jar")

            }
            val writer = PrintWriter("$mainDir/temp/force_java$java_version", "UTF-8")
            writer.close()
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

    private fun getCurseFile(project: String, file: String): Any {

        val currentLoader = net.nitrado.server.autoupdater.api.Curse()
        val curseFileArray = arrayOf( "mods", project )

        val curseFile = currentLoader.api(curseFileArray)

        val curseObj = JsonParser().parse(curseFile["data"].toString()).asJsonObject

        val getdata = JsonParser().parse(curseObj.get("data").toString()).asJsonObject

        var slug = getdata.get("slug").toString().substring(1, getdata.get("slug").toString().length - 1  )

        return "https://www.curseforge.com/minecraft/mc-mods/" + slug + "/download/" + file

        // return "https://mediafiles.forgecdn.net/files/3735/758/1.18.2-betteramethyst-1.0.1.jar"

    }

    override fun jobFindStartFile(){

        val startFiles = listFilesinDirectory("$userDir/")

        startFiles["files"]?.forEach { file ->

            val matchForge = Pattern.compile("forge-([.0-9]+)-([.0-9]+).([jar|zip]+)").matcher(file)

            if (matchForge.find()) {

                logInfo("Find StartFile: $file")
                val curseVersionMinecraft = matchForge.group(1)
                val curseVersionForge = matchForge.group(2)
                logInfo("Minecraft-Version: $curseVersionMinecraft")
                logInfo("Forge-Version: $curseVersionForge")

                this.startFile = file

                return
            }

        }

        val startFilesNew = listFilesinDirectory("$userDir/libraries/net/minecraftforge/forge/")

        startFilesNew["dirs"]?.forEach { folder ->

            //println( folder )
            if (File("$userDir/libraries/net/minecraftforge/forge/$folder/").exists() ) {

                val startupFile = if ( OS.contains("win")) "win_args.txt" else "unix_args.txt"

                this.startFile ="@libraries/net/minecraftforge/forge/$folder/$startupFile"

                logInfo("Find StartFile: ${this.startFile}")


                return
            }

        }

    }


}