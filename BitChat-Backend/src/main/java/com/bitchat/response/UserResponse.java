package com.bitchat.response;

import java.util.UUID;

public class UserResponse {

	private UUID id;

	private String name;
	
	private int unreadCount;
	
	public UserResponse(UUID id, String name, int unreadCount) {
		this.id = id;
		this.name = name;
		this.unreadCount = unreadCount;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
}
