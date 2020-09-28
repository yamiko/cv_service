package org.cvs.data;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.cvs.data.entities.ApplicationUser;
import org.cvs.data.repositories.ApplicationUserRepository;
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
 * Integration tests for {@link ApplicationUser}.
 *
 * @author Yamiko Msosa
 *
 */
@Transactional
@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=validate" })
public class ApplicationUserRepositoryTest {

	@Autowired
	ApplicationUserRepository repository;

	@Autowired
	private Validator validator;

	ApplicationUser user, invalidUser, fetchedUser;

	int FALSE = 0;
	
	@BeforeEach
    void init() {
		user = new ApplicationUser("test1", "password1", "Test User1");
		user.setRetired(Lookup.NOT_RETIRED);
		user.setVoided(Lookup.NOT_VOIDED);

		invalidUser = new ApplicationUser("test1", "", "");
		invalidUser.setRetired(Lookup.NOT_RETIRED);
		invalidUser.setVoided(Lookup.NOT_VOIDED);
	}

	@Test
	@WithMockUser
	public void testAddUser() {
		user = repository.save(user);

		assertAll("Properties", 
				() -> assertTrue(user.getId() > 0),
		        () -> assertTrue(user.getFullName().equals("Test User1")), 
		        () -> assertNotNull(user.getCreatedDate()), 
		        () -> assertNotNull(user.getCreatedBy()), 
		        () -> assertNotNull(user.getLastModifiedBy()), 
		        () -> assertNotNull(user.getModifiedDate()), 
		        () -> {
			        fetchedUser = repository.findByUsername("test1").orElse(null);
			        assertNotNull(fetchedUser);
		        }
			);
	}

	@Test
	@WithMockUser
	public void testAmendUser() {
		user = repository.save(user);
		log.info("Added user with ID: " + user.getId());
		user.setRetired(Lookup.RETIRED);
		user = repository.save(user);	
		assertTrue(user.getRetired() == Lookup.RETIRED);	
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() {
		user = repository.save(user);

		assertAll("Properties", 
				() -> assertTrue(user.getCreatedBy().equals("Peter")),
				() -> assertTrue(user.getLastModifiedBy().equals("Peter"))
			);
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() {
		// Validate using Bean constraints
		Set<ConstraintViolation<ApplicationUser>> violations = validator.validate(invalidUser);

		assertAll("Properties", 
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Password should not be blank")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Full name should not be blank")).findAny().orElse(null)),
				() -> assertFalse(violations.isEmpty())
			);
	}


}
