package com.bitchat.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.bitchat.model.Messages;
import com.bitchat.model.Session;
import com.bitchat.model.User;
import com.bitchat.repository.MessagesRepository;
import com.bitchat.repository.SessionRepository;
import com.bitchat.repository.UserRepository;
import com.bitchat.request.MessageRequest;
import com.bitchat.response.MessageResponse;
import com.bitchat.response.UserResponse;
import com.bitchat.response.WebsocketResponse;
import com.bitchat.services.BlackListingService;
import com.bitchat.util.TransportActionEnum;
import com.google.gson.Gson;

@Component
public class WebsocketController implements WebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger(WebsocketController.class);
	
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private MessagesRepository messagesRepository;    

    @Autowired
    private BlackListingService blackListingService;

    @Autowired
    @Value("${loadingMessagesChunksize}")
    public int loadingMessagesChunksize;

    private final ReentrantLock lock = new ReentrantLock(true);

    /**
     * After connection close, return list of users
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession wss) throws IOException {
        try {
            lock.lock();
            Session session = getSession(wss);
            
            if(session != null){
            	logger.info("Websocket connection established!. Found session for user : {}", session.getUser().getUsername());
            	
            	session.setWebSocketSession(wss);
            	WebsocketResponse websocketResponse = new WebsocketResponse();
            	websocketResponse.setReponseType(TransportActionEnum.USER_LIST);
            	websocketResponse.setUsers(getUserList(session));
            	Gson gson = new Gson();
            	sendResponseToWebSocket(gson.toJson(websocketResponse), wss);
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Prepare user list response
     * @param session
     * @return
     */
    private List<UserResponse> getUserList(Session session){
    	List<User> users = userRepository.findAll();
    	List<UserResponse> userResponseList = new ArrayList<>();
    	
    	for (User user : users) {
    		int unreadCount = 0;
    		
    		UUID senderUserID = session.getUser().getId();
        	UUID receiverUserID = user.getId();
        	List<UUID> listOfUserIDs = new ArrayList<>();
        	listOfUserIDs.add(senderUserID);
        	listOfUserIDs.add(receiverUserID);
        	List<Messages> messages = messagesRepository.findBySenderUserIDInAndReceiverUserIDInOrderByDateAsc(listOfUserIDs, listOfUserIDs);
    		
    		if(!user.getEmail().equalsIgnoreCase(session.getUser().getEmail())){
    			String lastMessageSnipate = "";
    			Long lastMessageDate = new Long(0);
    			for (int i = 0; i < messages.size(); i++) {
    				
    				// Unseen Counts
    				Messages messages2 = messages.get(i);
					if(messages2.getSenderUserID().equals(user.getId()) && messages2.getSeen() == 0){
						unreadCount++;
					}
					
					// Last message
					if(i== messages.size() - 1){
						lastMessageSnipate = messages2.getMessageBody();
						if(lastMessageSnipate.length()> 10){
							lastMessageSnipate = lastMessageSnipate.substring(0, 9);
						}
						lastMessageDate = messages2.getDate();
					}
				}
    			userResponseList.add(new UserResponse(user.getId(), user.getName(), unreadCount, lastMessageSnipate, lastMessageDate));
    		}
		}
    	
    	// sort user list based on last messages
    	Collections.sort(userResponseList, new Comparator<UserResponse>(){
    		   public int compare(UserResponse o1, UserResponse o2){
    		      return o2.compareTo(o1.getDate());
    		   }
    		});
    	
    	return userResponseList;
    }
    
    /**
     * Common method for submit response to websocket 
     * @param reponseBody
     * @param wss
     * @throws IOException
     */
    private void sendResponseToWebSocket(String reponseBody, WebSocketSession wss) throws IOException{
    	Session session = getSession(wss);
    	logger.info("Sending response to user : {}", session.getUser().getUsername());
    	session.getWebSocketSession().sendMessage(new TextMessage(reponseBody));
    }
    
    
    /**
     * Handle message implementation to receive websocket request
     */
    @Override
    public void handleMessage(WebSocketSession wss, WebSocketMessage<?> webSocketMessage) throws Exception {
    	try{
    		lock.lock();
            Session currentSession = getSession(wss);
            User currentUser = currentSession.getUser();
            logger.info("Received websocket request from user : {}. Request : {}", currentUser.getUsername(), webSocketMessage);
            
            String payload = ((TextMessage) webSocketMessage).getPayload();
            MessageRequest messageRequest = new Gson().fromJson(payload, MessageRequest.class);
            
            if(TransportActionEnum.SEND_MESSAGE.equals(messageRequest.getAction())){
            	sendMessage(currentUser, messageRequest);
            }else if(TransportActionEnum.DELETE_MESSAGE.equals(messageRequest.getAction())){
            	deleteMessage(messageRequest);
            }else if(TransportActionEnum.MESSAGE_CONVERSATION.equals(messageRequest.getAction())){
            	fetchMessage(wss, currentUser, messageRequest);
            }
            	
    	}catch(Exception e){
    		e.printStackTrace();
    	}finally {
            lock.unlock();
        }
    }
    
    /**
     * 'Send message' functionality implementation
     * @param currentUser
     * @param messageRequest
     */
    private void sendMessage(User currentUser, MessageRequest messageRequest){
    	Messages messages = new Messages();
    	messages.setSenderUserID(currentUser.getId());
    	messages.setReceiverUserID(messageRequest.getUserId());
    	messages.setMessageBody(messageRequest.getMessage());
    	messages.setDate(new Date().getTime());
    	messagesRepository.save(messages);
    	
    	// Boardcast to All WS sessions
    	List<Session> sessionList = sessionRepository.findAll();
	    for (Session session : sessionList) {
	       	
	    	// Refresh User List area
		    try{
	    		WebsocketResponse websocketResponse = new WebsocketResponse();
		       	websocketResponse.setReponseType(TransportActionEnum.USER_LIST);
		       	websocketResponse.setUsers(getUserList(session));
		       	Gson gson = new Gson();
	    		sendResponseToWebSocket(gson.toJson(websocketResponse), session.getWebSocketSession());
	       	}catch(Exception e){
	       		e.printStackTrace();
	       	}
	       	
		    // Refresh Message conversation area
		    try{
		    	if(session.getTargetUserID().equals(currentUser.getId())){
			    	MessageRequest messageRequestForMessageConversation = new MessageRequest();
			    	messageRequestForMessageConversation.setUserId(session.getUser().getId());
			    	fetchMessage(session.getWebSocketSession(), currentUser, messageRequestForMessageConversation);
		    	}
		    }catch(Exception e){
	       		e.printStackTrace();
	       	}
		    
		}
    }
    
    /**
     * 'Fetch message conversation' functionality implementation 
     * @param wss
     * @param currentUser
     * @param messageRequest
     * @throws IOException
     */
    private void fetchMessage(WebSocketSession wss, User currentUser, MessageRequest messageRequest) throws IOException{
    	UUID senderUserID = currentUser.getId();
    	UUID receiverUserID = messageRequest.getUserId();
    	List<UUID> listOfUserIDs = new ArrayList<>();
    	listOfUserIDs.add(senderUserID);
    	listOfUserIDs.add(receiverUserID);
    	List<Messages> messages = messagesRepository.findBySenderUserIDInAndReceiverUserIDInOrderByDateAsc(listOfUserIDs, listOfUserIDs);
    	List<MessageResponse> messageResponses = new ArrayList<>();
    	List<User> users = userRepository.findAll();
    	// Set name against User ID
    	for (Messages message : messages) {
    		MessageResponse messageResponse = MessageResponse.copyFrom(message);
    		setUserNameAgainstUserID(users, messageResponse);
    		messageResponses.add(messageResponse);
		}
    	
    	WebsocketResponse websocketResponse = new WebsocketResponse();
    	websocketResponse.setReponseType(TransportActionEnum.MESSAGE_CONVERSATION);
    	websocketResponse.setMessages(messageResponses);
    	
    	Gson gson = new Gson();
    	sendResponseToWebSocket(gson.toJson(websocketResponse), wss);
    	
    	// Change Target user id in session 
    	Session session = getSession(wss);
    	session.setTargetUserID(receiverUserID);
    	
    	// Change seen status
    	messagesRepository.updateSeenStatus(receiverUserID, senderUserID);
    }
    
    /**
     * Set username against user id
     * @param users
     * @param messageResponse
     */
    private void setUserNameAgainstUserID(List<User> users, MessageResponse messageResponse){
    	for (User user : users) {
			if(messageResponse.getSenderUserID().equals(user.getId())){
				messageResponse.setSenderUserName(user.getName());
			}else if(messageResponse.getReceiverUserID().equals(user.getId())){
				messageResponse.setReceiverUserName(user.getName());
			}
		}
    }
    
    /**
     * 'Delete message' functionality implementation
     * @param messageRequest
     */
    private void deleteMessage(MessageRequest messageRequest){
    	logger.info("'Delete' Functionality implementation pending..!!!");
    	
    }
    

    /**
     * Remove websocket session after disconnect
     */
    @Override
    public void afterConnectionClosed(WebSocketSession wss, CloseStatus closeStatus) throws Exception {
        try {
            lock.lock();
            removeWebSocketSession(wss);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 'Logout' functionality implementation 
     * @param request
     */
    public void logout(HttpServletRequest request) {
        try {
            lock.lock();

            // get accesstoken
        	String jwtToken = request.getParameter("token");
        	
        	if(sessionRepository.findById(jwtToken) != null) {
	           	Session session = sessionRepository.findById(jwtToken).get();
	           	
	           	// Remove Websocket session
	            removeWebSocketSession(session.getWebSocketSession());
	            
	            logger.info("Successfully logout for user : " + session.getUser().getUsername());
        	}
            
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove websocket session
     * @param wss
     */
    private void removeWebSocketSession(WebSocketSession wss) {
    	Session session = getSession(wss);
    	blackListingService.blackListJwt(session.getId());
        sessionRepository.deleteById(wss.getId());
    }

    /**
     * Get custom session object from websocket object
     * @param wss
     * @return
     */
    private Session getSession(WebSocketSession wss) {
    	Session session = null;
    	
    	if(wss.getUri() != null) {
	    	String requestQueryString = wss.getUri().getQuery();
	    	String jwtToken = requestQueryString.substring(requestQueryString.indexOf('=') + 1, requestQueryString.length());
	    	
	    	if(sessionRepository.findById(jwtToken) != null) {
	    		session = sessionRepository.findById(jwtToken).get();
	    	}
    	}
        return session;
    }

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

}
