package org.cvs.data.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

import org.cvs.data.entities.Portfolio;

/**
 * 
 * Provides CRUD operations for {@link Portfolio}.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
public interface PortfolioRepository extends CrudRepository<Portfolio, Long> {

	/**
	 * Returns an optional {@link Portfolio} given its ID.
	 *
	 * @param id the identifier to be used as a search key
	 * 
	 * @return an optional portfolio DTO that matches the search criteria
	 */
	Optional<Portfolio> findById(Long id);

	/**
	 * Persists a given {@link Portfolio} DTO to the database.
	 *
	 * @param portfolio the portfolio DTO to be persisted
	 * 
	 * @return the portfolio instance that has been persisted in the database
	 */
	<S extends Portfolio> S save(S portfolio);

	/**
	 * Returns all {@link Portfolio}s that match a given name.
	 *
	 * @param name name to be used in the search criteria
	 * 
	 * @return list of portfolio instances that match the search criteria
	 */
	List<Portfolio> findAllByName(String name);

	/**
	 * Returns all {@link Portfolio}s.
	 *
	 * @param
	 * 
	 * @return list of all portfolio instances from the database
	 */
	List<Portfolio> findAll();

}
