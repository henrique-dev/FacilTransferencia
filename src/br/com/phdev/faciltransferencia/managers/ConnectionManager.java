/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.phdev.faciltransferencia.managers;

import br.com.phdev.faciltransferencia.connetion.BroadcastServer;
import br.com.phdev.faciltransferencia.connetion.intefaces.Connection;
import br.com.phdev.faciltransferencia.connetion.intefaces.OnReadListener;
import br.com.phdev.faciltransferencia.connetion.intefaces.WriteListener;
import br.com.phdev.faciltransferencia.transfer.FTClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import br.com.phdev.faciltransferencia.trasnfer.interfaces.OnObjectReceivedListener;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public class ConnectionManager implements OnReadListener, Connection.OnClientConnectionBroadcastStatusListener {

    private final BroadcastServer broadcastServer;

    private final Connection.OnClientConnectionTCPStatusListener onClientConnectionTCPStatusListener;
    private final OnObjectReceivedListener onObjectReceivedListener;

    private final List<FTClient> clients;
    private final List<WriteListener> writeListeners;

    public ConnectionManager(Connection.OnClientConnectionTCPStatusListener onClientConnectionTCPStatusListener, OnObjectReceivedListener onObjectReceivedListener) {
        this.clients = new ArrayList<>();
        this.writeListeners = new ArrayList<>();
        this.onObjectReceivedListener = onObjectReceivedListener;
        this.onClientConnectionTCPStatusListener = onClientConnectionTCPStatusListener;
        this.broadcastServer = new BroadcastServer(this);
    }

    public void startBroadcastServer() {
        this.broadcastServer.start();
    }

    public List<FTClient> getClients() {
        return this.clients;
    }

    @Override
    public void onClientFound(FTClient client) {       
        for (FTClient c : this.clients) {
            if (client.getAddress().getHostAddress().equals(c.getAddress().getHostAddress())) {
                return;
            }
        }
        System.out.println("Cliente adicionado");
        client.getTcpConnection().setOnClientConnectionTCPStatusListener(this.onClientConnectionTCPStatusListener);
        client.getTcpConnection().setOnReadListener(this);
        client.getTcpConnection().start();
        this.writeListeners.add(client.getTcpConnection().getWriteListener());
        this.clients.add(client);
    }

    public List<WriteListener> getWriteListeners() {
        return this.writeListeners;
    }

    @Override
    public void onRead(byte[] buffer, int bufferSize) {        
        this.onObjectReceivedListener.onObjectReceived(getObjectFromBytes(buffer, bufferSize));
    }

    public Object getObjectFromBytes(byte[] buffer, int bufferSize) {
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer, 0, bufferSize);
        ObjectInput in = null;
        Object obj = null;

        try {
            in = new ObjectInputStream(bais);
            obj = in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                bais.close();
            } catch (Exception e) {
            }
        }
        return obj;
    }

}
