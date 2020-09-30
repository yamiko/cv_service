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
import org.cvs.application.services.QualificationService;
import org.cvs.data.entities.Qualification;
import org.cvs.data.entities.Skill;

@Controller 
@RequestMapping(path = "/qualifications") 
public class QualificationController {

	@Autowired
	private QualificationService qualificationService;

	/**
	 * 
	 * Adds a new qualification to an existing candidate via POST through URL:
	 * <code>/qualifications</code>.
	 * <p>
	 * 
	 * Example payload:
	 * 
	 * <code> 
	 * {
	 *   "name": "PhD in Agroforestry",
	 *   "institution": "university of Texas",
	 *   "country": "USA",
	 *   "dateObtained": "2008-06-15",
	 *   "candidate": {"id": 111},
	 *   "qualificationType" : {"id": 98}
	 *  }
	 * </code>
	 * 
	 * @param qualification the qualification (can be a JSON payload) to be added to
	 *                      the system.
	 * 
	 * @return the newly added qualification
	 */
	@PostMapping(path = "")
	public @ResponseBody Qualification addNewApplicationQualification(@RequestBody Qualification qualification) {
		try {
			Qualification newqualification = qualificationService.addQualification(qualification);
			return newqualification;
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
	 * Fetches an active qualification via GET through URL:
	 * <code>/qualifications/active/{qualificationId}</code>.
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /qualifications/active/1
	 * </code>
	 * 
	 * @param qualificationId the qualification ID to be used in the query
	 * 
	 * @return an active qualification if found
	 */
	@GetMapping(path = "/active")
	public @ResponseBody Qualification getQualification(@PathVariable Long qualificationId) {
		try {
			Qualification qualification = qualificationService.getActiveQualification(qualificationId);
			return qualification;
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		} catch (EntryNotActiveException e) {
			throw new ResponseStatusException(HttpStatus.LOCKED, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Deletes a qualification via DELETE method through base URL:
	 * <code>/qualifications/{qualificationId}</code>.
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /qualifications/1
	 * </code>
	 * 
	 * @param qualificationId the qualification ID of the qualification to be
	 *                        deleted from the system.
	 * 
	 * @return a string that says 'Deleted'
	 * 
	 */
	@DeleteMapping(path = "/{qualificationId}")
	public @ResponseBody String deleteQualification(@PathVariable Long qualificationId) {
		try {
			qualificationService.deleteQualification(qualificationId);
			return "Deleted";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Retires a qualification via POST through URL:
	 * <code>/qualifications/retire/{qualificationId}</code>.
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /qualifications/retire/1
	 * </code>
	 * 
	 * @param qualificationId the ID of the qualification that is to be retired from
	 *                        the system.
	 * 
	 * @return a string that says 'Retired'
	 * 
	 */
	@PostMapping(path = "/retire/{qualificationId}")
	public @ResponseBody String retireQualification(@PathVariable Long qualificationId) {

		try {
			qualificationService.retireQualification(qualificationId);
			return "Retired";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}

	}

	/**
	 * 
	 * Fetches all active qualifications via GET through URL:
	 * <code>/qualifications</code>.
	 * 
	 * @return a list of all active qualifications
	 *
	 */
	@GetMapping(path = "")
	public @ResponseBody Iterable<Qualification> getAllQualifications() {
		// This returns a JSON or XML with the qualifications
		return qualificationService.getQualifications();
	}

	/**
	 * 
	 * Fetches all active qualification entries via GET through URL:
	 * <code>/qualifications/candidate/{candidateId}</code>.
	 * 
	 * @param candidateId the ID of the candidate ID to filter work experience
	 *                    entries for.
	 * 
	 * @return a list of all active qualifications entries for a particular
	 *         candidate
	 * 
	 */
	@GetMapping(path = "/candidate/{candidateId}")
	public @ResponseBody Iterable<Qualification> getQualifications(@PathVariable Long candidateId) {
		// This returns a JSON or XML with the workExperiences
		return qualificationService.getQualifications(candidateId);
	}

}
