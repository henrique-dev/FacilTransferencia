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
public class ArchiveInfo implements Serializable{        
    
    private String archiveName;
    private long archiveLength;
    private int fragmentsAmount;
    private int fragmentLength;

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    public long getArchiveLength() {
        return archiveLength;
    }

    public void setArchiveLength(long archiveLength) {
        this.archiveLength = archiveLength;
    }

    public int getFragmentsAmount() {
        return fragmentsAmount;
    }

    public void setFragmentsAmount(int fragmentsAmount) {
        this.fragmentsAmount = fragmentsAmount;
    }

    public int getFragmentLength() {
        return fragmentLength;
    }

    public void setFragmentLength(int fragmentLength) {
        this.fragmentLength = fragmentLength;
    }
            
}
