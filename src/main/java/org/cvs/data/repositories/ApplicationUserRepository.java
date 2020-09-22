package org.cvs.data.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import org.cvs.data.entities.ApplicationUser;

/**
 * 
 * Provides CRUD operations for {@link ApplicationUser}.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
public interface ApplicationUserRepository extends CrudRepository<ApplicationUser, Long> {

	/**
	 * Returns an optional {@link ApplicationUser} given its ID.
	 *
	 * @param id application user identifier for the search criteria
	 * 
	 * @return An optional {@link ApplicationUser} instance that matches the search
	 *         criteria
	 * 
	 */
	Optional<ApplicationUser> findById(Long id);

	/**
	 * Persists the given {@link ApplicationUser} in the database.
	 *
	 * @param applicationUser the application user to be persisted to the database.
	 * 
	 * @return the persisted application user instance
	 */
	<S extends ApplicationUser> S save(S applicationUser);

	/**
	 * Returns all {@link ApplicationUser}s.
	 *
	 * @param
	 * 
	 * @return a list of all application user instances from the database
	 */
	List<ApplicationUser> findAll();

	/**
	 * Returns an optional {@link ApplicationUser} given its userName;
	 *
	 * @param username username to be used for the search
	 * 
	 * @return an optional application user instance that matches the search
	 *         criteria
	 */
	Optional<ApplicationUser> findByUsername(String username);

	/**
	 * Returns all {@link ApplicationUser}s that match a given name.
	 *
	 * @param name username to be used for the search
	 * 
	 * @return a list of application user instances that match the search criteria
	 */
	List<ApplicationUser> findAllByUsername(String name);

	/**
	 * Returns all {@link ApplicationUser}s that match a given set of retired and
	 * voided flags;
	 *
	 * @param voidedFlag  void value to search for in the database
	 * @param retiredFlag retired value to search for in the database
	 * 
	 * @return a list of application user instances that match the search criteria
	 */
	@Query("SELECT a FROM ApplicationUser a WHERE a.voided = :voidedFlag AND a.retired = :retiredFlag")
	List<ApplicationUser> findByActiveFlags(@Param("voidedFlag") Integer voidedFlag,
	        @Param("retiredFlag") Integer retiredFlag);

	/**
	 * Returns all {@link ApplicationUser}s that match a given a username and a set
	 * of retired and voided flags;
	 *
	 * @param username    username for the application user to be used in the search
	 *                    criteria
	 * @param voidedFlag  void flag for the search criteria
	 * @param retiredFlag retired flag for the search criteria
	 * 
	 * @return a list of application user instances that match the search criteria
	 */
	@Query("SELECT a from ApplicationUser a WHERE a.username = :username AND a.voided = :voidedFlag AND a.retired = :retiredFlag")
	List<ApplicationUser> findByUsernameAndActiveFlags(@Param("username") String username,
	        @Param("voidedFlag") Integer voidedFlag, @Param("retiredFlag") Integer retiredFlag);

}
