import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
  constructor() {
    this.client = null;
    this.connected = false;
  }

  connect(onMessageReceived) {
    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => {
        console.log('STOMP: ' + str);
      },
      onConnect: () => {
        console.log('✅ Connected to WebSocket');
        this.connected = true;
        
        this.client.subscribe('/topic/tasks', (message) => {
          const taskExecution = JSON.parse(message.body);
          console.log('📨 Received task:', taskExecution);
          onMessageReceived(taskExecution);
        });
      },
      onDisconnect: () => {
        console.log('❌ Disconnected from WebSocket');
        this.connected = false;
      },
      onStompError: (frame) => {
        console.error('❌ STOMP error:', frame.headers['message']);
        console.error('Details:', frame.body);
      },
    });

    this.client.activate();
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
    }
  }

  isConnected() {
    return this.connected;
  }
}

export default new WebSocketService();