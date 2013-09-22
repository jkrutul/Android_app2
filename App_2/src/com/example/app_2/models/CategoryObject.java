package com.example.app_2.models;

public class CategoryObject {
	private Long id;  		
	private String categoryName;
	
	public CategoryObject(){
		
	}
	public CategoryObject(String categoryName){
		this.categoryName = categoryName;
	}
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
	@Override
	public String toString(){
		return "ID:"+id+" "+categoryName;
		
	}
	
}
