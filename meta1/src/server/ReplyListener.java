package server;

import utils.Control;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

// https://stackoverflow.com/questions/6599202/java-how-interrupt-stop-a-thread
class ReplyListener extends Thread {
    private byte[] buffer;
    private Control control;
    private int timeout;

    ReplyListener(byte[] buffer, Control control, int timeout) {
        this.buffer = buffer;
        this.control = control;
        this.timeout = timeout;
        this.start();
    }

    @Override
    public void run() {
        //TODO: compare reply hash instead of whole packet data
        MulticastSocket socket;
        try {
            socket = new MulticastSocket(MulticastServer.MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(MulticastServer.MULTICAST_ADDRESS);
            socket.joinGroup(group);
            while (!this.isInterrupted()) {
                byte[] aux;
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.setSoTimeout(timeout);
                socket.receive(packet);
                aux = new String(packet.getData(), 0, packet.getLength()).getBytes();
                if (Arrays.equals(buffer, aux)) {
                    control.flag = true;
                    this.interrupt();
                }
            }
        } catch (IOException e) {
            this.interrupt();
        }
    }
}