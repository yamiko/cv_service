package org.cvs.application.services;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.data.entities.Candidate;
import org.cvs.data.entities.Skill;
import org.cvs.data.repositories.SkillRepository;
import org.cvs.utils.Lookup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SkillServiceImpl implements SkillService {

	@Autowired
	private SkillRepository skillRepository;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private Validator validator;

	@Override
	public Skill addSkill(Skill skill) {

		Skill greenSkill = new Skill();

		// Extract all fields to safely add to DB
		greenSkill.setDescription(skill.getDescription());

		greenSkill.setVoided(Lookup.NOT_VOIDED);
		greenSkill.setRetired(Lookup.NOT_RETIRED);

		// Validate using Bean constraints
		Set<ConstraintViolation<Skill>> violations = validator.validate(greenSkill);
		if (!violations.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (ConstraintViolation<Skill> constraintViolation : violations) {
				sb.append(" -> " + constraintViolation.getMessage());
			}

			throw new ConstraintViolationException("Validation error: " + sb.toString(), violations);
		}

		// Only proceed to search for a candidate if we have reference
		if (skill.getCandidate() == null || skill.getCandidate().getId() == null) {
			throw new EntryNotFoundException("Unable to find existing CANDIDATE reference");
		}

		Skill newSkill = new Skill();

		// Get a skill entity for the Candidate instance
		Candidate existingCandidate = new Candidate();
		try {
			existingCandidate = candidateService.getActiveCandidate(skill.getCandidate().getId());
		} catch (EntryNotFoundException e) {
			throw new EntryNotFoundException("Unable to find existing [CANDIDATE] " + skill.getCandidate().getId());
		} catch (EntryNotActiveException e) {
			throw new EntryNotFoundException("Unable to find active [CANDIDATE] " + skill.getCandidate().getId());
		}

		// Only attempt to save the Skill if we have an existing Candidate
		newSkill = skillRepository.save(greenSkill);

		// Add the Skill to an existing Candidate instance
		newSkill.setCandidate(existingCandidate);
		newSkill = skillRepository.save(newSkill);

		return newSkill;
	}

	@Override
	public List<Skill> getSkills() {

		List<Skill> skills = skillRepository.findAll().stream()
		        .filter(p -> p.getVoided() != Lookup.VOIDED && p.getRetired() != Lookup.RETIRED)
		        .collect(Collectors.toList());
		return skills;
	}

	@Override
	public Skill getActiveSkill(Long skillId) throws EntryNotActiveException, EntryNotFoundException {
		Skill skill = skillRepository.findById(skillId).orElse(null);
		if (skill != null && skill.getVoided() != Lookup.VOIDED && skill.getRetired() != Lookup.RETIRED) {
			return skill;
		} else {
			if (skill == null || skill.getVoided() == Lookup.VOIDED) {
				throw new EntryNotFoundException("Invalid operation for [SKILL]." + skillId);
			} else {
				throw new EntryNotActiveException("Invalid operation for [SKILL]." + skillId);
			}
		}
	}

	@Override
	public void deleteSkill(Long skillId) throws EntryNotFoundException {
		Skill skill = skillRepository.findById(skillId).orElse(null);
		if (skill != null && skill.getVoided() != Lookup.VOIDED) {
			skill.setVoided(Lookup.VOIDED);
			skill.setVoidedReason("System operation - voided");
			skillRepository.save(skill);
			log.info("Deleted skill with ID: " + skillId);
		} else {
			throw new EntryNotFoundException("Invalid operation for [SKILL]." + skillId);
		}
	}

	@Override
	public void retireSkill(Long skillId) throws EntryNotFoundException {
		Skill skill = skillRepository.findById(skillId).orElse(null);
		if (skill != null && skill.getRetired() != Lookup.RETIRED) {
			skill.setRetired(Lookup.RETIRED);
			skill.setRetiredReason("System operation - retired");
			skillRepository.save(skill);
			log.info("Retired skill with ID: " + skillId);
		} else {
			throw new EntryNotFoundException("Invalid operation for [SKILL]." + skillId);
		}
	}
}
