package org.cvs.data.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

import org.cvs.data.entities.Candidate;

/**
 * 
 * Provides CRUD operations for {@link Candidate}.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
public interface CandidateRepository extends CrudRepository<Candidate, Long> {

	/**
	 * Returns an optional {@link Candidate} given its ID.
	 *
	 * @param id the identifier to be used as the search key
	 * 
	 * @return an optional candidate DTO that matches the search criteria
	 */
	Optional<Candidate> findById(Long id);

	/**
	 * Persists a given {@link Candidate} instance.
	 *
	 * @param candidate the candidate instance to be persisted in the database
	 * 
	 * @return the candidate that has been persisted in this operation
	 */
	<S extends Candidate> S save(S candidate);

	/**
	 * Persists and flushes a given {@link Candidate} instance.
	 *
	 * @param candidate the candidate instance to be persisted in the database
	 * 
	 * @return the candidate that has been persisted in this operation
	 */
	<S extends Candidate> S saveAndFlush(S candidate);

	/**
	 * Returns all {@link Candidate}s from the database.
	 *
	 * @param
	 * 
	 * @return list of all candidate instances from the database
	 */
	List<Candidate> findAll();

}
