package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.AST;

public interface IParser {
	
	AST parse() throws PLCException;

	@SuppressWarnings("serial")
	class PLCRuntimeException extends RuntimeException {

		public PLCRuntimeException() {
			super();
		}

		public PLCRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}

		public PLCRuntimeException(String message, Throwable cause) {
			super(message, cause);
		}

		public PLCRuntimeException(String message) {
			super(message);
		}

		public PLCRuntimeException(Throwable cause) {
			super(cause);
		}



	}
}
