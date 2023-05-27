package com.bitchat.model;


import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Messages extends BaseModel implements Comparable<Messages> {

    @Column(length = 100)
    private UUID senderUserID;
    
    @Column(length = 100)
    private UUID receiverUserID;
    
    @Column
    private String messageBody;

    @Column
    private Long date;
    
    @Column
    private int seen;
    


    @Override
    public int compareTo(Messages message) {
        return date.compareTo(message.getDate());
    }


	public UUID getSenderUserID() {
		return senderUserID;
	}


	public void setSenderUserID(UUID senderUserID) {
		this.senderUserID = senderUserID;
	}


	public UUID getReceiverUserID() {
		return receiverUserID;
	}


	public void setReceiverUserID(UUID receiverUserID) {
		this.receiverUserID = receiverUserID;
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

	public int getSeen() {
		return seen;
	}


	public void setSeen(int seen) {
		this.seen = seen;
	}

	
}
