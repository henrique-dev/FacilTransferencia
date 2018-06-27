/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.phdev.faciltransferencia.teste;

import br.com.phdev.faciltransferencia.connetion.OnConnectedListener;
import br.com.phdev.faciltransferencia.connetion.WriteListener;
import br.com.phdev.faciltransferencia.misc.TransferManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public class TCPServer extends Thread implements WriteListener {

    public static final int TRANSFER_PORT = 10011;
    private final int BUFFER_MSG_SIZE = 50;

    private final int RECEIVING_MSG = 0;
    private final int RECEIVING_FILE = 1;

    private int receiveStatus = RECEIVING_MSG;

    private Socket socket;

    private OutputStream out;
    private InputStream in;

    byte[] buffer;
    int bufferSize = BUFFER_MSG_SIZE;

    private OnFailedConnection failedConnection;
    private OnReadListener onReadListener;
    private OnConnectedListener connectedListener;

    public TCPServer(TesteMain mainActivity) {
        this.onReadListener = mainActivity;        
        this.failedConnection = mainActivity;
    }

    @Override
    public void run () {
        try {                        
            System.out.println("Esperando conexão");
            this.socket = new ServerSocket(TRANSFER_PORT).accept();
            System.out.println("Conectado ao servidor");                        

            this.out = this.socket.getOutputStream();

            while (true) {
                InputStream in = this.socket.getInputStream();
                int totalDataReaded = 0;
                int dataReaded;
                int bufferReaded;
                boolean msg = false;
                
                System.out.println("Novo tamanho para o buffer: " + bufferSize);
                byte[] buffer = new byte[bufferSize];
                byte[] finalBuffer = new byte[bufferSize];

                while (totalDataReaded < buffer.length) {
                    dataReaded = in.read(buffer);
                    for (int i=0; i<dataReaded; i++) {
                        finalBuffer[totalDataReaded + i] = buffer[i];
                    }
                    totalDataReaded += dataReaded;
                    if (totalDataReaded <= 128) {
                        try {
                            if (TransferManager.getObjectFromBytes(finalBuffer, totalDataReaded) != null) {
                                for (int i=0; i<totalDataReaded; i++) {                                    
                                    System.out.println("msg data: " + finalBuffer[i]);
                                }                                
                                System.err.println(((String)TransferManager.getObjectFromBytes(finalBuffer, totalDataReaded)));
                                msg = true;
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }                
                System.out.println("Buffer recebido com sucesso. BufferSize final: " + totalDataReaded);
                bufferReaded = totalDataReaded;
                bufferSize = this.onReadListener.onRead(finalBuffer, bufferReaded);

                //this.onReadListener.onRead(finalBuffer, bufferReaded);
                //this.onReadListener.onRead(finalBuffer, counter);

                if (!msg) {
                    System.out.println("Enviando mensagem de arquivo recebido...");                    
                    write(TransferManager.getBytesFromObject("cango"));
                    System.out.println("Mensagem de arquivo recebido enviada!");                    
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (this.socket != null)
                    this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                this.socket = null;                                
            }
        }
    }

    @Override
    public void write(byte[] bytes) {
        try {            
            this.out.write(bytes);
            this.out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}