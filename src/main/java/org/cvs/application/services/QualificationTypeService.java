package org.cvs.application.services;

import java.util.List;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.data.entities.QualificationType;
import org.springframework.stereotype.Service;

/**
 * 
 * Provides service operations that can be carried out with qualification types.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@Service
public interface QualificationTypeService {
	/**
	 * Adds a new {@link QualificationType} instance.
	 *
	 * @param qualificationType a qualification type instance to be added
	 * 
	 * @return a newly added qualification type instance
	 */
	public QualificationType addQualificationType(QualificationType qualificationType);

	/**
	 * Fetches a {@link QualificationType} instance given its name.
	 *
	 * @param name parameter to be used as the search key
	 * 
	 * @return an qualification type instance that matches the search criteria
	 */
	public QualificationType getByQualificationTypeName(String name) throws EntryNotFoundException;

	/**
	 * Fetches an active {@link QualificationType} instance given its ID.
	 *
	 * @param qualificationTypeId ID to be used as a key field during search
	 * 
	 * @return a qualification type instance that matches the search criteria
	 */
	public QualificationType getActiveQualificationType(Long qualificationTypeId)
	        throws EntryNotFoundException, EntryNotActiveException;

	/**
	 * Marks a given {@link QualificationType} instance as deleted in the database.
	 *
	 * @param qualificationTypeId ID to be used as a key field during search
	 * 
	 */
	public void deleteQualificationType(Long qualificationTypeId) throws EntryNotFoundException;

	/**
	 * Marks a given {@link QualificationType} instance as retired in the database.
	 *
	 * @param qualificationTypeId ID to be used as a key field during search
	 * 
	 */
	public void retireQualificationType(Long qualificationTypeId) throws EntryNotFoundException;

	/**
	 * Fetches and returns all active {@link QualificationType} instances from the
	 * database.
	 *
	 * @param
	 * 
	 * @return list of all active qualification type instances
	 */
	public List<QualificationType> getQualificationTypes();
}
