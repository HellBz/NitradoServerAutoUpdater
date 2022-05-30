

import net.nitrado.server.autoupdater.utils.capitalize
import net.nitrado.server.autoupdater.utils.doesClassExist
import net.nitrado.server.autoupdater.utils.loadConfig
import net.nitrado.server.autoupdater.utils.logError

val config = loadConfig()

fun main() {

    var currentLoader = net.nitrado.server.autoupdater.api.Base()
    currentLoader.jobGreeting()

    val loader = capitalize( config?.get("loader").toString()  )
    if ( doesClassExist("net.nitrado.server.autoupdater.api.$loader") ){

        when ( loader ) {
            "Curse" -> currentLoader = net.nitrado.server.autoupdater.api.Curse()
            "Bukkit" -> currentLoader = net.nitrado.server.autoupdater.api.Base()
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

    currentLoader.jobDoUpdateYesOrNo()

    if (currentLoader.doupdate) {

        //Backup current Server-Files
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

    //Set Eula.txt to True or create a new
    currentLoader.jobSetEulaTrue()

    //TODO currentLoader.jobStartServer()
    currentLoader.jobStartServer()

    currentLoader.jobServerStopped()
}