package org.cvs.application.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
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
 * Integration tests for {@link ApplicationUserRepository}. 
 * 
 * @author Yamiko Msosa
 *
 */
@Transactional
@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=validate" })
public class ApplicationUserServiceTest {

	@Autowired
	ApplicationUserService service;

	ApplicationUser user, invalidApplicationUser, fetchedApplicationUser;

	int FALSE = 0;
	
	@BeforeEach
    void init() {
		// Create a valid new user
		user = new ApplicationUser("test1", "password1", "Test User1");
		user.setRetired(Lookup.NOT_RETIRED);
		user.setVoided(Lookup.NOT_VOIDED);

		
		// Create an invalid new user
		invalidApplicationUser = new ApplicationUser("test1", "", "");
		invalidApplicationUser.setRetired(Lookup.NOT_RETIRED);
		invalidApplicationUser.setVoided(Lookup.NOT_VOIDED);
	}

	@Test
	@WithMockUser
	public void testAddApplicationUser() {
		user = service.addUser(user);

		assertAll("Properties", 
				() -> assertTrue(user.getId() > 0),
		        () -> assertTrue(user.getFullName().equals("Test User1")), 
		        () -> assertNotNull(user.getCreatedDate()), 
		        () -> assertNotNull(user.getCreatedBy()), 
		        () -> assertNotNull(user.getLastModifiedBy()), 
		        () -> assertNotNull(user.getModifiedDate()), 
		        () -> {
			        fetchedApplicationUser = service.getActiveUser(user.getId());
			        assertNotNull(fetchedApplicationUser);
		        }
			);
	}

	@Test
	@WithMockUser
	public void testFindNonExistentApplicationUser() {	
		Long userId = -1L;
						
		assertThrows(EntryNotFoundException.class, () -> {
			user = service.getActiveUser(userId);
		});
	}
	
	@Test
	@WithMockUser
	public void testVoidApplicationUser() {
		user = service.addUser(user);
		log.info("Added user with ID: " + user.getId());
		
		Long userId = user.getId();
				
		service.deleteUser(userId);
		
		assertThrows(EntryNotFoundException.class, () -> {
			user = service.getActiveUser(userId);
		});
	}

	@Test
	@WithMockUser
	public void testRetireApplicationUser() {
		user = service.addUser(user);
		log.info("Added user with ID: " + user.getId());
		
		Long userId = user.getId();
				
		service.retireUser(userId);
		
		assertThrows(EntryNotActiveException.class, () -> {
			user = service.getActiveUser(userId);
		});
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() {
		user = service.addUser(user);

		assertAll("Properties", 
				() -> assertTrue(user.getCreatedBy().equals("Peter")),
				() -> assertTrue(user.getLastModifiedBy().equals("Peter"))
			);
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() {		
		assertThrows(ConstraintViolationException.class, () -> {
			service.addUser(invalidApplicationUser);
		});
	}


}
