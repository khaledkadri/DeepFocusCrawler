package com.crawl.manager;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.crawl.downloader.Extracteur;
import com.crawl.downloader.Noeud;
import com.crawl.interfaces.GoogleRecherche;
import com.crawl.observer.Observable;
import com.crawl.observer.Observer;
import com.crawl.vue.Fenetre;

/**
 * Manager class - Central coordinator for the web crawler application
 * Implements both Observer and Observable patterns for event notification
 * Handles search initialization, queue management, and URL processing
 * 
 * * Copyright (c) 2013 Khaled Kadri
 * Licensed under MIT License
 * https://github.com/khaledkadri
 * 
 */
public class Manager implements Observer, Observable{
	
	/** Search keyword or phrase to look for */
	private String chaineDeRecherche;
	
	/** Directory location where downloaded content will be saved */
	private String emplacement;
	
	/** Language manager instance for handling language codes */
	private Langue langue;
	
	/** Google search interface for performing web searches */
	private GoogleRecherche grech;
	
	/** Queue of URLs to be processed with their depth levels */
	private ArrayList<Noeud> queue = new ArrayList<Noeud>();
	
	/** Extractor instance for extracting links from downloaded pages */
	private Extracteur extracteur;
	
	/** List of observers to be notified of state changes */
	private ArrayList<Observer> listObserver = new ArrayList<Observer>();
	
	/** Maximum crawling depth for recursive link extraction */
	private int profondeur;
	
	/** Counter for processed URLs */
	private int urlTraites;
	
	/**
	 * Constructor - initializes the Manager and sets up language handling
	 */
	public Manager(){
		this.langue = new Langue();
	}
	
	/**
	 * Initialize the search operation by resetting counters and creating search engine instance
	 */
	public void init(){
		// Reset the processed URLs counter
		urlTraites = 0;
		// Initialize Google search interface
		this.grech = new GoogleRecherche();
	}
	
	/**
	 * Launch the initial search using Google and populate the queue with results
	 * Performs keyword analysis and adds unique URLs to the queue
	 */
	public void lancerrecherche(){
		// Append the search keyword to the download location path
		this.emplacement = this.emplacement + this.chaineDeRecherche;
		
		// Replace spaces with %20 URL encoding since URLs cannot contain spaces
		String chaineAnalysee = chaineDeRecherche.replaceAll(" ", "%20");
		
		// Execute Google search with the encoded keyword and language code
		grech.requeteDeRecherche(chaineAnalysee, langue.getCodelangue());
		
		// Get the list of URLs returned by the search
		ArrayList<URL> listeurls = grech.getUrl();
		
		// Add search results to the queue, ensuring each URL is added only once
		for(URL url : listeurls){
			// Check if URL is not already in the queue
			if(!this.queue.contains(url)){
				// Add URL to queue with initial depth level
				this.queue.add(new Noeud(url, profondeur));
			}
		}
		
		// Notify observers to update the seed display with initial results
		notifyObserver();
	}
	
	/**
	 * Process the queue of URLs by iterating through and downloading/extracting content
	 * Validates page types and handles recursive extraction with depth control
	 */
	public void parcourirLaQueue(){
		// Initialize the extractor with search keyword, location, and observers
		extracteur = new Extracteur(chaineDeRecherche, emplacement, this.listObserver);
		
		// Process queue only if it is not empty
		if(!queue.isEmpty()){
			// Iterate through all URLs in the queue
			for(int i = 0; i < queue.size(); i++){
				// Get the URL from the current node
				URL url = queue.get(i).getUrl();
				
				System.out.println(i+"  "+url);
				
				// Validate that the page is HTML or text type before processing
				if(PageHtmlText(url)){
					// Notify observers of the current URL being processed
					notifyAsp(url);
					
					// Set the extraction depth to the initial profondeur value
					extracteur.setProfondeur(profondeur);
					
					// Send the URL to the downloader/extractor module
					envoyerurl(queue.get(i));
					
					// MANDATORY DELAY (minimum 2-5 seconds)
			        try {
			            Thread.sleep(2000 + new Random().nextInt(3000));
			        } catch (InterruptedException e) {
			            e.printStackTrace();
			        }
					
					// Decrement counter to reprocess this position if new URLs are added
					i--;
				}
				else{
					// Increment processed counter if page type is not supported
					urlTraites++;
					// Notify observers of the processed URL count
					listObserver.get(0).updateurlTraites(urlTraites);
					// Log error message for incorrect file type
					System.out.println("Incorrect extension : " + url);
				}
			}
		}
		else {
			System.err.println("Queue is empty !");
		}
	}
	
	/**
	 * Validate if a URL points to an HTML or text file
	 * Uses regular expressions to check the content type header
	 * @param url the URL to validate
	 * @return true if URL is HTML or text type, false otherwise
	 */
	private boolean PageHtmlText(URL url) {
		try {
			// Open a connection to the URL to access its headers
			URLConnection conn = url.openConnection();
			
			// Get the content type from the connection header
			String typePage = conn.getContentType();
			
			// Create regex pattern to match HTML or text content types
			Pattern tag = Pattern.compile("text/html|text/plain");
			
			// Check if content type is not null
			if(typePage != null){
				// Apply regex pattern to the content type
				Matcher mtag = tag.matcher(typePage);
				
				// If pattern matches, extract the matched group
				while (mtag.find()) 
					typePage = mtag.group(0);
				
				// Return true if content type is HTML or plain text
				if(typePage.equals("text/html") || typePage.equals("text/plain"))
					return true;
			}
		} 
		catch (IOException e) {
			// Silently handle IOException
		}
		
		// Return false if content type is not supported
		return false;
	}

