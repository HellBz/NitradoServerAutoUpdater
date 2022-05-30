
fun main() {


    val currentLoader = net.nitrado.server.autoupdater.api.Base()

    currentLoader.baseurl = "https://api.atlauncher.com/v1/"

    currentLoader.cache = "atlauncher"

    val atlArray = arrayOf("packs", "full", "all")

    println( currentLoader.api(atlArray) )

}

