package com.rest.szz.entities;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LucaPellegrini
 *
 * Class for the links management
 */
public class LinkManager {
	/**
	 * It return a list of Links.
	 * Each Link object of the list contains a Transiction, a Bug and a number (long)
	 * Confidence is calculated in the constructor
	 * @param ts
	 * @return
	 */
	public List<Link> getLinks(List<Transaction> ts, String projectName, PrintWriter writer, Boolean useJira) {
		int counter=ts.size();
		writer.println("Missing "+counter +" commits");
		List<Link> links = new ArrayList<Link>();
		for(Transaction t : ts) {
			if (counter%100==0)
				writer.println("Missing "+counter +" commits");
			if (useJira) {
                for (long bugId : t.getBugIds()){
                    Link l = new Link(t, bugId, projectName);
                    links.add(l);
                }
            } else {
			    Link l = new Link(t, projectName);
			    links.add(l);
            }
			counter--;
		}
		return links;
	}

}
