/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.phdev.faciltransferencia.connetion;

import br.com.phdev.faciltransferencia.connetion.intefaces.Connection;
import br.com.phdev.faciltransferencia.connetion.intefaces.OnReadListener;
import br.com.phdev.faciltransferencia.connetion.intefaces.WriteListener;
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
    private Connection.OnClientConnectionTCPStatusListener onClientConnectionTCPStatusListener;

    private boolean running;

    public TCPClient(InetAddress address, String alias) {
        this.address = address;
        this.alias = alias;
    }

    public void setOnClientConnectionTCPStatusListener(Connection.OnClientConnectionTCPStatusListener onClientConnectionTCPStatusListener) {
        this.onClientConnectionTCPStatusListener = onClientConnectionTCPStatusListener;
    }

    public void setOnReadListener(OnReadListener readListener) {
        this.readListener = readListener;
    }

    public WriteListener getWriteListener() {
        return this;
    }

    public void close() {
        try {
            this.clientTcpSocket.close();
            this.outputStream.close();
            this.inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.onClientConnectionTCPStatusListener = null;
            this.readListener = null;
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        try {

            System.out.println("Tentando contanto com o smartphone via TCP...");
            this.clientTcpSocket = new Socket(this.address.getHostAddress(), SERVER_TRANSFER_PORT);
            System.out.println("Conectado ao smartphone");

            this.outputStream = this.clientTcpSocket.getOutputStream();
            this.inputStream = this.clientTcpSocket.getInputStream();

            if (this.onClientConnectionTCPStatusListener != null) {
                this.onClientConnectionTCPStatusListener.onConnect(TCPClient.this, alias);
            } else {
                throw new RuntimeException("onClientConnectionTCPStatusListener não registrado");
            }

            bytes = new byte[50];

            this.running = true;
            while (this.running) {
                try {
                    int bytesReaded = inputStream.read(bytes);
                    if (bytesReaded == -1) {
                        this.onClientConnectionTCPStatusListener.onDisconnect(this.alias);
                        break;
                    }
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
            this.outputStream.write(bytes);
            this.outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(byte[] bytes, int off, int length) {
        try {
            this.outputStream.write(bytes, off, length);
            this.outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
