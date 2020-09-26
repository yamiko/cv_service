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
import org.cvs.data.entities.Qualification;
import org.cvs.data.entities.QualificationType;
import org.cvs.data.repositories.QualificationRepository;
import org.cvs.utils.Lookup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class QualificationServiceImpl implements QualificationService {

	@Autowired
	private QualificationRepository qualificationRepository;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private QualificationTypeService qualificationTypeService;

	@Autowired
	private Validator validator;

	@Override
	public Qualification addQualification(Qualification qualification)
	        throws EntryNotFoundException, EntryNotActiveException {

		Qualification greenQualification = new Qualification();

		// Extract all fields to safely add to DB
		greenQualification.setName(qualification.getName());
		greenQualification.setInstitution(qualification.getInstitution());
		greenQualification.setCountry(qualification.getCountry());
		greenQualification.setDateObtained(qualification.getDateObtained());

		greenQualification.setVoided(Lookup.NOT_VOIDED);
		greenQualification.setRetired(Lookup.NOT_RETIRED);

		// Validate using Bean constraints
		Set<ConstraintViolation<Qualification>> violations = validator.validate(greenQualification);
		if (!violations.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (ConstraintViolation<Qualification> constraintViolation : violations) {
				sb.append(" -> " + constraintViolation.getMessage());
			}

			throw new ConstraintViolationException("Validation error: " + sb.toString(), violations);
		}

		// Only proceed to search for qualification and candidate if we have references
		if (qualification.getQualificationType() == null || qualification.getCandidate() == null
		        || qualification.getQualificationType().getId() == null
		        || qualification.getCandidate().getId() == null) {
			throw new EntryNotFoundException("Unable to find existing CANDIDATE or QUALIFICATION_TYPE references");
		}

		Qualification newQualification = new Qualification();

		// Get reference entities
		Candidate existingCandidate = new Candidate();
		QualificationType existingQualificationType = new QualificationType();
		try {
			existingCandidate = candidateService.getActiveCandidate(qualification.getCandidate().getId());
			existingQualificationType = qualificationTypeService
			        .getActiveQualificationType(qualification.getQualificationType().getId());
		} catch (EntryNotFoundException e) {
			throw new EntryNotFoundException("Unable to find existing CANDIDATE or QUALIFICATION_TYPE references");
		} catch (EntryNotActiveException e) {
			throw new EntryNotFoundException("Unable to find active CANDIDATE or QUALIFICATION_TYPE references");
		}

		// Only attempt to save the qualification if we have an existing candidate and
		// qualification type
		newQualification = qualificationRepository.save(greenQualification);

		// Add references to existing Candidate and QualificationType instances
		newQualification.setCandidate(existingCandidate);
		newQualification.setQualificationType(existingQualificationType);
		newQualification = qualificationRepository.save(newQualification);

		return newQualification;
	}

	@Override
	public List<Qualification> getQualifications() {

		List<Qualification> qualifications = qualificationRepository.findAll().stream()
		        .filter(p -> p.getVoided() != Lookup.VOIDED && p.getRetired() != Lookup.RETIRED)
		        .collect(Collectors.toList());
		return qualifications;
	}

	@Override
	public Qualification getActiveQualification(Long qualificationId)
	        throws EntryNotActiveException, EntryNotFoundException {
		Qualification qualification = qualificationRepository.findById(qualificationId).orElse(null);
		if (qualification != null && qualification.getVoided() != Lookup.VOIDED
		        && qualification.getRetired() != Lookup.RETIRED) {
			return qualification;
		} else {
			if (qualification == null || qualification.getVoided() == Lookup.VOIDED) {
				throw new EntryNotFoundException("Invalid operation for [QUALIFICATION]." + qualificationId);
			} else {
				throw new EntryNotActiveException("Invalid operation for [QUALIFICATION]." + qualificationId);
			}
		}
	}

	@Override
	public void deleteQualification(Long qualificationId) throws EntryNotFoundException {
		Qualification qualification = qualificationRepository.findById(qualificationId).orElse(null);
		if (qualification != null && qualification.getVoided() != Lookup.VOIDED) {
			qualification.setVoided(Lookup.VOIDED);
			qualification.setVoidedReason("System operation - voided");
			qualificationRepository.save(qualification);
			log.info("Deleted qualification with ID: " + qualificationId);
		} else {
			throw new EntryNotFoundException("Invalid operation for [QUALIFICATION]." + qualificationId);
		}
	}

	@Override
	public void retireQualification(Long qualificationId) throws EntryNotFoundException {
		Qualification qualification = qualificationRepository.findById(qualificationId).orElse(null);
		if (qualification != null && qualification.getRetired() != Lookup.RETIRED) {
			qualification.setRetired(Lookup.RETIRED);
			qualification.setRetiredReason("System operation - retired");
			qualificationRepository.save(qualification);
			log.info("Retired qualification with ID: " + qualificationId);
		} else {
			throw new EntryNotFoundException("Invalid operation for [QUALIFICATION]." + qualificationId);
		}
	}
}
