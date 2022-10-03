package net.nitrado.server.autoupdater.utils

import java.lang.reflect.Constructor
import java.lang.reflect.Method


class DataUtils {
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

class JavaClassLoader : ClassLoader() {
    fun invokeClassMethod(classBinName: String?, methodName: String?) {
        try {

            // Create a new JavaClassLoader
            val classLoader = this.javaClass.classLoader

            // Load the target class using its binary name
            val loadedMyClass = classLoader.loadClass(classBinName)
            println("Loaded class name: " + loadedMyClass.name)

            // Create a new instance from the loaded class
            val constructor = loadedMyClass.getConstructor()
            val myClassObject: Any = constructor.newInstance()

            // Getting the target method from the loaded class and invoke it using its name
            val method: Method = loadedMyClass.getMethod(methodName)
            System.out.println("Invoked method name: " + method.getName())
            method.invoke(myClassObject)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}