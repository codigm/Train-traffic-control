import { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export const useStompClient = () => {
    const [client, setClient] = useState(null);
    const [connected, setConnected] = useState(false);
    const [liveStateUpdates, setLiveStateUpdates] = useState([]);
    const [emergencies, setEmergencies] = useState([]);

    useEffect(() => {
        const socket = new SockJS(import.meta.env.VITE_WS_BASE_URL || 'http://localhost:8080/ws');
        const stompClient = new Client({
            webSocketFactory: () => socket,
            onConnect: () => {
                setConnected(true);
                console.log('Connected to WebSocket!');
                
                // Subscribe to Live State
                stompClient.subscribe('/topic/livestate', (message) => {
                    if (message.body) {
                        const state = JSON.parse(message.body);
                        setLiveStateUpdates((prev) => [state, ...prev].slice(0, 50));
                    }
                });

                // Subscribe to Emergencies
                stompClient.subscribe('/topic/emergencies', (message) => {
                    if (message.body) {
                        const event = JSON.parse(message.body);
                        setEmergencies((prev) => [event, ...prev].slice(0, 10));
                    }
                });
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
            },
            onDisconnect: () => {
                setConnected(false);
                console.log('Disconnected from WebSocket!');
            }
        });

        stompClient.activate();
        setClient(stompClient);

        return () => {
            if (stompClient.active) {
                stompClient.deactivate();
            }
        };
    }, []);

    return { client, connected, liveStateUpdates, emergencies };
};
