package com.syhlion;

import java.nio.channels.SocketChannel;

/**
 * Created by scott on 2015/4/28.
 */
public class ServerDataEvent {
    public static final int LOGIN = 1;
    public static final int MESSAGE = 2;

    public final NioChatServer server;
    public final SocketChannel socket;
    public byte[] data;
    public String stringData;

    public ServerDataEvent(NioChatServer server, SocketChannel socket, byte[] data) {
        this.server = server;
        this.socket = socket;
        this.data = data;
        this.stringData = new String(data);
    }

    public int getEventType(){
        int eventType = 0;
        String[] type = stringData.split(":");
        switch (type[0]) {
            case "login" :
                eventType = ServerDataEvent.LOGIN;
                break;
            case "message" :
                eventType = ServerDataEvent.MESSAGE;
        }
        return eventType;
    }

    public String getMessage(){
        String m = "";
        String[] message = stringData.split(":");
        switch (getEventType()) {
            case ServerDataEvent.LOGIN :
                m = message[1];
                break;
            case ServerDataEvent.MESSAGE :
                m = message[1]+message[2];
                break;
        }

        return m;
    }
}
