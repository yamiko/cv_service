package org.cvs.application.services;

import java.util.List;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.data.entities.WorkExperience;
import org.springframework.stereotype.Service;

/**
 * 
 * Provides service operations that can be carried out with work experiences.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@Service
public interface WorkExperienceService {

	/**
	 * Adds a new {@link WorkExperience} instance for an existing candidate in the
	 * database.
	 *
	 * @param workExperience a work experience instance to be added to the database
	 * 
	 * @return a newly added work experience instance
	 */
	public WorkExperience addWorkExperience(WorkExperience workExperience);

	/**
	 * Fetches a given active {@link WorkExperience} instance given its ID from the
	 * database.
	 *
	 * @param workExperienceId ID to be used as a key field during search
	 * 
	 * @return an active work experience instance that matches the search criteria
	 */
	public WorkExperience getActiveWorkExperience(Long workExperienceId)
	        throws EntryNotFoundException, EntryNotActiveException;

	/**
	 * Marks a given {@link WorkExperience} instance as deleted in the database.
	 *
	 * @param workExperienceId ID to be used as a key field during search
	 * 
	 */
	public void deleteWorkExperience(Long workExperienceId) throws EntryNotFoundException;

	/**
	 * Marks a given {@link WorkExperience} instance as retired in the database.
	 *
	 * @param workExperienceId ID to be used as a key field during search
	 * 
	 */
	public void retireWorkExperience(Long workExperienceId) throws EntryNotFoundException;

	/**
	 * Fetches all active {@link WorkExperience} instances from the database.
	 *
	 * @param
	 * 
	 * @return a list of all active work experience instances
	 */
	public List<WorkExperience> getWorkExperiences();
}
