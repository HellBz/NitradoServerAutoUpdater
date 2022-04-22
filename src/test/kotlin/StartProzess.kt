import net.nitrado.server.autoupdater.utils.basename
import net.nitrado.server.autoupdater.utils.logInfo
import java.io.File
import kotlin.system.exitProcess

fun main (args: Array<String>) {


    var installerFile = "G:/GIT/NitradoServerAutoUpdater/test/forge-1.16.5-36.2.34.jar"
    val filename = basename(installerFile)
    val filepath = installerFile.replace( filename.toString() , "" )

    logInfo("Attempting to start Server $installerFile")
    logInfo("Filename: $filename")
    logInfo("Directory: $filepath")

    val processBuilder = ProcessBuilder(
        "java",
        "-Xmx5G",
        "-Xms1G",
        "@libraries/net/minecraftforge/forge/1.17.1-37.1.1/win_args.txt",
        "nogui",
    )

    processBuilder.directory(File("$filepath"))
    processBuilder.inheritIO();
    //serverProcess = processBuilder.start()
    val process = processBuilder.start()

    val exitCode: Int = process.waitFor()

    logInfo("Exit with Code $exitCode ")

    exitProcess( 1 )
    
}