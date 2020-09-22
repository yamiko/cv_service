package org.cvs.application.exceptions;

/**
 * 
 * Supports throwing of user defined data consistency errors.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@SuppressWarnings("serial")
public class InconsistentDataException extends RuntimeException {

	/**
	 * Constructor for this class.
	 *
	 * @param message the error message 
	 * 
	 */
	public InconsistentDataException(String message) {
		super("Inconsistent data :: " + message);
	}

}
