package com.example.developer.chat.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;

public class TCPConnection {

    private final Socket socket;
    private final Thread rxThread;
    private final TCPConnectionListener eventListener;
    private final BufferedReader in;
    private final BufferedWriter out;

    public TCPConnection(TCPConnectionListener eventListener, String ipAddress, int port) throws IOException {
        this(eventListener, new Socket(ipAddress, port));
    }

    public TCPConnection(final TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter((new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8"))));
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while (!rxThread.isInterrupted())
                        if(!in.readLine().equals(""))
                            eventListener.onReceiveString(TCPConnection.this, in.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });

        rxThread.start();
    }

    public synchronized void sendString(String value){
        try {
            out.write(value + "\r\n");
            out.flush();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    private synchronized void disconnect() {
        rxThread.interrupt();
        try{
            socket.close();
        }catch (IOException e){
            eventListener.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String   toString(){
        return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort();
    }
}