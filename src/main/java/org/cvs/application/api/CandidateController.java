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
import org.cvs.application.exceptions.InconsistentDataException;
import org.cvs.application.services.CandidateService;
import org.cvs.application.services.PortfolioService;
import org.cvs.data.entities.Candidate;
import org.cvs.data.entities.Portfolio;
import org.cvs.data.entities.Skill;

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

	@Autowired
	private PortfolioService portfolioService;

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
	public @ResponseBody Candidate addNewCandidate(@RequestBody Candidate candidate) {
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
	 * Updates a new portfolio to an existing candidate via POST through URL:
	 * <code>/candidates/{candidateId}/portfolios/{portfolioId}</code>.
	 * <p>
	 * 
	 * Example URL: /candidates/1/portfolios/3
	 * 
	 * <p>
	 * 
	 * @param candidateId the ID of the user to associate with the portfolio
	 * @param portfolioId the ID of the portfolio to associate with the candidate
	 * 
	 * @return the newly associated portfolio
	 */
	@PostMapping(path = "/{candidateId}/portfolios/{portfolioId}")
	public @ResponseBody Portfolio updatePortfolioWithCandidate(@PathVariable Long candidateId,
	        @PathVariable Long portfolioId) {

		try {
			Portfolio updatedPortfolio = portfolioService.updatePortfolioWithCandidate(candidateId, portfolioId);
			return updatedPortfolio;
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
	 * Fetches an active candidate via GET through URL:
	 * <code>/candidates/active/{candidateId}</code>.
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /candidates/active/1
	 * </code>
	 * 
	 * @param candidateId the candidate ID as a request parameter to be used in the
	 *                    query
	 * 
	 * @return an active candidate if found
	 */
	@GetMapping(path = "/active/{candidateId}")
	public @ResponseBody Candidate getCandidate(@PathVariable Long candidateId) {
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
	 * <code>/candidates/{candidate1}</code>.
	 * <p>
	 * 
	 * Example payload:
	 * 
	 * <code> 
	 *  /candidates/1
	 * </code>
	 * 
	 * @param candidateId the ID of the candidate to be deleted from the system.
	 * 
	 * @return a string that says 'Deleted'
	 */
	@DeleteMapping(path = "/{candidateId}")
	public @ResponseBody String deleteCandidate(@PathVariable Long candidateId) {
		try {
			candidateService.deleteCandidate(candidateId);
			return "Deleted";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Retires a candidate via POST through URL:
	 * <code>/candidates/retire/{candidateId}</code>.
	 * <p>
	 * 
	 * Example payload:
	 * 
	 * <code> 
	 *  /candidates/retire/1
	 * </code>
	 * 
	 * @param candidateId the ID of the candidate to be retired from the system.
	 * 
	 * @return a string that says 'Retired'
	 */
	@PostMapping(path = "/retire/{candidateId}")
	public @ResponseBody String retireCandidate(@PathVariable Long candidateId) {

		try {
			candidateService.retireCandidate(candidateId);
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
	@GetMapping(path = "")
	public @ResponseBody Iterable<Candidate> getAllCandidates() {
		// This returns a JSON or XML with the candidates
		return candidateService.getCandidates();
	}

	/**
	 * 
	 * Fetches active candidates for a specific portfolio via GET through URL:
	 * <code>/candidates/portfolios/{portfolioId}</code>.
	 * 
	 * @param portfolioId the ID of the portfolio to filter candidates for
	 * 
	 * @return a list of all active candidates entries for a particular portfolio
	 * 
	 */
	@GetMapping(path = "/portfolio/{portfolioId}")
	public @ResponseBody Iterable<Candidate> getCandidates(@PathVariable Long portfolioId) {
		// This returns a JSON or XML with the workExperiences
		return candidateService.getCandidates(portfolioId);
	}

}
