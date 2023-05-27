package com.bitchat.response;

import java.util.List;

import com.bitchat.model.Messages;
import com.bitchat.util.TransportActionEnum;

public class WebsocketResponse {

	private TransportActionEnum reponseType;
	
	private List<UserResponse> users;
	
	private List<MessageResponse> messages;
	
	private Messages lastMessage;

	

	public TransportActionEnum getReponseType() {
		return reponseType;
	}

	public void setReponseType(TransportActionEnum reponseType) {
		this.reponseType = reponseType;
	}

	public List<UserResponse> getUsers() {
		return users;
	}

	public void setUsers(List<UserResponse> users) {
		this.users = users;
	}

	public List<MessageResponse> getMessages() {
		return messages;
	}

	public void setMessages(List<MessageResponse> messages) {
		this.messages = messages;
	}

	public Messages getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(Messages lastMessage) {
		this.lastMessage = lastMessage;
	}
	
	

}
