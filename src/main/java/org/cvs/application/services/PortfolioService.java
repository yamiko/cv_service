package org.cvs.application.services;

import java.util.List;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.data.entities.Portfolio;
import org.springframework.stereotype.Service;

/**
 * 
 * Provides service operations that can be carried out with portfolios.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@Service
public interface PortfolioService {

	/**
	 * Adds a new {@link Portfolio} instance.
	 *
	 * @param portfolio a portfolio instance to be added 
	 * 
	 * @return a newly added portfolio instance
	 */
	public Portfolio addPortfolio(Portfolio portfolio);

	/**
	 * Fetches an active {@link Portfolio} instance given its name.
	 *
	 * @param name a name to be used as the search key
	 * 
	 * @return an active portfolio instance that matches the search criteria
	 */
	public Portfolio getByPortfolioName(String name) throws EntryNotFoundException;

	/**
	 * Fetches an active {@link Portfolio} instance given its name.
	 *
	 * @param portfolioId ID to be used as the search key
	 * 
	 * @return an active portfolio instance that matches the search criteria
	 */
	public Portfolio getActivePortfolio(Long portfolioId) throws EntryNotFoundException, EntryNotActiveException;

	/**
	 * Marks a given {@link Portfolio} instance as deleted in the database.
	 *
	 * @param portfolioId ID to be used as a key field during search
	 * 
	 */
	public void deletePortfolio(Long portfolioId) throws EntryNotFoundException;

	/**
	 * Marks a given {@link Portfolio} instance as retired in the database.
	 *
	 * @param portfolioId ID to be used as a key field during search
	 * 
	 */
	public void retirePortfolio(Long portfolioId) throws EntryNotFoundException;

	/**
	 * Fetches all active {@link Portfolio} instances from the database.
	 *
	 * @param 
	 * 
	 * @return a list of portfolio instances 
	 */
	public List<Portfolio> getPortfolios();
}
