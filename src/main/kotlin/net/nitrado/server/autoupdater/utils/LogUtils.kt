package net.nitrado.server.autoupdater.utils

import config
import mainDir
import java.awt.GraphicsEnvironment
import java.awt.HeadlessException
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class LogUtils {

}

// Define Text Colors
val TXT_RESET = "\u001B[0m"
val TXT_BLACK = "\u001B[30m"
val TXT_RED = "\u001B[31m"
val TXT_GREEN = "\u001B[32m"
val TXT_YELLOW = "\u001B[33m"
val TXT_BLUE = "\u001B[34m"
val TXT_PURPLE = "\u001B[35m"
val TXT_CYAN = "\u001B[36m"
val TXT_WHITE = "\u001B[37m"

//Define Background Colors
val BG_BLACK = "\u001B[40m"
val BG_RED = "\u001B[41m"
val BG_GREEN = "\u001B[42m"
val BG_YELLOW = "\u001B[43m"
val BG_BLUE = "\u001B[44m"
val BG_PURPLE = "\u001B[45m"
val BG_CYAN = "\u001B[46m"
val BG_WHITE = "\u001B[47m"

private val display = null


fun logInfo(message: String) {
    println(currentTime() + TXT_GREEN + "[N-S-A/INFO] " + TXT_RESET + message)
    try {
        doLog(currentTime() + "[N-S-A/INFO] " + cleanLog(
            message))
    } catch (e: IOException) {
        e.printStackTrace()
    }
    //if ( display != null) display.append(CurrentTime() + "[F-S-S/INFO] " + cleanLog( message) + System.lineSeparator())
}

fun logWarn(message: String) {
    println(currentTime() + TXT_YELLOW + "[N-S-A/WARNING] " + TXT_RESET + message)
    try {
        doLog(currentTime() + "[N-S-A/WARNING] " + cleanLog( message))
    } catch (e: IOException) {
        e.printStackTrace()
    }
    //if (display != null) display.append(CurrentTime() + "[F-S-S/WARNING] " + cleanLog( message) + System.lineSeparator())
}

fun logError(message: String) {
    println(currentTime() + TXT_RED + "[N-S-A/ERROR] " + TXT_RESET + message)
    try {
        doLog(currentTime() + "[N-S-A/ERROR] " + cleanLog( message))
    } catch (e: IOException) {
        e.printStackTrace()
    }
    //if (display != null) display.append(CurrentTime() + "[F-S-S/ERROR] " + cleanLog( message) + System.lineSeparator())
}

fun logDebug(message: String) {
    if ( config?.get("debug") == true ) {
        println(currentTime() + TXT_CYAN + "[F-S-S/DEBUG] " + TXT_RESET + message)
        try {
            doLog(currentTime() + "[N-S-A/DEBUG] " + cleanLog( message))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //if (display != null) display.append(CurrentTime() + "[F-S-S/DEBUG] " + cleanLog( message) + System.lineSeparator())
    }
}

private fun currentTime(): String {
    return if ( config?.get("timezone").toString() != "UTC" && config?.get("timezone") != null ) {
        val z = ZoneId.of(config?.get("timezone").toString())
        "[" + ZonedDateTime.now(z).format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT)) + "] "
    } else {
        "[" + ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT)) + "] "
    }
}

private fun cleanLog(message: String): String {
    return message.replace("\\x1b\\[[0-9;]*m".toRegex(), "")
}

@Throws(IOException::class)
fun doLog(message: String?) {
    if ( config?.get("log-to-file") == true ) {
        val fileWriter = FileWriter("$mainDir/server-autoupdater.log", true) //Set true for append mode
        val printWriter = PrintWriter(fileWriter)
        printWriter.println(message) //New line
        printWriter.close()
    }
}

val isReallyHeadless: Boolean
    get() = if (GraphicsEnvironment.isHeadless()) {
        true
    } else try {
        val screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices
        screenDevices == null || screenDevices.size == 0
    } catch (e: HeadlessException) {
        e.printStackTrace()
        true
    }
