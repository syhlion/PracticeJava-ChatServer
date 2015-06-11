package com.syhlion;

import java.nio.channels.SocketChannel;

/**
 * Created by scott on 2015/6/11.
 */
public class ChangeRequest {
    public static final int REGISTER = 1;
    public static final int CHANGEOPS = 2;

    public SocketChannel socket;
    public int type;
    public int ops;

    public ChangeRequest(SocketChannel socket, Object p1, int opWrite) {
        this.socket = socket;
        this.type = type;
        this.ops = ops;
    }
}
