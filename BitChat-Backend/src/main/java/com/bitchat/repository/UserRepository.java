package com.bitchat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bitchat.model.User;

import java.util.UUID;

/**
 * @author masoud
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    public User findByUsername(String username);
    Boolean existsByUsername(String username);
	Boolean existsByEmail(String email);
}
