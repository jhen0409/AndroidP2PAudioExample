package jhen.example.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Scanner;

public class P2PRelayServer {
    private int port = 8765, index = 0;
    private boolean isReceive = false, isMainRunning = false;
    private byte[] recbuf = new byte[1024];
    private DatagramPacket rec = new DatagramPacket(recbuf, recbuf.length);
    private SocketAddress pointAddr[] = new SocketAddress[2];
    private Object[][] addrIpPort = new Object[2][2];
    private DatagramSocket ds;

    public static void main(String[] args) throws Exception {
        new P2PRelayServer();
    }

    public P2PRelayServer() throws Exception {
        println("start");
        println("Local Port: " + port);

        isMainRunning = true;
        isReceive = true;

        ds = new DatagramSocket(port);
        Thread receiver = new Thread() {

            public void run() {
                receive();
            }
        };
        receiver.setDaemon(true);
        receiver.start();

        Scanner input = new Scanner(System.in);
        while (isMainRunning) {
            String line = input.nextLine();
            if (line.equals("exit")) {
                isMainRunning = false;
                isReceive = false;
                ds.close();
            } else if (line.equals("clean")) {
                pointAddr = new SocketAddress[2];
                addrIpPort = new Object[2][2];
                index = 0;
                println("Clean success.");
            }
        }
    }

    private void receive() {
        while (isReceive) {
            try {
                ds.receive(rec);
                String msg = new String(rec.getData(), rec.getOffset(),
                        rec.getLength());
                if (msg.equals("register")) {

                    addrIpPort[index][0] = rec.getAddress().getHostAddress();
                    addrIpPort[index][1] = rec.getPort();
                    pointAddr[index] = rec.getSocketAddress();

                    String line = rec.getSocketAddress() + ":" + msg;
                    println(line + "\n" + addrIpPort[index][0] + ":"
                            + addrIpPort[index][1]);

                    index++;

                    if (index == 2) {
                        doSend(pointAddr[0],
                                ((String) addrIpPort[1][0] + ":" + (Integer) addrIpPort[1][1])
                                        .getBytes());
                        doSend(pointAddr[1],
                                ((String) addrIpPort[0][0] + ":" + (Integer) addrIpPort[0][1])
                                        .getBytes());
                    }
                    index %= 2;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doSend(SocketAddress addr, byte[] data) throws Exception {
        DatagramPacket pack = new DatagramPacket(data, data.length, addr);
        ds.send(pack);
    }

    private void println(String s) {
        System.out.println("[System, " + new Date(System.currentTimeMillis())
                + "]: " + s);
    }
}