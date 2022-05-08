package net.nitrado.server.autoupdater.api

open class Base {

    open fun API(){}

    open fun latestVersion(): Any = Unit

    open fun latestFile(): Any = Unit

    open fun latestGet(): Any = Unit

    open fun latestServer(): Any = Unit
}