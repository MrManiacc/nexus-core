package nexus.engine.module.ex

/**
 * Thrown to indicate a string is not a valid ResourceUrn.
 *
 * 
 */
class InvalidUrnException : RuntimeException {
    constructor() {}
    constructor(message: String) : super(message) {}
    constructor(message: String, cause: Throwable) : super(message, cause) {}
    constructor(cause: Throwable) : super(cause) {}
    constructor(message: String, cause: Throwable, enableSuppression: Boolean, writableStackTrace: Boolean) : super(
        message,
        cause,
        enableSuppression,
        writableStackTrace) {
    }
}
