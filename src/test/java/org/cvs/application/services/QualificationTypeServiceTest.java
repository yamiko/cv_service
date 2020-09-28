package org.cvs.application.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.data.entities.QualificationType;
import org.cvs.data.repositories.QualificationTypeRepository;
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
 * Integration tests for {@link QualificationTypeRepository}. 
 * 
 * @author Yamiko Msosa
 *
 */
@Transactional
@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=validate" })
public class QualificationTypeServiceTest {

	@Autowired
	QualificationTypeService service;

	QualificationType qualificationType, invalidQualificationType, fetchedQualificationType;

	int FALSE = 0;
	
	@BeforeEach
    void init() {
		// Create a valid qualificationType
		qualificationType = new QualificationType("QualificationType Name1");
		qualificationType.setRetired(Lookup.NOT_RETIRED);
		qualificationType.setVoided(Lookup.NOT_VOIDED);

		// Create an invalid qualificationType
		invalidQualificationType = new QualificationType("");
		invalidQualificationType.setRetired(Lookup.NOT_RETIRED);
		invalidQualificationType.setVoided(Lookup.NOT_VOIDED);
	}

	@Test
	@WithMockUser
	public void testAddQualificationType() {
		qualificationType = service.addQualificationType(qualificationType);

		assertAll("Properties", 
				() -> assertTrue(qualificationType.getId() > 0),
		        () -> assertTrue(qualificationType.getName().equals("QualificationType Name1")), 
		        () -> assertNotNull(qualificationType.getCreatedDate()), 
		        () -> assertNotNull(qualificationType.getCreatedBy()), 
		        () -> assertNotNull(qualificationType.getLastModifiedBy()), 
		        () -> assertNotNull(qualificationType.getModifiedDate()), 
		        () -> {
			        fetchedQualificationType = service.getActiveQualificationType(qualificationType.getId());
			        assertNotNull(fetchedQualificationType);
		        }
			);
	}

	@Test
	@WithMockUser
	public void testFindNonExistentQualificationType() {	
		Long qualificationTypeId = -1L;
						
		assertThrows(EntryNotFoundException.class, () -> {
			qualificationType = service.getActiveQualificationType(qualificationTypeId);
		});
	}
	
	@Test
	@WithMockUser
	public void testVoidQualificationType() {
		qualificationType = service.addQualificationType(qualificationType);
		log.info("Added qualificationType with ID: " + qualificationType.getId());
		
		Long qualificationTypeId = qualificationType.getId();
				
		service.deleteQualificationType(qualificationTypeId);
		
		assertThrows(EntryNotFoundException.class, () -> {
			qualificationType = service.getActiveQualificationType(qualificationTypeId);
		});
	}

	@Test
	@WithMockUser
	public void testRetireQualificationType() {
		qualificationType = service.addQualificationType(qualificationType);
		log.info("Added qualificationType with ID: " + qualificationType.getId());
		
		Long qualificationTypeId = qualificationType.getId();
				
		service.retireQualificationType(qualificationTypeId);
		
		assertThrows(EntryNotActiveException.class, () -> {
			qualificationType = service.getActiveQualificationType(qualificationTypeId);
		});
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() {
		qualificationType = service.addQualificationType(qualificationType);

		assertAll("Properties", 
				() -> assertTrue(qualificationType.getCreatedBy().equals("Peter")),
				() -> assertTrue(qualificationType.getLastModifiedBy().equals("Peter"))
			);
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() {		
		assertThrows(ConstraintViolationException.class, () -> {
			service.addQualificationType(invalidQualificationType);
		});
	}


}
