/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.phdev.faciltransferencia.transfer;

import java.io.Serializable;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public class SystemInfo implements Serializable{

    private long spaceAvailable;

    public long getSpaceAvailable() {
        return spaceAvailable;
    }

    public void setSpaceAvailable(long spaceAvailable) {
        this.spaceAvailable = spaceAvailable;
    }
    
}