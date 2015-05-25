package com.syhlion;

import sun.misc.Cleaner;

import java.nio.channels.SocketChannel;

/**
 * Created by scott on 2015/5/19.
 */
public class Client {
    private final SocketChannel channel;
    private String clientName;
    public static final String UNKNOWN = "Unknown";
    private int loginMissCount;
    private boolean destoryFlag;

    public Client(SocketChannel socket) {
        this.channel = socket;
        this.clientName = Client.UNKNOWN;
        this.loginMissCount = 0;
        this.destoryFlag = false;
    }

    public void setClientName(String name){
        if(this.clientName.equals(Client.UNKNOWN)) {
            this.clientName = name;
        }
    }


    public void read(){

    }

    public void write(){

    }

    public boolean isDestory(){
        return destoryFlag;
    }

    @Override
    public String toString(){
        return clientName;
    }


}
