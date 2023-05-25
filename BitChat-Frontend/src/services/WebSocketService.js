// WebSocketService.js

class WebSocketService {
  constructor(token) {
    this.socket = null;
    this.token = token;
  }

  connect() {

    this.socket = new WebSocket('wss://localhost:8888/ws?token='+this.token); // Replace with your WebSocket server URL

    this.socket.onopen = () => {
      console.log('WebSocket connection established.');
      // Perform any actions after the connection is established
    };

    this.socket.onmessage = (event) => {
      console.log('Received message:', event.data);
      alert('Websocket response : ' + event.data)


    };

    this.socket.onclose = () => {
      console.log('WebSocket connection closed.');
      // Perform any actions after the connection is closed
    };
  }

  send(message) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(message);
    } else {
      console.log('WebSocket connection is not open.');
    }
  }

  disconnect() {
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
  }
}

export default WebSocketService;
