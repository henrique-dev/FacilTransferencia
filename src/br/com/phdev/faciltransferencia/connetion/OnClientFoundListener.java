/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.phdev.faciltransferencia.connetion;

import br.com.phdev.faciltransferencia.misc.FTClient;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public interface OnClientFoundListener {
    
    void onClientFound(FTClient client);
    
}
