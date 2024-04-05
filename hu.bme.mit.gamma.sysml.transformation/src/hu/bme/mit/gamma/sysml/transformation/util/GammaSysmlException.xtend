package hu.bme.mit.gamma.sysml.transformation.util

class GammaSysmlException extends Exception {

	new() {}

	new(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace)
	}

	new(String message, Throwable cause) {
		super(message, cause)
	}

	new(String message) {
		super(message)
	}

	new(Throwable cause) {
		super(cause)
	}
	
}
