/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.phdev.faciltransferencia.teste;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public class TesteByte {
    
    public static void main(String[] args) {
        String a = "paulo";
        String b = "\n\n\n\n";
        
        System.out.println(b.length());
        System.out.println(b.getBytes().length);
        byte[] tmp = b.getBytes();
        for (int i=0; i < tmp.length; i++) {
            System.out.println("data " + tmp[i]);
        }        
    }
    
}
