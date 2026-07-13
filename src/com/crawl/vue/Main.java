package com.crawl.vue;

import javax.swing.JFrame;


/**
 * ========================================
 * WEB CRAWLER APPLICATION
 * ========================================
 * 
 * A powerful multi-threaded web crawler built in Java that automatically
 * discovers and downloads web pages based on keyword searches.
 * 
 * FEATURES:
 * - Google-powered search for initial URL discovery
 * - Recursive link extraction and crawling with depth control
 * - Multi-language support for search queries
 * - Automatic content saving to disk
 * - robots.txt compliance with caching
 * - Real-time progress tracking and monitoring
 * - HTML/Text content filtering
 * 
 * ARCHITECTURE:
 * - Manager: Central coordinator for search and queue management
 * - Extracteur: HTML parser and link extractor using htmlparser library
 * - Sauvegarde: File persistence handler with duplicate title management
 * - Langue: Language code mapper for multilingual support
 * - Observer/Observable: Event notification pattern for UI updates
 * 
 * WORKFLOW:
 * 1. User enters search keyword, language, and depth level
 * 2. Manager launches Google search to get initial URLs
 * 3. Extracteur downloads each page and extracts matching content
 * 4. Links are recursively extracted and added to processing queue
 * 5. Matching results are saved to disk with proper naming
 * 6. UI updates in real-time with progress and results
 * 
 * ========================================
 * LICENSE INFORMATION
 * ========================================
 * 
 * This project is licensed under the MIT License
 * See the LICENSE file in the root directory for full details
 * 
 * DEPENDENCIES:
 * - htmlparser - Licensed under LGPL (Lesser General Public License)
 *   Used for HTML parsing and link extraction
 * 
 * ========================================
 * PROJECT INFORMATION
 * ========================================
 * 
 * Author: Khaled Kadri
 * GitHub: https://github.com/khaledkadri
 * 
 * Copyright (c) 2013 Khaled Kadri
 * 
 * ========================================
 * 
 */

public class Main{
	public static void main(String[] args){
		Fenetre fen = new Fenetre();
		fen.setVisible(true);
		fen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fen.setLocationRelativeTo(null);
	}
}
