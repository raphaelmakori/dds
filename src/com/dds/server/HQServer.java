package com.dds.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HQServer {
    private static final int DEFAULT_PORT = 5050;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        BusinessState businessState = new BusinessState();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("HQ server started on port " + port + ".");
            System.out.println("Waiting for customer and administrator devices to connect...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connected: " + clientSocket.getRemoteSocketAddress());
                Thread thread = new Thread(new ClientHandler(clientSocket, businessState));
                thread.start();
            }
        } catch (IOException exception) {
            System.err.println("Unable to start the HQ server: " + exception.getMessage());
        }
    }
}
