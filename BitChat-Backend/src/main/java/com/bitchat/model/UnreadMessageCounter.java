package com.bitchat.model;


import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class UnreadMessageCounter extends BaseModel {

    @Column(length = 100)
    private String currentSideUsername;

    @Column(length = 100)
    private String otherSideUsername;

    @Column
    private int count;

	public String getCurrentSideUsername() {
		return currentSideUsername;
	}

	public void setCurrentSideUsername(String currentSideUsername) {
		this.currentSideUsername = currentSideUsername;
	}

	public String getOtherSideUsername() {
		return otherSideUsername;
	}

	public void setOtherSideUsername(String otherSideUsername) {
		this.otherSideUsername = otherSideUsername;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
    
    
}
