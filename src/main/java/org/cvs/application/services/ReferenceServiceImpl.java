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
import org.cvs.data.entities.Reference;
import org.cvs.data.repositories.ReferenceRepository;
import org.cvs.utils.Lookup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReferenceServiceImpl implements ReferenceService {

	@Autowired
	private ReferenceRepository referenceRepository;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private Validator validator;

	@Override
	public Reference addReference(Reference reference) {

		Reference greenReference = new Reference();

		// Extract all fields to safely add to DB
		greenReference.setName(reference.getName());
		greenReference.setInstitution(reference.getInstitution());
		greenReference.setJobTitle(reference.getJobTitle());

		greenReference.setAddressLine1(reference.getAddressLine1());
		greenReference.setAddressLine2(reference.getAddressLine2());
		greenReference.setAddressLine3(reference.getAddressLine3());
		greenReference.setPostcode(reference.getPostcode());
		greenReference.setCountry(reference.getCountry());

		greenReference.setContactNumber(reference.getContactNumber());
		greenReference.setEmail(reference.getEmail());


		greenReference.setVoided(Lookup.NOT_VOIDED);
		greenReference.setRetired(Lookup.NOT_RETIRED);

		// Validate using Bean constraints
		Set<ConstraintViolation<Reference>> violations = validator.validate(greenReference);
		if (!violations.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (ConstraintViolation<Reference> constraintViolation : violations) {
				sb.append(" -> " + constraintViolation.getMessage());
			}
			
			throw new ConstraintViolationException("Validation error: " + sb.toString(), violations);
		}		
		
		Reference newReference = new Reference();

		// Get a reference entity for the Candidate instance 
		Candidate existingCandidate = new Candidate();
		try {
			existingCandidate = candidateService.getActiveCandidate(reference.getCandidate().getId());
		} catch (EntryNotFoundException e) {
			throw new EntryNotFoundException("Unable to find existing [CANDIDATE] " + reference.getCandidate().getId());
		} catch (EntryNotActiveException e) {
			throw new EntryNotFoundException("Unable to find active [CANDIDATE] " + reference.getCandidate().getId());
		}

		// Only attempt to save the qualification if we have an existing candidate
		newReference = referenceRepository.save(greenReference);

		// Add references to an existing Candidate instance
		newReference.setCandidate(existingCandidate);
		newReference = referenceRepository.save(newReference);
		
		return newReference;

	}

	@Override
	public List<Reference> getReferences() {

		List<Reference> references = referenceRepository.findAll().stream()
		        .filter(p -> p.getVoided() != Lookup.VOIDED && p.getRetired() != Lookup.RETIRED)
		        .collect(Collectors.toList());
		return references;
	}

	@Override
	public Reference getActiveReference(Long referenceId) throws EntryNotActiveException, EntryNotFoundException {
		Reference reference = referenceRepository.findById(referenceId).orElse(null);
		if (reference != null && reference.getVoided() != Lookup.VOIDED && reference.getRetired() != Lookup.RETIRED) {
			return reference;
		} else {
			if (reference == null) {
				throw new EntryNotFoundException("Invalid operation for [REFERENCE]." + referenceId);
			} else {
				throw new EntryNotActiveException("Invalid operation for [REFERENCE]." + referenceId);
			}
		}
	}

	@Override
	public void deleteReference(Long referenceId) throws EntryNotFoundException {
		Reference reference = referenceRepository.findById(referenceId).orElse(null);
		if (reference != null && reference.getVoided() != Lookup.VOIDED) {
			reference.setVoided(Lookup.VOIDED);
			reference.setVoidedReason("System operation - voided");
			referenceRepository.save(reference);
			log.info("Deleted reference with ID: " + referenceId);
		} else {
			throw new EntryNotFoundException("Invalid operation for [REFERENCE]." + referenceId);
		}
	}

	@Override
	public void retireReference(Long referenceId) throws EntryNotFoundException {
		Reference reference = referenceRepository.findById(referenceId).orElse(null);
		if (reference != null && reference.getRetired() != Lookup.RETIRED) {
			reference.setRetired(Lookup.RETIRED);
			reference.setRetiredReason("System operation - retired");
			referenceRepository.save(reference);
			log.info("Retired reference with ID: " + referenceId);
		} else {
			throw new EntryNotFoundException("Invalid operation for [REFERENCE]." + referenceId);
		}
	}
}
