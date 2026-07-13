package com.crawl.downloader;

import java.net.URL;

/**
 * Noeud class - Represents a node in the web crawler's processing queue
 * Stores URL information along with metadata about extracted content
 * Used to track URLs at different crawling depth levels
 * 
 * * Copyright (c) 2013 Khaled Kadri
 * Licensed under MIT License
 * https://github.com/khaledkadri
 */
public class Noeud {
	
	/** The URL this node represents */
	private URL url;
	
	/** The crawling depth level for this URL (used to control recursion depth) */
	private int profondeur;
	
	/** The title of the web page at this URL */
	private String titre;
	
	/** The text content extracted from the web page at this URL */
	private String texte;
	
	/** THe status of the downloaded page **/
	private int Status;

	/**
	 * Constructor - creates a node with a URL and depth level
	 * @param url the URL this node represents
	 * @param profondeur the crawling depth level for this URL
	 */
	public Noeud(URL url, int profondeur){
		this.url = url;
		this.profondeur = profondeur;
	}

	/**
	 * Getter method to retrieve the page title
	 * @return the title of the web page
	 */
	public String getTitre() {
		return titre;
	}
	
	/**
	 * Setter method to set the page title
	 * @param titre the page title to set
	 */
	public void setTitre(String titre) {
		this.titre = titre;
	}
	
	/**
	 * Getter method to retrieve the page text content
	 * @return the extracted text content from the page
	 */
	public String getTexte() {
		return texte;
	}
	
	/**
	 * Setter method to set the page text content
	 * @param texte the text content to set
	 */
	public void setTexte(String texte) {
		this.texte = texte;
	}

	/**
	 * Getter method to retrieve the URL
	 * @return the URL this node represents
	 */
	public URL getUrl() {
		return url;
	}
	
	/**
	 * Setter method to set the URL
	 * @param url the URL to set
	 */
	public void setUrl(URL url) {
		this.url = url;
	}
	
	/**
	 * Getter method to retrieve the crawling depth level
	 * @return the depth level for this URL
	 */
	public int getProfondeur() {
		return profondeur;
	}
	
	/**
	 * Setter method to set the crawling depth level
	 * @param profondeur the depth level to set
	 */
	public void setProfondeur(int profondeur) {
		this.profondeur = profondeur;
	}
	
	public int getStatus() {
		return Status;
	}
	
	public void setStatus(int Status) {
		this.Status = Status;
	}
	
}