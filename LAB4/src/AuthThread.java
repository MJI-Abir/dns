import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class AuthThread extends Thread {
    public static void main(String[] args) throws Exception {
        // Create a DatagramSocket
        int port = 1240;
        DatagramSocket socket = new DatagramSocket(port);

        // RECEIVE A PACKET FROM THE CLIENT
        byte[] messageByte = new byte[1024];
        DatagramPacket dnsPacket = new DatagramPacket(messageByte, messageByte.length);

        while (true) {
            socket.receive(dnsPacket);
            Thread t = new AuthServer(socket, messageByte, dnsPacket);
            t.start();
        }

    }
}
