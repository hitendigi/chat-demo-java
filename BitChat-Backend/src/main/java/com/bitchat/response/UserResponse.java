package com.bitchat.response;

import java.util.UUID;

import com.bitchat.model.Messages;

public class UserResponse {

	private UUID id;

	private String name;
	
	private int unreadCount;
	
	private String lastMessage;
	
	private Long date;
	
	public UserResponse(UUID id, String name, int unreadCount, String lastMessage, Long date) {
		this.id = id;
		this.name = name;
		this.unreadCount = unreadCount;
		this.lastMessage = lastMessage;
		this.date = date;
	}
	
	public int compareTo(Long date) {
        return this.date.compareTo(date);
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

	public int getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}

	public String getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
	}
	
	
	
}
