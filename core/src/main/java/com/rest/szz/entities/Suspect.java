package com.rest.szz.entities;

import java.sql.Timestamp;
import java.util.Date;

public class Suspect {

	private String commitId;
	private Date ts;
	private String fileName;
	private String note;

	public enum Type {
		PARTIAL_FIX,
		WEAK,
		HARD
	}

	/**
	 * Class representation of a suspect
	 *
	 * @param commitId
	 * @param ts
	 * @param fileName
	 */
	public Suspect(String commitId, Date ts, String fileName, String note) {
		this.commitId = commitId;
		this.ts = ts;
		this.fileName = fileName;
		this.note = note;
	}

	public String getCommitId() {
		return this.commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public Date getTs() {
		return this.ts;
	}

	public void setTs(Timestamp ts) {
		this.ts = ts;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
