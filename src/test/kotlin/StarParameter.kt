
import java.io.File
import java.lang.management.ManagementFactory
import java.util.*


fun main (args: Array<String>) {


    //Get System-Variables like xmx and xms
    val runtimeMxBean = ManagementFactory.getRuntimeMXBean()
    val arguments = runtimeMxBean.inputArguments
    val startupParameter = arguments.toTypedArray()


    //DEBUG
    val joinedStartupParameter: String = Arrays.toString(startupParameter)
    println("STARTUP-PARAMETER $joinedStartupParameter")


    println("Building Startup-Parameter")

    var where: MutableList<String> = ArrayList()
    where.add("java")

    val startup_file = "libraries/net/minecraftforge/forge/1.17.1-37.1.1/win_args.txt"

    where.add("@" + startup_file)
    //where.add("nogui")



    java.util.Collections.addAll<kotlin.String?>(where, *startupParameter)

    where.add("-Duser.timezone=" + "Africa/Djibouti")

    println( where.toTypedArray() )

    var CMD_ARRAY = arrayOfNulls<String>(where.size)


    //DEBUG StartUp Commands
    println("CMD-ARRAY " + where.toString() )


    var installerFile = "G:/GIT/NitradoServerAutoUpdater/test/forge-1.16.5-36.2.34.jar"
    val filename = File(installerFile).name
    val filepath = installerFile.replace( filename.toString() , "" )

    val cmdArray = arrayOfNulls<String>(1)


    val testprozess = ProcessBuilder(
        where
    )
        .inheritIO()
        .directory(File("$filepath"))
        .start()

    println( testprozess.toString() )
}