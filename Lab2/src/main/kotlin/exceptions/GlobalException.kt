package exceptions

open class GlobalException(message: String, cause: Throwable? = null) : Exception(message, cause)
