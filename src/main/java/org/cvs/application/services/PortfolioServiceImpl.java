package org.cvs.application.services;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.data.entities.ApplicationUser;
import org.cvs.data.entities.Candidate;
import org.cvs.data.entities.Portfolio;
import org.cvs.data.repositories.ApplicationUserRepository;
import org.cvs.data.repositories.CandidateRepository;
import org.cvs.data.repositories.PortfolioRepository;
import org.cvs.utils.Lookup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PortfolioServiceImpl implements PortfolioService {

	@Autowired
	private ApplicationUserService userService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private ApplicationUserRepository userRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private CandidateRepository candidateRepository;

	@Autowired
	private Validator validator;

	@Override
	public Portfolio addPortfolio(Portfolio portfolio) {

		Portfolio greenPortfolio = new Portfolio();

		greenPortfolio.setName(portfolio.getName());

		greenPortfolio.setVoided(Lookup.NOT_VOIDED);
		greenPortfolio.setRetired(Lookup.NOT_RETIRED);

		// Validate using Bean constraints
		Set<ConstraintViolation<Portfolio>> violations = validator.validate(greenPortfolio);
		if (!violations.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (ConstraintViolation<Portfolio> constraintViolation : violations) {
				sb.append(" -> " + constraintViolation.getMessage());
			}

			throw new ConstraintViolationException("Validation error: " + sb.toString(), violations);
		}

		List<ApplicationUser> users = portfolio.getApplicationUser().stream().collect(Collectors.toList());
		List<Candidate> candidates = portfolio.getCandidate().stream().collect(Collectors.toList());

		// Save bare portfolio record
		Portfolio newPortfolio = portfolioRepository.save(greenPortfolio);

		// Safely add relationships with (an) existing user(s)
		for (ApplicationUser user : users) {
			try {
				ApplicationUser existingUser = userService.getActiveUser(user.getId());
				if (existingUser != null) {
					log.info(
					        "Found user : " + existingUser.getId() + ", created on : " + existingUser.getCreatedDate());
					existingUser.getPortfolio().add(greenPortfolio);
					userRepository.save(existingUser);
					log.info("added portfolio to user");
				}
			} catch (EntryNotFoundException e) {
				log.info("Skipping non-existent user entry for User ID " + user.getId());
			} catch (EntryNotActiveException e) {
				log.info("Skipping inactive user entry for User ID " + user.getId());
			}

		}

		// Safely add relationships with (an) existing candidate(s)
		for (Candidate candidate : candidates) {
			try {
				Candidate existingCandidate = candidateService.getActiveCandidate(candidate.getId());
				if (existingCandidate != null) {
					log.info("Found candidate : " + existingCandidate.getId() + ", created on : "
					        + existingCandidate.getCreatedDate());
					existingCandidate.getPortfolio().add(greenPortfolio);
					candidateRepository.save(existingCandidate);
					log.info("added portfolio to candidate");
				}
			} catch (EntryNotFoundException e) {
				log.info("Skipping non-existent candidate entry for Candidate ID " + candidate.getId());
			} catch (EntryNotActiveException e) {
				log.info("Skipping inactive candidate entry for Candidate ID " + candidate.getId());
			}

		}

		newPortfolio = getActivePortfolio(newPortfolio.getId());

		return newPortfolio;
	}

	@Override
	public Portfolio updatePortfolioWithUser(Long userId, Long portfolioId)
	        throws EntryNotFoundException, EntryNotActiveException {
		ApplicationUser user;

		try {
			user = userService.getActiveUser(userId);
		} catch (EntryNotFoundException ex) {
			throw new EntryNotFoundException(ex.getMessage());
		} catch (EntryNotActiveException ex) {
			throw new EntryNotActiveException(ex.getMessage());
		}

		Portfolio existingPortfolio, updatedPortfolio;

		try {
			existingPortfolio = getActivePortfolio(portfolioId);
			user.getPortfolio().add(existingPortfolio); // Add user to portfolio from the main side of the
			                                            // many-to-many relationship
			user = userRepository.save(user);
			updatedPortfolio = getActivePortfolio(existingPortfolio.getId());
		} catch (EntryNotFoundException ex) {
			throw new EntryNotFoundException(ex.getMessage());
		} catch (EntryNotActiveException ex) {
			throw new EntryNotActiveException(ex.getMessage());
		}

		return updatedPortfolio;
	}

	@Override
	public Portfolio updatePortfolioWithCandidate(Long candidateId, Long portfolioId)
	        throws EntryNotFoundException, EntryNotActiveException {
		Candidate candidate;

		try {
			candidate = candidateService.getActiveCandidate(candidateId);
		} catch (EntryNotFoundException ex) {
			throw new EntryNotFoundException(ex.getMessage());
		} catch (EntryNotActiveException ex) {
			throw new EntryNotActiveException(ex.getMessage());
		}

		Portfolio existingPortfolio, updatedPortfolio;

		try {
			existingPortfolio = getActivePortfolio(portfolioId);
			candidate.getPortfolio().add(existingPortfolio); // Add candidate to portfolio from the main side of the
			                                                 // relationship
			candidate = candidateRepository.save(candidate);
			updatedPortfolio = getActivePortfolio(existingPortfolio.getId());
		} catch (EntryNotFoundException ex) {
			throw new EntryNotFoundException(ex.getMessage());
		} catch (EntryNotActiveException ex) {
			throw new EntryNotActiveException(ex.getMessage());
		}

		return updatedPortfolio;
	}

	@Override
	public List<Portfolio> getPortfolios() {

		List<Portfolio> portfolios = portfolioRepository.findAll().stream()
		        .filter(p -> p.getVoided() != Lookup.VOIDED && p.getRetired() != Lookup.RETIRED)
		        .collect(Collectors.toList());

		// Remove deleted records
		portfolios.stream().forEach(p -> p.setApplicationUser(p.getApplicationUser().stream()
		        .filter(u -> u.getVoided() == Lookup.NOT_VOIDED).collect(Collectors.toSet())));

		portfolios.stream().forEach(p -> p.setCandidate(
		        p.getCandidate().stream().filter(c -> c.getVoided() == Lookup.NOT_VOIDED).collect(Collectors.toSet())));
		return portfolios;
	}

	@Override
	public List<Portfolio> getPortfolios(Long userId) {

		List<Portfolio> portfolios = userService.getActiveUser(userId).getPortfolio().stream()
		        .filter(p -> p.getVoided() != Lookup.VOIDED && p.getRetired() != Lookup.RETIRED)
		        .collect(Collectors.toList());

		// Remove deleted records
		portfolios.stream().forEach(p -> p.setApplicationUser(p.getApplicationUser().stream()
		        .filter(u -> u.getVoided() == Lookup.NOT_VOIDED).collect(Collectors.toSet())));

		portfolios.stream().forEach(p -> p.setCandidate(
		        p.getCandidate().stream().filter(c -> c.getVoided() == Lookup.NOT_VOIDED).collect(Collectors.toSet())));

		portfolios.sort(Comparator.comparing(Portfolio::getId));

		return portfolios;
	}

	@Override
	public Portfolio getByPortfolioName(String portfolioName) throws EntryNotFoundException {

		Optional<Portfolio> portfolio = portfolioRepository.findAllByName(portfolioName).stream()
		        .filter(p -> p.getVoided() != Lookup.VOIDED && p.getRetired() != Lookup.RETIRED).findFirst();

		return portfolio
		        .orElseThrow(() -> new EntryNotFoundException("Invalid operation for [PORTFOLIO]." + portfolioName));
	}

	@Override
	public Portfolio getActivePortfolio(Long portfolioId) throws EntryNotActiveException, EntryNotFoundException {
		Portfolio portfolio = portfolioRepository.findById(portfolioId).orElse(null);
		if (portfolio != null && portfolio.getVoided() != Lookup.VOIDED && portfolio.getRetired() != Lookup.RETIRED) {
			// Remove deleted references
			portfolio.setApplicationUser(portfolio.getApplicationUser().stream()
			        .filter(u -> u.getVoided() == Lookup.NOT_VOIDED).collect(Collectors.toSet()));
			portfolio.setCandidate(portfolio.getCandidate().stream().filter(c -> c.getVoided() == Lookup.NOT_VOIDED)
			        .collect(Collectors.toSet()));
			return portfolio;
		} else {
			if (portfolio == null || portfolio.getVoided() == Lookup.VOIDED) {
				throw new EntryNotFoundException("Invalid operation for [PORTFOLIO]." + portfolioId);
			} else {
				throw new EntryNotActiveException("Invalid operation for [PORTFOLIO]." + portfolioId);
			}
		}
	}

	@Override
	public void deletePortfolio(Long portfolioId) throws EntryNotFoundException {
		Portfolio portfolio = portfolioRepository.findById(portfolioId).orElse(null);
		if (portfolio != null && portfolio.getVoided() != Lookup.VOIDED) {
			portfolio.setVoided(Lookup.VOIDED);
			portfolio.setVoidedReason("System operation - voided");
			portfolioRepository.save(portfolio);
			log.info("Deleted portfolio with ID: " + portfolioId);
		} else {
			throw new EntryNotFoundException("Invalid operation for [PORTFOLIO]." + portfolioId);
		}
	}

	@Override
	public void retirePortfolio(Long portfolioId) throws EntryNotFoundException {
		Portfolio portfolio = portfolioRepository.findById(portfolioId).orElse(null);
		if (portfolio != null && portfolio.getRetired() != Lookup.RETIRED) {
			portfolio.setRetired(Lookup.RETIRED);
			portfolio.setRetiredReason("System operation - retired");
			portfolioRepository.save(portfolio);
			log.info("Retired portfolio with ID: " + portfolioId);
		} else {
			throw new EntryNotFoundException("Invalid operation for [PORTFOLIO]." + portfolioId);
		}
	}
}
