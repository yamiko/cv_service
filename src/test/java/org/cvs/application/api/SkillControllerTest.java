package org.cvs.application.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.transaction.Transactional;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.application.services.SkillService;
import org.cvs.application.services.CandidateService;
import org.cvs.data.entities.Skill;
import org.cvs.data.entities.Candidate;
import org.cvs.data.repositories.SkillRepository;
import org.cvs.utils.Lookup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.JsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.Month;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

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
@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=validate" })
@AutoConfigureMockMvc
public class SkillControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	SkillService skillService;

	@Autowired
	CandidateService candidateService;

	Candidate candidate, candidate2;

	Skill skill, skill2, invalidSkill, fetchedSkill;

	@BeforeEach
	void init() {
		// Create valid skills
		skill = new Skill("Java 8 - Advanced");
		skill.setRetired(Lookup.NOT_RETIRED);
		skill.setVoided(Lookup.NOT_VOIDED);

		skill2 = new Skill(".Net - Advanced");
		skill2.setRetired(Lookup.NOT_RETIRED);
		skill2.setVoided(Lookup.NOT_VOIDED);

		// Create an invalid skill
		invalidSkill = new Skill("");
		invalidSkill.setRetired(Lookup.NOT_RETIRED);
		invalidSkill.setVoided(Lookup.NOT_VOIDED);

		// Create valid candidates
		candidate = new Candidate("John", "", "Smith");
		candidate.setAddressLine1("Address 1");
		candidate.setCountry("UK");
		candidate.setGender("M");
		candidate.setEmail("email@email.com");
		candidate.setDateOfBirth(LocalDate.of(1987, Month.JUNE, 15));
		candidate.setRetired(Lookup.NOT_RETIRED);
		candidate.setVoided(Lookup.NOT_VOIDED);
		
		candidate2 = new Candidate("John2", "", "Smith2");
		candidate2.setAddressLine1("Address 1");
		candidate2.setCountry("UK");
		candidate2.setGender("M");
		candidate2.setEmail("email2@email.com");
		candidate2.setDateOfBirth(LocalDate.of(1987, Month.JUNE, 15));
		candidate2.setRetired(Lookup.NOT_RETIRED);
		candidate2.setVoided(Lookup.NOT_VOIDED);		
		
	}

	@Test
	@WithMockUser
	public void testGetSkills() throws Exception {
		candidate = candidateService.addCandidate(candidate);

		skill.setCandidate(candidate);
		skillService.addSkill(skill);

		skill2.setCandidate(candidate);
		skillService.addSkill(skill2);

		MvcResult result = mockMvc.perform(get("/skills").with(csrf().asHeader())).andExpect(status().isOk())
		        .andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.[1].description").value(".Net - Advanced")).andReturn();

		assertNotNull(result);
	}

	@Test
	@WithMockUser
	public void testGetCandidateSkills() throws Exception {
		candidate = candidateService.addCandidate(candidate);
		candidate2 = candidateService.addCandidate(candidate2);

		skill.setCandidate(candidate);
		skillService.addSkill(skill);

		skill2.setCandidate(candidate2);
		skillService.addSkill(skill2);

		MvcResult result = mockMvc
		        .perform(get("/skills/candidate/{candidateId}", candidate2.getId()).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.[0].candidate.id").value(candidate2.getId())).andReturn();

		assertNotNull(result);
	}

	@Test
	@WithMockUser
	public void testAddSkill() throws Exception {
		candidate = candidateService.addCandidate(candidate);

		skill.setCandidate(candidate);

		MvcResult result = mockMvc
		        .perform(post("/skills").content(asJsonString(skill)).contentType(MediaType.APPLICATION_JSON)
		                .accept(MediaType.APPLICATION_JSON).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.description").value("Java 8 - Advanced")).andReturn();

		assertNotNull(result);

		String response = result.getResponse().getContentAsString();

		Long skillId = ((Integer) (JsonPath.parse(response).read("$.id"))).longValue();
		skill = skillService.getActiveSkill(skillId);

		assertAll("Properties", () -> assertTrue(skill.getId() > 0), () -> assertTrue(skill.getId() > 0),
		        () -> assertTrue(skill.getDescription().equals("Java 8 - Advanced")),
		        () -> assertNotNull(skill.getCreatedDate()), () -> assertNotNull(skill.getCreatedBy()),
		        () -> assertNotNull(skill.getLastModifiedBy()), () -> assertNotNull(skill.getModifiedDate()));
	}

	@Test
	@WithMockUser
	public void testAddWithoutCandidateReference() throws Exception {
		mockMvc.perform(post("/skills").content(asJsonString(skill)).contentType(MediaType.APPLICATION_JSON)
		        .accept(MediaType.APPLICATION_JSON).with(csrf().asHeader())).andExpect(status().isNotFound())
		        .andReturn();
	}

	@Test
	@WithMockUser
	public void testFindNonExistentSkill() throws Exception {
		Long skillId = -1L;

		mockMvc.perform(get("/skills/active/{skillId}", skillId).with(csrf().asHeader()))
		        .andExpect(status().isNotFound()).andReturn();
	}

	@Test
	@WithMockUser
	public void testVoidSkill() throws Exception {
		candidate = candidateService.addCandidate(candidate);

		skill.setCandidate(candidate);
		skill = skillService.addSkill(skill);
		log.info("Added skill with ID: " + skill.getId());

		Long skillId = skill.getId();

		mockMvc.perform(delete("/skills/{skillId}", skillId).with(csrf().asHeader())).andExpect(status().isOk())
		        .andExpect(content().contentType("text/plain;charset=UTF-8")).andReturn();

		assertThrows(EntryNotFoundException.class, () -> {
			skill = skillService.getActiveSkill(skillId);
		});
	}

	@Test
	@WithMockUser
	public void testRetireSkill() throws Exception {
		candidate = candidateService.addCandidate(candidate);

		skill.setCandidate(candidate);
		skill = skillService.addSkill(skill);
		log.info("Added skill with ID: " + skill.getId());

		Long skillId = skill.getId();

		mockMvc.perform(post("/skills/retire/{skillId}", skillId).with(csrf().asHeader())).andExpect(status().isOk())
		        .andExpect(content().contentType("text/plain;charset=UTF-8")).andReturn();

		assertThrows(EntryNotActiveException.class, () -> {
			skill = skillService.getActiveSkill(skillId);
		});
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() throws Exception {
		candidate = candidateService.addCandidate(candidate);

		skill.setCandidate(candidate);
		skill = skillService.addSkill(skill);
		log.info("Added skill with ID: " + skill.getId());

		MvcResult result = mockMvc
		        .perform(post("/skills").content(asJsonString(skill)).contentType(MediaType.APPLICATION_JSON)
		                .accept(MediaType.APPLICATION_JSON).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.description").value("Java 8 - Advanced")).andReturn();

		String response = result.getResponse().getContentAsString();

		Long skillId = ((Integer) (JsonPath.parse(response).read("$.id"))).longValue();
		skill = skillService.getActiveSkill(skillId);

		assertAll("Properties", () -> assertTrue(skill.getCreatedBy().equals("Peter")),
		        () -> assertTrue(skill.getLastModifiedBy().equals("Peter")));
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() throws Exception {
		mockMvc.perform(post("/skills").content(asJsonString(invalidSkill)).contentType(MediaType.APPLICATION_JSON)
		        .accept(MediaType.APPLICATION_JSON).with(csrf().asHeader())).andExpect(status().isNotAcceptable())
		        .andReturn();
	}

	public static String asJsonString(final Object obj) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
