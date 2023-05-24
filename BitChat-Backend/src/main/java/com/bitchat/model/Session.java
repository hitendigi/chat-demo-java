package com.bitchat.model;


import org.springframework.web.socket.WebSocketSession;

public class Session {

    private String id;

    private User user;

    private Long lastModified;

    private String redirectedUri;

    private WebSocketSession webSocketSession;

    private String otherSideUsername;
    
    private String jwt;

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

	public String getRedirectedUri() {
		return redirectedUri;
	}

	public void setRedirectedUri(String redirectedUri) {
		this.redirectedUri = redirectedUri;
	}

	public WebSocketSession getWebSocketSession() {
		return webSocketSession;
	}

	public void setWebSocketSession(WebSocketSession webSocketSession) {
		this.webSocketSession = webSocketSession;
	}

	public String getOtherSideUsername() {
		return otherSideUsername;
	}

	public void setOtherSideUsername(String otherSideUsername) {
		this.otherSideUsername = otherSideUsername;
	}

	public String getJwt() {
		return jwt;
	}

	public void setJwt(String jwt) {
		this.jwt = jwt;
	}
    
    
}
