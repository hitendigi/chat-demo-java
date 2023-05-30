package com.bitchat.operations;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.bitchat.controller.WebsocketController;
import com.bitchat.jwt.JwtUtils;
import com.bitchat.model.Session;
import com.bitchat.model.User;
import com.bitchat.repository.SessionRepository;
import com.bitchat.repository.UserRepository;
import com.bitchat.request.LoginRequest;
import com.bitchat.request.SignupRequest;
import com.bitchat.response.JwtResponse;
import com.bitchat.services.UserDetailsImpl;

@Component 
public class LoginOperation {

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;
    
    @Autowired
    private WebsocketController websocketController;

    /**
     * Login through jwt token
     * @param request
     * @param response
     * @param loginRequest
     * @return
     */
    public ResponseEntity<?> signin(HttpServletRequest request, HttpServletResponse response, LoginRequest loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getName()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		User dbUser = userRepository.findByUsername(loginRequest.getUsername());
		Session session = new Session(jwt, dbUser, System.currentTimeMillis());
        sessionRepository.save(session);
				
		return ResponseEntity
				.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail()));
	}

    /**
     * Username and email should be unique while signup
     * @param signUpRequest
     * @return
     */
	public ResponseEntity<?> signup(SignupRequest signUpRequest) {
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity.badRequest().body("Error: username is already taken!");
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity.badRequest().body("Error: Email is already in use!");
		}

		// Create new user account
		User user = new User(signUpRequest.getName(), signUpRequest.getUsername(), signUpRequest.getEmail(),
				encoder.encode(signUpRequest.getPassword()));

		userRepository.save(user);

		return ResponseEntity.ok("user registered successfully!");
	}
	
	/**
	 * Reset password
	 * @param request
	 * @param response
	 * @param params
	 * @return
	 */
	public ResponseEntity<Object> resetPassword(HttpServletRequest request, HttpServletResponse response, Map<String, String> params) {
         return new ResponseEntity<Object>("Implementation pending!", HttpStatus.OK);
    }
		
	/**
	 * Logout
	 * @param request
	 * @param response
	 * @return
	 */
	public ResponseEntity<Object> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
        	websocketController.logout(request);
            return new ResponseEntity<Object>("Logout Successfully!", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);
        }
    }

	/**
	 * Delete user account
	 * @param request
	 * @param response
	 * @return
	 */
    public ResponseEntity<Object> deleteAccount(HttpServletRequest request, HttpServletResponse response) {
    	return new ResponseEntity<Object>("Implementation pending!", HttpStatus.OK);
    }

}
