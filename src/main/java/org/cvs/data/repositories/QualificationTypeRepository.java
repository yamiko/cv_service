package org.cvs.data.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

import org.cvs.data.entities.QualificationType;

/**
 * 
 * Provides CRUD operations for {@link QualificationType}.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
public interface QualificationTypeRepository extends CrudRepository<QualificationType, Long> {

	/**
	 * Returns an optional {@link QualificationType} given its ID.
	 *
	 * @param id	the identifier to be used as a key in the search criteria
	 * 
	 * @return	an optional qualification type instance that matches the search criteria
	 */
	Optional<QualificationType> findById(Long id);

	/**
	 * Saves a given {@link QualificationType} instance in to the database.
	 *
	 * @param qualificationType	the qualification type instance to be saved in the database
	 * 
	 * @return	the qualification type instance that was saved in the database
	 */
	<S extends QualificationType> S save(S qualificationType);

	/**
	 * Returns all {@link QualificationType} instances from the database.
	 *
	 * @param
	 * 
	 * @return	list of qualification type instances from the database
	 */
	List<QualificationType> findAll();

	/**
	 * Returns all {@link QualificationType} instances that match a given name.
	 *
	 * @param name	a name to use as the key in the search criteria
	 * 
	 * @return	list of qualification type instances that match the search criteria
	 */
	List<QualificationType> findAllByName(String name);

}
