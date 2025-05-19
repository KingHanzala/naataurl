package com.urlshortener.naataurl.persistence.model;

import lombok.Data;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.util.Date;

import jakarta.persistence.Column;

@Data
@Entity
@Table(name = "users")
public class User {
        
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "email")
    private String userEmail;

    @Column(name = "password_hash")
    private String userPassword;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")  
    private Date updatedAt;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "deleted_at")
    private Date deletedAt;

    @Column(name = "usage_credits", nullable = false)
    private Long usageCredits = 5L;

    public void decrementCredits(){
        this.usageCredits --;
    }

    public void incrementCredits(){
        this.usageCredits ++;
    }
}
