package com.crawl.observer;
import java.net.URL;

import com.crawl.downloader.Noeud;
import com.crawl.vue.Fenetre;

public interface Observable{
	public void notifyObserver();
	public void addObserver(Fenetre fenetre);
	public void notifyAsp(URL url);
	public void envoyerurl(Noeud noeud);
	public void supprimeUrl(Noeud noeud);
}
