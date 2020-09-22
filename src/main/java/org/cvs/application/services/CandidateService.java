package org.cvs.application.services;

import java.util.List;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.data.entities.Candidate;
import org.springframework.stereotype.Service;

/**
 * 
 * Provides service operations that can be carried out with candidates.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@Service
public interface CandidateService {

	/**
	 * Adds a new {@link Candidate} instance.
	 *
	 * @param candidate a candidate instance to add
	 * 
	 * @return a newly added candidate instance
	 */
	public Candidate addCandidate(Candidate candidate);

	/**
	 * Fetches a given {@link Candidate} instance if found and active.
	 *
	 * @param candidateId an identifier to be used in the search criteria
	 * 
	 * @return a candidate instance if found
	 */
	public Candidate getActiveCandidate(Long candidateId) throws EntryNotFoundException, EntryNotActiveException;

	/**
	 * Marks a given {@link Candidate} instance as deleted in the database.
	 *
	 * @param candidateId an identifier to be used in the search criteria
	 * 
	 */
	public void deleteCandidate(Long candidateId) throws EntryNotFoundException;

	/**
	 * Marks a given {@link Candidate} instance as retired in the database.
	 *
	 * @param candidateId an identifier to be used in the search criteria
	 * 
	 */
	public void retireCandidate(Long candidateId) throws EntryNotFoundException;

	/**
	 * Fetches all active {@link Candidate} instances from the database.
	 *
	 * @param
	 * 
	 * @return list of active candidate instances
	 */
	public List<Candidate> getCandidates();
}
