package com.syhlion;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;


/**
 * Created by scott on 2015/4/21.
 */
public class ChatServer2 implements Runnable{
    private int port;
    private Selector selector;
    private SelectionKey serverKey;
    private boolean isRun;
    private Vector<String> unames;
    private HashMap<String, SelectionKey> userList;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public ChatServer2(int port) {

        unames = new Vector<String>();
        userList = new HashMap<String, SelectionKey>();
        isRun = true;
        init(port);

    }

    private void init(int port) {
        try {
            selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            serverKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        try {
            while(isRun) {
                int n = selector.select();
                if(n > 0) {
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        if(key.isAcceptable()) {
                            iter.remove();
                            ServerSocketChannel serverSocketChannel = ((ServerSocketChannel) key.channel());
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            if(socketChannel == null) {
                                continue;
                            }
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ);
                            Socket socket = socketChannel.socket();
                            key.attach("IP : " + socket.getRemoteSocketAddress()+" connect \t");
                        }

                        if(key.isValid() && key.isReadable()) {
                            readMsg(key);
                        }

                        if(key.isValid() && key.isWritable()) {
                            writeMsg(key);
                        }
                    }
                }
            }
        } catch (IOException e) {

        }
    }

    public void readMsg(SelectionKey key) throws IOException{
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        StringBuffer sb = new StringBuffer();
        int count = 0;
        try {
            count = channel.read(buffer);
            if(count < 0) throw new IOException();
            buffer.flip();
            sb.append(new String(buffer.array(), 0, count));
            String str = sb.toString();
            if (str.indexOf("open_") != -1) {
                String name = str.substring(5);
                printInfo(name + " online");
                userList.put(name, key);
                unames.add(name);
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey selKey = iter.next();
                    if (selKey != serverKey) {
                        selKey.attach(unames);
                        selKey.interestOps(selKey.interestOps() | SelectionKey.OP_WRITE);
                    }
                }
            } else if (str.indexOf("exit_") != -1) {
                String uname = str.substring(5);
                unames.remove(uname);
                key.attach("close");
                key.interestOps(SelectionKey.OP_WRITE);
                Iterator<SelectionKey> iter = key.selector().selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey selKey = iter.next();
                    if (selKey != serverKey && selKey != key) {
                        selKey.attach(unames);
                        selKey.interestOps(selKey.interestOps() | SelectionKey.OP_WRITE);
                    }
                }
                printInfo(uname + " offline");
            } else {// 读取客户端聊天消息
                String uname = str.substring(0, str.indexOf("^"));
                String msg = str.substring(str.indexOf("^") + 1);
                printInfo("(" + uname + ")：" + msg);
                SelectionKey userKey = userList.get(msg);
                String dateTime = sdf.format(new Date());
                String smsg = uname + " " + dateTime + "\n  " + msg + "\n";

                userKey.attach(smsg);
                userKey.interestOps(userKey.interestOps() | SelectionKey.OP_WRITE);
                /*Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey selKey = iter.next();
                    if (selKey != serverKey) {
                        selKey.attach(smsg);
                        selKey.interestOps(selKey.interestOps() | SelectionKey.OP_WRITE);
                    }
                }*/
            }

        } catch (IOException | AssertionError e) {
            key.cancel();
            channel.socket().close();
            channel.close();
            return;
        }



    }

    private void writeMsg(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Object obj = key.attachment();
        key.attach("");
        if (obj.toString().equals("close")) {
            key.cancel();
            channel.socket().close();
            channel.close();
            return;
        }else {
            channel.write(ByteBuffer.wrap(obj.toString().getBytes()));
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private void printInfo(String str) {
        System.out.println("[" + sdf.format(new Date()) + "] -> " + str);
    }




}