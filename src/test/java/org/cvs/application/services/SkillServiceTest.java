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
import org.cvs.data.entities.Skill;
import org.cvs.data.repositories.SkillRepository;
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
 * Integration tests for {@link SkillRepository}. 
 * 
 * @author Yamiko Msosa
 *
 */
@Transactional
@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=validate" })
public class SkillServiceTest {

	@Autowired
	SkillService skillService;

	@Autowired
	CandidateService candidateService;


	Skill skill, invalidSkill, fetchedSkill;
	
	Candidate candidate;
	
	int FALSE = 0;
	
	@BeforeEach
    void init() {
		// Create a valid skill
		skill = new Skill("Java 8 - Advanced");
		
		skill.setRetired(Lookup.NOT_RETIRED);
		skill.setVoided(Lookup.NOT_VOIDED);

		//Create an invalid skill
		invalidSkill = new Skill("");

		invalidSkill.setRetired(Lookup.NOT_RETIRED);
		invalidSkill.setVoided(Lookup.NOT_VOIDED);

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
	public void testAddSkill() {
		candidate = candidateService.addCandidate(candidate);
		
		skill.setCandidate(candidate);
		skill = skillService.addSkill(skill);

		assertAll("Properties", 
				() -> assertTrue(skill.getId() > 0),
		        () -> assertTrue(skill.getDescription().equals("Java 8 - Advanced")), 
		        () -> assertNotNull(skill.getCreatedDate()), 
		        () -> assertNotNull(skill.getCreatedBy()), 
		        () -> assertNotNull(skill.getLastModifiedBy()), 
		        () -> assertNotNull(skill.getModifiedDate()), 
		        () -> {
			        fetchedSkill = skillService.getActiveSkill(skill.getId());
			        assertNotNull(fetchedSkill);
		        }
			);
	}

	@Test
	@WithMockUser
	public void testAddWithoutCandidateReference() {
		assertThrows(EntryNotFoundException.class, () -> {
			skill = skillService.addSkill(skill);
		});
	}

	
	@Test
	@WithMockUser
	public void testFindNonExistentSkill() {	
		Long skillId = -1L;
						
		assertThrows(EntryNotFoundException.class, () -> {
			skill = skillService.getActiveSkill(skillId);
		});
	}
	
	@Test
	@WithMockUser
	public void testVoidSkill() {
		candidate = candidateService.addCandidate(candidate);
		
		skill.setCandidate(candidate);
		skill = skillService.addSkill(skill);
		log.info("Added skill with ID: " + skill.getId());
		
		Long skillId = skill.getId();
				
		skillService.deleteSkill(skillId);
		
		assertThrows(EntryNotFoundException.class, () -> {
			skill = skillService.getActiveSkill(skillId);
		});
	}

	@Test
	@WithMockUser
	public void testRetireSkill() {
		candidate = candidateService.addCandidate(candidate);
		
		skill.setCandidate(candidate);
		skill = skillService.addSkill(skill);
		log.info("Added skill with ID: " + skill.getId());
		
		Long skillId = skill.getId();
				
		skillService.retireSkill(skillId);
		
		assertThrows(EntryNotActiveException.class, () -> {
			skill = skillService.getActiveSkill(skillId);
		});
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() {
		candidate = candidateService.addCandidate(candidate);
		
		skill.setCandidate(candidate);
		skill = skillService.addSkill(skill);

		assertAll("Properties", 
				() -> assertTrue(skill.getCreatedBy().equals("Peter")),
				() -> assertTrue(skill.getLastModifiedBy().equals("Peter"))
			);
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() {		
		assertThrows(ConstraintViolationException.class, () -> {
			skillService.addSkill(invalidSkill);
		});
	}
}
