/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.phdev.faciltransferencia.transfer;

import br.com.phdev.faciltransferencia.connetion.TCPClient;
import br.com.phdev.faciltransferencia.connetion.intefaces.WriteListener;
import java.net.InetAddress;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public class FTClient {
    
    private final String alias;
    private final InetAddress address;
    private final TCPClient tcpConnection;   
    private WriteListener writeListener;
    
    public FTClient(String name, InetAddress address) {
        this.alias = name;
        this.address = address;
        this.tcpConnection = new TCPClient(address, name);
    }    

    public String getAlias() {
        return alias;
    }

    public InetAddress getAddress() {
        return address;
    }        

    public TCPClient getTcpConnection() {
        return tcpConnection;
    }   

    public WriteListener getWriteListener() {
        return tcpConnection.getWriteListener();
    }         
    
    @Override
    public String toString() {
        return this.alias;
    }
    
}
