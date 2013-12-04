package com.example.app_2.spinner.model;

public class UserSpinnerItem extends ImageSpinnerItem {
	private final Long user_root_fk;
	
	 public UserSpinnerItem(String filename,String strItem, Long id, Long user_root_fk, boolean flag) {
		 super(filename, strItem, id, flag);
		 this.user_root_fk = user_root_fk;
     }

	public Long getUser_root_fk() {
		return user_root_fk;
	}

}
