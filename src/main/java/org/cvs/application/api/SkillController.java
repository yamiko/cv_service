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
import org.cvs.application.services.SkillService;
import org.cvs.data.entities.Skill;
import org.cvs.data.entities.WorkExperience;

@Controller
@RequestMapping(path = "/skills")
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
	 * @param skill the skill (can be a JSON payload) to be added to the system.
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
	 * <code>/skills/active/{skillId}</code>.
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /skills/active/1
	 * </code>
	 * 
	 * @param skillId the skill ID as a request parameter to be used in the query
	 * 
	 * @return an active skill if found
	 */
	@GetMapping(path = "/active/{skillId}")
	public @ResponseBody Skill getSkill(@PathVariable Long skillId) {
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
	 * Deletes a skill via DELETE method through base URL:
	 * <code>/skills/{skillId}</code>.
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /skills/1
	 * </code>
	 * 
	 * @param skillId the ID of the skill that is to be deleted from the system.
	 * 
	 * @return a string that says 'Deleted'
	 * 
	 */
	@DeleteMapping(path = "/{skillId}")
	public @ResponseBody String deleteSkill(@PathVariable Long skillId) {
		try {
			skillService.deleteSkill(skillId);
			return "Deleted";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Retires a skill via POST through URL: <code>/skills/retire/{skillId}</code>.
	 * <p>
	 * 
	 * Example payload:
	 * 
	 * <code> 
	 *  /skills/retire/1
	 * </code>
	 * 
	 * @param skillId the ID of the skill to be retired from the system.
	 * 
	 * @return a string that says 'Retired'
	 */
	@PostMapping(path = "/retire/{skillId}")
	public @ResponseBody String retireSkill(@PathVariable Long skillId) {

		try {
			skillService.retireSkill(skillId);
			return "Retired";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}

	}

	/**
	 * 
	 * Fetches all active skills via GET through URL: <code>/skills</code>.
	 * 
	 * @return a list of all active skill entries in JSON or XML
	 * 
	 */
	@GetMapping(path = "")
	public @ResponseBody Iterable<Skill> getAllSkills() {
		// This returns a JSON or XML with the skills
		return skillService.getSkills();
	}

	/**
	 * 
	 * Fetches active skill entries for a particular candidate via GET through URL:
	 * <code>/skills/candidate/{candidateId}</code>.
	 * 
	 * @param candidateId the ID of the candidate ID to filter work experience
	 *                    entries for.
	 * 
	 * @return a list of all active skill entries for a particular candidate
	 * 
	 */
	@GetMapping(path = "/candidate/{candidateId}")
	public @ResponseBody Iterable<Skill> getSkills(@PathVariable Long candidateId) {
		// This returns a JSON or XML with the workExperiences
		return skillService.getSkills(candidateId);
	}
}
