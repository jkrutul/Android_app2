package com.example.app_2.spinner.model;

public class ImageSpinnerItem {
	protected final String filename;
	protected final String text;
	protected final Long id;
	protected final boolean isHint;
	
	 public ImageSpinnerItem(String filename,String strItem, Long id, boolean flag) {
         this.isHint = flag;
         this.id = id;
         this.text = strItem;
         this.filename = filename;
     }

     public String getItemString() {
         return text;
     }
     
     public Long getItemId(){
    	 return this.id;
     }

     public boolean isHint() {
         return isHint;
     }
     public String getFilename() {
         return filename;
     }
}
