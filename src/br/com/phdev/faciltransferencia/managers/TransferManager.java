/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.phdev.faciltransferencia.managers;

import br.com.phdev.faciltransferencia.connetion.intefaces.Connection;
import br.com.phdev.faciltransferencia.connetion.intefaces.WriteListener;
import br.com.phdev.faciltransferencia.gui.FTGui;
import br.com.phdev.faciltransferencia.transfer.Archive;
import br.com.phdev.faciltransferencia.transfer.FTClient;
import br.com.phdev.faciltransferencia.transfer.SizeInfo;
import br.com.phdev.faciltransferencia.trasnfer.interfaces.TransferStatusListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import br.com.phdev.faciltransferencia.trasnfer.interfaces.OnObjectReceivedListener;
import javax.swing.JOptionPane;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public class TransferManager extends Thread implements OnObjectReceivedListener, Connection.OnClientConnectionTCPStatusListener {

    private final ConnectionManager connectionManager;
    private final LinkedList<Archive> archives;
    private final TransferStatusListener transferStatusListener;

    private final Connection.OnClientConnectionTCPStatusListener onClientConnectionTCPStatusListener;

    private boolean cantSend = false;
    private boolean waitingClient = true;

    private FTClient currentClient;

    public TransferManager(FTGui context) {
        this.transferStatusListener = (TransferStatusListener) context;
        this.connectionManager = new ConnectionManager(this);
        this.connectionManager.startBroadcastServer();
        this.archives = new LinkedList<>();
        this.onClientConnectionTCPStatusListener = (Connection.OnClientConnectionTCPStatusListener) context;
    }

    public List<FTClient> getClientsList() {
        return this.connectionManager.getClientsList();
    }

    synchronized public LinkedList<Archive> getArchivesList() {
        return this.archives;
    }

    synchronized public void addArchiveForTransfer(Archive archive) {
        notify();
        getArchivesList().add(archive);
    }

    synchronized public Archive getArchiveToTransfer() {
        if (getArchivesList().isEmpty()) {
            try {
                wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return getArchivesList().pop();
    }

    synchronized void clientReady() {
        notify();
    }

    synchronized void setWaitingClient(boolean waitingClient) {
        this.waitingClient = waitingClient;
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    synchronized void canSend() {
        notify();
    }

    synchronized private void setCantSend(boolean cantSend) {
        this.cantSend = cantSend;
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisconnect(String alias) {
        System.out.println("Desconectou");
        System.out.println(connectionManager.getClientsList().size());
        int indexToRemove = -1;
        for (int i = 0; i < this.connectionManager.getClientsList().size(); i++) {
            if (this.connectionManager.getClientsList().get(i).getAlias().equals(alias)) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove > -1) {
            this.connectionManager.getClientsList().remove(indexToRemove);
        }        
        System.out.println(connectionManager.getClientsList().size());
        this.onClientConnectionTCPStatusListener.onDisconnect(alias);
    }

    @Override
    public void onConnect(String alias
    ) {
        System.out.println("Conectou");
        this.onClientConnectionTCPStatusListener.onConnect(alias);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Archive archive = this.getArchiveToTransfer();
                File file = new File(archive.getPath());
                archive.setBytes(getBytesFromFile(file));
                List<FTClient> clientsToWrite = this.connectionManager.getClientsList();
                byte[] bytesToSend = getBytesFromObject(archive);
                for (FTClient ftc : clientsToWrite) {
                    this.currentClient = ftc;
                    WriteListener wl = (WriteListener) ftc.getWriteListener();
                    wl.write(getBytesFromObject(new SizeInfo(bytesToSend.length)));
                    System.out.println("Esperando confirmação do cliente para enviar!");
                    this.setWaitingClient(true);
                    sleep(500);
                    System.out.println("Tamanho do buffer/arquivo a ser enviado: " + bytesToSend.length);
                    wl.write(bytesToSend);
                    System.out.println("Enviando o arquivo");
                    archive.setStatusTranfer(1);
                    transferStatusListener.onSending();
                    System.out.println("Esperando cliente receber e salvar o arquivo!");
                    this.setCantSend(true);
                }
                archive.setStatusTranfer(2);
                transferStatusListener.onSendComplete();
                archive.setBytes(null);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onObjectReceived(Object msg
    ) {
        if (msg instanceof String) {
            switch ((String) msg) {
                case "cango":
                    this.canSend();
                    break;
                case "sm":
                    clientReady();
                    break;
                default:
                    System.out.println("Mensagem desconhecida");
                    break;
            }
        }
    }

    public static byte[] getBytesFromObject(Object obj) {
        if (obj == null) {
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] bytes = null;

        try {
            out = new ObjectOutputStream(baos);
            out.writeObject(obj);
            out.flush();
            baos.flush();
            bytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                baos.close();
            } catch (Exception e) {
            } finally {
                return bytes;
            }
        }
    }

    public byte[] getBytesFromFile(File file) {
        byte[] bytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

}
