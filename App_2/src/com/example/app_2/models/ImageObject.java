package com.example.app_2.models;

public class ImageObject { //ID filename, AUDIOPATH, DESCRIPTION, times_used, modified, last_used, category,parent_fk 
	private Long id;
	private String filename, description, tts_m, tts_f;
	private int isCategory, isAddToExpr, isAddToCatList;
	
	private Long times_used;
	private String modified;
	private String last_used;

	private Long author_fk;
	


	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}	
	
	public ImageObject(){
		
	}
	
	public ImageObject(String filename){
		this.filename = filename;
	}
	
	public ImageObject(String filename, Long author_id){ 					// konstruktor dla liœcia
		this.filename = filename;
		this.author_fk = author_id;
	}
	
	
	public ImageObject(String filename, String description, Long author_id){ 					// konstruktor dla liœcia
		this.filename = filename;
		this.author_fk = author_id;
		this.description = description;
	}
	
	
	public ImageObject(String filename, String description, int isCategory){
		this.filename = filename;

		this.description = description;
		this.isCategory =isCategory;
	
	}

	public ImageObject(String filename, String description, int isCategory, int addToExpression){
		this.filename = filename;

		this.description = description;
		this.isCategory = isCategory;
		this.isAddToExpr = addToExpression;
	}
	
	public ImageObject(String filename, String description, String tts_m, String tts_f, int isCategory, int isAddToExpr, int isAddToCatList){
		this.filename = filename;
		this.tts_m = tts_m;
		this.tts_f = tts_f;
		this.description = description;
		this.isCategory = isCategory;
		this.isAddToExpr = isAddToExpr;
		this.isAddToCatList = isAddToCatList;
	}
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public Long getTimes_used() {
		return times_used;
	}
	public void setTimes_used(Long times_used) {
		this.times_used = times_used;
	}
	public String getModified() {
		return modified;
	}
	public void setModified(String modified) {
		this.modified = modified;
	}
	public String getLast_used() {
		return last_used;
	}
	public void setLast_used(String last_used) {
		this.last_used = last_used;
	}

	@Override
	public String toString(){				//ID filename, AUDIOPATH, DESCRIPTION, times_used, modified, last_used, category,parent_fk 
		return id+";"+filename+";"+description+";"+times_used+";"+modified+";"+last_used+";"+isCategory+";";
	}
	public Long getAuthor_fk() {
		return author_fk;
	}
	public void setAuthor_fk(Long author_fk) {
		this.author_fk = author_fk;
	}

	public String getTts_m() {
		return tts_m;
	}
	public void setTts_m(String tts_m) {
		this.tts_m = tts_m;
	}
	public String getTts_f() {
		return tts_f;
	}
	public void setTts_f(String tts_f) {
		this.tts_f = tts_f;
	}
	public int isAddToExpr() {
		return isAddToExpr;
	}
	public void setAddToExpr(int isAddToExpr) {
		this.isAddToExpr = isAddToExpr;
	}
	public int isAddToCatList() {
		return isAddToCatList;
	}
	public void setAddToCastList(int isAddToCatList) {
		this.isAddToCatList = isAddToCatList;
	}
	public int isCategory() {
		return isCategory;
	}
	public void setCategory(int isCategory) {
		this.isCategory = isCategory;
	}

}
