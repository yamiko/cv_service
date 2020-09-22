package org.cvs.data.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

import org.cvs.data.entities.Reference;

/**
 * 
 * Provides CRUD operations for {@link Reference}.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
public interface ReferenceRepository extends CrudRepository<Reference, Long> {

	/**
	 * Returns an optional {@link Reference} given its ID.
	 *
	 * @param id	the identifier to be used as a key in the search criteria
	 * 
	 * @return	a reference instance that matches a search criteria from the database 
	 */
	Optional<Reference> findById(Long id);

	/**
	 * Saves the given {@link Reference} instance to the database.
	 *
	 * @param reference	a reference instance to be saved in the database
	 * 
	 * @return	a reference instance that was saved in the database
	 */
	<S extends Reference> S save(S reference);

	/**
	 * Returns all {@link Reference} instances from the database.
	 *
	 * @param
	 * 
	 * @return	a list of all reference instances from the database
	 */
	List<Reference> findAll();
}
