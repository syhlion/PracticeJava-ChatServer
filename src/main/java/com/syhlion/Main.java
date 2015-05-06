package com.syhlion;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception{

        System.out.print("Please Input Port:");
        Scanner scanner = new Scanner(System.in);
        String s = scanner.next();
        int port = Integer.parseInt(s);
        Thread t = new Thread(new NioChatServer(port));
        t.start();
        System.out.println("Server Start!");


    }


}
