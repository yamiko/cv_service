package org.cvs.application.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.application.exceptions.InconsistentDataException;
import org.cvs.application.services.ApplicationUserService;
import org.cvs.data.entities.ApplicationUser;

/**
 * 
 * REST service endpoint for <b>application user</b> services available on
 * <code>/users</code>.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@Controller
@RequestMapping(path = "/users")
public class ApplicationUserController {

	@Autowired
	private ApplicationUserService userService;

	/**
	 * 
	 * Adds a new application user via POST through URL: <code>/users</code>.
	 * <p>
	 * 
	 * Example payload:
	 * 
	 * <code> 
	 * {
	 *     "fullName" : "New User",
	 *     "password" : "testpass",
	 *     "username" : "testuser"
	 * }
	 * </code>
	 * 
	 * @param user the user (can be a JSON payload) to be added to the system.
	 * 
	 * @return the newly added application user
	 */
	@PostMapping(path = "")
	public @ResponseBody ApplicationUser addNewApplicationUser(@RequestBody ApplicationUser user) {
		try {
			ApplicationUser newUser = userService.addUser(user);
			return newUser;
		} catch (InconsistentDataException e) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Fetches an active application user via GET through URL:
	 * <code>/users/active</code>.
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /users/active?userId=1
	 * </code>
	 * 
	 * @param userId the user ID as a request parameter to be used in the search
	 *               query
	 * 
	 * @return an active application user if found
	 */
	@GetMapping(path = "/active")
	public @ResponseBody ApplicationUser getUser(@RequestParam Long userId) {
		try {
			ApplicationUser user = userService.getActiveUser(userId);
			return user;
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		} catch (EntryNotActiveException e) {
			throw new ResponseStatusException(HttpStatus.LOCKED, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Deletes an application user via DELETE through base URL: <code>/users</code>.
	 * <p>
	 * 
	 * Example payload:
	 * 
	 * <code> 
	 * {
	 *     "id" : 1
	 * }
	 * </code>
	 * 
	 * @param user the user (can be a JSON payload) to be deleted from the system.
	 * 
	 * @return a string that says 'Deleted'
	 */
	@DeleteMapping(path = "")
	public @ResponseBody String deleteUser(@RequestBody ApplicationUser user) {
		try {
			userService.deleteUser(user.getId());
			return "Deleted";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Retires an application user via POST through URL: <code>/users/retire</code>.
	 * <p>
	 * 
	 * Example payload:
	 * 
	 * <code> 
	 * {
	 *     "id" : 1
	 * }
	 * </code>
	 * 
	 * @param user the user (can be a JSON payload) to be retired from the system.
	 * 
	 * @return a string that says 'Retired'
	 */
	@PostMapping(path = "/retire")
	public @ResponseBody String retireUser(@RequestBody ApplicationUser user) {

		try {
			userService.retireUser(user.getId());
			return "Retired";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}

	}

	/**
	 * 
	 * Fetches all active application users via GET through URL:
	 * <code>/users/all</code>.
	 * 
	 * @return a list of all active application users in JSON or XML depending on
	 *         client preferences
	 * 
	 */
	@GetMapping(path = "/all")
	public @ResponseBody Iterable<ApplicationUser> getAllUsers() {
		return userService.getUsers();
	}

}
