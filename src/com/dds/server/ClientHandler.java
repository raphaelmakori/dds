package com.dds.server;

import com.dds.shared.CustomerOrder;
import com.dds.shared.Request;
import com.dds.shared.RequestType;
import com.dds.shared.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final BusinessState businessState;

    public ClientHandler(Socket socket, BusinessState businessState) {
        this.socket = socket;
        this.businessState = businessState;
    }

    @Override
    public void run() {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {

            while (true) {
                Request request = (Request) inputStream.readObject();
                Response response = handleRequest(request);
                outputStream.writeObject(response);
                outputStream.flush();
            }
        } catch (EOFException ignored) {
            System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
        } catch (Exception exception) {
            System.out.println("Client handler stopped for " + socket.getRemoteSocketAddress()
                    + " because: " + exception.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
                // Nothing else to do during cleanup.
            }
        }
    }

    private Response handleRequest(Request request) {
        if (request == null || request.getType() == null) {
            return new Response(false, "Invalid request.", null);
        }

        if (request.getType() == RequestType.PLACE_ORDER) {
            return businessState.placeOrder((CustomerOrder) request.getPayload());
        }
        if (request.getType() == RequestType.GET_REPORT) {
            return new Response(true, "Report generated successfully.", businessState.buildAdminReport());
        }
        if (request.getType() == RequestType.GET_MENU) {
            return new Response(true, "Menu retrieved successfully.", businessState.getMenu());
        }
        if (request.getType() == RequestType.PING) {
            return new Response(true, "Server is running.", null);
        }

        return new Response(false, "Unsupported request type.", null);
    }
}
