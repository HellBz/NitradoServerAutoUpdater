package net.nitrado.server.autoupdater.utils

import mainDir
import modDownloadFile
import sun.rmi.runtime.Log
import java.io.*
import java.net.URL
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


/**
 * This utility extracts files and directories of a standard zip file to
 * a destination directory.
 * @author www.codejava.net
 */
class ZipUtility {
    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    @Throws(IOException::class)
    fun unzip(zipFilePath: String?, destDirectory: String) {
        val destDir = File(destDirectory)
        if (!destDir.exists()) {
            destDir.mkdir()
        }
        val zipIn = ZipInputStream(FileInputStream(zipFilePath))
        var entry = zipIn.nextEntry
        // iterates over entries in the zip file
        while (entry != null) {
            val filePath = destDirectory + File.separator + entry.name
            if (!entry.isDirectory) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath)
            } else {
                // if the entry is a directory, make the directory
                val dir = File(filePath)
                dir.mkdirs()
            }
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
        zipIn.close()
    }

    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun extractFile(zipIn: ZipInputStream, filePath: String) {
        val bos = BufferedOutputStream(FileOutputStream(filePath))
        val bytesIn = ByteArray(BUFFER_SIZE)
        var read = 0
        while (zipIn.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }

    companion object {
        /**
         * Size of the buffer to read/write data
         */
        private const val BUFFER_SIZE = 4096
    }


}

fun listZipContents(fileName: String): MutableList<String> {
    var zipFile: ZipFile? = null
    val zipContent: MutableList<String> = ArrayList()
    val file = File(fileName)
    try {
        zipFile = ZipFile(file)
        val entries = zipFile.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val entryName = entry.name
            logInfo("Add to Zip: $entryName")
            zipContent.add(entryName)
        }
        zipFile.close()
    } catch (ioException: IOException) {
        println("Error opening zip file$ioException")
    }
    return zipContent
}

fun readZipFileFromRemote(remoteFileUrl: String?): MutableList<String> {
    val sb = StringBuilder()
    val zipContent: MutableList<String> = java.util.ArrayList()
    try {
        val url = URL(remoteFileUrl)
        val `in`: InputStream = BufferedInputStream(url.openStream(), 1024)
        val stream = ZipInputStream(`in`)

        var entry: ZipEntry

        while ((stream.nextEntry.also { entry = it }) != null)
        {
            val name = entry.toString()
            println("Zip-Entry: $name")
            zipContent.add( name.toString() )
        }


    } catch (e: Exception) {
        e.printStackTrace()
    }
    //return sb.toString();
    return zipContent
}


object ZipFileExample {
    private const val INPUT_FOLDER = "\\"
    private const val ZIPPED_FOLDER = "backup.zip"

    private val jarPath: String = FileUntils::class.java.protectionDomain.codeSource.location.toURI().path
    private val jarName: String = jarPath.substring(jarPath.lastIndexOf("/") + 1)

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            zip(INPUT_FOLDER, ZIPPED_FOLDER)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun zip(inputFolder: String?, targetZippedFolder: String?) {
        var fileOutputStream: FileOutputStream? = null
        fileOutputStream = FileOutputStream(targetZippedFolder)
        val zipOutputStream = ZipOutputStream(fileOutputStream)
        val inputFile = File(inputFolder)
        if (inputFile.isFile)
            zipFile(inputFile, "", zipOutputStream)
        else if (inputFile.isDirectory)
            zipFolder( zipOutputStream, inputFile,"")
        zipOutputStream.close()
    }

    @Throws(IOException::class)
    fun zipFolder(zipOutputStream: ZipOutputStream, inputFolder: File, parentName: String) {
        val myname = parentName + inputFolder.name + "\\"
        val folderZipEntry = ZipEntry(myname)
        zipOutputStream.putNextEntry(folderZipEntry)
        val contents = inputFolder.listFiles()
        for (f in contents) {
            // if ( !containsMulti( f.toString(), forbittenFiles ) ) {
            if  ( !f.toString().contains( jarName ) &&
                !f.toString().contains( mainDir ) &&
                !f.toString().contains( modDownloadFile )
            )
            {
                if (f.isFile) zipFile(f, myname, zipOutputStream) else if (f.isDirectory) zipFolder(zipOutputStream,
                    f,
                    myname)
            }
        }
        zipOutputStream.closeEntry()
    }

