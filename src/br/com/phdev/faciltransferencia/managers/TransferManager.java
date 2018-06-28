/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.phdev.faciltransferencia.managers;

import br.com.phdev.faciltransferencia.connetion.intefaces.WriteListener;
import br.com.phdev.faciltransferencia.gui.FTGui;
import br.com.phdev.faciltransferencia.transfer.Archive;
import br.com.phdev.faciltransferencia.transfer.SizeInfo;
import br.com.phdev.faciltransferencia.trasnfer.interfaces.OnMessageReceivedListener;
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

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public class TransferManager extends Thread implements OnMessageReceivedListener {

    private final ConnectionManager connectionManager;
    private final LinkedList<Archive> archives;
    private final TransferStatusListener transferStatusListener;

    private boolean cantSend = false;
    private boolean waitingClient = true;

    public TransferManager(FTGui context) {
        this.transferStatusListener = (TransferStatusListener) context;
        this.connectionManager = new ConnectionManager(context, this);
        this.connectionManager.startBroadcastServer();
        this.archives = new LinkedList<>();
    }

    synchronized public LinkedList<Archive> getList() {
        return this.archives;
    }

    public void addArchiveForTransfer(Archive archive) {
        getList().add(archive);
    }

    public Archive getArchiveToTransfer() {
        while (getList().isEmpty()) {
        }
        return getList().pop();
    }

    synchronized boolean isWaitingClient() {
        return this.waitingClient;
    }

    synchronized void setWaitingClient(boolean waitingClient) {
        this.waitingClient = waitingClient;
    }

    synchronized private void setCantSend(boolean cantSend) {
        this.cantSend = cantSend;
    }

    synchronized boolean isCantSend() {
        return this.cantSend;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Archive archive = getArchiveToTransfer();
                File file = new File(archive.getPath());
                archive.setBytes(getBytesFromFile(file));
                List<WriteListener> clientsToWrite = this.connectionManager.getWriteListeners();
                byte[] bytesToSend = getBytesFromObject(archive);
                for (WriteListener wl : clientsToWrite) {
                    wl.write(getBytesFromObject(new SizeInfo(bytesToSend.length)));
                    setWaitingClient(true);
                    System.out.println("Esperando confirmação do cliente para enviar!");
                    while (isWaitingClient()) {
                    }
                    sleep(500);
                    System.out.println("Tamanho do buffer a ser enviado: " + bytesToSend.length);
                    wl.write(bytesToSend);
                    setCantSend(true);
                }
                System.out.println("Esperando cliente receber e salvar o arquivo!");
                while (isCantSend()) {
                }
                archive.setStatusTranfer(2);
                transferStatusListener.onSendComplete();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }    

    @Override
    public void onMessageReceived(Object msg) {
        if (msg instanceof String) {
            switch ((String)msg) {
                case "cango":
                    setCantSend(false);
                    break;
                case "sm":
                    setWaitingClient(false);
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
