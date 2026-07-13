package com.crawl.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Language Manager class
 * Manages language codes and language names mapping
 * Loads language configuration from an external file
 * 
 * * Copyright (c) 2013 Khaled Kadri
 * Licensed under MIT License
 * https://github.com/khaledkadri
 */
public class Langue {
	
	/** Current language name selected */
	private String langue;
	
	/** Language code corresponding to the selected language */
	private String codelangue;
	
	/** HashMap to store mapping of language names to language codes */
	private HashMap<String, String> listelangues;
	
	/**
	 * Constructor - initializes the language manager and loads language list
	 */
	public Langue(){
		chargerliste();
	}
	
	/**
	 * Load the list of languages from file into a HashMap
	 * Reads from "languages" file with format: code,language_name
	 * Stores mapping of language_name -> code in the HashMap
	 */
	private void chargerliste() {
		// Initialize a new HashMap to store language mappings
		listelangues = new HashMap<>();
		
		// Create a File object pointing to the "languages" file
		File texte = new File("languages");
		
		// File reader and buffered reader for reading the file
    	FileReader entree;
    	BufferedReader readFile;
    	
    	// Variable to store each line read and array to store split data
    	String lire, s[];
    	
    	try {
    		// Initialize FileReader to read from the languages file
    		entree = new FileReader(texte);
    		
    		// Wrap FileReader with BufferedReader for efficient reading
			readFile = new BufferedReader(entree);
			
			// Read file line by line until end of file
			while ((lire = readFile.readLine()) != null ){
				// Each line contains language code followed by comma and language name
				// Example: "en,English" or "fr,French"
				s = lire.split(",");
				
				// Store mapping: language name (s[1]) -> language code (s[0])
				listelangues.put(s[1], s[0]);
			}
			
			// Close the buffered reader after reading is complete
			readFile.close();
		}
    	// Handle FileNotFoundException if languages file is not found
    	catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} 
    	// Handle IOException for other I/O related errors
    	catch (IOException e) {
    		e.printStackTrace();
    	}
	}
	
	/**
	 * Getter method to retrieve the current language name
	 * @return the current language name
	 */
	public String getLangue() {
		return langue;
	}
	
	/**
	 * Setter method to set the current language
	 * Also automatically sets the corresponding language code
	 * @param langue the language name to set
	 */
	public void setLangue(String langue) {
		this.langue = langue;
		// Automatically retrieve and set the corresponding language code from HashMap
		setCodelangue(listelangues.get(langue));
	}
	
	/**
	 * Getter method to retrieve the current language code
	 * @return the language code corresponding to the selected language
	 */
	public String getCodelangue() {
		return codelangue;
	}
	
	/**
	 * Setter method to set the language code
	 * @param codelangue the language code to set
	 */
	public void setCodelangue(String codelangue) {
		this.codelangue = codelangue;
	}
}