package com.rest.szz.entities;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.rest.szz.git.*;

public class TransactionManager {

	private List<Transaction> transactions;
	private Storage storage;

	/**
	 * Gets transaction containing at least one number or keyword that suggests it could be a bug
	 * @param url
	 * @return
	 */
	public List<Transaction> getBugFixingCommits(URL url, String projectName, String searchQuery) {
		if (this.transactions != null) return this.transactions;

		this.transactions = new ArrayList<>();

		try {
			this.storage = new Storage();
			this.transactions = this.storage.checkoutCvs(url,projectName, searchQuery);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (searchQuery == null) {
            filterTransactions();
        }

		return this.transactions;
	}

    public List<Transaction> getBugFixingCommits(URL url, String projectName) {
        return getBugFixingCommits(url, projectName, null);
    }

    private void filterTransactions() {
        List<Transaction> t = new LinkedList<Transaction>();
        for (Transaction tran : this.transactions){
            if (!tran.hasBugId())
                t.add(tran);
        }
        this.transactions.removeAll(t);
    }

    //This is a message
    public Git getGit(){
        return this.storage.getGit();
    }
}
