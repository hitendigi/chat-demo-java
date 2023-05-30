	var accessToken = null;
	var websocket = null;
	var loggedUserID = null;
	var conversationUserID = null;
	
	function doLogin(){
		
	 var name = $('#login_name').val();
	 var username = $('#login_username').val();
	 
	 var loginData = {name:name, username:username} 
		
	  $.ajax({
  	    url: "https://localhost:8888/api/auth/signin",
	        type: "POST",
	        data: JSON.stringify(loginData),
	        contentType: "application/json",
	        dataType: "json",
	        crossDomain: true,
	        success: function (data) {
	        	console.log('Successfully login!')
	        	accessToken = data.accessToken;
	        	loggedUserID = data.id;
	        	console.log(data);
	        	
	        	// Set login user 
	        	$('#loginuser').html("Logged in as " + data.username + 
	        			"   <a href='#' onclick='logout()';>Logout</a>")
	        	
	        	$('#loginbox').hide()
	        	$('#chatbox').show()
	        	// wss connect
	        	wssconnect(accessToken);	
	        },
	        error: function (err) {
	            // handle your error logic here
	        }
	    });
	}
	
	function logout(){
		$.ajax({
	  	    url: "https://localhost:8888/api/auth/logout?token="+accessToken,
		        type: "GET",
		        contentType: "application/json",
		        dataType: "json",
		        crossDomain: true,
		        success: function (data) {
		        	console.log('Successfully logout!')	
		        },
		        error: function (err) {
		            // handle your error logic here	
		        	console.log(err)
		        }
		    });
		location.reload();
	}
	
	function displayUserList(websocketJsonResponseData){
		console.log('displayUserList()');
		var userList = websocketJsonResponseData.users;
		
		var userListUI = "";
		for(let i = 0; i < userList.length; i++) {
		    let user = userList[i];
		    let unreadCountUI = "";
		    if(user.unreadCount > 0){
		    	unreadCountUI = " (" + user.unreadCount + ")";
		    }
		    
		    userListUI = userListUI + 
		    		"<div style='margin-bottom: 50px;'>"+
			    		"<div class='chatbox__user--active' onclick='fetchMessageConversation(\""+user.id+"\")'>"+
				    		"<div>"+
			        			"<p style='font-size: smaller;font-weight: bold;' id='useractive_"+user.id+"'>"+user.name+ " " + unreadCountUI +"</p>"+
				    		"</div>"+
						    "<div>"+
				    			"<p style='margin-left: 26px;margin-top: 4px;'>"+user.lastMessage+"</p>"+
				    		"</div>"+
			    		"</div>"+
		    		"</div>";
		}
		$('#userlist').html(userListUI);
	}
	
	function fetchMessageConversation(userid){
		console.log('calling message convesation for userid : ' + userid)
		conversationUserID = userid;
		var wssRequestData = 
			{
			    "userId" : userid,
			    "action" : "MESSAGE_CONVERSATION"
			}
		sendWssRequest(wssRequestData);
	}
	
	function displayMessageConversation(websocketJsonResponse){
		console.log('displayMessageConversation()');
		console.log(websocketJsonResponse)
		var messageConversationUI = "";
		var messageList = websocketJsonResponse.messages;
		
		for(let i = 0; i < messageList.length; i++) {
		    let message = messageList[i];
		    
		    var floatClassName = "";
		    if(loggedUserID == message.senderUserID){
		    	floatClassName = "floatright";
		    }else{
		    	floatClassName = "floatleft"
		    }
		    messageConversationUI = messageConversationUI + 
				"<div class='chatbox__messages ng-scope' ng-repeat='message in messages'>"+
			    "  <div class='chatbox__messages__user-message'>"+
			    "    <div class='chatbox__messages__user-message--ind-message "+floatClassName+"'>"+
			    "      <p class='name ng-binding'>"+message.senderUserName+"</p>"+
			    "      <br>"+
			    "      <p class='message ng-binding'>"+message.messageBody+"</p>"+
			    "    </div>"+
			    "  </div>"+
			    "</div>"	
		}
		
		if(messageConversationUI == ''){
			messageConversationUI = '<div class="nocontent"><p>No messages found!</p></div>'
		}
		
		$('#messageconversation').html(messageConversationUI)
		
		// change active element
		$('#userlist').find('div > div > div > p').removeClass('active');
		$('#useractive_' + conversationUserID).addClass('active');
		
		// Scroll to bottom
		$('#messageconversation').scrollTop($('#messageconversation')[0].scrollHeight);
	}
	
	function sendMessage(){
		if(!conversationUserID){return;}
		
		var messageBody = $('#messageinput').val();
		
		var wssRequestData = 
		{
			    "userId" : conversationUserID,
			    "action" : "SEND_MESSAGE",
			    "message" : messageBody
		}
		sendWssRequest(wssRequestData);
		//console.log(wssRequestData);
		
		// reload conversation
		fetchMessageConversation(conversationUserID)
		
		$('#messageinput').val('');
	}
	
	function sendWssRequest(wssRequest){
		websocket.send(JSON.stringify(wssRequest));
	}
	
	function wssconnect(token){
		let socket = new WebSocket("wss://localhost:8888/ws?token="+token);
		websocket = socket;
		
		socket.onopen = function(e) {
		  //alert("[open] Connection established");
		  //alert("Sending to server");
		  //socket.send("My name is John");
		};

		socket.onmessage = function(event) {
		  	//alert(`[message] Data received from server: ${event.data}`);
		  	var websocketJsonResponse = event.data;
		  	websocketJsonResponse = JSON.parse(websocketJsonResponse);
		  	console.log(websocketJsonResponse);
		  	console.log(websocketJsonResponse.reponseType)
		  	if(websocketJsonResponse.reponseType == 'USER_LIST'){
		  		displayUserList(websocketJsonResponse);
		  	}else if(websocketJsonResponse.reponseType == 'MESSAGE_CONVERSATION'){
		  		displayMessageConversation(websocketJsonResponse);
		  	}
		};

		socket.onclose = function(event) {
		  if (event.wasClean) {
		    alert(`[close] Connection closed cleanly, code=${event.code} reason=${event.reason}`);
		  } else {
		    // e.g. server process killed or network down
		    // event.code is usually 1006 in this case
		    //alert('[close] Connection died');
		  }
		};

		socket.onerror = function(error) {
		  alert(`[error]`);
		};
	}