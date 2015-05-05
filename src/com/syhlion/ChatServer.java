package com.syhlion;

import java.io.BufferedInputStream;

import java.io.BufferedOutputStream;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.*;



/**
 * Created by scott on 2015/4/21.
 */
public class ChatServer{

    private ServerSocket serverSocket;
    private Vector<ChatConnection> connList;
    private byte[] buffer = new byte[2048];


    public ChatServer(int port) throws IOException{
        serverSocket = new ServerSocket(port);
        connList = new Vector<ChatConnection>();
        while (true) {
            Socket socket = serverSocket.accept();

            ChatConnection conn = new ChatConnection(socket);
            connList.add(conn);
            Thread t = new Thread(conn);
            t.start();
        }

    }



    public void writeMsg(String target, String msg) {
        if(target.equals("ALL")) {

        } else {
            for(ChatConnection cc : connList) {
                String s = cc.toString();
                if (s.equals(target)) {
                    cc.write0(msg);
                }
            }
        }
    }

    public class ChatConnection implements Runnable{
        private Socket socket;
        private BufferedInputStream bufferInput;
        private BufferedOutputStream bufferOutput;
        private byte[] buffer = new byte[2048];
        private String name;
        public ChatConnection(Socket socket) throws IOException{
            this.socket = socket;
            bufferInput = new BufferedInputStream(this.socket.getInputStream());
            bufferOutput =  new BufferedOutputStream(this.socket.getOutputStream());
        }

        public void write0(String msg) {
            try {
                socket.getOutputStream().write(msg.getBytes("UTF-8"));
                socket.getOutputStream().flush();
                //bufferOutput.write(msg.getBytes("UTF-8"));
                bufferOutput.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            int a = 0;
            int length = 0;
            String context;
            try {
                while((a = bufferInput.read(buffer))>0) {
                    length = 0;

                    for (int i = 0; i < buffer.length; ++i) {
                        if (buffer[i] == 0) {
                            length = i;
                            break;
                        }
                    }

                    context = new String(buffer, 0, length);
                    if(context.indexOf("open_") != -1) {
                        this.name = context.substring(5);
                    }else {
                        String uname = context.substring(0, context.indexOf("^"));
                        String msg = context.substring(context.indexOf("^") + 1);
                        writeMsg(uname, msg+"\n");


                    }
                }
            } catch(IOException e) {

            }

        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
