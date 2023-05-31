package com.bitchat.response;

import java.util.Date;
import java.util.UUID;

import com.bitchat.model.Messages;

public class MessageResponse {
	
    private UUID id;

    private Date createdDate;

    private Date modifiedDate;

	private UUID senderUserID;
    
    private String senderUserName;

    private UUID receiverUserID;
    
    private String receiverUserName;

    private String messageBody;

    private Long date;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public UUID getSenderUserID() {
		return senderUserID;
	}

	public void setSenderUserID(UUID senderUserID) {
		this.senderUserID = senderUserID;
	}

	public String getSenderUserName() {
		return senderUserName;
	}

	public void setSenderUserName(String senderUserName) {
		this.senderUserName = senderUserName;
	}

	public UUID getReceiverUserID() {
		return receiverUserID;
	}

	public void setReceiverUserID(UUID receiverUserID) {
		this.receiverUserID = receiverUserID;
	}

	public String getReceiverUserName() {
		return receiverUserName;
	}

	public void setReceiverUserName(String receiverUserName) {
		this.receiverUserName = receiverUserName;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
	}

    public static MessageResponse copyFrom(Messages message){
    	MessageResponse messageResponse = new MessageResponse();
    	messageResponse.setId(message.getId());
    	messageResponse.setCreatedDate(message.getCreatedDate());
    	messageResponse.setModifiedDate(message.getModifiedDate());
    	messageResponse.setDate(message.getDate());
    	messageResponse.setMessageBody(message.getMessageBody());
    	messageResponse.setSenderUserID(message.getSenderUserID());
    	messageResponse.setReceiverUserID(message.getReceiverUserID());
    	return messageResponse;
    }
}
