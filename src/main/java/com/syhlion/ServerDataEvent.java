package com.syhlion;

import java.nio.channels.SocketChannel;

/**
 * Created by scott on 2015/4/28.
 */
public class ServerDataEvent {
    public NioChatServer server;
    public SocketChannel socket;
    public byte[] data;

    public ServerDataEvent(NioChatServer server, SocketChannel socket, byte[] data) {
        this.server = server;
        this.socket = socket;
        this.data = data;
    }
}
