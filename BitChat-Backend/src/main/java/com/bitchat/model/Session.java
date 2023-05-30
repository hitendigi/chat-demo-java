package com.bitchat.model;

import java.util.UUID;

import org.springframework.web.socket.WebSocketSession;

public class Session {

	private String id; // JWT token

	private User user;

	private Long lastModified;

	private WebSocketSession webSocketSession;

	private UUID targetUserID;

	public Session(String id, User user, Long lastModified) {
		this.id = id;
		this.user = user;
		this.lastModified = lastModified;
	}

	public void logout() {
		setUser(null);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getLastModified() {
		return lastModified;
	}

	public void setLastModified(Long lastModified) {
		this.lastModified = lastModified;
	}

	public WebSocketSession getWebSocketSession() {
		return webSocketSession;
	}

	public void setWebSocketSession(WebSocketSession webSocketSession) {
		this.webSocketSession = webSocketSession;
	}

	public UUID getTargetUserID() {
		return targetUserID;
	}

	public void setTargetUserID(UUID targetUserID) {
		this.targetUserID = targetUserID;
	}

}
