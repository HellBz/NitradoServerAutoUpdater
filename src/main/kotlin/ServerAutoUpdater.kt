
import net.nitrado.server.autoupdater.utils.*
import java.lang.System.getProperty

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

    serverProcess?.waitFor()

    currentLoader.jobServerStopped()
}