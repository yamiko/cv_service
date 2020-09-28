package org.cvs.application.security;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 
 * Provides Spring security configuration for the application.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	private UserDetailsService userDetailsService;

	private static final String[] AUTH_WHITELIST = { "/v1/samples", "/demo-resources", "/demo-resources/**" };

	/**
	 * Constructor for this class.
	 *
	 * @param userDetailsService    the {@link UserDetailsService} for this
	 *                              application
	 * @param bCryptPasswordEncoder the {@link BCryptPasswordEncoder} for this
	 *                              application
	 * 
	 */
	public WebSecurityConfiguration(UserDetailsService userDetailsService,
	        BCryptPasswordEncoder bCryptPasswordEncoder) {
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Configures HTTP security whilst allowing default URL white list and
	 * <code>/users</> (registration endpoint) for public user registration.
	 *
	 * @param httpSecurity the HttpSecurity object for this application
	 * 
	 */
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.cors().and().csrf().disable().authorizeRequests().antMatchers(AUTH_WHITELIST).permitAll()
		        .antMatchers(HttpMethod.POST, "/users").permitAll().anyRequest().authenticated().and()
		        .addFilter(new AuthenticationFilter(authenticationManager()))
		        .addFilter(new AuthorizationFilter(authenticationManager())).sessionManagement()
		        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

	/**
	 * Configures context for authentication manager
	 *
	 * @param authenticationManagerBuilder the authentication manager builder for
	 *                                     this application
	 * 
	 */
	public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
		authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
		return source;
	}

}
