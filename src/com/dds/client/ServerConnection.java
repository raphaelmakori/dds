package com.dds.client;

import com.dds.shared.Request;
import com.dds.shared.Response;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnection implements Closeable {
    private final Socket socket;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;

    public ServerConnection(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        this.inputStream = new ObjectInputStream(socket.getInputStream());
    }

    public Response send(Request request) throws IOException, ClassNotFoundException {
        outputStream.writeObject(request);
        outputStream.flush();
        return (Response) inputStream.readObject();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        outputStream.close();
        socket.close();
    }
}
