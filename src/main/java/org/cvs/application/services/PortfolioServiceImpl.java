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
import org.cvs.data.entities.ApplicationUser;
import org.cvs.data.entities.Portfolio;
import org.cvs.data.repositories.ApplicationUserRepository;
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
	private ApplicationUserRepository userRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

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

		return newPortfolio;
	}

	@Override
	public List<Portfolio> getPortfolios() {

		List<Portfolio> portfolios = portfolioRepository.findAll().stream()
		        .filter(p -> p.getVoided() != Lookup.VOIDED && p.getRetired() != Lookup.RETIRED)
		        .collect(Collectors.toList());
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
			return portfolio;
		} else {
			if (portfolio == null) {
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
