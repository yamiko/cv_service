package org.cvs.data.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

import org.cvs.data.entities.Skill;

/**
 * 
 * Provides CRUD operations for {@link Skill}.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
public interface SkillRepository extends CrudRepository<Skill, Long> {

	/**
	 * Returns an optional {@link Skill} given its ID.
	 *
	 * @param id key to be used in the search criteria
	 * 
	 * @return an optional skill instance from the database that matches the search
	 *         criteria
	 */
	Optional<Skill> findById(Long id);

	/**
	 * Saves the given {@link Skill} instance.
	 *
	 * @param skill a skill instance to be persisted in the database
	 * 
	 * @return an instance of skill that was saved in the database
	 */
	<S extends Skill> S save(S skill);

	/**
	 * Returns all {@link Skill} instances from the database.
	 *
	 * @param
	 * 
	 * @return a list of all skill instances from the database
	 */
	List<Skill> findAll();

}
