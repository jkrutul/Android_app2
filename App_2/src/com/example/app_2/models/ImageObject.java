package com.example.app_2.models;

public class ImageObject { //ID imageName, AUDIOPATH, DESCRIPTION, times_used, modified, last_used, category,parent_fk 
	private Long id;  		
	private String imageName;
	private String description;
	private Long times_used;
	private String modified;
	private String last_used;
	private String category;
	private int isContextualCategory;
	private Long author_fk;
	
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
	
	public ImageObject(String imageName, Long author_id){ 					// konstruktor dla liœcia
		this.imageName = imageName;
		this.author_fk = author_id;
	}
	
	
	public ImageObject(String imageName, String description, Long author_id){ 					// konstruktor dla liœcia
		this.imageName = imageName;
		this.author_fk = author_id;
		this.description = description;
	}
	
	
	public ImageObject(String imageName, String description,String category){
		this.imageName = imageName;

		this.description = description;
		this.category = category;
	
	}

	public ImageObject(String imageName, String description,String category, boolean isContextualCategory){
		this.imageName = imageName;

		this.description = description;
		this.category = category;
		this.setIsContextualCategory((isContextualCategory) ? 1 : 0);
	
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
	public String toString(){				//ID imageName, AUDIOPATH, DESCRIPTION, times_used, modified, last_used, category,parent_fk 
		return id+";"+imageName+";"+description+";"+times_used+";"+modified+";"+last_used+";"+category+";";
	}
	public Long getAuthor_fk() {
		return author_fk;
	}
	public void setAuthor_fk(Long author_fk) {
		this.author_fk = author_fk;
	}
	public int getIsContextualCategory() {
		return isContextualCategory;
	}
	public void setIsContextualCategory(int isContextualCategory) {
		this.isContextualCategory = isContextualCategory;
	}

}
