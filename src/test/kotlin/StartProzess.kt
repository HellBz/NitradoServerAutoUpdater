import net.nitrado.server.autoupdater.utils.logInfo
import net.nitrado.server.autoupdater.utils.logWarn
import net.nitrado.server.autoupdater.utils.serverProcess
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

fun main (args: Array<String>) {

    var installerFile = "G:/GIT/NitradoServerAutoUpdater/test/forge-1.16.5-36.2.34.jar"
    val filename = File(installerFile).name
    val filepath = installerFile.replace( filename.toString() , "" )

    logInfo("Attempting to start Server $installerFile")
    logInfo("Filename: $filename")
    logInfo("Directory: $filepath")

    serverProcess = ProcessBuilder(
        "java",
        "-Xmx5G",
        "-Xms1G",
        "@libraries/net/minecraftforge/forge/1.17.1-37.1.1/win_args.txt",
        "nogui",
    )
        /*.inheritIO()*/
        .directory(File("$filepath"))
        .start()

    if( serverProcess != null  ) {

        val logger = Thread {
            val serverLog = Scanner(serverProcess!!.inputStream)
            //logInfo("Server-Starter - LOG-Printer is started")
            //logInfo("-----------------------------------------------")

            while (serverLog.hasNextLine()) {
                val println = serverLog.nextLine()
                println(println)
            }
        }

        val console = Thread {

            val stdin = serverProcess!!.outputStream // <- Eh?
            val writer = BufferedWriter(OutputStreamWriter(stdin))
            val scanner = Scanner(System.`in`)

            //logInfo("Server-Starter - Console-Scanner is started")
            //logInfo("-----------------------------------------------")

            while (true) {
                val input = scanner.nextLine()
                if (input == "") break
                //System.out.println( input );
                try {
                    writer.write(input + System.lineSeparator())
                    writer.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            try {
                writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        val check_server = Thread {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(5)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

                if ( !(serverProcess?.isAlive)!! ) {
                    logger.stop()
                    console.stop()
                    Thread.currentThread().stop()
                }
            }
        }

        logInfo("-----------------------------------------------")
        logInfo("--- Start Minecraft Server")
        logInfo("-----------------------------------------------")

        logger.start()
        console.start()
        check_server.start()
    }

    val exitCode: Int? = serverProcess?.waitFor()

    if ( exitCode == 0 ){
        logInfo("Server is successfully stopped.")
        exitProcess( exitCode as Int )
    }else{
        logWarn("Server is Crashed with Exit-Code: $exitCode ")
        exitProcess( exitCode as Int )
    }





}