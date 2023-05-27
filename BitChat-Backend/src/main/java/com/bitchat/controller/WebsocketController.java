package com.bitchat.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

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

import com.bitchat.model.Message;
import com.bitchat.model.Messages;
import com.bitchat.model.Session;
import com.bitchat.model.UnreadMessageCounter;
import com.bitchat.model.User;
import com.bitchat.repository.MessageRepository;
import com.bitchat.repository.MessagesRepository;
import com.bitchat.repository.SessionRepository;
import com.bitchat.repository.UnreadMessageCounterRepository;
import com.bitchat.repository.UserRepository;
import com.bitchat.request.MessageRequest;
import com.bitchat.response.MessageResponse;
import com.bitchat.response.UserResponse;
import com.bitchat.response.WebsocketResponse;
import com.bitchat.util.Constants;
import com.bitchat.util.TransportActionEnum;
import com.bitchat.util.Utils;
import com.google.gson.Gson;

@Component
public class WebsocketController implements WebSocketHandler {

    @Autowired
    private Utils utils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private MessageRepository messageRepository; // DEPTRICATED!
    
    @Autowired
    private MessagesRepository messagesRepository;    

    @Autowired
    private UnreadMessageCounterRepository unreadMessageCounterRepository;

    @Autowired
    @Value("${loadingMessagesChunksize}")
    public int loadingMessagesChunksize;

