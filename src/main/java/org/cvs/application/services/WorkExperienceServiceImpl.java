package org.cvs.application.services;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.application.exceptions.InconsistentDataException;
import org.cvs.data.entities.Candidate;
import org.cvs.data.entities.WorkExperience;
import org.cvs.data.repositories.WorkExperienceRepository;
import org.cvs.utils.Lookup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WorkExperienceServiceImpl implements WorkExperienceService {

	@Autowired
	private WorkExperienceRepository workExperienceRepository;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private Validator validator;

	@Override
	public WorkExperience addWorkExperience(WorkExperience workExperience) {

		WorkExperience greenWorkExperience = new WorkExperience();

		// Extract all fields to safely add to DB
		greenWorkExperience.setOrganisation(workExperience.getOrganisation());
		greenWorkExperience.setPosition(workExperience.getPosition());
		greenWorkExperience.setCountry(workExperience.getCountry());

		greenWorkExperience.setStartDate(workExperience.getStartDate());
		greenWorkExperience.setEndDate(workExperience.getEndDate());

		greenWorkExperience.setVoided(Lookup.NOT_VOIDED);
		greenWorkExperience.setRetired(Lookup.NOT_RETIRED);

		// Validate using Bean constraints
		Set<ConstraintViolation<WorkExperience>> violations = validator.validate(greenWorkExperience);
		if (!violations.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (ConstraintViolation<WorkExperience> constraintViolation : violations) {
				sb.append(" -> " + constraintViolation.getMessage());
			}

			throw new ConstraintViolationException("Validation error: " + sb.toString(), violations);
		}

		if (!greenWorkExperience.getEndDate().isAfter(greenWorkExperience.getStartDate())) {
			throw new InconsistentDataException("End Date should be after Start Date ");
		}

		// Only proceed to search for a candidate if we have reference
		if (workExperience.getCandidate() == null || workExperience.getCandidate().getId() == null) {
			throw new EntryNotFoundException("Unable to find existing CANDIDATE reference");
		}

		WorkExperience newWorkExperience = new WorkExperience();

		// Get a workExperience entity for the Candidate instance
		Candidate existingCandidate = new Candidate();
		try {
			existingCandidate = candidateService.getActiveCandidate(workExperience.getCandidate().getId());
		} catch (EntryNotFoundException e) {
			throw new EntryNotFoundException(
			        "Unable to find existing [CANDIDATE] " + workExperience.getCandidate().getId());
		} catch (EntryNotActiveException e) {
			throw new EntryNotFoundException(
			        "Unable to find active [CANDIDATE] " + workExperience.getCandidate().getId());
		}

		// Only attempt to save the qualification if we have an existing candidate
		newWorkExperience = workExperienceRepository.save(greenWorkExperience);

		// Add workExperiences to an existing Candidate instance
		newWorkExperience.setCandidate(existingCandidate);
		newWorkExperience = workExperienceRepository.save(newWorkExperience);

		return newWorkExperience;
	}

	@Override
	public List<WorkExperience> getWorkExperiences() {
		List<WorkExperience> workExperiences = workExperienceRepository.findAll().stream()
		        .filter(p -> p.getVoided() != Lookup.VOIDED && p.getRetired() != Lookup.RETIRED)
		        .collect(Collectors.toList());
		log.info("Listing");
		workExperiences.sort(Comparator.comparing(WorkExperience::getId));
		return workExperiences;
	}

	@Override
	public List<WorkExperience> getWorkExperiences(Long candidateId) {
		List<WorkExperience> workExperiences = workExperienceRepository.findAll().stream()
		        .filter(p -> p.getVoided() != Lookup.VOIDED && p.getRetired() != Lookup.RETIRED
		                && p.getCandidate().getId().longValue() == candidateId)
		        .collect(Collectors.toList());

		workExperiences.sort(Comparator.comparing(WorkExperience::getId));
		return workExperiences;
	}

	@Override
	public WorkExperience getActiveWorkExperience(Long workExperienceId)
	        throws EntryNotActiveException, EntryNotFoundException {
		WorkExperience workExperience = workExperienceRepository.findById(workExperienceId).orElse(null);
		if (workExperience != null && workExperience.getVoided() != Lookup.VOIDED
		        && workExperience.getRetired() != Lookup.RETIRED) {
			return workExperience;
		} else {
			if (workExperience == null || workExperience.getVoided() == Lookup.VOIDED) {
				throw new EntryNotFoundException("Invalid operation for [WORK_EXPERIENCE]." + workExperienceId);
			} else {
				throw new EntryNotActiveException("Invalid operation for [WORK_EXPERIENCE]." + workExperienceId);
			}
		}
	}

	@Override
	public void deleteWorkExperience(Long workExperienceId) throws EntryNotFoundException {
		WorkExperience workExperience = workExperienceRepository.findById(workExperienceId).orElse(null);
		if (workExperience != null && workExperience.getVoided() != Lookup.VOIDED) {
			workExperience.setVoided(Lookup.VOIDED);
			workExperience.setVoidedReason("System operation - voided");
			workExperienceRepository.save(workExperience);
			log.info("Deleted workExperience with ID: " + workExperienceId);
		} else {
			throw new EntryNotFoundException("Invalid operation for [WORK_EXPERIENCE]." + workExperienceId);
		}
	}

	@Override
	public void retireWorkExperience(Long workExperienceId) throws EntryNotFoundException {
		WorkExperience workExperience = workExperienceRepository.findById(workExperienceId).orElse(null);
		if (workExperience != null && workExperience.getRetired() != Lookup.RETIRED) {
			workExperience.setRetired(Lookup.RETIRED);
			workExperience.setRetiredReason("System operation - retired");
			workExperienceRepository.save(workExperience);
			log.info("Retired workExperience with ID: " + workExperienceId);
		} else {
			throw new EntryNotFoundException("Invalid operation for [WORK_EXPERIENCE]." + workExperienceId);
		}
	}
}
