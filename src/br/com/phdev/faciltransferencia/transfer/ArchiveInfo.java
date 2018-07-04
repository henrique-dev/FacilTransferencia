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
    private String masterPath;
    private String localPath;
    private long archiveLength;
    private int fragmentsAmount;
    private int fragmentLength;
    private int lastFragmentLength;

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

    public int getLastFragmentLength() {
        return lastFragmentLength;
    }

    public void setLastFragmentLength(int lastFragmentLength) {
        this.lastFragmentLength = lastFragmentLength;
    }     

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }        

    public String getMasterPath() {
        return masterPath;
    }

    public void setMasterPath(String masterPath) {
        this.masterPath = masterPath;
    }       
            
}
