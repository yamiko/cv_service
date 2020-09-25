package org.cvs.data.entities;

import java.util.Optional;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 
 * Provides user details from the application's security context for auditing
 * purposes.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@EnableAsync
@SpringBootApplication
@EnableJpaAuditing
public class AuditingConfiguration {

	@Bean
	public AuditorAware<String> auditorProvider() {
		return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication().getName());
	} 
}
