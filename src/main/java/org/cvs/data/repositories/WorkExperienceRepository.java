package org.cvs.data.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

import org.cvs.data.entities.WorkExperience;

/**
 * 
 * Provides CRUD operations for {@link WorkExperience}.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
public interface WorkExperienceRepository extends CrudRepository<WorkExperience, Long> {

	/**
	 * Returns an optional {@link WorkExperience} given its id.
	 *
	 * @param id ID field user as a key for the search operation
	 * 
	 * @return an optional work experience instance
	 */
	Optional<WorkExperience> findById(Long id);

	/**
	 * Persists the given {@link WorkExperience} instance to the database.
	 *
	 * @param workExperience work experience instance to be saved to the database
	 * 
	 * @return a work experience instance that was saved in the database
	 */
	<S extends WorkExperience> S save(S workExperience);

	/**
	 * Returns all {@link WorkExperience} instances.
	 *
	 * @param
	 * 
	 * @return a list of all work experience instances from the database
	 */
	List<WorkExperience> findAll();

}
