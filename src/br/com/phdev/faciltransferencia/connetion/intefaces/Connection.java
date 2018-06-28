/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.phdev.faciltransferencia.connetion.intefaces;

import br.com.phdev.faciltransferencia.transfer.FTClient;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public interface Connection {
    
    public interface OnClientConnectionTCPStatusListener {
        
        void onDisconnect(String alias);
        void onConnect(String alias);
        
    }
    
    public interface OnClientConnectionBroadcastStatusListener {
        
        void onClientFound(FTClient fTClient);
        
    }
    
}
