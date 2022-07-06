import net.nitrado.server.autoupdater.utils.downloadFile
import net.nitrado.server.autoupdater.utils.installCurseLoader

class TestInstaller {
}

fun main (args: Array<String>) {

    //downloadFile( "https://maven.minecraftforge.net/net/minecraftforge/forge/1.18.2-40.1.25/forge-1.18.2-40.1.25-installer.jar"  , "F:\\Benutzerdaten\\Downloads\\forgetest\\forge-1.18.2-40.1.25-installer.jar"  )

    //installCurseLoader("F:\\Benutzerdaten\\Downloads\\forgetest\\forge-1.18.2-40.1.25-installer.jar")

    var version = "1.16.5-36.2.33"

    downloadFile( "https://maven.minecraftforge.net/net/minecraftforge/forge/$version/forge-$version-installer.jar"  , "F:\\Benutzerdaten\\Downloads\\forgetest\\forge-$version-installer.jar"  )

    installCurseLoader("F:\\Benutzerdaten\\Downloads\\forgetest\\forge-$version-installer.jar")
}