package exceptions

class NoDataException(message: String, cause: Throwable? = null) : GlobalException(message, cause)