	/**
	 * Set the search keyword
	 * @param mot the search keyword to set
	 */
	public void setMot(String mot) {
		this.chaineDeRecherche = mot;
	}

	/**
	 * Set the language for the search
	 * @param langue the language name to set
	 */
	public void setLangue(String langue) {
		this.langue.setLangue(langue);
	}

	/**
	 * Get the queue of URLs to be processed
	 * @return ArrayList of Noeud objects containing URLs
	 */
	public ArrayList<Noeud> getURLs() {
		return queue;
	}

	/**
	 * Set the download location directory
	 * @param emplacement the directory path where content will be saved
	 */
	public void setEmplacement(String emplacement) {
		this.emplacement = emplacement;
	}
	
	/**
	 * Set the maximum crawling depth
	 * @param Profondeur the depth level for recursive link extraction
	 */
	public void setProfondeur(int Profondeur) {
		this.profondeur = Profondeur;
	}
	
	/**
	 * Add an observer to the list of objects to be notified of state changes
	 * Adds both the GUI frame and the Manager instance as observers
	 * @param fenetre the GUI frame (window) to add as observer
	 */
	@Override
	public void addObserver(Fenetre fenetre) {
		// Add the GUI frame as observer
		this.listObserver.add(fenetre);
		// Add the Manager itself as observer
		this.listObserver.add(this);
	}

	/**
	 * Notify all observers to update the seed URLs display table
	 * Constructs an array of URLs from the queue and sends it to observers
	 */
	@Override
	public void notifyObserver() {
		// Create array to hold all URLs from the queue
		Object tab[] = new Object[this.queue.size()];
		
		// Populate array with URLs from queue
		for(int i = 0; i < this.queue.size(); i++)
			tab[i] = this.queue.get(i).getUrl();
		
		// Notify first observer (GUI frame) to update the seed URLs table
		listObserver.get(0).update(tab);
	}

	/**
	 * Notify observers of the current URL being crawled
	 * @param url the URL currently being processed
	 */
	@Override
	public void notifyAsp(URL url) {
		// Notify first observer (GUI frame) to display current URL
		listObserver.get(0).updateurl(url);
	}

	/**
	 * Update method - receives objects from other observers (not implemented)
	 * @param objects array of objects to receive
	 */
	@Override
	public void update(Object[] objects) {
		// TODO Auto-generated method stub
	}

	/**
	 * Update method - receives URL from other observers (not implemented)
	 * @param url the URL received
	 */
	@Override
	public void updateurl(URL url) {
		// TODO Auto-generated method stub
	}

	/**
	 * Update method - receives progress bar maximum value (not implemented)
	 * @param taille the maximum value for progress bar
	 */
	@Override
	public void updatepb(int taille) {
		// TODO Auto-generated method stub
	}

	/**
	 * Send a URL node to the extractor for downloading and link extraction
	 * @param url the Noeud (node) containing the URL to send
	 */
	@Override
	public void envoyerurl(Noeud url) {
		// Send URL node to third observer (Extractor) for processing
		this.listObserver.get(2).recevoirUrl(url);
	}

	/**
	 * Receive a new URL node from the extractor (discovered during extraction)
	 * Adds the discovered URL to the queue if it is not already present
	 * @param noeud the Noeud containing the newly discovered URL
	 */
	@Override
	public void recevoirUrl(Noeud noeud) {
		// Check if URL is not already in the queue
		if(!this.queue.contains(noeud.getUrl())){
			// Add the new node to the end of the queue for processing
			this.queue.add(noeud);
			// Notify observers of the new queue size for progress bar update
			listObserver.get(0).updatepb(this.queue.size());
		}
	}

	/**
	 * Remove the first URL from the queue after it has been processed
	 * Updates the processed URLs counter
	 * @param noeud the Noeud to remove from the queue
	 */
	@Override
	public void supprimeUrl(Noeud noeud) {
		// Remove the first element from the queue
		this.queue.remove(0);
		// Increment the counter of processed URLs
		urlTraites++;
		// Notify observers of the updated processed URLs count
		listObserver.get(0).updateurlTraites(urlTraites);
	}

	/**
	 * Update method - receives processed URLs count (not implemented)
	 * @param urlTraites the number of processed URLs
	 */
	@Override
	public void updateurlTraites(int urlTraites) {
		// TODO Auto-generated method stub
	}

	/**
	 * Update method - receives search result with URL and title (not implemented)
	 * @param url the discovered URL
	 * @param titre the page title
	 */
	@Override
	public void updateresultat(String url, String titre, int status) {
		// TODO Auto-generated method stub
	}

	/**
	 * Update method - receives updated depth level (not implemented)
	 * @param profondeur the remaining depth level
	 */
	@Override
	public void updateprfondeur(int profondeur) {
		// TODO Auto-generated method stub
	}
}