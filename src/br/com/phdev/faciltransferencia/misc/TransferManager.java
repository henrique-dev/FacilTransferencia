/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.phdev.faciltransferencia.misc;

import br.com.phdev.faciltransferencia.connetion.ConnectionManager;
import br.com.phdev.faciltransferencia.connetion.OnReadListener;
import br.com.phdev.faciltransferencia.connetion.WriteListener;
import br.com.phdev.faciltransferencia.gui.FTGui;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public class TransferManager extends Thread implements OnReadListener {

    private ConnectionManager connectionManager;
    private LinkedList<Archive> archives;
    private OnSendComplete app;

    private boolean cantSend = false;
    private boolean waitingClient = true;

    public TransferManager(FTGui app) {
        this.app = app;
        this.connectionManager = new ConnectionManager(app, this);
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
                //System.out.println("Tamanho do arquivo a ser enviado: " + sizeObject);
                for (WriteListener wl : clientsToWrite) {
                    //String msg = "<bs>" + bytesToSend.length + "</bs>";
                    wl.write(getBytesFromObject(new SizeInfo(bytesToSend.length)));
                    setWaitingClient(true);
                    System.out.println("Esperando confirmação do cliente para enviar!");
                    while (isWaitingClient()) {
                    }
                    sleep(500);
                    System.out.println("Tamanho do buffer a ser enviado: " + bytesToSend.length);
                    wl.write(bytesToSend);
                    setCantSend(true);
                    //break;                    
                }
                System.out.println("Esperando cliente receber e salvar o arquivo!");
                while (isCantSend()) {
                }
                archive.setStatusTranfer(2);
                app.onComplete();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRead(byte[] buffer, int bufferSize) {
        String msg = (String) getObjectFromBytes(buffer, bufferSize);
        switch (msg) {
            case "cango":
                setCantSend(false);
                break;
            case "sm":
                setWaitingClient(false);
                break;
        }
        System.out.println("Mensagem recebida: " + msg);
    }

    public static Object getObjectFromBytes(byte[] buffer, int bufferSize) {
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer, 0, bufferSize);
        ObjectInput in = null;
        Object obj = null;

        try {
            in = new ObjectInputStream(bais);
            obj = in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                bais.close();
            } catch (Exception e) {
            }
        }
        return obj;
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
