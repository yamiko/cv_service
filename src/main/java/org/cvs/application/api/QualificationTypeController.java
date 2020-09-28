package org.cvs.application.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolationException;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.application.services.QualificationTypeService;
import org.cvs.data.entities.QualificationType;

@Controller
@RequestMapping(path = "/qualifications/types")
public class QualificationTypeController {

	@Autowired
	private QualificationTypeService qualificationTypeService;

	/**
	 * 
	 * Adds a new qualification types candidate via POST through URL:
	 * <code>/qualifications/types</code>.
	 * <p>
	 * 
	 * Example payload:
	 * 
	 * <code> 
	 * {
	 *   "name": "Bachelors Degree"
	 * }
	 * </code>
	 * 
	 * @param qualificationType the qualification type (can be a JSON payload) to be
	 *                          added to the system.
	 * 
	 * @return the newly added qualification type
	 */
	@PostMapping(path = "")
	public @ResponseBody QualificationType addNewApplicationQualificationType(
	        @RequestBody QualificationType qualificationType) {
		try {
			QualificationType newqualificationType = qualificationTypeService.addQualificationType(qualificationType);
			return newqualificationType;
		} catch (ConstraintViolationException e) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage(), e);
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		} catch (EntryNotActiveException e) {
			throw new ResponseStatusException(HttpStatus.LOCKED, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Fetches an active qualificationType via GET through URL:
	 * <code>/qualification/types/active/{qualificationTypeId}</code>.
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /qualification/types/active/{qualificationTypeId}
	 * </code>
	 * 
	 * @param qualificationTypeId the qualificationType ID as a request parameter to
	 *                            be used in the query
	 * 
	 * @return an active qualificationType if found
	 */
	@GetMapping(path = "/active/{qualificationTypeId}")
	public @ResponseBody QualificationType getQualificationType(@PathVariable Long qualificationTypeId) {
		try {
			QualificationType qualificationType = qualificationTypeService
			        .getActiveQualificationType(qualificationTypeId);
			return qualificationType;
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		} catch (EntryNotActiveException e) {
			throw new ResponseStatusException(HttpStatus.LOCKED, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Deletes a qualification type via DELETE method through URL:
	 * <code>/qualifications/types/{qualificationTypeId}</code>.
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /qualification/types/1
	 * </code>
	 * 
	 * @param qualificationTypeId the ID of the qualification type (can be a JSON
	 *                            payload) to be deleted from the system.
	 * 
	 * @return a string that says 'Deleted'
	 * 
	 */
	@DeleteMapping(path = "/{qualificationTypeId}")
	public @ResponseBody String deleteQualificationType(@PathVariable Long qualificationTypeId) {
		try {
			qualificationTypeService.deleteQualificationType(qualificationTypeId);
			return "Deleted";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Retires an qualification type via POST through URL:
	 * <code>/qualifications/types/retire/{qualificationTypeId}</code>.
	 * <p>
	 * 
	 * Example payload:
	 * 
	 * <code> 
	 *  /qualification/types/retire/{qualificationTypeId}
	 * </code>
	 * 
	 * @param qualificationTypeId the ID of the qualification type to be retired
	 *                            from the system.
	 * 
	 * @return a string that says 'Retired'
	 */
	@PostMapping(path = "/retire/{qualificationTypeId}")
	public @ResponseBody String retireQualificationType(@PathVariable Long qualificationTypeId) {

		try {
			qualificationTypeService.retireQualificationType(qualificationTypeId);
			return "Retired";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}

	}

	/**
	 * 
	 * Fetches all active qualification types via GET through URL:
	 * <code>/qualifications</code>.
	 * 
	 * @return a list of all active qualification types in JSON or XML depending on
	 *         client preferences
	 * 
	 */
	@GetMapping(path = "")
	public @ResponseBody Iterable<QualificationType> getAllQualificationTypes() {
		// This returns a JSON or XML with the qualificationTypes
		return qualificationTypeService.getQualificationTypes();
	}
}
