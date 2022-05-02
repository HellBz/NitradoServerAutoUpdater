
package net.nitrado.server.autoupdater.utils

import jarName
import userDir
import java.io.File

class CleanUp {

}

fun cleanupServer() {
    logInfo( TXT_YELLOW + "CleanUp Servers Directory's & Files, before Update" + TXT_RESET )

    val serverDir = "$userDir/test/"

    val serverFiles = listFilesUsingDirectoryStream(serverDir)

    serverFiles["dirs"]?.forEach { dir ->
        dir

        val tempWorldCheck = File("$serverDir$dir/level.dat")
        if (    !tempWorldCheck.exists() &&
            !dir.contains("server-autoupdater") &&
            !dir.contains("plugins")
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

        if (    file.contains("user_jvm_args.txt") ||
            (
                    getExt(file).toString() != "properties" &&
                            getExt(file).toString() != "json" &&
                            getExt(file).toString() != "txt" &&
                            jarName != file
                    )
        ) {
            logInfo( TXT_YELLOW + "Cleanup File: $TXT_RED$file$TXT_RESET" )
            val toDir = File("$serverDir/$file" )
            if( toDir.exists() )  deleteDirectory( toDir )
        }else{
            logInfo(TXT_CYAN + "CleanUp SKIP: $TXT_RESET$file")
        }
    }
}

fun cleanupTemp() {

    logInfo( TXT_YELLOW + "CleanUp Temp Directory's & Files, before Update" + TXT_RESET )

    val tempDir = "F:\\Benutzerdaten\\Downloads\\SIMPLE-SERVER-FILES-0.3.18\\SIMPLE-SERVER-FILES-0.3.18\\setup"

    val tempFiles = listFilesUsingDirectoryStream(tempDir)

    tempFiles["dirs"]?.forEach { dir ->
        dir

        val tempWorldCheck = File("$tempDir$dir/level.dat")
        if (    !tempWorldCheck.exists() &&
            !dir.contains("server-autoupdater") &&
            !dir.contains("plugins")
        ) {
            logInfo( TXT_YELLOW + "Cleanup Directory: $TXT_RED$dir$TXT_RESET" )
            val toDir = File("$tempDir/$dir" )
            if( toDir.exists() )  deleteDirectory( toDir )
        }else{
            logInfo( TXT_CYAN + "CleanUp SKIP: $TXT_RESET$dir$TXT_RESET")
        }
    }

    tempFiles["files"]?.forEach { file ->
        file

        if (
                file.contains("user_jvm_args.txt") ||
                file.contains("manifest.json") ||
                getExt(file).toString() == "zip" ||
                getExt(file).toString() == "log" ||
                getExt(file).toString() == "sh" ||
                getExt(file).toString() == "bat"
        ) {
            logInfo( TXT_YELLOW + "Cleanup File: $TXT_RED$file$TXT_RESET" )
            val toDir: File = File("$tempDir/$file" )
            if( toDir.exists() )  deleteDirectory( toDir )
        }else{
            logInfo(TXT_CYAN + "CleanUp SKIP: $TXT_RESET$file")
        }
    }

}