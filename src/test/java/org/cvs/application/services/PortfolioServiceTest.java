package org.cvs.application.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Month;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.application.exceptions.InconsistentDataException;
import org.cvs.data.entities.ApplicationUser;
import org.cvs.data.entities.Candidate;
import org.cvs.data.entities.Portfolio;
import org.cvs.data.repositories.PortfolioRepository;
import org.cvs.utils.Lookup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;

import lombok.extern.slf4j.Slf4j;

/**
 * Integration tests for {@link PortfolioRepository}.
 * 
 * @author Yamiko Msosa
 *
 */
@Transactional
@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=validate" })
public class PortfolioServiceTest {

	@Autowired
	PortfolioService portfolioService;

	@Autowired
	CandidateService candidateService;

	@Autowired
	ApplicationUserService userService;

	Portfolio portfolio, invalidPortfolio, fetchedPortfolio;

	ApplicationUser user;
	
	Candidate candidate;

	int FALSE = 0;

	@BeforeEach
	void init() {
		// Create a valid portfolio
		portfolio = new Portfolio("Portfolio Name1");
		portfolio.setRetired(Lookup.NOT_RETIRED);
		portfolio.setVoided(Lookup.NOT_VOIDED);

		// Create an invalid portfolio
		invalidPortfolio = new Portfolio("");
		invalidPortfolio.setRetired(Lookup.NOT_RETIRED);
		invalidPortfolio.setVoided(Lookup.NOT_VOIDED);

		// Create a valid new user
		user = new ApplicationUser("test1", "password1", "Test User1");
		user.setRetired(Lookup.NOT_RETIRED);
		user.setVoided(Lookup.NOT_VOIDED);

		// Create a valid candidate
		candidate = new Candidate("John", "", "Smith");
		
		candidate.setAddressLine1("Address 1");
		candidate.setCountry("UK");
		candidate.setGender("M");
		candidate.setEmail("email@email.com");
		candidate.setDateOfBirth(LocalDate.of(1987, Month.JUNE, 15));

		candidate.setRetired(Lookup.NOT_RETIRED);
		candidate.setVoided(Lookup.NOT_VOIDED);
	}

	@Test
	@WithMockUser
	public void testAddPortfolio() {
		portfolio = portfolioService.addPortfolio(portfolio);

		assertAll("Properties", () -> assertTrue(portfolio.getId() > 0),
		        () -> assertTrue(portfolio.getName().equals("Portfolio Name1")),
		        () -> assertNotNull(portfolio.getCreatedDate()), () -> assertNotNull(portfolio.getCreatedBy()),
		        () -> assertNotNull(portfolio.getLastModifiedBy()), () -> assertNotNull(portfolio.getModifiedDate()),
		        () -> {
			        fetchedPortfolio = portfolioService.getActivePortfolio(portfolio.getId());
			        assertNotNull(fetchedPortfolio);
		        });
	}

	@Test
	@WithMockUser
	public void testUpdatePortfolioWithUserUsingExistingEntries() {
		portfolio = portfolioService.addPortfolio(portfolio);
		user = userService.addUser(user);

		portfolio = portfolioService.updatePortfolioWithUser(user.getId(), portfolio);

		assertTrue(portfolio.getApplicationUser().stream().filter(u -> u.getId() == user.getId()).findFirst()
		        .orElse(null) != null);
	}

	@Test
	@WithMockUser
	public void testUpdatePortfolioWithUserUsingDuplicateUser() {
		portfolio = portfolioService.addPortfolio(portfolio);
		user = userService.addUser(user);

		portfolio = portfolioService.updatePortfolioWithUser(user.getId(), portfolio);
		assertThrows(InconsistentDataException.class, () -> {
			portfolio = portfolioService.updatePortfolioWithUser(user.getId(), portfolio);// Duplicate operation
		});
	}

	@Test
	@WithMockUser
	public void testUpdatePortfolioWithUserUsingExistingUserAndNewPortfolio() {
		user = userService.addUser(user);

		portfolio = portfolioService.updatePortfolioWithUser(user.getId(), portfolio);
		assertTrue(user.getPortfolio().stream().filter(p -> p.getId() == portfolio.getId()).findFirst()
		        .orElse(null) != null);
	}

	@Test
	@WithMockUser
	public void testUpdatePortfolioWithCandidateUsingExistingEntries() {
		portfolio = portfolioService.addPortfolio(portfolio);
		candidate = candidateService.addCandidate(candidate);

		portfolio = portfolioService.updatePortfolioWithCandidate(candidate.getId(), portfolio);

		assertTrue(portfolio.getCandidate().stream().filter(c -> c.getId() == candidate.getId()).findFirst()
		        .orElse(null) != null);
	}

	@Test
	@WithMockUser
	public void testUpdatePortfolioWithCandidateUsingDuplicateCandidate() {
		portfolio = portfolioService.addPortfolio(portfolio);
		candidate = candidateService.addCandidate(candidate);

		portfolio = portfolioService.updatePortfolioWithCandidate(candidate.getId(), portfolio);
		assertThrows(InconsistentDataException.class, () -> {
			portfolio = portfolioService.updatePortfolioWithCandidate(candidate.getId(), portfolio);// Duplicate operation
		});
	}

	@Test
	@WithMockUser
	public void testUpdatePortfolioWithCandidateUsingExistingCandidateAndNewPortfolio() {
		candidate = candidateService.addCandidate(candidate);

		portfolio = portfolioService.updatePortfolioWithCandidate(candidate.getId(), portfolio);
		assertTrue(candidate.getPortfolio().stream().filter(p -> p.getId() == portfolio.getId()).findFirst()
		        .orElse(null) != null);
	}
	
	
	
	@Test
	@WithMockUser
	public void testFindNonExistentPortfolio() {
		Long portfolioId = -1L;

		assertThrows(EntryNotFoundException.class, () -> {
			portfolio = portfolioService.getActivePortfolio(portfolioId);
		});
	}

	@Test
	@WithMockUser
	public void testVoidPortfolio() {
		portfolio = portfolioService.addPortfolio(portfolio);
		log.info("Added portfolio with ID: " + portfolio.getId());

		Long portfolioId = portfolio.getId();

		portfolioService.deletePortfolio(portfolioId);

		assertThrows(EntryNotFoundException.class, () -> {
			portfolio = portfolioService.getActivePortfolio(portfolioId);
		});
	}

	@Test
	@WithMockUser
	public void testRetirePortfolio() {
		portfolio = portfolioService.addPortfolio(portfolio);
		log.info("Added portfolio with ID: " + portfolio.getId());

		Long portfolioId = portfolio.getId();

		portfolioService.retirePortfolio(portfolioId);

		assertThrows(EntryNotActiveException.class, () -> {
			portfolio = portfolioService.getActivePortfolio(portfolioId);
		});
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() {
		portfolio = portfolioService.addPortfolio(portfolio);

		assertAll("Properties", () -> assertTrue(portfolio.getCreatedBy().equals("Peter")),
		        () -> assertTrue(portfolio.getLastModifiedBy().equals("Peter")));
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() {
		assertThrows(ConstraintViolationException.class, () -> {
			portfolioService.addPortfolio(invalidPortfolio);
		});
	}

}
