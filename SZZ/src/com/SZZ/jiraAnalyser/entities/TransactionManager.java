package com.SZZ.jiraAnalyser.entities;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.SZZ.jiraAnalyser.git.Git;
import com.SZZ.*;

public class TransactionManager {
	
	private List<Transaction> transactions;
	
	private List<Transaction> totalTransactions;
	
	private Storage storage;

	/**
	 * Gets transaction containing at least one number or keyword that suggests it could be a bug
	 * @param url
	 * @return
	 */
	public List<Transaction> getBugFixingCommits(URL url, String projectName, String jiraKey) {
		if (this.transactions != null) return this.transactions;
		
		this.transactions = new ArrayList<Transaction>(); //Arrays.asList(new Transaction[] { Transaction.EXAMPLE_TRANSACTION1, Transaction.EXAMPLE_TRANSACTION2,Transaction.EXAMPLE_TRANSACTION3,Transaction.EXAMPLE_TRANSACTION4,});
	
		// TODO: Parse stuff from url
		try {
			storage = new Storage(projectName);
			transactions = this.storage.checkoutCvs(url,projectName, jiraKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<Transaction> t = new LinkedList<Transaction>();
	    for (Transaction tran : this.transactions){
	    	if (!tran.hasBugId())
	    		t.add(tran);
	    }
	    this.transactions.removeAll(t);
			
		return transactions;
	}
	
	
	public Git getGit(){
		return this.storage.getGit();
	}
}
