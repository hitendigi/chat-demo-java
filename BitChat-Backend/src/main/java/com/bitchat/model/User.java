package com.bitchat.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "Users") // because a table with this class name may exist (reserved names)
public class User extends BaseModel implements Comparable<User> {

	@Column(length = 100)
    private String name;
	
	@Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(length = 100)
    private String password;
    
    @Column(length = 100)
    private String email;
    
    public User(){
    	
    }
    
    public User(String name, String username, String email, String password){
    	this.name = name;
    	this.username = username;
    	this.email = email;
    	this.password = password;
    }

    public void setUsername(String username) {
        this.username = username.toLowerCase().trim();
    }
    
    public static String validateName(String s) {
        int lb = 1;
        int ub = 90;
        if (s.length() < lb || s.length() > ub) {
            return "Name must be at least " + lb + " at most " + ub + " characters";
        } else {
            if (hasIllegalCharacters(s)) {
                return "Name includes illegal characters";
            }
            return null;
        }
    }

    public static String validateUsername(String s) {
        int lb = 1;
        int ub = 90;
        if (s.length() < lb || s.length() > ub) {
            return "Username must be at least " + lb + " at most " + ub + " characters";
        } else {
            if (hasIllegalCharacters(s)) {
                return "Username includes illegal characters";
            }
            return null;
        }
    }

    public static String validatePassword(String s) {
        int lb = 6;
        int ub = 90;
        if (s.length() < lb || s.length() > ub) {
            return "Passwords must be at least " + lb + " at most " + ub + " characters";
        } else {
            if (hasIllegalCharacters(s)) {
                return "Password includes illegal characters";
            }
            return null;
        }
    }

    public static boolean hasIllegalCharacters(String s) {
        boolean ok = true;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (!(Character.isLetterOrDigit(ch) || (ch >= 32 && ch <= 46) || (ch == 64))) {
                ok = false;
                break;
            }
        }
        return !ok;
    }

    public static String validateAll(User user) {
        String problem = User.validateName(user.getName());
        if (problem == null) {
            problem = User.validateUsername(user.getUsername());
        }
        if (problem == null) {
            problem = User.validatePassword(user.getPassword());
        }
        return problem;
    }

    @Override
    public int compareTo(User user) {
        return getName().compareTo(user.getName());
    }

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
    
    
}
