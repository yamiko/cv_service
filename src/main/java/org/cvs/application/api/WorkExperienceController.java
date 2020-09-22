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

import javax.validation.ConstraintViolationException;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.application.exceptions.InconsistentDataException;
import org.cvs.application.services.WorkExperienceService;
import org.cvs.data.entities.WorkExperience;

@Controller	
@RequestMapping(path="/experiences") 
public class WorkExperienceController {
	
	@Autowired
	private WorkExperienceService workExperienceService;

	/**
	 * 
	 * Adds a new work experience entry to an existing candidate via POST through URL:
	 * <code>/qualifications</code>.
	 * <p>
	 * 
	 * Example payload:
	 * 
	 * <code> 
	 * {
     *   "organisation": "SADF tkints",
     *   "position": "Test one",
     *   "country": "UK",
     *   "startDate": "2012-06-15",
     *   "endDate": "2017-09-15",
     *   "candidate": {"id": 111}
     * }
	 * </code>
	 * 
	 * @param workExperience the work experience entry (can be a JSON payload) to be added to the
	 *                  system.
	 * 
	 * @return the newly added work experience entry
	 */	
	@PostMapping(path = "")
	public @ResponseBody WorkExperience addNewApplicationWorkExperience(@RequestBody WorkExperience workExperience) {
		try {
			WorkExperience newworkExperience = workExperienceService.addWorkExperience(workExperience);
			return newworkExperience;
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		} catch (EntryNotActiveException e) {
			throw new ResponseStatusException(HttpStatus.LOCKED, e.getMessage(), e);
		} catch (InconsistentDataException e) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage(), e);
		} catch (ConstraintViolationException e) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Fetches an active work experience entry via GET through URL:
	 * <code>/experiences/active</code>.
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /experiences/active?userId=1
	 * </code>
	 * 
	 * @param workExperienceId the work experience ID as a request parameter to be used in the
	 *                    query
	 * 
	 * @return an active work experience entry if found
	 */
	@GetMapping(path = "/active")
	public @ResponseBody WorkExperience getWorkExperience(@RequestParam Long workExperienceId) {
		try {
			WorkExperience workExperience = workExperienceService.getActiveWorkExperience(workExperienceId);
			return workExperience;
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		} catch (EntryNotActiveException e) {
			throw new ResponseStatusException(HttpStatus.LOCKED, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Deletes a work experience entry via DELETE method through base URL: <code>/experiences</code>.
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
	 * @param workExperience the work experience entry (can be a JSON payload) to be deleted from the system.
	 * 
	 * @return a string that says 'Deleted'
	 * 
	 */		
	@DeleteMapping(path = "")
	public @ResponseBody String deleteWorkExperience(@RequestBody WorkExperience workExperience) {
		try {
			workExperienceService.deleteWorkExperience(workExperience.getId());
			return "Deleted";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Retires a work experience entry via POST through URL: <code>/experiences/retire</code>.
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
	 * @param workExperience the work experience entry (can be a JSON payload) to be retired from the system.
	 * 
	 * @return a string that says 'Retired'
	 */
	@PostMapping(path = "/retire")
	public @ResponseBody String retireWorkExperience(@RequestBody WorkExperience workExperience) {

		try {
			workExperienceService.retireWorkExperience(workExperience.getId());
			return "Retired";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}

	}

	/**
	 * 
	 * Fetches all active work experience entries via GET through URL:
	 * <code>/experiences/all</code>.
	 * 
	 * @return a list of all active work experience entries in JSON or XML 
	 * 
	 */
	@GetMapping(path = "/all")
	public @ResponseBody Iterable<WorkExperience> getAllWorkExperiences() {
		// This returns a JSON or XML with the workExperiences
		return workExperienceService.getWorkExperiences();
	}

}
