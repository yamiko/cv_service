package org.cvs.application.exceptions;

/**
 * 
 * Supports throwing of of errors that arise from missing references.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@SuppressWarnings("serial")
public class EntryNotFoundException extends RuntimeException {

	/**
	 * Constructor for this class.
	 *
	 * @param message the error message
	 * 
	 */
	public EntryNotFoundException(String message) {
		super("Entry not found for :: " + message);
	}

}
