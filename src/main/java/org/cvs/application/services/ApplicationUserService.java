package org.cvs.application.services;

import java.util.List;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.data.entities.ApplicationUser;
import org.springframework.stereotype.Service;

/**
 * 
 * Provides service operations that can be carried out with application users.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@Service
public interface ApplicationUserService {

	/**
	 * Adds a new {@link ApplicationUser} instance.
	 *
	 * @param user a new application user instance to be added
	 * 
	 * @return an application user instance that has been added
	 */
	public ApplicationUser addUser(ApplicationUser user);

	/**
	 * Fetches a given active {@link ApplicationUser} instance from the database by
	 * username.
	 *
	 * @param userName an identifier to be used in the search criteria
	 * 
	 * @return an active application user instance from the database
	 */
	public ApplicationUser getByUsername(String userName) throws EntryNotFoundException;

	/**
	 * Fetches a given active {@link ApplicationUser} instance from the database by
	 * username and returns a default user for auditing publicly accessible resources.
	 *
	 * @param userName an identifier to be used in the search criteria
	 * 
	 * @return an active application user instance from the database
	 */
	public ApplicationUser getAlwaysByUsername(String userName);

	
	/**
	 * Fetches a given {@link ApplicationUser} instance from the database.
	 *
	 * @param id an identifier to be used in the search criteria
	 * 
	 * @return an application user instance if found
	 */
	public ApplicationUser getActiveUser(Long id) throws EntryNotFoundException, EntryNotActiveException;

	/**
	 * Marks a given {@link ApplicationUser} instance as deleted in the database.
	 *
	 * @param userId an identifier to be used in the search criteria
	 * 
	 */
	public void deleteUser(Long userId) throws EntryNotFoundException;

	/**
	 * Marks a given {@link ApplicationUser} instance as retired in the database.
	 *
	 * @param userId an identifier to be used in the search criteria
	 * 
	 */
	public void retireUser(Long userId) throws EntryNotFoundException;

	/**
	 * Fetches all active {@link ApplicationUser} instances from the database.
	 *
	 * @param
	 * 
	 * @return list of active application users from the database
	 */
	public List<ApplicationUser> getUsers();
}
