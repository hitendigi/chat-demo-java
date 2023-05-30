package com.bitchat.request;

import java.util.UUID;

import com.bitchat.util.TransportActionEnum;

public class MessageRequest {

	private UUID userId;

    private TransportActionEnum action;

    private String message;

    private UUID messageId;

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public TransportActionEnum getAction() {
		return action;
	}

	public void setAction(TransportActionEnum action) {
		this.action = action;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public UUID getMessageId() {
		return messageId;
	}

	public void setMessageId(UUID messageId) {
		this.messageId = messageId;
	}

	
    
}
