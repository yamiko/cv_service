package org.cvs.application.exceptions;

/**
 * 
 * Supports throwing of errors that arise from accessing inactive database entries.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@SuppressWarnings("serial")
public class EntryNotActiveException extends RuntimeException {

	/**
	 * Constructor for this class.
	 *
	 * @param message the error message 
	 * 
	 */	
	public EntryNotActiveException(String message) {
		super("Entry not found for :: " + message);
	}

}
