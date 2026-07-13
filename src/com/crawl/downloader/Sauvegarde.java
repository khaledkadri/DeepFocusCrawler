package com.crawl.downloader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

import com.crawl.observer.Observable;
import com.crawl.observer.Observer;
import com.crawl.vue.Fenetre;

/**
 * Sauvegarde class - Handles saving extracted page content to disk
 * Implements Observable pattern to notify observers of saved results
 * Manages file naming, directory creation, and duplicate title handling
 * 
 * * Copyright (c) 2013 Khaled Kadri
 * Licensed under MIT License
 * https://github.com/khaledkadri
 */
public class Sauvegarde implements Observable{
	
	/** Directory path where downloaded content will be saved */
	String emplacement;
	
	/** List of observers to be notified when content is saved */
	public ArrayList<Observer> listObserver;
	
	/** HashMap to track and handle duplicate file titles */
	private HashMap<String, Integer> existe = new HashMap<String, Integer>();
	
	/** Current page title being processed */
	String titre;
	
	/** Current page URL being processed */
	String url;
	
	/** Status of the URL being processed */
	int status;
	
	/** File counter for numbering saved files */
	int i = 0;
	
	/**
	 * Constructor - initializes the save handler with directory and observers
	 * Creates the download directory if it does not exist
	 * @param emplacement the directory path where files will be saved
	 * @param listObserver the list of observers to notify on save
	 */
	public Sauvegarde(String emplacement, ArrayList<Observer> listObserver) {
		// Store the download directory path
		this.emplacement = emplacement;
		// Store the list of observers
		this.listObserver = listObserver;
		
		// Create the directory at the specified location if it doesn't exist
		// This directory will contain all downloaded and extracted content
		new File(this.emplacement).mkdir();
	}
	
	/**
	 * Serialize and save page content to disk as a text file
	 * Handles file naming with title verification and duplicate handling
	 * Notifies observers after successful save
	 * @param texte the extracted text content to save
	 * @param titre the page title to use as filename
	 * @param url the source URL of the page
	 */
	public void serialize(String texte, String titre, String url, int status){
		// Store the source URL for notification
		this.url = url;
		this.status = status;
		
		// Verify and clean the title for use as filename
		// Removes invalid filename characters and handles duplicates
		verifierTitre(titre);
		
		try {
			// Increment file counter for sequential numbering
			i++;
			
			// Create output stream chain for writing to file
			// Format: emplacement/counter + title + .txt
			// Example: downloads/1 - HomePage.txt
			ObjectOutputStream oos = new ObjectOutputStream(
										new BufferedOutputStream(
												new FileOutputStream(
														new File(emplacement + "/" + i + " - " + this.titre + ".txt"))));
			
			// Write the extracted text content to the file
			oos.write(texte.getBytes());
			
			// Close the output stream to finalize the file
			oos.close();
			
			// Notify all observers that a result has been saved
			notifyObserver();
		} 
		catch (FileNotFoundException e) {
			// Display error message if file cannot be found or created
			JOptionPane.showMessageDialog(null, "Erreur d'enregistrement dans le fichier !", "ERREUR", JOptionPane.ERROR_MESSAGE);
		} 
		catch (IOException e) {
			// Display error message if I/O error occurs during writing
			JOptionPane.showMessageDialog(null, "Erreur d'enregistrement dans le fichier !", "ERREUR", JOptionPane.ERROR_MESSAGE);
		}
		
		// Increment counter for next file (counter is incremented twice: before save and after)
		i++;
	}
	
	/**
	 * Verify and clean the page title for use as a filename
	 * Removes invalid filename characters and handles duplicate titles
	 * Invalid characters: : / \ * ? = < > " and control characters (tab, newline, carriage return)
	 * @param titre the original page title to verify and clean
	 */
	void verifierTitre(String titre){
		// Create regex pattern to match invalid filename characters
		// Invalid chars: : / \ * ? = < > " (char 34) TAB (char 9) LF (char 10) CR (char 13) |
		Pattern tag = Pattern.compile("[:/\\*?=<>" + (char)34 + (char)9 + (char)10 + (char)13 + "|]" + ".*?");
		
		// Check if title is not null before processing
		if(titre != null){
			// Create matcher to find invalid characters
			Matcher mtag = tag.matcher(titre);
			
			// Replace all invalid characters with empty string
			while (mtag.find()) 
				titre = mtag.replaceAll("");
			
			// Remove leading and trailing whitespace
			this.titre = titre.trim();
			
			// Check if this title has been used before (duplicate handling)
			if(existe.containsKey(this.titre)){
				// If title exists, append a number to make it unique
				existe.put(titre, existe.get(this.titre) + 1);
				this.titre += existe.get(this.titre);
			}
			else{
				// If title is new, add it to the HashMap with count 1
				existe.put(this.titre, 1);
			}
		}
	}
	
	/**
	 * Notify all observers of the saved result
	 * Observers are updated with the URL and cleaned title of the saved content
	 */
	@Override
	public void notifyObserver() {
		// TODO Auto-generated method stub
		// Iterate through all observers and notify them of the result
		// This updates the GUI results table with the new saved page information
		for(Observer obs : this.listObserver)
			obs.updateresultat(this.url, this.titre, this.status);
	}
	
	/**
	 * Add an observer to the notification list (not implemented)
	 * @param fenetre the GUI frame to add as observer
	 */
	@Override
	public void addObserver(Fenetre fenetre) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Notify observers of current URL aspiration (not implemented)
	 * @param url the current URL being processed
	 */
	@Override
	public void notifyAsp(URL url) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Send a URL node to observers (not implemented)
	 * @param noeud the node containing URL to send
	 */
	@Override
	public void envoyerurl(Noeud noeud) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Remove a URL node from processing (not implemented)
	 * @param noeud the node to remove
	 */
	@Override
	public void supprimeUrl(Noeud noeud) {
		// TODO Auto-generated method stub
	}
}