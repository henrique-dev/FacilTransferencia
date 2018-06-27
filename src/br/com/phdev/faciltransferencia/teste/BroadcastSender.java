/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.phdev.faciltransferencia.teste;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public class BroadcastSender extends Thread {

    public static final int SERVER_BROADCAST_PORT = 6012;

    public BroadcastSender() {

    }

    @Override
    public void run() {
        DatagramSocket socket = null;
        try {

            List<InetAddress> addresses = new ArrayList<>();

            socket = new DatagramSocket();
            socket.setBroadcast(true);

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = networkInterfaces.nextElement();
                if (netInterface.isVirtual())
                    continue;
                //System.out.println(netInterface.getName());
                List<InterfaceAddress> interfaces = netInterface.getInterfaceAddresses();

                for (InterfaceAddress address : interfaces) {
                    //System.out.println("        Endereço de host: " + address.getAddress().getHostAddress());
                    InetAddress broadcastAddress = address.getBroadcast();
                    if (broadcastAddress != null) {
                      //  System.out.println("        Endereço de broadcast: " + broadcastAddress.getHostAddress());
                        addresses.add(broadcastAddress);
                    } else {
                        //System.out.println("        Endereço de broadcast: Sem endereço de broadcast");
                    }
                }
            }

            String msg = "PauloHenrique\n";

            for (InetAddress address : addresses) {
                socket.send(new DatagramPacket(msg.getBytes(), msg.getBytes().length, address, SERVER_BROADCAST_PORT));
            }

            //NetworkInterface networkInterface = NetworkInterface.getByName("eth0");
            //InetAddress broadcastAddress = networkInterface.getInterfaceAddresses().get(1).getBroadcast();
            //System.out.println("Endereço de broadcast: " + broadcastAddress.getHostAddress());
            socket.close();

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
        }
    }

}
