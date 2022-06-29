import net.nitrado.server.autoupdater.utils.FileUtils
import net.nitrado.server.autoupdater.utils.configFileName
import net.nitrado.server.autoupdater.utils.logInfo
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*


fun main() {

    var IP = "127.0.0.1"
    var timezone = "UTC"

    //Get IP From Internet
    val ipv4Services = arrayOf(
        "http://ip-api.com/csv/?fields=query",
        "http://checkip.amazonaws.com/",
        "https://ipv4.icanhazip.com/",
        "http://myexternalip.com/raw",
        "http://ipecho.net/plain",
        "https://myexternalip.com/raw",
    )

    //Get IP From Config
    try {
        FileInputStream("server.properties").use { input ->
            val prop = Properties()
            prop.load(input)
            if ( prop.getProperty("server-ip").isNotEmpty() ){
                IP = prop.getProperty("server-ip")
                logInfo( "Config-IP is: $IP" )
            }
        }
    } catch (ex: IOException) {} catch (ex: FileNotFoundException) {}

    //GET IP / WEBSITE TIMEZONE
    if( IP == "127.0.0.1" ) {

        val ipv4Service = ipv4Services.get(((Math.random() * ipv4Services.size).toInt()))
        logInfo("Use following IP-Service: $ipv4Service")
        try {
            IP = BufferedReader(InputStreamReader(URL(ipv4Service).openStream())).readLine()
        } catch (ex: IOException) {} catch (ex: FileNotFoundException) {}

        logInfo("Public-IP is: $IP")
    }

    if( IP != "127.0.0.1" ){

        try {
            val URL = URL("http://ip-api.com/csv/$IP?fields=status,timezone&lang=en")
            val bufferedURL = BufferedReader(InputStreamReader(URL.openStream()))
            val line = bufferedURL.readLine()
            if ( line.contains("success,") ) {
                timezone = line.replace("success,","" )
            }
            bufferedURL.close()
        } catch (me: MalformedURLException) {} catch (ioe: IOException) {}

    }

    logInfo( "Set Timezone to: $timezone" )

    //Write Config
    try {
        val writer = BufferedWriter(FileWriter("test.txt"))
        FileUtils::class.java.classLoader.getResourceAsStream(configFileName).use { inputStream ->
            InputStreamReader(inputStream, StandardCharsets.UTF_8).use { streamReader ->
                BufferedReader(streamReader).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val currentLine = line.toString().replace("timezone: UTC", "timezone: $timezone" )
                        //logInfo( currentLine )
                        writer.write(currentLine + System.lineSeparator() )
                    }
                    writer.close()
                }
            }
        }
    } catch (e: IOException) {
        //e.printStackTrace()
    }
}