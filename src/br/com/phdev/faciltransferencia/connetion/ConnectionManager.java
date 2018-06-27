/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.phdev.faciltransferencia.connetion;

import br.com.phdev.faciltransferencia.misc.FTClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public class ConnectionManager {
    
    private OnReadListener readListener;
    private OnConnectedListener connectedListener;
    private List<FTClient> clients;
    private List<WriteListener> writeListeners;
    
    public ConnectionManager(OnConnectedListener connectedListener, OnReadListener readListener) {
        this.clients = new ArrayList<>();
        this.writeListeners = new ArrayList<>();
        this.connectedListener = connectedListener;
        this.readListener = readListener;
        new BroadcastServer().start();
    }   
    
    public List<FTClient> getClients() {
        return this.clients;
    }        
        
    public void onClientFound(FTClient client) {
        client.getTcpConnection().setOnConnectedListener(this.connectedListener);
        client.getTcpConnection().setOnReadListener(this.readListener);        
        this.writeListeners.add(client.getTcpConnection().getWriteListener());
        client.getTcpConnection().start();
        this.clients.add(client);        
    }
    
    public List<WriteListener> getWriteListeners() {
        return this.writeListeners;
    }
    
    class BroadcastServer extends Thread{
                
        private boolean serverBroadcastRunning;
        
        public BroadcastServer() {            
        }
        
        @Override
        public void run() {
            DatagramSocket broadcastSocket = null;
            this.serverBroadcastRunning = true;
            boolean canJump = false;
            try {
                broadcastSocket = new DatagramSocket(6012);
                byte[] bytes = new byte[30];
                while (this.serverBroadcastRunning) {
                    DatagramPacket broadcastPacket = new DatagramPacket(bytes, 30);
                    //System.out.println("Esperando broadcast de possiveis clientes");
                    broadcastSocket.receive(broadcastPacket);
                    //System.out.println("Um possivel cliente mandou broadcast");
                    String clientAlias = new String(broadcastPacket.getData());                   
                    InetAddress address = broadcastPacket.getAddress();
                    
                    for (FTClient c : clients) {
                        if (c.getAddress().getHostAddress().equals(address.getHostAddress())) {
                            //System.out.println("Host ja adicionado: " + address.getHostAddress());
                            canJump = true;
                            break;
                        }                            
                    }
                    if (canJump) {
                        canJump = false;
                        continue;
                    }                                            
                    FTClient ftc = new FTClient(clientAlias, address);                    
                    ConnectionManager.this.onClientFound(ftc);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean isServerBroadcastRunning() {
            return serverBroadcastRunning;
        }                
        
    }
    
}
