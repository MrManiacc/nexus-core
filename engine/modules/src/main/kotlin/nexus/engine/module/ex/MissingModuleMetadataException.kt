package nexus.engine.module.ex


/**
 * Exception for when metadata cannot be resolved for a module
 */
class MissingModuleMetadataException : RuntimeException {
    constructor() {}
    constructor(s: String) : super(s) {}
    constructor(s: String, throwable: Throwable) : super(s, throwable) {}
}
