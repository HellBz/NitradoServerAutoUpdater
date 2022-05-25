package net.nitrado.server.autoupdater.utils


class DataUtils {
}

fun basename(path: String): String? {
    var filename = path.substring(path.lastIndexOf('/') + 1)
    if (filename == null || filename.equals("", ignoreCase = true)) {
        filename = ""
    }
    return filename
}

fun getExt(fileName: String?): String? {
    requireNotNull(fileName) { "fileName must not be null!" }
    var extension: String? = ""
    val index = fileName.lastIndexOf('.')
    if (index > 0) {
        extension = fileName.substring(index + 1)
    }
    return extension
}

fun containsMulti(inputString: String, items: Array<String?>): Boolean {
    var found = true
    for (item in items) {
        if (!inputString.contains(item!!)) {
            found = false
            break
        }
    }
    return found
}

// with some null and length checking
fun capitalize(str: String?): String? {
    return if (str == null || str.length == 0) str else str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase()
}

fun doesClassExist(name: String?): Boolean {
    try {
        val c = Class.forName(name)
        if (c != null) return true
    } catch (e: ClassNotFoundException) {
        // Class not found
    } catch (e: NoClassDefFoundError) {
        // Class not found
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}