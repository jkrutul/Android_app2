package com.example.app_2.models;

public class ImageObject { //ID imageName, AUDIOPATH, DESCRIPTION, CATEGORY_FK
	private Long id;  		
	private String imageName;
	private String audioPath;
	private String description;
	private Long category_fk;
	
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
	
	@Override
	public String toString(){				//ID imageName, AUDIOPATH, DESCRIPTION, CATEGORY_FK
		return "ID:"+id+" P:"+imageName+" D:"+description+" C_FK:"+category_fk;
		
	}
}
