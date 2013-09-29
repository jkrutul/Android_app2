package com.example.app_2.models;

public class ImageObject { //ID imageName, AUDIOPATH, DESCRIPTION, times_used, modified, last_used, category,parent_fk 
	private Long id;  		
	private String imageName;
	private String audioPath;
	private String description;
	private Long times_used;
	private String modified;
	private String last_used;
	private String category;
	private Long parent_fk;
	
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}


	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}	
	public ImageObject(){
		
	}
	
	public ImageObject(String imageName){
		this.imageName = imageName;
	}
	
	public ImageObject(String imageName, Long parent_fk){ 					// konstruktor dla liœcia
		this.imageName = imageName;
		this.parent_fk = parent_fk;
	}
	
	public ImageObject(String imageName, String audioPath, String description,String category, String paretn_fk){
		this.imageName = imageName;
		this.audioPath = audioPath;
		this.description = description;
		this.category = category;
		this.parent_fk =Long.valueOf(paretn_fk);
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

	public void setParent_fk(Long parent_fk) {
		this.parent_fk = parent_fk;
	}
	
	@Override
	public String toString(){				//ID imageName, AUDIOPATH, DESCRIPTION, times_used, modified, last_used, category,parent_fk 
		return id+";"+imageName+";"+audioPath+";"+description+";"+times_used+";"+modified+";"+last_used+";"+category+";"+parent_fk;
	}

}
