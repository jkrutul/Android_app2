package com.example.app_2.models;

public class EdgeModel {
	private Long parent;
	private Long child;
	
	public EdgeModel(Long parent, Long child){
		this.setParent(parent);
		this.setChild(child);
	}

	public Long getParent() {
		return parent;
	}

	public void setParent(Long parent) {
		this.parent = parent;
	}

	public Long getChild() {
		return child;
	}

	public void setChild(Long child) {
		this.child = child;
	}
	

}