    @Throws(IOException::class)
    fun zipFile(inputFile: File, parentName: String, zipOutputStream: ZipOutputStream) {

        // A ZipEntry represents a file entry in the zip archive
        // We name the ZipEntry after the original file's name
        val zipEntry = ZipEntry(parentName + inputFile.name)
        zipOutputStream.putNextEntry(zipEntry)
        val fileInputStream = FileInputStream(inputFile)
        val buf = ByteArray(1024)
        var bytesRead: Int

        // Read the input file by chucks of 1024 bytes
        // and write the read bytes to the zip stream
        while (fileInputStream.read(buf).also { bytesRead = it } > 0) {
            zipOutputStream.write(buf, 0, bytesRead)
        }

        // close ZipEntry to store the stream to the file
        zipOutputStream.closeEntry()
        logInfo("Backup File :" + parentName + inputFile.name + " to :" + ZIPPED_FOLDER)
    }
}

fun unzip(zipFilePath: String?, uncompressedDirectory: String) {
    //Open the file
    try {
        ZipFile("$zipFilePath").use { file ->
            val fileSystem: FileSystem? = FileSystems.getDefault()
            //Get file entries
            val entries = file.entries()

            val mainDir: Path = fileSystem!!.getPath(uncompressedDirectory)
            if ( !Files.exists( mainDir ) ) Files.createDirectory( fileSystem!!.getPath(uncompressedDirectory))


            //Iterate over entries
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                //If directory then create a new directory in uncompressed folder
                if (entry.isDirectory) {
                    logInfo("Zipping: Create Directory: " + uncompressedDirectory + entry.name)
                    Files.createDirectories(fileSystem!!.getPath(uncompressedDirectory + entry.name))
                } else {

                    val `is` = file.getInputStream(entry)
                    val bis = BufferedInputStream(`is`)

                    val uncompressedFileName = uncompressedDirectory + entry.name
                    val uncompressedFilePath: Path = fileSystem!!.getPath(uncompressedFileName)

                    val baseFilePath = basename( uncompressedFileName )
                    val uncompressedDirPath: Path = fileSystem!!.getPath(uncompressedFileName.replace( baseFilePath.toString(),""))

                    if ( !Files.exists( uncompressedDirPath )) {
                        Files.createDirectories( uncompressedDirPath )
                        println("Zipping: CreateDirectory before File: " + uncompressedFileName.replace( baseFilePath.toString(),"") )
                    }

                    try {
                        if ( !Files.exists( uncompressedFilePath )) Files.createFile(uncompressedFilePath)
                    } catch (e: NoSuchFileException) {
                        Files.createDirectories(uncompressedFilePath)
                    } catch (e: FileAlreadyExistsException) {
                        println(e)
                    }

                    val fileOutput = FileOutputStream(uncompressedFileName)
                    while (bis.available() > 0) {
                        fileOutput.write(bis.read())
                    }
                    fileOutput.close()
                    println("Zipping: File: " + entry.name)
                }
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

class DecompressFast(private val _zipFile: String, private val _location: String) {
    fun unzip() {
        try {
            val fin = FileInputStream(_zipFile)
            val zin = ZipInputStream(fin)
            var ze: ZipEntry? = null
            while (zin.nextEntry.also { ze = it } != null) {

                if (ze!!.isDirectory) {
                    _dirChecker(ze!!.name)
                    logInfo("Unzipping Directory: " + ze!!.name)
                } else {
                    logInfo("Unzipping File: " + ze!!.name)
                    val baseFilePath = basename(ze!!.name)
                    val outDirPath = ze!!.name.replace( baseFilePath.toString() , "")
                    _dirChecker(outDirPath)

                    val fout = FileOutputStream(_location + ze!!.name)
                    val bufout = BufferedOutputStream(fout)
                    val buffer = ByteArray(1024)
                    var read = 0
                    while (zin.read(buffer).also { read = it } != -1) {
                        bufout.write(buffer, 0, read)
                    }
                    bufout.close()
                    zin.closeEntry()
                    fout.close()
                }
            }
            zin.close()
            logInfo("Unzip Unzipping complete. path :  $_location")
        } catch (e: Exception) {
            logError("Decompress unzip $e")
            logError("Unzip Unzipping failed")
        }
    }

    private fun _dirChecker(dir: String) {
        val f = File(_location + dir)
        if (!f.isDirectory) {
            f.mkdirs()
        }
    }

    init {
        _dirChecker("")
    }
}