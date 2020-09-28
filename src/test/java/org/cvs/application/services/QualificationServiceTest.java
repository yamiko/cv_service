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
import org.cvs.data.entities.Candidate;
import org.cvs.data.entities.Qualification;
import org.cvs.data.entities.QualificationType;
import org.cvs.data.repositories.QualificationRepository;
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
 * Integration tests for {@link QualificationRepository}. 
 * 
 * @author Yamiko Msosa
 *
 */
@Transactional
@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=validate" })
public class QualificationServiceTest {

	@Autowired
	QualificationService qualificationService;

	@Autowired
	CandidateService candidateService;

	@Autowired
	QualificationTypeService qualificationTypeService;

	Qualification qualification, qualification2, invalidQualification, fetchedQualification;
	
	Candidate candidate;
	
	QualificationType qualificationType;

	int FALSE = 0;
	
	@BeforeEach
    void init() {
		// Create valid qualifications
		qualification = new Qualification("PhD in Computer Science", "University of Essex", "UK", LocalDate.of(2016, Month.JUNE, 15));
		qualification.setRetired(Lookup.NOT_RETIRED);
		qualification.setVoided(Lookup.NOT_VOIDED);

		qualification2 = new Qualification("PhD in Chemistry", "University of Kent", "UK", LocalDate.of(2016, Month.JUNE, 15));
		qualification2.setRetired(Lookup.NOT_RETIRED);
		qualification2.setVoided(Lookup.NOT_VOIDED);

		//Create an invalid qualification
		invalidQualification = new Qualification("", "", "", LocalDate.of(2080, Month.JUNE, 15));

		invalidQualification.setRetired(Lookup.NOT_RETIRED);
		invalidQualification.setVoided(Lookup.NOT_VOIDED);

		// Create a valid qualification type
		qualificationType = new QualificationType("Doctorate");
		
		qualificationType.setRetired(Lookup.NOT_RETIRED);
		qualificationType.setVoided(Lookup.NOT_VOIDED);

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
	public void testGetQualifications() {
		candidate = candidateService.addCandidate(candidate);
		qualificationType = qualificationTypeService.addQualificationType(qualificationType);
		
		qualification.setCandidate(candidate);
		qualification.setQualificationType(qualificationType);
		qualification = qualificationService.addQualification(qualification);

		qualification2.setCandidate(candidate);
		qualification2.setQualificationType(qualificationType);
		qualificationService.addQualification(qualification2);
		
		assertTrue(qualificationService.getQualifications().size() == 2);
	}

	
	@Test
	@WithMockUser
	public void testAddQualification() {
		candidate = candidateService.addCandidate(candidate);
		qualificationType = qualificationTypeService.addQualificationType(qualificationType);
		
		qualification.setCandidate(candidate);
		qualification.setQualificationType(qualificationType);
		qualification = qualificationService.addQualification(qualification);

		assertAll("Properties", 
				() -> assertTrue(qualification.getId() > 0),
		        () -> assertTrue(qualification.getName().equals("PhD in Computer Science")), 
		        () -> assertTrue(qualification.getInstitution().equals("University of Essex")), 
		        () -> assertTrue(qualification.getDateObtained().isEqual(LocalDate.of(2016, Month.JUNE, 15))), 
		        () -> assertNotNull(qualification.getCreatedDate()), 
		        () -> assertNotNull(qualification.getCreatedBy()), 
		        () -> assertNotNull(qualification.getLastModifiedBy()), 
		        () -> assertNotNull(qualification.getModifiedDate()), 
		        () -> {
			        fetchedQualification = qualificationService.getActiveQualification(qualification.getId());
			        assertNotNull(fetchedQualification);
		        }
			);
	}

	@Test
	@WithMockUser
	public void testAddWithoutCandidateReference() {
		qualificationType = qualificationTypeService.addQualificationType(qualificationType);
		
		qualification.setQualificationType(qualificationType);

		assertThrows(EntryNotFoundException.class, () -> {
			qualification = qualificationService.addQualification(qualification);
		});
	}

	@Test
	@WithMockUser
	public void testAddWithoutQualificationTypeReference() {
		candidate = candidateService.addCandidate(candidate);
		
		qualification.setCandidate(candidate);

		assertThrows(EntryNotFoundException.class, () -> {
			qualification = qualificationService.addQualification(qualification);
		});
	}

	@Test
	@WithMockUser
	public void testFindNonExistentQualification() {	
		Long qualificationId = -1L;
						
		assertThrows(EntryNotFoundException.class, () -> {
			qualification = qualificationService.getActiveQualification(qualificationId);
		});
	}
	
	@Test
	@WithMockUser
	public void testVoidQualification() {
		candidate = candidateService.addCandidate(candidate);
		qualificationType = qualificationTypeService.addQualificationType(qualificationType);
		
		qualification.setCandidate(candidate);
		qualification.setQualificationType(qualificationType);
		qualification = qualificationService.addQualification(qualification);
		log.info("Added qualification with ID: " + qualification.getId());
		
		Long qualificationId = qualification.getId();
				
		qualificationService.deleteQualification(qualificationId);
		
		assertThrows(EntryNotFoundException.class, () -> {
			qualification = qualificationService.getActiveQualification(qualificationId);
		});
	}

	@Test
	@WithMockUser
	public void testRetireQualification() {
		candidate = candidateService.addCandidate(candidate);
		qualificationType = qualificationTypeService.addQualificationType(qualificationType);
		
		qualification.setCandidate(candidate);
		qualification.setQualificationType(qualificationType);
		qualification = qualificationService.addQualification(qualification);
		log.info("Added qualification with ID: " + qualification.getId());
		
		Long qualificationId = qualification.getId();
				
		qualificationService.retireQualification(qualificationId);
		
		assertThrows(EntryNotActiveException.class, () -> {
			qualification = qualificationService.getActiveQualification(qualificationId);
		});
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() {
		candidate = candidateService.addCandidate(candidate);
		qualificationType = qualificationTypeService.addQualificationType(qualificationType);
		
		qualification.setCandidate(candidate);
		qualification.setQualificationType(qualificationType);
		qualification = qualificationService.addQualification(qualification);

		assertAll("Properties", 
				() -> assertTrue(qualification.getCreatedBy().equals("Peter")),
				() -> assertTrue(qualification.getLastModifiedBy().equals("Peter"))
			);
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() {		
		assertThrows(ConstraintViolationException.class, () -> {
			qualificationService.addQualification(invalidQualification);
		});
	}
}
