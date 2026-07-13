package com.crawl.downloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.crawl.observer.Observable;
import com.crawl.observer.Observer;
import com.crawl.vue.Fenetre;

/**
 * Extracteur class - Handles HTML parsing, link extraction, and content analysis
 * Implements both Observer and Observable patterns
 * Processes downloaded pages to extract links, text, and titles
 * Respects robots.txt restrictions with caching
 * 
 * * Copyright (c) 2013 Khaled Kadri
 * Licensed under MIT License
 * https://github.com/khaledkadri
 */
public class Extracteur implements Observer, Observable{
	
	/** Maximum crawling depth level */
	int profondeur;
	
	/** Instance for saving extracted content to disk */
	Sauvegarde sauvgarde;
	
	/** List of observers to be notified of changes */
	ArrayList<Observer> listObserver;
	
	/** HTML parser for parsing web pages */
	Parser parser;
	
	/** Search keyword to find in page content */
	String motrech;
	
	/** Cache HashMap storing disallowed paths from robots.txt files */
	private HashMap cacheListeInterdite = new HashMap();
	
	/**
	 * Constructor - initializes the extractor with search parameters
	 * @param motrech the search keyword to look for
	 * @param emplacement the directory where content will be saved
	 * @param listObserver the list of observers to notify
	 */
	public Extracteur(String motrech, String emplacement, ArrayList<Observer> listObserver){
		// Store the search keyword
		this.motrech = motrech;
		// Initialize the save handler with location and observers
		sauvgarde = new Sauvegarde(emplacement, listObserver);
		// Store the observer list
		this.listObserver = listObserver;
		// Add this extractor as an observer
		this.listObserver.add(this);
	}

	/**
	 * Extract all hyperlinks from a page using HTML parser
	 * Handles relative URLs, filters invalid links, and respects depth limits
	 * @param noeudP the node containing the URL to extract links from
	 * @return ArrayList of extracted URLs
	 */
	public ArrayList<URL> extraireLiens(Noeud noeudP) {
		ArrayList<URL> result = new ArrayList<URL>();
		
		// Only extract links if maximum depth has not been reached
		if(noeudP.getProfondeur() > 1){
			// Reset the parser to start fresh
			parser.reset();
			
			try {
				// Extract all link nodes using a NodeClassFilter
				NodeList liste = parser.extractAllNodesThatMatch(new NodeClassFilter(LinkTag.class));
				
				// Iterate through all found link nodes
				for (int i = 0; i < liste.size(); i++){
					// Get the link tag from the list
					LinkTag extrait = (LinkTag)liste.elementAt(i);
					
					// Skip non-HTTP/HTTPS links
					if(!extrait.isHTTPLikeLink()) 
						continue;
					
					// Extract the link URL
					String lienExtrait = extrait.extractLink();
					
					// Replace spaces with %20 URL encoding since URI class doesn't support spaces
					lienExtrait = lienExtrait.replaceAll(" ", "%20");
					
					// Remove leading and trailing whitespace
					lienExtrait = lienExtrait.trim();
					
					// Skip empty links as they would raise exceptions
					if(lienExtrait.length() == 0) 
						continue;
					
					// Skip JavaScript protocol links (not valid for crawling)
					if(lienExtrait.matches("(?i)^javascript:.*"))
						continue;
					
					// Skip anchor links (#) as they are not useful for crawling
					if(lienExtrait.startsWith("#")) 
						continue;
					
					// Remove URL fragments after &amp; (often incomplete URLs)
					if(lienExtrait.contains("&amp"))
						lienExtrait = lienExtrait.substring(0, lienExtrait.indexOf("&amp"));
					
					// Create URI from the extracted link
					URI uriRelative = new URI(lienExtrait);
					
					// Get the parent URL as a URI
					URI lienUri = noeudP.getUrl().toURI();
					
					// Resolve relative URLs to absolute URLs
					// Example: "ext.html" becomes "http://www.foo.com/ext.html"
					URI resolu = lienUri.resolve(uriRelative);

					String scheme = resolu.getScheme();
					if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
						
						// Check if the link is not already in the result list
						if(!result.contains(resolu.toString()) && !result.contains(resolu.toString() + "/")){
							// Add resolved URL to result list
							result.add(resolu.toURL());
							
							// Send the URL to be placed in the manager's queue
							// Create a child node (URL found in parent page) and decrement depth by 1
							envoyerurl(new Noeud(resolu.toURL(), noeudP.getProfondeur() - 1));
						}
					}
				}
			} 
			catch (URISyntaxException e) {
				// Handle bad link syntax error (do not process malformed links)
			} 
			catch (MalformedURLException e) {
				// Handle malformed URL error
				e.printStackTrace();
			} 
			catch (ParserException e) {
				// Handle parser exception (malformed HTML)
			}
		}
		