    private final ReentrantLock lock = new ReentrantLock(true);
    private final Map<String, Session> sessionMapFromWSS = new HashMap<>();
    private final Map<String, Session> sessionMapFromUN = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession wss) throws IOException {
        try {
            lock.lock();
            Session session = getSession(wss);
            
            if(session != null){
            	session.setWebSocketSession(wss);
            	
            	WebsocketResponse websocketResponse = new WebsocketResponse();
            	websocketResponse.setReponseType(TransportActionEnum.USER_LIST);
            	websocketResponse.setUsers(getUserList(session));
            	Gson gson = new Gson();
            	///session.getWebSocketSession().sendMessage(new TextMessage(gson.toJson(userResponseList)));
            	sendResponseToWebSocket(gson.toJson(websocketResponse), wss);
            }
        } finally {
            lock.unlock();
        }
    }
    
    private List<UserResponse> getUserList(Session session){
    	List<User> users = userRepository.findAll();
    	List<UserResponse> userResponseList = new ArrayList<>();
    	
    	List<Messages> messages = messagesRepository.findByReceiverUserIDAndSeen(session.getUser().getId(), 0);
    	
    	//UUID loggedinUserID = session.getUser().getId();
    	for (User user : users) {
    		// Skip logged in user
    		int unreadCount = 0;
    		if(!user.getEmail().equalsIgnoreCase(session.getUser().getEmail())){
    			
    			for (Messages messages2 : messages) {
					if(messages2.getSenderUserID().equals(user.getId())){
						unreadCount++;
					}
				}
    			
    			userResponseList.add(new UserResponse(user.getId(), user.getName(), unreadCount));
    		}
		}
    	
    	return userResponseList;
    }
    
    public void sendResponseToWebSocket(String reponseBody, WebSocketSession wss) throws IOException{
    	Session session = getSession(wss);
    	session.getWebSocketSession().sendMessage(new TextMessage(reponseBody));
    }
    

    @Override
    public void handleMessage(WebSocketSession wss, WebSocketMessage<?> webSocketMessage) throws Exception {
    	
    	try{
    		lock.lock();
            Session currentSession = getSession(wss);
            User currentUser = currentSession.getUser();
            
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
    	
        /*try {
            lock.lock();
            Session currentSession = getSession(wss);
            User currentUser = currentSession.getUser();
            String payload = ((TextMessage) webSocketMessage).getPayload();
            String cmd = payload.substring(0, payload.indexOf("\n"));
            String body = payload.substring(payload.indexOf("\n") + 1, payload.length());
            if ("msg".equals(cmd)) {
                Message msg = new Message();
                msg.setTextMessage(true);
                msg.setBody(body);
                msg.setDate(System.currentTimeMillis());
                msg.setSenderPresentation(currentUser.getPresentation());
                msg.setSenderUsername(currentUser.getUsername());
                msg.setReceiverUsername(currentSession.getOtherSideUsername());

                routeMessage(currentUser.getUsername(), msg);
            } else if ("change-page".equals(cmd)) {
                currentSession.setOtherSideUsername(body);
                changePage(currentSession);
            } else if ("delete-msg".equals(cmd)) {
                Message msg = messageRepository.findById(UUID.fromString(body)).get();
                deleteMessage(msg, currentSession);
            } else if ("top".equals(cmd)) {
                String otherSideUsername = currentSession.getOtherSideUsername();
                sendMessages(currentUser.getUsername(), otherSideUsername, currentSession, Long.parseLong(body), "load");
            } else if ("ping".equals(cmd)) {
                currentSession.getWebSocketSession().sendMessage(new TextMessage("pong\n"));
            } else {
                logger.error("Unsupported command!");
            }
        } finally {
            lock.unlock();
        }*/
    }
    
    private void sendMessage(User currentUser, MessageRequest messageRequest){
    	Messages messages = new Messages();
    	messages.setSenderUserID(currentUser.getId());
    	messages.setReceiverUserID(messageRequest.getUserId());
    	messages.setMessageBody(messageRequest.getMessage());
    	messages.setDate(new Date().getTime());
    	messagesRepository.save(messages);
    	
    	try{
	    	// Boardcast to All WS sessions
	    	List<Session> sessionList = sessionRepository.findAll();
	    	for (Session session : sessionList) {
	    		WebsocketResponse websocketResponse = new WebsocketResponse();
	        	websocketResponse.setReponseType(TransportActionEnum.USER_LIST);
	        	websocketResponse.setUsers(getUserList(session));
	        	Gson gson = new Gson();
	        	
				sendResponseToWebSocket(gson.toJson(websocketResponse), session.getWebSocketSession());
			}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    }
    
    private void fetchMessage(WebSocketSession wss, User currentUser, MessageRequest messageRequest) throws IOException{
    	UUID senderUserID = currentUser.getId();
    	UUID receiverUserID = messageRequest.getUserId();
    	List<UUID> listOfUserIDs = new ArrayList<UUID>();
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
    	
    	// Change seen status
    	messagesRepository.updateSeenStatus(receiverUserID, senderUserID);
    }
    
    private void setUserNameAgainstUserID(List<User> users, MessageResponse messageResponse){
    	for (User user : users) {
			if(messageResponse.getSenderUserID().equals(user.getId())){
				messageResponse.setSenderUserName(user.getName());
			}else if(messageResponse.getReceiverUserID().equals(user.getId())){
				messageResponse.setReceiverUserName(user.getName());
			}
		}
    }
    
    private void deleteMessage(MessageRequest messageRequest){
    	System.out.println("deleteMessage Implementation pending..!!");
    }
    

    private void sendMessages(String currentSideUsername, String otherSideUsername, Session currentSession, long date, String cmd) throws IOException {
        List<Message> messages;
        if (Constants.broadcastUsername.equals(otherSideUsername)) {
            messages = messageRepository.fetchMessages(loadingMessagesChunksize, otherSideUsername, date);
        } else {
            messages = messageRepository.fetchMessages(loadingMessagesChunksize, currentSideUsername, otherSideUsername, date);
        }
        Collections.reverse(messages);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messages.size(); i++) {
            Message m = messages.get(i);
            sb.append(createTextMessageUIComponent(m, currentSideUsername.equals(m.getSenderUsername())));
        }
        currentSession.getWebSocketSession().sendMessage(new TextMessage(cmd + "\n" + sb.toString()));
        if (messages.size() > 0) {
            currentSession.getWebSocketSession().sendMessage(new TextMessage("checkForLoadingMore\n"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession wss, CloseStatus closeStatus) throws Exception {
        try {
            lock.lock();
            removeWebSocketSession(wss);
        } finally {
            lock.unlock();
        }
    }

    public void logout(User user) {
        try {
            lock.lock();
            if (user != null && sessionMapFromUN.containsKey(user.getUsername())) {
                removeWebSocketSession(sessionMapFromUN.get(user.getUsername()).getWebSocketSession());
            }
        } finally {
            lock.unlock();
        }
    }

    private void removeWebSocketSession(WebSocketSession wss) {
        if (sessionMapFromWSS.containsKey(wss.getId())) {
            User user = sessionMapFromWSS.get(wss.getId()).getUser();
            if (user != null) {
                sessionMapFromUN.remove(user.getUsername());
            }
            sessionMapFromWSS.remove(wss.getId());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void updateAllUserLists(String excludeUsername) {
        try {
            lock.lock();
            if (excludeUsername != null) {
                List<Message> messages = messageRepository.findAllBySenderUsername(excludeUsername);
                messages.addAll(messageRepository.findAllByReceiverUsername(excludeUsername));
                messages.forEach(x -> messageRepository.delete(x));
                List<UnreadMessageCounter> unreadMessageCounters = unreadMessageCounterRepository.findAllByCurrentSideUsername(excludeUsername);
                unreadMessageCounters.addAll(unreadMessageCounterRepository.findAllByOtherSideUsername(excludeUsername));
                unreadMessageCounterRepository.deleteAll(unreadMessageCounters);
            }
            sessionMapFromWSS.values().forEach(x -> {
                try {
                    if (x.getUser() != null) {
                        if (excludeUsername != null && x.getOtherSideUsername().equals(excludeUsername)) {
                            x.setOtherSideUsername(Constants.broadcastUsername);
                            changePage(x);
                        }
                        x.getWebSocketSession().sendMessage(new TextMessage(
                                createUsersListUIComponent(x.getUser().getUsername(), x.getOtherSideUsername())));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } finally {
            lock.unlock();
        }
    }

    private Session getSession(WebSocketSession wss) {
    	Session session = null;
    	
    	String requestQueryString = wss.getUri().getQuery();
    	String jwtToken = requestQueryString.substring(requestQueryString.indexOf('=') + 1, requestQueryString.length());
       	session = sessionRepository.findById(jwtToken).get();
        return session;
    }

    private String createTextMessageUIComponent(Message msg, boolean self) throws IOException {
        String text = "";
        Map<String, String> params = new HashMap<>();
        params.put("id", "" + msg.getId());
        params.put("date", "date=\"" + msg.getDate() + "\"");
        params.put("image", "");
        params.put("onclick", "");
        params.put("body", msg.getBody());
        params.put("dateStr", utils.formatTime(msg.getDate()));
        if (self) {
            text += utils.readPage("/chat-msg-right.html", params);
        } else {
            params.put("title", msg.getSenderPresentation());
            text += utils.readPage("/chat-msg-left.html", params);
        }
        return text;
    }

    
    
    private String createUsersListUIComponent(String username, String activeUsername) throws IOException {
        List<User> users = userRepository.findAll();
        String text = "users\n";
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            params.clear();
            if (user.getUsername().equals(activeUsername)) {
                params.put("name", user.getPresentation());
                text += utils.readPage("/sidebar-entry-active.html", params);
            } else {
                params.put("name", user.getPresentation());
                params.put("onclick", " onclick=\'changePage(\"" + user.getUsername() + "\")\' ");
                UnreadMessageCounter unreadMessageCounter = unreadMessageCounterRepository.findByCurrentSideUsernameAndOtherSideUsername(username, user.getUsername());
                Integer count = (unreadMessageCounter == null) ? 0 : unreadMessageCounter.getCount();
                params.put("count", (count == 0) ? ""
                        : "<div class=\"SidebarEntryUnreadCount SimpleText SimpleFont\">" + count + "</div>");
                text += utils.readPage("/sidebar-entry-passive.html", params);
            }
        }
        return text;
    }

    private void changePage(Session session) throws IOException {
        String otherSideUsername = session.getOtherSideUsername();
        session.getWebSocketSession().sendMessage(new TextMessage(
                createUsersListUIComponent(session.getUser().getUsername(), otherSideUsername)));
        sendMessages(session.getUser().getUsername(), otherSideUsername, session, System.currentTimeMillis(), "page");
        UnreadMessageCounter unreadMessageCounter = unreadMessageCounterRepository.findByCurrentSideUsernameAndOtherSideUsername(session.getUser().getUsername(), otherSideUsername);
        if (unreadMessageCounter != null) {
            unreadMessageCounterRepository.delete(unreadMessageCounter);
        }
    }

    private void routeMessage(String senderUsername, Message msg) throws IOException {
        Session currentSession = sessionMapFromUN.get(senderUsername);
        String otherSideUsername = currentSession.getOtherSideUsername();
        msg.setSenderUsername(senderUsername);
        msg.setReceiverUsername(otherSideUsername);
        messageRepository.save(msg);
        String selfPack = "msg\n" + createTextMessageUIComponent(msg, true);
        String otherPack = "msg\n" + createTextMessageUIComponent(msg, false);
        routePacket(selfPack, otherPack, currentSession);
    }

    private void routePacket(String selfPack, String otherPack, Session currentSession) throws IOException {
        String senderUsername = currentSession.getUser().getUsername();
        String otherSideUsername = currentSession.getOtherSideUsername();
        currentSession.getWebSocketSession().sendMessage(new TextMessage(selfPack));
        if (otherSideUsername.equals(Constants.broadcastUsername)) {
            for (User user : userRepository.findAll()) {
                String username = user.getUsername();
                if (username.equals(senderUsername) || username.equals(Constants.broadcastUsername)) {
                    continue;
                }
                Session userSession = sessionMapFromUN.get(username);
                sendOtherSideMessage(otherPack, otherSideUsername, username, userSession);
            }
        } else {
            if (!otherSideUsername.equals(senderUsername)) {
                Session otherSideSession = sessionMapFromUN.get(otherSideUsername);
                sendOtherSideMessage(otherPack, senderUsername, otherSideUsername, otherSideSession);
            }
        }
    }

    private void sendOtherSideMessage(String msg, String otherSideUsername, String senderUsername, Session session) throws IOException {
        if ((session != null) && session.getOtherSideUsername().equals(otherSideUsername)) {
            session.getWebSocketSession().sendMessage(new TextMessage(msg));
        } else {
            UnreadMessageCounter unreadMessageCounter = unreadMessageCounterRepository.findByCurrentSideUsernameAndOtherSideUsername(senderUsername, otherSideUsername);
            int cnt = (unreadMessageCounter == null) ? 1 : (unreadMessageCounter.getCount() + 1);
            if (unreadMessageCounter == null) {
                unreadMessageCounter = new UnreadMessageCounter();
                unreadMessageCounter.setCurrentSideUsername(senderUsername);
                unreadMessageCounter.setOtherSideUsername(otherSideUsername);
            }
            unreadMessageCounter.setCount(cnt);
            unreadMessageCounterRepository.save(unreadMessageCounter);
            if (session != null) {
                session.getWebSocketSession().sendMessage(new TextMessage(
                        createUsersListUIComponent(session.getUser().getUsername(), session.getOtherSideUsername())));
            }
        }
    }

    public void deleteMessage(Message msg, Session currentSession) throws IOException {
        String pack = "delete-msg\n" + msg.getId();
        messageRepository.delete(msg);
        routePacket(pack, pack, currentSession);
    }
}
