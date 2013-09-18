package com.example.app_2.models;

public class ImageObject { //ID imageName, AUDIOPATH, DESCRIPTION, CATEGORY_FK
	private Long id;  		
	private String imageName;
	private String audioPath;
	private String description;
	private Long times_used;
	private String modified;
	private String last_used;
	private Long is_category;
	private Long category_fk;
	private Long parent_fk;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}	
	public ImageObject(){
		
	}
	public ImageObject(String imageName){
		this.imageName= imageName;
		this.category_fk= Long.valueOf(0);
	}
	public String getImageName() {
		return imageName;
	}
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Long getCategory_fk() {
		return category_fk;
	}
	public void setCategory_fk(Long category_fk) {
		this.category_fk = category_fk;
	}
	public String getAudioPath() {
		return audioPath;
	}
	public void setAudioPath(String audioPath) {
		this.audioPath = audioPath;
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
	public Long getParent_fk() {
		return parent_fk;
	}
	public Long getIs_category() {
		return is_category;
	}
	public void setIs_category(Long is_category) {
		this.is_category = is_category;
	}

	public void setParent_fk(Long parent_fk) {
		this.parent_fk = parent_fk;
	}
	
	@Override
	public String toString(){				//ID imageName, AUDIOPATH, DESCRIPTION, CATEGORY_FK
		return "ID:"+id+" P:"+imageName+" D:"+description+" M:"+modified+" TU:"+times_used+" C_FK:"+category_fk+"P_FK:"+parent_fk;
		
	}

}
