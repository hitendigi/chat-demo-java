package com.bitchat.operations;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.bitchat.controller.WebsocketController;
import com.bitchat.jwt.JwtUtils;
import com.bitchat.model.Session;
import com.bitchat.model.User;
import com.bitchat.repository.SessionRepository;
import com.bitchat.repository.UserRepository;
import com.bitchat.request.LoginRequest;
import com.bitchat.request.SignupRequest;
import com.bitchat.response.JwtResponse;
import com.bitchat.response.SignupResponse;
import com.bitchat.services.UserDetailsImpl;
import com.bitchat.util.Constants;
import com.bitchat.util.Utils;

@Component 
public class LoginOperation {

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private Utils utils;
    
    @Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;
    
    @Autowired
    private WebsocketController websocketController;

    public ResponseEntity<?> signin(HttpServletRequest request, HttpServletResponse response, LoginRequest loginRequest) {

    	//logout(request, response);
    	
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getName()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		//HttpSession httpSession = request.getSession();
		User dbUser = userRepository.findByUsername(loginRequest.getUsername());
		Session session = new Session(jwt, dbUser, System.currentTimeMillis());
        sessionRepository.save(session);
				
		return ResponseEntity
				.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail()));
	}

	public ResponseEntity<?> signup(SignupRequest signUpRequest) {
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new SignupResponse("Error: username is already taken!"));
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new SignupResponse("Error: Email is already in use!"));
		}

		// Create new user account
		User user = new User(signUpRequest.getName(), signUpRequest.getUsername(), signUpRequest.getEmail(),
				encoder.encode(signUpRequest.getPassword()));

		userRepository.save(user);

		return ResponseEntity.ok(new SignupResponse("user registered successfully!"));
	}
	
	public ResponseEntity<Object> resetPassword(HttpServletRequest request, HttpServletResponse response, Map<String, String> params) {
         return new ResponseEntity<Object>("Implementation pending!", HttpStatus.OK);
    }
		
	public ResponseEntity<Object> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
        	websocketController.logout(request);
            return new ResponseEntity<Object>("Logout Successfully!", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            String exStr = utils.serializeException(e);
            return new ResponseEntity<Object>(exStr, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<Object> deleteAccount(HttpServletRequest request, HttpServletResponse response) {
    	return new ResponseEntity<Object>("Implementation pending!", HttpStatus.OK);
    }

}
