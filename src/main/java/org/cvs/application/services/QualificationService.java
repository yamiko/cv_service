package org.cvs.application.services;

import java.util.List;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.data.entities.Qualification;
import org.cvs.data.entities.Candidate;
import org.springframework.stereotype.Service;

/**
 * 
 * Provides service operations that can be carried out with qualifications for
 * specific candidates.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@Service
public interface QualificationService {

	/**
	 * Adds a {@link Qualification} to an existing {@link Candidate} instance.
	 *
	 * @param qualification a new qualification that has a valid reference to an
	 *                      existing candidate
	 * 
	 * @return a qualification that has been added to a candidates CV
	 */
	public Qualification addQualification(Qualification qualification)
	        throws EntryNotFoundException, EntryNotActiveException;

	/**
	 * Gets an active {@link Qualification} instance given its identifier.
	 *
	 * @param qualificationId an identifier to be used in the search criteria
	 * 
	 * @return an active qualification if found
	 */
	public Qualification getActiveQualification(Long qualificationId)
	        throws EntryNotFoundException, EntryNotActiveException;

	/**
	 * Marks a given {@link Qualification} instance as deleted in the database.
	 *
	 * @param qualificationId an identifier to be used in the search criteria
	 * 
	 */
	public void deleteQualification(Long qualificationId) throws EntryNotFoundException;

	/**
	 * Marks a given {@link Qualification} instance as retired, to bar it from
	 * future usage.
	 *
	 * @param qualificationId an identifier to be used in the search criteria
	 * 
	 */
	public void retireQualification(Long qualificationId) throws EntryNotFoundException;

	/**
	 * Lists all active {@link Qualification} instances from the database.
	 *
	 * @param
	 * 
	 * @return a list of active qualification instances
	 */
	public List<Qualification> getQualifications();

	/**
	 * Lists all active {@link Qualification} instances for a specified candidate
	 * from the database.
	 *
	 * @param candidateId the candidate to filter qualification instances for
	 * 
	 * @return a list of active qualification instances
	 */
	public List<Qualification> getQualifications(Long candidateId);
}
