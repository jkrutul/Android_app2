package com.example.app_2.provider;

public class SpinnerItem {
	private final String filename;
	private final String text;
	private final Long id;
	private final boolean isHint;
	
	 public SpinnerItem(String filename,String strItem, Long id, boolean flag) {
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
