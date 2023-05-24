package com.bitchat.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bitchat.jwt.JwtUtils;
import com.bitchat.model.User;
import com.bitchat.operations.LoginOperation;
import com.bitchat.repository.UserRepository;
import com.bitchat.request.LoginRequest;
import com.bitchat.request.SignupRequest;
import com.bitchat.response.JwtResponse;
import com.bitchat.response.MessageResponse;
import com.bitchat.services.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class LoginController {

	@Autowired
    LoginOperation loginOperation;

	@PostMapping("/signin")
	public ResponseEntity<?> signin(HttpServletRequest request, HttpServletResponse response, @RequestBody LoginRequest loginRequest) {
		return loginOperation.signin(request, response, loginRequest);
	}

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestBody SignupRequest signUpRequest) {
		return loginOperation.signup(signUpRequest);
	}
	
	@GetMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request, HttpServletResponse response) {
        return loginOperation.logout(request, response);
    }
	
	@PostMapping("/resetpassword")
    public ResponseEntity<Object> resetPassword(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, String> params) {
        return loginOperation.resetPassword(request, response, params);
    }
	
	@GetMapping("/deleteaccount")
    public ResponseEntity<Object> deleteAccount(HttpServletRequest request, HttpServletResponse response) {
        return loginOperation.deleteAccount(request, response);
    }
}
