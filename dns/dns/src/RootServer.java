import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

public class RootServer extends Thread {
    final DatagramSocket socket;
    byte[] messageByte;
    DatagramPacket packet;
    String ipAddress, portNumber;
    ArrayList<String> authData;

    public RootServer(DatagramSocket socket, byte[] messageByte, DatagramPacket packet) {
        this.socket = socket;
        this.messageByte = messageByte;
        this.packet = packet;
    }

    public ArrayList<String> findAuthData() {
        authData = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("dns/root server database/authServerIPAddress.txt"));
            String line = reader.readLine();
            while (line != null) {
                ipAddress = line.split(" ", 2)[0];
                portNumber = (line.split(" ", 2)[1]);
                authData.add(ipAddress);
                authData.add(portNumber);
                line = reader.readLine();
            }
            authData.remove(0);
            authData.remove(0);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return authData;
    }

    public void run() {
        // ----CLIENT MESSAGE---- //
        String message = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Client Request: " + message);

        // ----SEND AUTH SERVER IP ADDRESS AND PORT NUMBER TO CLIENT---- //
        ArrayList<String> authData = findAuthData();
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
    }
}