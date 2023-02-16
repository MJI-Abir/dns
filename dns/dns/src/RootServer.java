import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class RootServer extends Thread {
    final DatagramSocket socket;
    byte[] messageByte;
    DatagramPacket packet;
    String ipAddress, portNumber;
    ArrayList<String> tldData;

    public RootServer(DatagramSocket socket, byte[] messageByte, DatagramPacket packet) {
        this.socket = socket;
        this.messageByte = messageByte;
        this.packet = packet;
    }

    public void run() {

        // ********* Iterative DNS Resolution ********* //

        // ----CLIENT MESSAGE---- //
        String message = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Client Request: " + message);

        // ----SEND AUTH SERVER IP ADDRESS AND PORT NUMBER TO CLIENT---- //
        ArrayList<String> authData = findTLDData();
        for (String data : authData) {
            System.out.println("ip Address and port number: " + data);
            messageByte = data.getBytes();
            packet = new DatagramPacket(messageByte, messageByte.length, packet.getAddress(), packet.getPort());

            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        // ********* Recursive DNS Resolution ********* //
        // ----forward query packet to TLD server---- //
//        ArrayList<String> tldData = findTLDData();
//        try {
//            InetAddress tldIpAddress = InetAddress.getByName(tldData.get(0));
//            int tldPortNumber = Integer.parseInt(tldData.get(1));
//            packet = new DatagramPacket(messageByte, messageByte.length, tldIpAddress, tldPortNumber);
//            socket.send(packet);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    private ArrayList<String> findTLDData() {
        tldData = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("dns/root server database/tldServerData.txt"));
            String line = reader.readLine();
            while (line != null) {
                ipAddress = line.split(" ", 2)[0];
                portNumber = (line.split(" ", 2)[1]);
                tldData.add(ipAddress);
                tldData.add(portNumber);
                line = reader.readLine();
            }
            tldData.remove(0);
            tldData.remove(0);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tldData;
    }
}