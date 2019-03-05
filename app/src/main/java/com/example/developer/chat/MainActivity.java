package com.example.developer.chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.developer.chat.network.TCPConnection;
import com.example.developer.chat.network.TCPConnectionListener;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements TCPConnectionListener {

    private String ipAddress;
    private final int PORT = 8189;

    private TCPConnection mConnection;

    private EditText mIpEditText;
    private EditText mNicknameEditText;
    private EditText mLogEditText;
    private EditText mInputEditText;
    private Button mCreateServerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIpEditText = findViewById(R.id.ip_edit_text);
        mNicknameEditText = findViewById(R.id.nickname_edit_text);
        mLogEditText = findViewById(R.id.log_edit_text);
        mInputEditText = findViewById(R.id.input_edit_text);
        mCreateServerButton = findViewById(R.id.create_server_btn);
    }

    public void onCreateServer(View v){
        mCreateServerButton.setEnabled(false);
        new ChatServer();
    }

    public void onConnectToServer(View v){
        ipAddress = mIpEditText.getText().toString();

        if (!ipAddress.equals(""))
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        mConnection = new TCPConnection(MainActivity.this, ipAddress, PORT);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }).start();
    }

    public void onSendMessage(View v){
        final String msg = mInputEditText.getText().toString();
        if (msg.equals("") || mNicknameEditText.getText().toString().equals(""))
            return;

        mInputEditText.setText("");

        new Thread(new Runnable() {
            @Override
            public void run() {
                mConnection.sendString(mNicknameEditText.getText().toString() + ": " + msg);
            }
        }).start();
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMessage("Connection ready...");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        printMessage(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {

    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMessage("Connection exception: " + e);
    }

    private void printMessage(String msg) {
        mLogEditText.append(msg + "\n");
    }
}
