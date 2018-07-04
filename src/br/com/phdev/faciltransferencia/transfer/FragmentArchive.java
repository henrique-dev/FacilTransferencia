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
public class FragmentArchive implements Serializable{
    
    private int fragmentId;
    private String archiveName;
    private int fragmentLength;
    private byte[] fragmentData;

    public int getFragmentId() {
        return fragmentId;
    }

    public void setFragmentId(int fragmentId) {
        this.fragmentId = fragmentId;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    public int getFragmentLength() {
        return fragmentLength;
    }

    public void setFragmentLength(int fragmentLength) {
        this.fragmentLength = fragmentLength;
    }

    public byte[] getFragmentData() {
        return fragmentData;
    }

    public void setFragmentData(byte[] fragmentData) {
        this.fragmentData = fragmentData;
    }            
    
}
