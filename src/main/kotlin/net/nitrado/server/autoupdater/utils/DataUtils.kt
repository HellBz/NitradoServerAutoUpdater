package net.nitrado.server.autoupdater.utils

import java.util.*


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