package org.cvs.data;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

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
public class PortfolioRepositoryTest {

	@Autowired
	PortfolioRepository repository;

	@Autowired
	private Validator validator;

	Portfolio portfolio, invalidPortfolio, fetchedPortfolio;

	int FALSE = 0;
	
	@BeforeEach
    void init() {
		portfolio = new Portfolio("Portfolio Name1");
		portfolio.setRetired(Lookup.NOT_RETIRED);
		portfolio.setVoided(Lookup.NOT_VOIDED);

		invalidPortfolio = new Portfolio("");
		invalidPortfolio.setRetired(Lookup.NOT_RETIRED);
		invalidPortfolio.setVoided(Lookup.NOT_VOIDED);
	}

	@Test
	@WithMockUser
	public void testAddPortfolio() {
		portfolio = repository.save(portfolio);

		assertAll("Properties", 
				() -> assertTrue(portfolio.getId() > 0),
		        () -> assertTrue(portfolio.getName().equals("Portfolio Name1")), 
		        () -> assertNotNull(portfolio.getCreatedDate()), 
		        () -> assertNotNull(portfolio.getCreatedBy()), 
		        () -> assertNotNull(portfolio.getLastModifiedBy()), 
		        () -> assertNotNull(portfolio.getModifiedDate()), 
		        () -> {
			        fetchedPortfolio = repository.findAllByName("Portfolio Name1").stream().findFirst().orElse(null);
			        assertNotNull(fetchedPortfolio);
		        }
			);
	}

	@Test
	@WithMockUser
	public void AmendPortfolio() {
		portfolio = repository.save(portfolio);
		log.info("Added portfolio with ID: " + portfolio.getId());
		portfolio.setRetired(Lookup.RETIRED);
		portfolio = repository.save(portfolio);	
		assertTrue(portfolio.getRetired() == Lookup.RETIRED);	
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() {
		portfolio = repository.save(portfolio);

		assertAll("Properties", 
				() -> assertTrue(portfolio.getCreatedBy().equals("Peter")),
				() -> assertTrue(portfolio.getLastModifiedBy().equals("Peter"))
			);
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() {
		// Validate using Bean constraints
		Set<ConstraintViolation<Portfolio>> violations = validator.validate(invalidPortfolio);

		assertAll("Properties", 
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Portfolio name should not be blank")).findAny().orElse(null)),
				() -> assertFalse(violations.isEmpty())
			);
	}


}
