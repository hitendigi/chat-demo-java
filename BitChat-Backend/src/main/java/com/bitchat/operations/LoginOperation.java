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
import com.bitchat.response.MessageResponse;
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

    public ResponseEntity<?> signin(@RequestBody LoginRequest loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getName()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		return ResponseEntity
				.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail()));
	}

	public ResponseEntity<?> signup(@RequestBody SignupRequest signUpRequest) {
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: username is already taken!"));
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
		}

		// Create new user account
		User user = new User(signUpRequest.getName(), signUpRequest.getUsername(), signUpRequest.getEmail(),
				encoder.encode(signUpRequest.getPassword()));

		userRepository.save(user);

		return ResponseEntity.ok(new MessageResponse("user registered successfully!"));
	}
	public ResponseEntity<Object> resetPassword(HttpServletRequest request, HttpServletResponse response, Map<String, String> params) {
        String currentPassword = params.get("currentPassword");
        String newPassword = params.get("newPassword");
        Boolean updateCookies = Boolean.parseBoolean(params.get("updateCookies"));
        HttpSession httpSession = request.getSession();
        User user = getUser(httpSession);
        if (user.getPassword().equals(utils.hash(currentPassword))) {
            String problem = User.validatePassword(newPassword);
            if (problem == null) {
                user.setPassword(utils.hash(newPassword));
                userRepository.save(user);
                if (updateCookies) {
                    Cookie usernameCookie = new Cookie("username", user.getUsername());
                    usernameCookie.setMaxAge(60 * 60 * 24 * 30 * 12);
                    response.addCookie(usernameCookie);
                    Cookie passwordCookie = new Cookie("password", user.getPassword());
                    passwordCookie.setMaxAge(60 * 60 * 24 * 30 * 12);
                    response.addCookie(passwordCookie);
                }
                return new ResponseEntity<Object>("Success!", HttpStatus.OK);
            } else {
                return new ResponseEntity<Object>(problem, HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<Object>("Incorrect current password", HttpStatus.BAD_REQUEST);
        }
    }
		
	public ResponseEntity<Object> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession httpSession = request.getSession();
        User user = getUser(httpSession);
        try {
            Cookie usernameCookie = new Cookie("username", "");
            usernameCookie.setMaxAge(0);
            response.addCookie(usernameCookie);
            Cookie passwordCookie = new Cookie("password", "");
            passwordCookie.setMaxAge(0);
            response.addCookie(passwordCookie);
            Session session = sessionRepository.findById(httpSession.getId()).get();
            websocketController.logout(user);
            session.logout();
            return new ResponseEntity<Object>("Logout Successfully!", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            String exStr = utils.serializeException(e);
            return new ResponseEntity<Object>(exStr, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<Object> deleteAccount(HttpServletRequest request, HttpServletResponse response) {
        HttpSession httpSession = request.getSession();
        Session session = sessionRepository.findById(httpSession.getId()).get();
        User user = session.getUser();
        ResponseEntity<Object> logout = logout(request, response);
        if (!Constants.adminUsername.equals(user.getUsername())) {
            sessionRepository.findByUsername(httpSession.getId())
                    .forEach(x -> x.setUser(null));
            userRepository.deleteById(user.getId());
        }
        websocketController.updateAllUserLists(user.getUsername());
        return logout;
    }

	
	private User getUser(HttpSession session) {
        return sessionRepository.findById(session.getId()).get().getUser();
    }
}
