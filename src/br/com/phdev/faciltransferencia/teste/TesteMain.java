/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.phdev.faciltransferencia.teste;

import br.com.phdev.faciltransferencia.connetion.OnConnectedListener;
import br.com.phdev.faciltransferencia.misc.Archive;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public class TesteMain implements OnReadListener, OnConnectedListener, OnFailedConnection {

    public static final String TAG = "MyApp";

    private TCPServer server;
    private BroadcastSender broadcastSender;

    public TesteMain() {
        server = new TCPServer(this);
        server.start();
        broadcastSender = new BroadcastSender();
        broadcastSender.start();
    }       
    
    public static void main(String[] args) {
        new TesteMain();
    }

    public Object getObjectFromBytes(byte[] buffer, int bufferSize) {
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

    public byte[] getBytesFromObject(Object obj) {
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
            }
        }
        return bytes;
    }

    private void writeFile(Archive file) {
        server.write(getBytesFromObject("cango"));
    }

    @Override
    public int onRead(byte[] buffer, int bufferSize) {
        Object obj = getObjectFromBytes(buffer, bufferSize);

        if (obj instanceof String) {
            String msg = (String)obj;
            if (msg.contains("<bs>") && msg.endsWith("</bs>")) {
                String sub = msg.substring(4, msg.length() - 5);
                return Integer.parseInt(sub);
            }
        } else if (obj instanceof Archive) {
            Archive fileReceived = (Archive)obj;
            //writeFile(fileReceived);
            return 128;
        }
        return 128;
    }   

    @Override
    public void onConnected(String alias) {
        
    }
    
}
