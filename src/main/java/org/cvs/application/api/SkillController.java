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
import org.cvs.application.services.SkillService;
import org.cvs.data.entities.Skill;

@Controller	
@RequestMapping(path="/skills") 
public class SkillController {
	
	@Autowired
	private SkillService skillService;

	/**
	 * 
	 * Adds a new skill to an existing candidate via POST through URL:
	 * <code>/skills</code>.
	 * <p>
	 * 
	 * Example payload:
	 * 
	 * <code> 
	 * {
     *   "description": "Java 8 - Spring/JPA",
     *   "candidate": {"id": 111}
     * }
	 * </code>
	 * 
	 * @param skill the skill (can be a JSON payload) to be added to the
	 *                  system.
	 * 
	 * @return the newly added skill
	 */
	@PostMapping(path = "")
	public @ResponseBody Skill addNewApplicationSkill(@RequestBody Skill skill) {
		try {
			Skill newskill = skillService.addSkill(skill);
			return newskill;
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		} catch (EntryNotActiveException e) {
			throw new ResponseStatusException(HttpStatus.LOCKED, e.getMessage(), e);
		} catch (ConstraintViolationException e) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Fetches an active skill via GET through URL:
	 * <code>/skills/active</code>.
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /skills/active?userId=1
	 * </code>
	 * 
	 * @param skillId the skill ID as a request parameter to be used in the
	 *                    query
	 * 
	 * @return an active skill if found
	 */	
	@GetMapping(path = "/active")
	public @ResponseBody Skill getSkill(@RequestParam Long skillId) {
		try {
			Skill skill = skillService.getActiveSkill(skillId);
			return skill;
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		} catch (EntryNotActiveException e) {
			throw new ResponseStatusException(HttpStatus.LOCKED, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Deletes a skill via DELETE method through base URL: <code>/skills</code>.
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
	 * @param skill the skill (can be a JSON payload) to be deleted from the system.
	 * 
	 * @return a string that says 'Deleted'
	 * 
	 */	
	@DeleteMapping(path = "")
	public @ResponseBody String deleteSkill(@RequestBody Skill skill) {
		try {
			skillService.deleteSkill(skill.getId());
			return "Deleted";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Retires a skill via POST through URL: <code>/skills/retire</code>.
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
	 * @param skill the skill (can be a JSON payload) to be retired from the system.
	 * 
	 * @return a string that says 'Retired'
	 */
	@PostMapping(path = "/retire")
	public @ResponseBody String retireSkill(@RequestBody Skill skill) {

		try {
			skillService.retireSkill(skill.getId());
			return "Retired";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}

	}

	/**
	 * 
	 * Fetches all active skills via GET through URL:
	 * <code>/skills/all</code>.
	 * 
	 * @return a list of all active skill entries in JSON or XML 
	 * 
	 */
	@GetMapping(path = "/all")
	public @ResponseBody Iterable<Skill> getAllSkills() {
		// This returns a JSON or XML with the skills
		return skillService.getSkills();
	}
}
