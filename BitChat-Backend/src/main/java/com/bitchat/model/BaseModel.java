package com.bitchat.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@MappedSuperclass
public class BaseModel {

    @Id
    @GeneratedValue
    private UUID id;

    @Column
    private Date createdDate;

    @Column
    private Date modifiedDate;

    @PrePersist
    public void onPrePersist() {
        createdDate = new Date();
        modifiedDate = createdDate;
    }

    @PreUpdate
    public void onPreUpdate() {
        modifiedDate = new Date();
    }

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
    
    

}
