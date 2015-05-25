package com.syhlion;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * Created by scott on 2015/4/24.
 */
public class NioChatServer implements Runnable{

    private Selector selector;
    private SelectionKey serverKey;
    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    private Vector<SocketChannel> channels;
    private HashMap<String, SocketChannel> userList;

    public NioChatServer(int port) {
        try {
            selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            serverKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            readBuffer = ByteBuffer.allocate(8192);
            userList = new HashMap<String, SocketChannel>();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void run() {
        try {
            while (true) {
                int n = selector.select();
                if ( n == 0) {
                    continue;
                }
                Iterator<SelectionKey> select =  selector.selectedKeys().iterator();
                while(select.hasNext()) {
                    SelectionKey clientKey = select.next();
                    select.remove();
                    if(clientKey.isAcceptable()) {
                        this.accept(clientKey);
                    }
                    if(clientKey.isReadable()) {
                        this.read(clientKey);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void accept(SelectionKey key) throws IOException{
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        Socket socket = socketChannel.socket();
        socketChannel.configureBlocking(false);

        socketChannel.register(this.selector, SelectionKey.OP_READ);
        System.out.println("登入 : " + socket.getInetAddress());

    }

    private void read(SelectionKey key) throws IOException{

        SocketChannel socketChannel = ((SocketChannel) key.channel());

        int numRead;
        try {
            numRead = socketChannel.read(this.readBuffer);
        } catch (IOException e) {
            System.out.println(getUser(socketChannel) + " Disconnect");
            userList.remove(getUser(socketChannel));
            key.cancel();
            socketChannel.close();
            boradCast("userlists:" + getOnlineUser());
            System.out.println("userlists: " + getOnlineUser());
            return;
        }

        if (numRead == -1) {
            System.out.println(getUser(socketChannel)+"Disconnect");
            userList.remove(getUser(socketChannel));
            key.cancel();
            socketChannel.close();
            boradCast("userlists:" + getOnlineUser());
            System.out.println("userlists: " + getOnlineUser());
            return;
        }

        this.readBuffer.flip();
        int limit = this.readBuffer.limit();
        int step = 0;
        byte[] buffer = new byte[limit];
        while (this.readBuffer.hasRemaining()) {
            buffer[step] = this.readBuffer.get();
            step++;
        }
        this.readBuffer.clear();
        String text = new String(buffer);
        String[] data = text.split(":");
        switch (data[0]) {
            case "login" :
                SocketChannel channel = userList.get(data[1]);
                if(channel == null) {
                    userList.put(data[1], socketChannel);

                    boradCast("userlists:" + getOnlineUser());
                    System.out.println("login: " + data[1]);
                    System.out.println("userlists: "+ getOnlineUser());

                } else {
                    String m = "duplicate:"+data[1];
                    socketChannel.write(ByteBuffer.wrap(m.getBytes()));
                    System.out.println("duplicate:" + data[1]);
                }

                break;
            case "message" :
                if(data.length != 3) break;
                SocketChannel toChannel = userList.get(data[1]);
                if(toChannel == null) {
                    String m = "errorTO:"+data[1];
                    socketChannel.write(ByteBuffer.wrap(m.getBytes()));
                    System.out.println("errorTO:" + data[1]);
                } else {
                    String m = "message:"+getUser(socketChannel)+":"+data[2];
                    toChannel.write(ByteBuffer.wrap(m.getBytes()));
                    System.out.println("message:" +getUser(socketChannel)+":"+data[2]);
                }


                break;
        }
    }

    private void boradCast(String msg) throws IOException{
        for(Map.Entry<String, SocketChannel> entry : userList.entrySet()){
            entry.getValue().write(ByteBuffer.wrap(msg.getBytes()));
        }

    }

    private String getOnlineUser(){
        StringBuffer sb = new StringBuffer();
        List<String> tmp = new ArrayList<String>();

        for(Map.Entry<String, SocketChannel> entry : userList.entrySet()){
            tmp.add(entry.getKey());
        }
        if (!tmp.isEmpty()) {
            sb.append(tmp.remove(0));
            for(String s : tmp) {
                sb.append(",");
                sb.append(s);
            }
        }

        return sb.toString();
    }
    private  String getUser(SocketChannel socketChannel) {
        String m = "";
        for(Map.Entry<String, SocketChannel> entry : userList.entrySet()){
            if(entry.getValue() == socketChannel) {
                m = entry.getKey();
            }
        }
        return m;
    }


}
