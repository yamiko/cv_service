package org.cvs.application.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.data.entities.QualificationType;
import org.cvs.data.repositories.QualificationTypeRepository;
import org.cvs.utils.Lookup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class QualificationTypeServiceImpl implements QualificationTypeService {

	@Autowired
	private QualificationTypeRepository qualificationTypeRepository;

	@Autowired
	private Validator validator;

	@Override
	public QualificationType addQualificationType(QualificationType qualificationType) {

		QualificationType greenQualificationType = new QualificationType();

		// Extract all fields to safely add to DB
		greenQualificationType.setName(qualificationType.getName());

		greenQualificationType.setVoided(Lookup.NOT_VOIDED);
		greenQualificationType.setRetired(Lookup.NOT_RETIRED);

		// Validate using Bean constraints
		Set<ConstraintViolation<QualificationType>> violations = validator.validate(greenQualificationType);
		if (!violations.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (ConstraintViolation<QualificationType> constraintViolation : violations) {
				sb.append(" -> " + constraintViolation.getMessage());
			}

			throw new ConstraintViolationException("Validation error: " + sb.toString(), violations);
		}

		QualificationType newQualificationType = qualificationTypeRepository.save(greenQualificationType);
		return newQualificationType;
	}

	@Override
	public List<QualificationType> getQualificationTypes() {

		List<QualificationType> qualificationTypes = qualificationTypeRepository.findAll().stream()
		        .filter(p -> p.getVoided() != Lookup.VOIDED && p.getRetired() != Lookup.RETIRED)
		        .collect(Collectors.toList());
		return qualificationTypes;
	}

	@Override
	public QualificationType getByQualificationTypeName(String qualificationTypeName) throws EntryNotFoundException {

		Optional<QualificationType> qualificationType = qualificationTypeRepository.findAllByName(qualificationTypeName)
		        .stream().filter(p -> p.getVoided() != Lookup.VOIDED && p.getRetired() != Lookup.RETIRED).findFirst();

		return qualificationType.orElseThrow(() -> new EntryNotFoundException(
		        "Invalid operation for [QUALIFICATION_TYPE]." + qualificationTypeName));
	}

	@Override
	public QualificationType getActiveQualificationType(Long qualificationTypeId)
	        throws EntryNotActiveException, EntryNotFoundException {
		QualificationType qualificationType = qualificationTypeRepository.findById(qualificationTypeId).orElse(null);
		if (qualificationType != null && qualificationType.getVoided() != Lookup.VOIDED
		        && qualificationType.getRetired() != Lookup.RETIRED) {
			return qualificationType;
		} else {
			if (qualificationType == null || qualificationType.getVoided() == Lookup.VOIDED) {
				throw new EntryNotFoundException("Invalid operation for [QUALIFICATION_TYPE]." + qualificationTypeId);
			} else {
				throw new EntryNotActiveException("Invalid operation for [QUALIFICATION_TYPE]." + qualificationTypeId);
			}
		}
	}

	@Override
	public void deleteQualificationType(Long qualificationTypeId) throws EntryNotFoundException {
		QualificationType qualificationType = qualificationTypeRepository.findById(qualificationTypeId).orElse(null);
		if (qualificationType != null && qualificationType.getVoided() != Lookup.VOIDED) {
			qualificationType.setVoided(Lookup.VOIDED);
			qualificationType.setVoidedReason("System operation - voided");
			qualificationTypeRepository.save(qualificationType);
			log.info("Deleted qualificationType with ID: " + qualificationTypeId);
		} else {
			throw new EntryNotFoundException("Invalid operation for [QUALIFICATION_TYPE]." + qualificationTypeId);
		}
	}

	@Override
	public void retireQualificationType(Long qualificationTypeId) throws EntryNotFoundException {
		QualificationType qualificationType = qualificationTypeRepository.findById(qualificationTypeId).orElse(null);
		if (qualificationType != null && qualificationType.getRetired() != Lookup.RETIRED) {
			qualificationType.setRetired(Lookup.RETIRED);
			qualificationType.setRetiredReason("System operation - retired");
			qualificationTypeRepository.save(qualificationType);
			log.info("Retired qualificationType with ID: " + qualificationTypeId);
		} else {
			throw new EntryNotFoundException("Invalid operation for [QUALIFICATION_TYPE]." + qualificationTypeId);
		}
	}
}
