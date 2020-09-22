package org.cvs.data.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

import org.cvs.data.entities.Qualification;

/**
 * 
 * Provides CRUD operations for {@link Qualification}.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
public interface QualificationRepository extends CrudRepository<Qualification, Long> {

	/**
	 * Returns an optional {@link Qualification} given its ID.
	 *
	 * @param id identifier to be used in the search criteria
	 * 
	 * @return an optional qualification instance that matches the search criteria
	 */
	Optional<Qualification> findById(Long id);

	/**
	 * Saves the given {@link Qualification}.
	 *
	 * @param qualification a qualification instance to be persisted in the database
	 * 
	 * @return the persisted qualification instance from the database
	 */
	<S extends Qualification> S save(S qualification);

	/**
	 * Returns all {@link Qualification} instances from the database.
	 *
	 * @param
	 * 
	 * @return	list of qualification instances from the database
	 */
	List<Qualification> findAll();

	/**
	 * Returns all {@link Qualification} instances that match a given name.
	 *
	 * @param name	a name to be used as a search parameter 
	 * 
	 * @return	list of qualification instances that match the search criteria
	 */
	List<Qualification> findAllByName(String name);

}
