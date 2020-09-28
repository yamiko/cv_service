package org.cvs.application.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.transaction.Transactional;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.application.services.QualificationTypeService;
import org.cvs.application.services.PortfolioService;
import org.cvs.data.entities.QualificationType;
import org.cvs.data.entities.Portfolio;
import org.cvs.data.repositories.QualificationTypeRepository;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

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
@AutoConfigureMockMvc
public class QualificationTypeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	QualificationTypeService qualificationTypeService;

	@Autowired
	PortfolioService portfolioService;

	QualificationType qualificationType, qualificationType2, invalidQualificationType, fetchedQualificationType;

	Portfolio portfolio;

	@BeforeEach
	void init() {
		// Create valid qualificationTypes
		qualificationType = new QualificationType("QualificationType Name1");
		qualificationType.setRetired(Lookup.NOT_RETIRED);
		qualificationType.setVoided(Lookup.NOT_VOIDED);

		qualificationType2 = new QualificationType("QualificationType Name2");
		qualificationType2.setRetired(Lookup.NOT_RETIRED);
		qualificationType2.setVoided(Lookup.NOT_VOIDED);

		// Create an invalid qualificationType
		invalidQualificationType = new QualificationType("");
		invalidQualificationType.setRetired(Lookup.NOT_RETIRED);
		invalidQualificationType.setVoided(Lookup.NOT_VOIDED);

	}

	int FALSE = 0;

	@Test
	@WithMockUser
	public void testGetQualificationTypes() throws Exception {
		qualificationTypeService.addQualificationType(qualificationType);
		qualificationTypeService.addQualificationType(qualificationType2);

		MvcResult result = mockMvc.perform(get("/qualifications/types").with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.[1].name").value("QualificationType Name2")).andReturn();

		assertNotNull(result);
	}

	@Test
	@WithMockUser
	public void testAddQualificationType() throws Exception {
		MvcResult result = mockMvc
		        .perform(post("/qualifications/types").content(asJsonString(qualificationType))
		                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
		                .with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.name").value("QualificationType Name1")).andReturn();

		assertNotNull(result);

		String response = result.getResponse().getContentAsString();

		Long qualificationTypeId = ((Integer) (JsonPath.parse(response).read("$.id"))).longValue();
		qualificationType = qualificationTypeService.getActiveQualificationType(qualificationTypeId);

		assertAll("Properties", () -> assertTrue(qualificationType.getId() > 0),
		        () -> assertTrue(qualificationType.getId() > 0), () -> assertTrue(qualificationType.getId() > 0),
		        () -> assertTrue(qualificationType.getName().equals("QualificationType Name1")),
		        () -> assertNotNull(qualificationType.getCreatedDate()),
		        () -> assertNotNull(qualificationType.getCreatedBy()),
		        () -> assertNotNull(qualificationType.getLastModifiedBy()),
		        () -> assertNotNull(qualificationType.getModifiedDate()));
	}

	@Test
	@WithMockUser
	public void testFindNonExistentQualificationType() throws Exception {
		Long qualificationTypeId = -1L;

		mockMvc.perform(
		        get("/qualifications/types/active/{qualificationTypeId}", qualificationTypeId).with(csrf().asHeader()))
		        .andExpect(status().isNotFound()).andReturn();
	}

	@Test
	@WithMockUser
	public void testVoidQualificationType() throws Exception {
		qualificationType = qualificationTypeService.addQualificationType(qualificationType);
		log.info("Added qualificationType with ID: " + qualificationType.getId());

		Long qualificationTypeId = qualificationType.getId();

		mockMvc.perform(
		        delete("/qualifications/types/{qualificationTypeId}", qualificationTypeId).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("text/plain;charset=UTF-8")).andReturn();

		assertThrows(EntryNotFoundException.class, () -> {
			qualificationType = qualificationTypeService.getActiveQualificationType(qualificationTypeId);
		});
	}

	@Test
	@WithMockUser
	public void testRetireQualificationType() throws Exception {
		qualificationType = qualificationTypeService.addQualificationType(qualificationType);
		log.info("Added qualificationType with ID: " + qualificationType.getId());

		Long qualificationTypeId = qualificationType.getId();

		mockMvc.perform(
		        post("/qualifications/types/retire/{qualificationTypeId}", qualificationTypeId).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("text/plain;charset=UTF-8")).andReturn();

		assertThrows(EntryNotActiveException.class, () -> {
			qualificationType = qualificationTypeService.getActiveQualificationType(qualificationTypeId);
		});
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() throws Exception {
		qualificationType = qualificationTypeService.addQualificationType(qualificationType);
		log.info("Added qualificationType with ID: " + qualificationType.getId());
		
		MvcResult result = null;
		result = mockMvc
		        .perform(post("/qualifications/types").content(asJsonString(qualificationType))
		                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
		                .with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.name").value("QualificationType Name1")).andReturn();

		assertNotNull(result);
		String response = result.getResponse().getContentAsString();

		Long qualificationTypeId = ((Integer) (JsonPath.parse(response).read("$.id"))).longValue();
		qualificationType = qualificationTypeService.getActiveQualificationType(qualificationTypeId);

		assertAll("Properties", () -> assertTrue(qualificationType.getCreatedBy().equals("Peter")),
		        () -> assertTrue(qualificationType.getLastModifiedBy().equals("Peter")));
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() throws Exception {
		mockMvc.perform(post("/qualifications/types").content(asJsonString(invalidQualificationType))
		        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).with(csrf().asHeader()))
		        .andExpect(status().isNotAcceptable()).andReturn();
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