		// Return the list of extracted URLs
		return result;
    }
	
	/**
	 * Extract all text content from the current page
	 * @return String containing all text from the page
	 */
	private String extraireTexte(){
		// Reset parser to start fresh
		if(parser==null) return "";
    	parser.reset();
    	
    	// Create a StringBean visitor to extract text nodes
    	StringBean sb = new StringBean();
    	
        try {
        	// Visit all nodes and extract text content
			parser.visitAllNodesWith(sb);
		} 
        catch (ParserException e) {
			// Handle parser exception
		}
		
		// Return extracted text strings
		return sb.getStrings();
	}
	
	/**
	 * Extract the page title from the HTML title tag
	 * @return String containing the page title, empty string if not found
	 */
	public String extraireTitre(){
		// Node filter for selecting title tags
		NodeFilter filter;
		
		// List to store found nodes
		NodeList list = null;
		
		// Reset parser to start fresh
        parser.reset();
        
        // Create a filter to extract only title tags
    	filter = new NodeClassFilter(TitleTag.class);
    	
    	try {
    		// Apply the filter to extract only title tags
			list = parser.extractAllNodesThatMatch(filter);
		} 
        catch (ParserException e) {
			// Handle parser exception
		}
    	
    	// Return the title if found, otherwise return empty string
    	if(list != null)
    		return list.elementAt(0).toPlainTextString();
    	
    	return "";
	}

	/**
	 * Determine if the search string is found in the page content
	 * @param texte the text content of the page
	 * @param chainerecherche the search keyword to find
	 * @return true if search keyword is found, false otherwise
	 */
   private boolean chainesCorrespondentes(String texte, String chainerecherche) {
       String Contenu = "";
       
       // Convert page content to lowercase for case-insensitive comparison
	   if(texte != null)
       Contenu = texte.toLowerCase();
       
       // Check if search keyword exists in page content
       if(Contenu.contains(chainerecherche))
    	   return true;
    	   
       return false;
   }
   
   /**
    * Check if the robot is authorized to access the given URL
    * Reads and caches robots.txt files to respect crawling restrictions
    * @param urlaTester the URL to check authorization for
    * @return true if robot is allowed to crawl this URL, false otherwise
    */
   private boolean siRobotAutorise(URL urlaTester) {
       // Get the hostname in lowercase for consistency
       String host = urlaTester.getHost().toLowerCase();
       
       // Retrieve the list of disallowed paths from cache
       ArrayList listeInterdite = (ArrayList) cacheListeInterdite.get(host);
       
       // If list is not in cache, download and cache it
       if (listeInterdite == null) {
           // Initialize new list for this host
           listeInterdite = new ArrayList();
           
           try {
               // Create URL pointing to robots.txt file on the host
               URL urlFichierRobot = new URL("http://" + host + "/robots.txt");
               
               // Open connection to robots.txt file for reading
               BufferedReader reader = new BufferedReader(new InputStreamReader(urlFichierRobot.openStream()));
               
               // Read robots.txt file and create list of disallowed paths
               String line;
               
               while ((line = reader.readLine()) != null) {
                   // Check if line contains a Disallow directive
                   if (line.indexOf("Disallow:") == 0) {
                       // Extract the disallowed path
                       String cheminInterdit = line.substring("Disallow:".length());
                       
                       // Remove comments from the path
                       int commentIndex = cheminInterdit.indexOf("#");
                       if (commentIndex != -1) {
                           cheminInterdit = cheminInterdit.substring(0, commentIndex);
                       }
                       
                       // Remove leading and trailing whitespace
                       cheminInterdit = cheminInterdit.trim();
                       
                       // Add the disallowed path to the list
                       listeInterdite.add(cheminInterdit);
                   }
               }
               
               // Cache the disallowed paths list for this host
               cacheListeInterdite.put(host, listeInterdite);
           } 
           catch (Exception e) {
        	   // Assume URL is authorized if robots.txt file doesn't exist or is unreachable
               return true;
           }
       }
       
       // Check if the URL path is in the disallowed list
       String file = urlaTester.getFile();
       for (int i = 0; i < listeInterdite.size(); i++) {
           // Get disallowed path from list
           String rejeter = (String) listeInterdite.get(i);
           
           // If URL path starts with disallowed path, deny access
           if (file.startsWith(rejeter)) {
               return false;
           }
       }
       
       // URL is allowed if not in disallowed list
       return true;
   }
   
   /**
    * Set the maximum crawling depth level
    * @param profondeur the depth level to set
    */
   public void setProfondeur(int profondeur) {
		this.profondeur = profondeur;
	}
   
   /**
    * Send a node (URL) to the manager for queue processing
    * @param noeud the node containing the URL to send
    */
   @Override
	public void envoyerurl(Noeud noeud) {
		// Send node to the second observer (Manager) to add to queue
		this.listObserver.get(1).recevoirUrl(noeud);
	}

	/**
	 * Receive a URL node from the manager for processing
	 * Downloads page, extracts content and links if search keyword is found
	 * @param noeud the node containing the URL to process
	 */
	@Override
	public void recevoirUrl(Noeud noeud) {
		// Get the URL from the node
		URL url = noeud.getUrl();
		
		// Check if robot is authorized to access this URL
       if(siRobotAutorise(url)) 
       {
       	try {
       		// Create a new parser to download and parse the page
       		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
       		connection.setRequestMethod("GET");
       		connection.setConnectTimeout(5000);
       		connection.setReadTimeout(5000);

       		// Connect to the server
       		connection.connect();

       		// Get HTTP status code
       		int statusCode = connection.getResponseCode();

       		// handle redirects manually if needed
       		if (statusCode == HttpURLConnection.HTTP_MOVED_PERM ||
       		    statusCode == HttpURLConnection.HTTP_MOVED_TEMP) {
       		    String redirectUrl = connection.getHeaderField("Location");
       		    System.out.println("Redirected to: " + redirectUrl);
       		}
       		
   			noeud.setStatus(statusCode);

       		// If status code is OK, parse the content
       		if (statusCode == 200) {
       		    //parser = new Parser(connection);
           		parser = new Parser(noeud.getUrl().toString());
       		    parser.setEncoding("UTF-8");

       		    // Then extract text or title, etc.
       		    String text = extraireTexte();
       		} else {
       		    System.out.println("Failed to load: " + url + " (Status: " + statusCode + ")");
       		}

       	} 
       	catch (ParserException | IOException e) {
       		// Handle parser exception (page download or parsing error)
       		//System.err.println(e.getStackTrace());
       	}
       	
       	// Extract text content from the downloaded page
       	String texte = extraireTexte();
		
       	// Check if the search keyword is found in the page content
       	if(chainesCorrespondentes(texte, motrech)){
       		// Store the extracted text in the node
       		noeud.setTexte(texte);
       		
       		// Extract the page title
       		String titre = extraireTitre();
       		// Store the title in the node
       		noeud.setTitre(titre);
       		
       		// Save the page text and title to disk
       		sauvgarde.serialize(noeud.getTexte(), noeud.getTitre(), url.toString(), noeud.getStatus());
       		
       		// Extract all links from the page for further crawling
       		extraireLiens(noeud);
       	}
       	else {
       		System.err.println("Searched string not found");
       	}
       }
       else {
    	   System.out.println("URL blocked by robots.txt: " + url);
    	   supprimeUrl(noeud);
    	   return;
       }
       
       // After processing the page and extracting links, remove it from the queue
       supprimeUrl(noeud);
	}

	/**
	 * Remove a processed URL node from the manager's queue
	 * @param noeud the node to remove from queue
	 */
	@Override
	public void supprimeUrl(Noeud noeud) {
		// Send remove request to the second observer (Manager) to delete from queue
		this.listObserver.get(1).supprimeUrl(noeud);
	}

	/**
	 * Notify all observers of state changes (not implemented)
	 */
	@Override
	public void notifyObserver() {
		// TODO Auto-generated method stub
	}

	/**
	 * Add a frame as an observer (not implemented)
	 * @param fenetre the GUI frame to add as observer
	 */
	@Override
	public void addObserver(Fenetre fenetre) {
		// TODO Auto-generated method stub
	}

	/**
	 * Notify observers of current URL being processed (not implemented)
	 * @param url the current URL
	 */
	@Override
	public void notifyAsp(URL url) {
		// TODO Auto-generated method stub
	}

	/**
	 * Update method - receives array of objects (not implemented)
	 * @param objects array of objects to receive
	 */
	@Override
	public void update(Object[] objects) {
		// TODO Auto-generated method stub
	}

	/**
	 * Update method - receives URL (not implemented)
	 * @param url the URL to receive
	 */
	@Override
	public void updateurl(URL url) {
		// TODO Auto-generated method stub
	}

	/**
	 * Update method - receives progress bar maximum value (not implemented)
	 * @param taille the progress bar maximum
	 */
	@Override
	public void updatepb(int taille) {
		// TODO Auto-generated method stub
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
	 * Update method - receives depth level update (not implemented)
	 * @param profondeur the updated depth level
	 */
	@Override
	public void updateprfondeur(int profondeur) {
		// TODO Auto-generated method stub
	}

	/**
	 * Update method - receives search result (not implemented)
	 * @param url the result URL
	 * @param titre the result title
	 */
	@Override
	public void updateresultat(String url, String titre,int status) {
		// TODO Auto-generated method stub
	}
}