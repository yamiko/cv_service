package org.cvs.application.services;

import java.util.List;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.data.entities.Skill;
import org.springframework.stereotype.Service;

/**
 * 
 * Provides service operations that can be carried out with skills.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@Service
public interface SkillService {

	/**
	 * Adds a new {@link Skill} instance for an existing candidate in the database.
	 *
	 * @param skill a new skill field during search
	 * 
	 * @return a newly added skill instance
	 */
	public Skill addSkill(Skill skill);

	/**
	 * Fetches a given active {@link Skill} instance from the database.
	 *
	 * @param skillId ID to be used as a key field during search
	 * 
	 * @return a skill instance that matches the search criteria
	 */
	public Skill getActiveSkill(Long skillId) throws EntryNotFoundException, EntryNotActiveException;

	/**
	 * Marks a given {@link Skill} instance as deleted in the database.
	 *
	 * @param skillId ID to be used as a key field during search
	 * 
	 */
	public void deleteSkill(Long skillId) throws EntryNotFoundException;

	/**
	 * Marks a given {@link Skill} instance as retired in the database.
	 *
	 * @param skillId ID to be used as a key field during search
	 * 
	 */
	public void retireSkill(Long skillId) throws EntryNotFoundException;

	/**
	 * Fetches all active {@link Skill} instances from the database.
	 *
	 * @param
	 * 
	 * @return a list of all active skill instances
	 */
	public List<Skill> getSkills();

	/**
	 * Fetches all active {@link Skill} instances for a specified candidate from the
	 * database.
	 *
	 * @param candidateId the ID of the candidate to filter skills for
	 * 
	 * @return a list of all active skill instances
	 */
	public List<Skill> getSkills(Long candidateId);
}
