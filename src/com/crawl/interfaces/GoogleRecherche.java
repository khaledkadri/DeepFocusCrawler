package com.crawl.interfaces;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * GoogleRecherche class - Google Custom Search API interface
 * Performs web searches using Google's Custom Search Engine API
 * 
 * Copyright (c) 2013 Khaled Kadri
 * Licensed under MIT License
 * https://github.com/khaledkadri
 */
public class GoogleRecherche {
	
	/** List to store the URLs returned by Google search results */
	private ArrayList<URL> listeResultats;
	
	/**
	 * Perform a web search using Google Custom Search API
	 * Retrieves up to 100 results (10 pages of 10 results each) for the given keyword
	 * @param motcle the search keyword to search for
	 * @param langue the language code for search filtering (e.g., "en", "fr", "ar")
	 * @return ArrayList of URLs found by the search
	 */
	public ArrayList<URL> requeteDeRecherche(String motcle, String langue){
		// Initialize the results list
		listeResultats = new ArrayList<URL>();
		
		// Google Custom Search API key (authentication)
		// SECURITY NOTE: This API key should be moved to a configuration file
		String key = "YOUR_API_KEY";
		
		// URL object for the API request
		URL url;
		
		// Start index for pagination (Google Custom Search uses 1-based indexing)
		int ii = 1;
		
		try {
			// Loop 10 times to fetch 10 pages of 10 results each (100 total results)
			for(int i = 0; i < 10; i++){
				// Build the Google Custom Search API URL with parameters
				// Parameters: key (API key), cx (search engine ID), q (query), 
				// lr (language restriction), start (pagination), num (results per page)
				url = new URL(
					"https://www.googleapis.com/customsearch/v1?key=" + key + 
					"&cx=013036536707430787589:_pqjad5hr1a&q=" + motcle + 
					"&lr=" + langue + "&start=" + ii + "&num=10");
				
				// Open HTTP connection to the API
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				
				// Set request method to GET
				conn.setRequestMethod("GET");
				
				// Replace user-agent-Crawler by your user-agent name
				//conn.setRequestProperty("User-Agent", "user-agent-Crawler/1.0");
				
				// Set request header to expect JSON response
				conn.setRequestProperty("Accept", "application/json");
				
				// Create buffered reader to read the response line by line
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
				
				// Variable to store each line read from the response
				String output;
				
				// Read response line by line
				while ((output = br.readLine()) != null) {
					// Check if the line contains a URL link in JSON format
					if(output.contains("\"link\": \"")){
						// Extract the URL from the JSON line
						// Find the position after "link": "" and before the closing quote
						String link = output.substring(
							output.indexOf("\"link\": \"") + ("\"link\": \"").length(), 
							output.indexOf("\","));
						
						// Add the extracted URL to the results list
						listeResultats.add(new URL(link));
					}     
				}
				
				// Increment the start index by 10 for the next page of results
				ii = ii + 10;
				
				// Close the HTTP connection
				conn.disconnect();
			}
		} 
		catch (IOException e) {
			// Handle any I/O exceptions that occur during the API request
			e.printStackTrace();
		}
		
		// Return the list of extracted URLs from search results
		return listeResultats;        
	}
	
	/**
	 * Getter method to retrieve the search results
	 * @return ArrayList of URLs from the last search query
	 */
	public ArrayList<URL> getUrl() {
		return listeResultats;
	}
}