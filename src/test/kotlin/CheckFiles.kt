import net.nitrado.server.autoupdater.utils.*
import java.io.File

fun main (args: Array<String>) {

    logInfo( TXT_YELLOW + "CleanUp Directory's & Files, before Update" + TXT_RESET )

    val serverDir = "$userDir/test/"

    val serverFiles = listFilesUsingDirectoryStream(serverDir)

    serverFiles["dirs"]?.forEach { dir ->
        dir

        val tempWorldCheck = File("$serverDir$dir/level.dat")
        if ( !tempWorldCheck.exists() &&
            !dir.contains("server-autoupdater")
            ) {
            logInfo( TXT_YELLOW + "Cleanup Directory: $TXT_RED$dir$TXT_RESET" )
            val toDir: File = File("$serverDir/$dir" )
            if( toDir.exists() )  deleteDirectory( toDir )
        }else{
            logInfo( TXT_CYAN + "CleanUp SKIP: $TXT_RESET$dir$TXT_RESET")
        }
    }

    serverFiles["files"]?.forEach { file ->
        file

        if ( file.contains(".jar") &&
            jarName != file
           ) {

            logInfo( TXT_YELLOW + "Cleanup File: $TXT_RED$file$TXT_RESET" )
            val toDir: File = File("$serverDir/$file" )
            if( toDir.exists() )  deleteDirectory( toDir )
        }else{
            logInfo(TXT_CYAN + "CleanUp SKIP: $TXT_RESET$file")
        }
    }

}
