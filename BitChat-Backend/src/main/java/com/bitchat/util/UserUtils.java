package com.bitchat.util;

import org.springframework.beans.factory.annotation.Autowired;

import com.bitchat.model.User;
import com.bitchat.repository.UserRepository;

public class UserUtils {

	@Autowired
    private UserRepository userRepository;

    @Autowired
    private Utils utils;
    
    /**
     * Add some users while setup time
     */
	public void addSomeUsers(){
		if (userRepository.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername(Constants.adminUsername);
            admin.setPassword(utils.hash("admin"));
            admin.setFirstname("Masoud");
            admin.setLastname("Taghavian");
            userRepository.save(admin);

            User bcUSer = new User();
            bcUSer.setUsername(Constants.broadcastUsername);
            bcUSer.setPassword(utils.hash("123456"));
            bcUSer.setFirstname("Broadcast");
            bcUSer.setLastname("");
            userRepository.save(bcUSer);

            User u1 = new User();
            u1.setUsername("me");
            u1.setPassword(utils.hash("123456"));
            u1.setFirstname("Mohsen");
            u1.setLastname("Esmaeili");
            userRepository.save(u1);

            User u2 = new User();
            u2.setUsername("sz");
            u2.setPassword(utils.hash("123456"));
            u2.setFirstname("Saeed");
            u2.setLastname("Zhiany");
            userRepository.save(u2);
        }
	}
	
}
