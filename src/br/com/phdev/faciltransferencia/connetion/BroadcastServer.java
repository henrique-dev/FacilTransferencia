/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.phdev.faciltransferencia.connetion;

import br.com.phdev.faciltransferencia.connetion.intefaces.Connection;
import br.com.phdev.faciltransferencia.managers.ConnectionManager;
import br.com.phdev.faciltransferencia.transfer.FTClient;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public class BroadcastServer extends Thread {

    private final Connection.OnClientConnectionBroadcastStatusListener onClientConnectionBroadcastStatusListener;
    private boolean serverBroadcastRunning;

    public BroadcastServer(Connection.OnClientConnectionBroadcastStatusListener onClientConnectionBroadcastStatusListener) {
        this.onClientConnectionBroadcastStatusListener = onClientConnectionBroadcastStatusListener;
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
                System.out.println("Esperando broadcast de possiveis clientes");
                broadcastSocket.receive(broadcastPacket);
                System.out.println("Um possivel cliente mandou broadcast");
                String clientAlias = new String(broadcastPacket.getData());
                InetAddress address = broadcastPacket.getAddress();
                
                if (canJump) {
                    canJump = false;
                    continue;
                }
                FTClient ftc = new FTClient(clientAlias.trim(), address);
                this.onClientConnectionBroadcastStatusListener.onClientFound(ftc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isServerBroadcastRunning() {
        return serverBroadcastRunning;
    }

}
