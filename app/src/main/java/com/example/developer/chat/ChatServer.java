package com.example.developer.chat;

import android.util.Log;

import com.example.developer.chat.network.TCPConnection;
import com.example.developer.chat.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ChatServer implements TCPConnectionListener {
    private ArrayList<TCPConnection> connections = new ArrayList<>();
    private final int PORT = 8189;
    private final String SERVER_TAG = "Server";

    ChatServer(){
        Log.i(SERVER_TAG, "Server running");

        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try(ServerSocket serverSocket = new ServerSocket(PORT)) {
                    while (true){
                        try{
                            new TCPConnection(ChatServer.this, serverSocket.accept());
                        }catch (IOException e){
                            Log.i(SERVER_TAG, "TCPConnection exception: " + e);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        serverThread.start();
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        sendToAllConnections("Client connected: " + tcpConnection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        sendToAllConnections(value);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnections("Client disconnected: " + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        Log.i(SERVER_TAG, "TCPConnection exception: " + e);
    }

    private void sendToAllConnections(String value){
        Log.i(SERVER_TAG, value);
        for (TCPConnection connection: connections)
            connection.sendString(value);
    }
}
