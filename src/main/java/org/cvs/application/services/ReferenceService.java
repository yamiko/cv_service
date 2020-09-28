package org.cvs.application.services;

import java.util.List;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.data.entities.Reference;
import org.springframework.stereotype.Service;

/**
 * 
 * Provides service operations that can be carried out with references.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@Service
public interface ReferenceService {

	/**
	 * Adds a new {@link Reference} instance to the the database.
	 *
	 * @param reference a reference to added
	 * 
	 * @return a newly added reference instance
	 */
	public Reference addReference(Reference reference);

	/**
	 * Fetches an active {@link Reference} instance from the database.
	 *
	 * @param referenceId ID to be used as a key field during search
	 * 
	 * @return a reference instance that matches the search criteria
	 */
	public Reference getActiveReference(Long referenceId) throws EntryNotFoundException, EntryNotActiveException;

	/**
	 * Marks a given {@link Reference} instance as deleted in the database.
	 *
	 * @param referenceId ID to be used as a key field during search
	 * 
	 */
	public void deleteReference(Long referenceId) throws EntryNotFoundException;

	/**
	 * Marks a given {@link Reference} instance as retired in the database.
	 *
	 * @param referenceId ID to be used as a key field during search
	 * 
	 */
	public void retireReference(Long referenceId) throws EntryNotFoundException;

	/**
	 * Fetches all active {@link Reference} instances from the database.
	 *
	 * @param
	 * 
	 * @return a list of all active references in the database
	 */
	public List<Reference> getReferences();

	/**
	 * Fetches all active {@link Reference} instances for a specified candidate from
	 * the database.
	 *
	 * @param candidateId the ID of the candidate to filter reference entries for
	 * 
	 * @return a list of all active references in the database
	 */
	public List<Reference> getReferences(Long candidateId);
}
