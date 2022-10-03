

import net.nitrado.server.autoupdater.utils.*
import java.io.File
import java.lang.reflect.Method


val config = loadConfig()

public class ServerAutoUpdater {}

fun main() {

    logInfo("-----------------------------------------------")
    logInfo("$TXT_YELLOW|\\| o _|_ __ _  _| _    __  _ _|_$TXT_RESET")
    logInfo("$TXT_YELLOW| | |  |_ | (_|(_|(_) o | |(/_ |_$TXT_RESET")
    logInfo("-----------------------------------------------")

    val loader = capitalize( config?.get("loader").toString()  )

    var classBinName = "net.nitrado.server.autoupdater.api.$loader"

    val any = try {

        // Create a new JavaClassLoader

        val cls = Class.forName("net.nitrado.server.autoupdater.api.Base")
        val classLoader =  cls.classLoader;

        // Load the target class using its binary name
        val loadedMyClass = classLoader.loadClass(classBinName)
        println("Loaded class name: " + loadedMyClass.name)

        // Create a new instance from the loaded class
        val constructor = loadedMyClass.getConstructor()
        val myClassObject: Any = constructor.newInstance()

        loadedMyClass.getMethod("jobGetLocalConfig").invoke(myClassObject)

        loadedMyClass.getMethod("jobDoUpdateYesOrNo").invoke(myClassObject)


        if ( loadedMyClass.getMethod("jobDoUpdateYesOrNo").invoke(myClassObject).toString() == "true" ) {

            //Backup current Server-Files
            loadedMyClass.getMethod("jobBackUpFiles").invoke(myClassObject)

            loadedMyClass.getMethod("jobGetDownloadFile").invoke(myClassObject)

            loadedMyClass.getMethod("jobDownloadFiles").invoke(myClassObject)

            loadedMyClass.getMethod("jobUnPackFiles").invoke(myClassObject)

            loadedMyClass.getMethod("jobFindInstaller").invoke(myClassObject)

            loadedMyClass.getMethod("jobCleanUpTemp").invoke(myClassObject)

            loadedMyClass.getMethod("jobCleanUpServer").invoke(myClassObject)

            loadedMyClass.getMethod("jobCopyTempToServer").invoke(myClassObject)

            loadedMyClass.getMethod("jobEraseTemp").invoke(myClassObject)

            loadedMyClass.getMethod("jobWriteCurrentBuildToFile").invoke(myClassObject)

        }

        //TODO loadedMyClass.getMethod("jobFindStartFile").invoke(myClassObject)
        loadedMyClass.getMethod("jobFindStartFile").invoke(myClassObject)

        //Set Eula.txt to True or create a new
        loadedMyClass.getMethod("jobSetEulaTrue").invoke(myClassObject)

        //TODO loadedMyClass.getMethod("jobStartServer").invoke(myClassObject)
        loadedMyClass.getMethod("jobStartServer").invoke(myClassObject)

        loadedMyClass.getMethod("jobServerStopped").invoke(myClassObject)

    } catch (e: ClassNotFoundException) {
        logError("Warning: $loader is not implemented yet")
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }

}
