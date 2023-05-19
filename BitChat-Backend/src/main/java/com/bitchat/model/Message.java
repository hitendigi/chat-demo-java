package com.bitchat.model;


import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Message extends BaseModel implements Comparable<Message> {

    @Column(length = 100)
    private String senderUsername;

    @Column(length = 100)
    private String receiverUsername;

    @Column
    private boolean textMessage;

    @Column
    private boolean imageFile;

    @Column(length = 100)
    private String senderPresentation;

    @Column
    private String body;

    @Column
    private Long date;

    @Column
    private UUID fileInfoId;

    public Message clone() {
        Message m = new Message();
        m.setSenderUsername(senderUsername);
        m.setReceiverUsername(receiverUsername);
        m.setTextMessage(textMessage);
        m.setImageFile(imageFile);
        m.setSenderPresentation(senderPresentation);
        m.setBody(body);
        m.setDate(date);
        m.setFileInfoId(fileInfoId);
        return m;
    }

    @Override
    public int compareTo(Message message) {
        return date.compareTo(message.getDate());
    }

	public String getSenderUsername() {
		return senderUsername;
	}

	public void setSenderUsername(String senderUsername) {
		this.senderUsername = senderUsername;
	}

	public String getReceiverUsername() {
		return receiverUsername;
	}

	public void setReceiverUsername(String receiverUsername) {
		this.receiverUsername = receiverUsername;
	}

	public boolean isTextMessage() {
		return textMessage;
	}

	public void setTextMessage(boolean textMessage) {
		this.textMessage = textMessage;
	}

	public boolean isImageFile() {
		return imageFile;
	}

	public void setImageFile(boolean imageFile) {
		this.imageFile = imageFile;
	}

	public String getSenderPresentation() {
		return senderPresentation;
	}

	public void setSenderPresentation(String senderPresentation) {
		this.senderPresentation = senderPresentation;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
	}

	public UUID getFileInfoId() {
		return fileInfoId;
	}

	public void setFileInfoId(UUID fileInfoId) {
		this.fileInfoId = fileInfoId;
	}
    
    
}
