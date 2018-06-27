/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.phdev.faciltransferencia.connetion;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public class TCPClient extends Thread implements WriteListener {

    private final int SERVER_TRANSFER_PORT = 10011;
    
    private String alias;
    private InetAddress address;
    private Socket clientTcpSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private byte[] bytes;
    private OnReadListener readListener;
    private OnConnectedListener connectedListener;    

    public TCPClient(InetAddress address, String alias) {
        this.address = address;        
        this.alias = alias;
    }
    
    public void setOnConnectedListener(OnConnectedListener connectedListener) {
        this.connectedListener = connectedListener;
    }

    public void setOnReadListener(OnReadListener readListener) {
        this.readListener = readListener;
    }

    public WriteListener getWriteListener() {
        return this;
    }

    @Override
    public void run() {
        try {
            System.out.println("Tentando contanto com o smartphone via TCP...");
            this.clientTcpSocket = new Socket(this.address.getHostAddress(), SERVER_TRANSFER_PORT);
            System.out.println("Conectado ao smartphone");
            if (this.connectedListener != null) {
                this.connectedListener.onConnected(alias);
            }

            this.outputStream = this.clientTcpSocket.getOutputStream();
            this.inputStream = this.clientTcpSocket.getInputStream();

            bytes = new byte[30];

            while (true) {
                try {
                    int bytesReaded = inputStream.read(bytes);
                    this.readListener.onRead(bytes, bytesReaded);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (this.clientTcpSocket != null) {
                    this.clientTcpSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void write(byte[] bytes) {
        try {
            System.out.println("tamanho do buffer: " + bytes.length);
            //this.outputStream.write(bytes, 0, bytes.length); 
            this.outputStream.write(bytes);
            this.outputStream.flush();                        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
