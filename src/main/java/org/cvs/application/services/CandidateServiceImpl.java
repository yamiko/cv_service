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
import org.cvs.data.entities.Portfolio;
import org.cvs.data.repositories.CandidateRepository;
import org.cvs.data.repositories.PortfolioRepository;
import org.cvs.utils.Lookup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CandidateServiceImpl implements CandidateService {

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private CandidateRepository candidateRepository;
	
	@Autowired
	private Validator validator;

	@Override
	public Candidate addCandidate(Candidate candidate) {
		Candidate greenCandidate = new Candidate();

		// Extract all fields to safely add to DB
		greenCandidate.setAddressLine1(candidate.getAddressLine1());
		greenCandidate.setAddressLine2(candidate.getAddressLine2());
		greenCandidate.setAddressLine3(candidate.getAddressLine3());
		greenCandidate.setCountry(candidate.getCountry());
		greenCandidate.setPostcode(candidate.getPostcode());

		greenCandidate.setGender(candidate.getGender());
		greenCandidate.setEmail(candidate.getEmail());
		greenCandidate.setPreferredContactNumber(candidate.getPreferredContactNumber());
		greenCandidate.setAlternativeContactNumber(candidate.getAlternativeContactNumber());

		greenCandidate.setFirstName(candidate.getFirstName());
		greenCandidate.setMiddleName(candidate.getMiddleName());
		greenCandidate.setLastName(candidate.getLastName());
		greenCandidate.setTitle(candidate.getTitle());

		greenCandidate.setVoided(Lookup.NOT_VOIDED);
		greenCandidate.setRetired(Lookup.NOT_RETIRED);
		
		
		// Validate using Bean constraints
		Set<ConstraintViolation<Candidate>> violations = validator.validate(greenCandidate);
		if (!violations.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (ConstraintViolation<Candidate> constraintViolation : violations) {
				sb.append(" -> " + constraintViolation.getMessage());
			}
			
			throw new ConstraintViolationException("Validation error: " + sb.toString(), violations);
		}		
		
		List<Portfolio> portfolios = candidate.getPortfolio().stream().collect(Collectors.toList());

		Candidate newCandidate = new Candidate();

		newCandidate = candidateRepository.save(greenCandidate);

		// Safely add relationships with (an) existing portfolio(s)
		for (Portfolio portfolio : portfolios) {
			Portfolio existingPortfolio = portfolioRepository.findById(portfolio.getId()).orElse(null);

			if (existingPortfolio == null) {
				log.info("Skipping invalid portfolio entry for Portfolio ID " + portfolio.getId());
			} else {
				log.info("Found portfolio : " + existingPortfolio.getId() + ", created on : "
				        + existingPortfolio.getCreatedDate());
				newCandidate.getPortfolio().add(existingPortfolio);
				newCandidate = candidateRepository.save(newCandidate);
				log.info("added portfolio to user");
			}
		}

		return newCandidate;
	}
	
 

	@Override
	public List<Candidate> getCandidates() {

		List<Candidate> candidates = candidateRepository.findAll().stream()
		        .filter(p -> p.getVoided() != Lookup.VOIDED && p.getRetired() != Lookup.RETIRED)
		        .collect(Collectors.toList());
		return candidates;
	}

	@Override
	public Candidate getActiveCandidate(Long candidateId) throws EntryNotActiveException, EntryNotFoundException {
		Candidate candidate = candidateRepository.findById(candidateId).orElse(null);
		if (candidate != null && candidate.getVoided() != Lookup.VOIDED && candidate.getRetired() != Lookup.RETIRED) {
			return candidate;
		} else {
			if (candidate == null) {
				throw new EntryNotFoundException("Invalid operation for [CANDIDATE]." + candidateId);
			} else {
				throw new EntryNotActiveException("Invalid operation for [CANDIDATE]." + candidateId);
			}
		}
	}

	@Override
	public void deleteCandidate(Long candidateId) throws EntryNotFoundException {
		Candidate candidate = candidateRepository.findById(candidateId).orElse(null);
		if (candidate != null && candidate.getVoided() != Lookup.VOIDED) {
			candidate.setVoided(Lookup.VOIDED);
			candidate.setVoidedReason("System operation - voided");
			candidateRepository.save(candidate);
			log.info("Deleted candidate with ID: " + candidateId);
		} else {
			throw new EntryNotFoundException("Invalid operation for [CANDIDATE]." + candidateId);
		}
	}

	@Override
	public void retireCandidate(Long candidateId) throws EntryNotFoundException {
		Candidate candidate = candidateRepository.findById(candidateId).orElse(null);
		if (candidate != null && candidate.getRetired() != Lookup.RETIRED) {
			candidate.setRetired(Lookup.RETIRED);
			candidate.setRetiredReason("System operation - retired");
			candidateRepository.save(candidate);
			log.info("Retired candidate with ID: " + candidateId);
		} else {
			throw new EntryNotFoundException("Invalid operation for [CANDIDATE]." + candidateId);
		}
	}
}
