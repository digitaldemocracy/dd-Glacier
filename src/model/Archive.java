package model;

import java.math.RoundingMode;
import java.sql.Date;
import java.text.NumberFormat;

public class Archive {
	private String archiveId;
	private String fileName;
	private String vault;
	private long size;
	private long date;
	
	public Archive() {
		this.archiveId = null;
		this.fileName = null;
		this.vault = null;
		this.size = 0l;
		this.date = 0l;
	}
	
	public Archive(String id, String name, String vault, long size, long date) {
		this.archiveId = id;
		this.fileName = name;
		this.vault = vault;
		this.size = size;
		this.date = date;
	}
	
	public String getArchiveId() {
		return this.archiveId;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	public String getVault() {
		return this.vault;
	}
	
	public long getSize() {
		return this.size;
	}
	
	public long getDate() {
		return this.date;
	}
	
	public Date getDateAsDate() {
		return new Date(this.date);
	}
	
	public String toString() {
		NumberFormat form = NumberFormat.getInstance();
		form.setRoundingMode(RoundingMode.HALF_UP);
		form.setMaximumFractionDigits(3);
		return this.getFileName() + ", "
	           + this.getVault() + ", " + form.format((double)this.getSize()/(double)1048576)
	           + "Mb, " + this.getDateAsDate().toString();
	}
}
