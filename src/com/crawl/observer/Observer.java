package com.crawl.observer;
import java.net.URL;

import com.crawl.downloader.Noeud;

public interface Observer{
	public void update(Object[] objects);
	public void updateresultat(String url,String titre, int status);
	public void updateurl(URL url);
	public void updatepb(int taille);
	public void updateprfondeur(int profondeur);
	public void updateurlTraites(int urlTraites);
	public void recevoirUrl(Noeud noeud);
	public void supprimeUrl(Noeud noeud);
}
