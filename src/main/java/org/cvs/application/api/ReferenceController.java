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
import org.cvs.application.services.ReferenceService;
import org.cvs.data.entities.Reference;
import org.cvs.data.entities.Skill;

@Controller
@RequestMapping(path = "/references")
public class ReferenceController {

	@Autowired
	private ReferenceService referenceService;

	/**
	 * 
	 * Adds a new reference to an existing candidate via POST through URL:
	 * <code>/references</code>.
	 * <p>
	 * 
	 * Example payload:
	 * 
	 * <code> 
	 *{
	 *   "name": "Tim Smith",
	 *   "jobTitle": "Sales Director",
	 *   "email": "pd@mail.com",
	 *   "institution": "University of Venda",
	 *   "country": "UK",
	 *   "contactNumber" : "08934514355",
	 *   "addressLine1" : "56 Barnet Street", 
	 *   "addressLine2" : "Cape Town",
	 *   "addressLine3" : "", 
	 *   "postcode" : "8800", 
	 *   "candidate": {"id": 111}
	 * }
	 * </code>
	 * 
	 * @param reference the reference (can be a JSON payload) to be added to the
	 *                  system.
	 * 
	 * @return the newly added reference
	 */
	@PostMapping(path = "")
	public @ResponseBody Reference addNewApplicationReference(@RequestBody Reference reference) {
		try {
			Reference newreference = referenceService.addReference(reference);
			return newreference;
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
	 * Fetches an active Reference via GET through URL:
	 * <code>/references/active/{referenceId}</code>.
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /references/active/1
	 * </code>
	 * 
	 * @param referenceId the reference ID as a request parameter to be used in the
	 *                    query
	 * 
	 * @return an active reference if found
	 */
	@GetMapping(path = "/active/{referenceId}")
	public @ResponseBody Reference getReference(@PathVariable Long referenceId) {
		try {
			Reference reference = referenceService.getActiveReference(referenceId);
			return reference;
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		} catch (EntryNotActiveException e) {
			throw new ResponseStatusException(HttpStatus.LOCKED, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Deletes a reference via DELETE method through base URL:
	 * <code>/references/{referenceId}</code>.
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /references/1
	 * </code>
	 * 
	 * @param referenceId the ID of the reference that is to be deleted from the
	 *                    system.
	 * 
	 * @return a string that says 'Deleted'
	 * 
	 */
	@DeleteMapping(path = "/{referenceId}")
	public @ResponseBody String deleteReference(@PathVariable Long referenceId) {
		try {
			referenceService.deleteReference(referenceId);
			return "Deleted";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Retires a reference via POST through URL:
	 * <code>/references/retire/{referenceId}</code>.
	 * <p>
	 * 
	 * Example payload:
	 * 
	 * <code> 
	 *  /references/retire/1
	 * </code>
	 * 
	 * @param referenceId the ID of the reference to be retired from the system.
	 * 
	 * @return a string that says 'Retired'
	 */
	@PostMapping(path = "/retire/{referenceId}")
	public @ResponseBody String retireReference(@PathVariable Long referenceId) {
		try {
			referenceService.retireReference(referenceId);
			return "Retired";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}

	}

	/**
	 * 
	 * Fetches all active qualification types via GET through URL:
	 * <code>/references</code>.
	 * 
	 * @return a list of all active qualification types in JSON or XML depending on
	 *         client preferences
	 * 
	 */
	@GetMapping(path = "")
	public @ResponseBody Iterable<Reference> getAllReferences() {
		// This returns a JSON or XML with the references
		return referenceService.getReferences();
	}

	/**
	 * 
	 * Fetches active reference entries for a particular candidate via GET through
	 * URL: <code>/references/candidate/{candidateId}</code>.
	 * 
	 * @param candidateId the ID of the candidate ID to filter work experience
	 *                    entries for.
	 * 
	 * @return a list of all active reference entries for a particular candidate
	 * 
	 */
	@GetMapping(path = "/candidate/{candidateId}")
	public @ResponseBody Iterable<Reference> getReferences(@PathVariable Long candidateId) {
		// This returns a JSON or XML with the workExperiences
		return referenceService.getReferences(candidateId);
	}

}
