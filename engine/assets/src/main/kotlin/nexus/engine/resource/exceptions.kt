package nexus.engine.resource

/**
 * Thrown to indicate the name of an asset file doesn't meet the necessary structure to derive the corresponding asset name.
 *
 * @author Immortius
 */
class InvalidAssetFilenameException : Exception {
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

/**
 * Thrown to indicate a string is not a valid ResourceUrn.
 *
 * @author Immortius
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
