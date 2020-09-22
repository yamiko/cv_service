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
import org.cvs.application.services.CandidateService;
import org.cvs.data.entities.Candidate;

/**
 * 
 * REST service endpoint for <b>candidate</b> resources on <code>/users</code>.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@Controller
@RequestMapping(path = "/candidates")
public class CandidateController {

	@Autowired
	private CandidateService candidateService;

	/**
	 * 
	 * Adds a new candidate to an optional portfolio via POST through URL:
	 * <code>/candidates</code>.
	 * <p>
	 * 
	 * Example payload:
	 * 
	 * <code> 
	 *{
	 * "title": "Mr",
	 *   "firstName" : "Test",
	 *   "middleName" : "",
	 *   "lastName" : "Candidate",
	 *   "gender" : "F", 
	 *   "email" : "test@mail.com", 
	 *   "preferredContactNumber" : "08934514355",
	 *	 "alternativeContactNumber" : "", 
	 *   "addressLine1" : "56 Test Road", 
	 *   "addressLine2" : "London",
	 *   "addressLine3" : "", 
	 *   "postcode" : "SE5 3SD", 
	 *   "country": "UK",
	 *   "dateOfBirth": "1995-05-26",
	 *   "portfolio":[{
	 *       "id": 136
	 *   }]
	 * }
	 * </code>
	 * 
	 * @param candidate the candidate (can be a JSON payload) to be added to the
	 *                  system.
	 * 
	 * @return the newly added candidate
	 */
	@PostMapping(path = "")
	public @ResponseBody Candidate addNewApplicationCandidate(@RequestBody Candidate candidate) {
		try {
			Candidate newcandidate = candidateService.addCandidate(candidate);
			return newcandidate;
		} catch (ConstraintViolationException e) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage(), e);
		} catch (InconsistentDataException e) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage(), e);
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		} catch (EntryNotActiveException e) {
			throw new ResponseStatusException(HttpStatus.LOCKED, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Fetches an active candidate via GET through URL:
	 * <code>/candidates/active</code>.
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /candidates/active?userId=1
	 * </code>
	 * 
	 * @param candidateId the candidate ID as a request parameter to be used in the
	 *                    query
	 * 
	 * @return an active candidate if found
	 */
	@GetMapping(path = "/active")
	public @ResponseBody Candidate getCandidate(@RequestParam Long candidateId) {
		try {
			Candidate candidate = candidateService.getActiveCandidate(candidateId);
			return candidate;
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		} catch (EntryNotActiveException e) {
			throw new ResponseStatusException(HttpStatus.LOCKED, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Deletes a candidate via DELETE method through base URL:
	 * <code>/candidates</code>.
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
	 * @param candidate the candidate (can be a JSON payload) to be deleted from the
	 *                  system.
	 * 
	 * @return a string that says 'Deleted'
	 */
	@DeleteMapping(path = "")
	public @ResponseBody String deleteCandidate(@RequestBody Candidate candidate) {
		try {
			candidateService.deleteCandidate(candidate.getId());
			return "Deleted";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Retires a candidate via POST through URL: <code>/candidates/retire</code>.
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
	 * @param candidate the candidate (can be a JSON payload) to be retired from the
	 *                  system.
	 * 
	 * @return a string that says 'Retired'
	 */
	@PostMapping(path = "/retire")
	public @ResponseBody String retireCandidate(@RequestBody Candidate candidate) {

		try {
			candidateService.retireCandidate(candidate.getId());
			return "Retired";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}

	}

	/**
	 * 
	 * Fetches all active candidates via GET through URL:
	 * <code>/candidates/all</code>.
	 * 
	 * 
	 */
	@GetMapping(path = "/all")
	public @ResponseBody Iterable<Candidate> getAllCandidates() {
		// This returns a JSON or XML with the candidates
		return candidateService.getCandidates();
	}

}
