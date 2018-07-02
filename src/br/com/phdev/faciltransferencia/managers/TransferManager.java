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
import br.com.phdev.faciltransferencia.transfer.ArchiveInfo;
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
import java.nio.channels.FileChannel;
import javax.swing.JOptionPane;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public class TransferManager extends Thread implements OnObjectReceivedListener, Connection.OnClientConnectionTCPStatusListener {

    private final int MAX_FILE_LENGTH_TO_SEND_WITHOUT_FRAGMENT = 10485760;

    private final ConnectionManager connectionManager;
    private final LinkedList<Archive> archives;
    private final TransferStatusListener transferStatusListener;

    private final Connection.OnClientConnectionTCPStatusListener onClientConnectionTCPStatusListener;

    private boolean waitingClientConfirmToSend = false;
    private boolean waitingClientReceiveAll = false;
    private boolean waitingClientReceiveFragment = false;
    private boolean noFilesToSend = false;
    private boolean errorOnTransfer = false;

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

    public synchronized LinkedList<Archive> getArchivesList() {
        return this.archives;
    }

    public synchronized void addArchiveForTransfer(Archive archive) {
        getArchivesList().add(archive);
        notify();
    }

    public synchronized boolean isWaitingClientConfirmToSend() {
        return waitingClientConfirmToSend;
    }

    public synchronized void setWaitingClientConfirmToSend(boolean waitingClientConfirmToSend) {
        this.waitingClientConfirmToSend = waitingClientConfirmToSend;
    }

    public synchronized boolean isWaitingClientReceiveAll() {
        return waitingClientReceiveAll;
    }

    public synchronized void setWaitingClientReceiveAll(boolean waitingClientReceiveAll) {
        this.waitingClientReceiveAll = waitingClientReceiveAll;
    }

    public synchronized boolean isNoFilesToSend() {
        return noFilesToSend;
    }

    public synchronized void setNoFilesToSend(boolean noFilesToSend) {
        this.noFilesToSend = noFilesToSend;
    }

    public synchronized boolean isWaitingClientReceiveFragment() {
        return waitingClientReceiveFragment;
    }

    public synchronized void setWaitingClientReceiveFragment(boolean waitingClientReceiveFragment) {
        this.waitingClientReceiveFragment = waitingClientReceiveFragment;
    }

    public synchronized boolean isErrorOnTransfer() {
        return errorOnTransfer;
    }

    public synchronized void setErrorOnTransfer(boolean errorOnTransfer) {
        this.errorOnTransfer = errorOnTransfer;
    }

    private synchronized Archive getArchiveToTransfer() {
        if (getArchivesList().isEmpty()) {
            try {
                wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return getArchivesList().pop();
    }

    @Override
    public void onDisconnect(String alias) {
        System.out.println("Desconectou");
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
        this.onClientConnectionTCPStatusListener.onDisconnect(alias);
    }

    @Override
    public void onConnect(String alias) {
        System.out.println("Conectou");
        this.onClientConnectionTCPStatusListener.onConnect(alias);
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("Esperando arquivos serem inseridos para serem enviados");
                Archive archive = this.getArchiveToTransfer();
                System.out.println("Arquivo inserido");
                File file = new File(archive.getPath());                
                List<FTClient> clientsToWrite = this.connectionManager.getClientsList();
                byte[] bytesToSend = getBytesFromFile(file);
                System.out.println("Tamanho do arquivo a ser enviado: " + bytesToSend.length + " bytes");
                ArchiveInfo archiveInfo = new ArchiveInfo();
                archiveInfo.setArchiveName(archive.getName());
                archiveInfo.setArchiveLength(bytesToSend.length);
                archiveInfo.setFragmentsAmount(1);

                if (bytesToSend.length > MAX_FILE_LENGTH_TO_SEND_WITHOUT_FRAGMENT) {
                    System.out.println("Arquivo superior a " + MAX_FILE_LENGTH_TO_SEND_WITHOUT_FRAGMENT + " bytes");
                    System.out.println("O envio do arquivo será fragmentado");
                    int fragmentsSize;
                    int fragmentLength = bytesToSend.length;

                    for (fragmentsSize = 1; fragmentLength > MAX_FILE_LENGTH_TO_SEND_WITHOUT_FRAGMENT; fragmentsSize++) {
                        fragmentLength = bytesToSend.length / fragmentsSize;
                    }

                    int lastFragmentLength = 0;
                    if (fragmentsSize * fragmentLength > bytesToSend.length) {
                        lastFragmentLength = bytesToSend.length % (fragmentsSize - 1);
                    }
                    System.out.println("Quantidade de fragmentos: " + fragmentsSize);
                    System.out.println("Tamanho de cada fragmento: " + fragmentLength);
                    System.out.println("Tamanho do ultimo fragmento: " + lastFragmentLength);
                    archiveInfo.setFragmentsAmount(fragmentsSize);
                    archiveInfo.setFragmentLength(fragmentLength);
                    archiveInfo.setLastFragmentLength(lastFragmentLength);
                }
                for (FTClient ftc : clientsToWrite) {
                    WriteListener wl = (WriteListener) ftc.getWriteListener();
                    wl.write(getBytesFromObject(archiveInfo));
                    System.out.println("Esperando confirmação do cliente para enviar!");
                    this.setWaitingClientConfirmToSend(true);
                    while (isWaitingClientConfirmToSend()) {
                        if (errorOnTransfer) {
                            break;
                        }
                    }
                    if (errorOnTransfer) {
                        archive.setStatusTranfer(3);
                        setWaitingClientConfirmToSend(false);
                        continue;
                    }
                    System.out.println("Enviando o arquivo");
                    archive.setStatusTranfer(1);
                    transferStatusListener.onSending();
                    if (archiveInfo.getFragmentsAmount() > 1) {
                        for (int i = 0; i < archiveInfo.getFragmentsAmount(); i++) {
                            System.out.println("Enviando " + (i + 1) + "º fragmento");
                            if (i == archiveInfo.getFragmentsAmount() - 1 && archiveInfo.getLastFragmentLength() != 0) {
                                wl.write(bytesToSend, i * archiveInfo.getFragmentLength(), archiveInfo.getLastFragmentLength());
                            } else {
                                wl.write(bytesToSend, i * archiveInfo.getFragmentLength(), archiveInfo.getFragmentLength());
                            }
                            System.out.println("Esperando cliente receber o fragmento");
                            setWaitingClientReceiveFragment(true);
                            while (isWaitingClientReceiveFragment()) {
                            }
                            System.out.println("Fragmento recebido");
                        }
                    } else {
                        wl.write(bytesToSend);
                    }
                    System.out.println("Esperando cliente receber e salvar o arquivo!");
                    this.setWaitingClientReceiveAll(true);
                    while (isWaitingClientReceiveAll()) {
                    }
                    System.out.println("Cliente recebeu o arquivo. Pronto para enviar outro");
                    break;
                }
                if (!errorOnTransfer) {
                    archive.setStatusTranfer(2);
                }
                transferStatusListener.onSendComplete();
                errorOnTransfer = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onObjectReceived(Object msg) {
        System.err.println(msg);
        if (msg instanceof String) {
            switch ((String) msg) {
                case "cango":
                    this.setWaitingClientReceiveAll(false);
                    break;
                case "sm":
                    this.setWaitingClientConfirmToSend(false);
                    break;
                case "smf":
                    this.setWaitingClientReceiveFragment(false);
                    break;
                case "nospace":
                    errorOnTransfer = true;
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
