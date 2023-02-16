import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TLDServer extends Thread{
    final DatagramSocket socket;
    byte[] messageByte;
    DatagramPacket packet;
    ArrayList<String> authData;
    String authIpAddress,authPortNumber;
    public TLDServer(DatagramSocket socket, byte[] messageByte, DatagramPacket packet) {
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
        ArrayList<String> authData = findAuthData();
        for (String data : authData) {
            System.out.println("ip Address and port number of auth server: " + data);
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
        // ----CLIENT MESSAGE---- //
        String message = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Client Request: " + message);

        // ----FORWARD QUERY TO AUTH SERVER---- //
        ArrayList<String> authData = findAuthData();
        try {
            InetAddress authIpAddress = InetAddress.getByName(authData.get(0));
            int authPortNumber = Integer.parseInt(authData.get(1));
            packet = new DatagramPacket(messageByte, messageByte.length, authIpAddress, authPortNumber);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // ----RECEIVE RECORDS FROM AUTH SERVER---- //
        ArrayList<String> response = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            messageByte = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(messageByte, messageByte.length);
            try {
                socket.receive(responsePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            response.add(new String(responsePacket.getData(), 0, responsePacket.getLength()));
            System.out.println("Server response: " + response.get(i));
        }

        // ----FORWARD RECORDS TO ROOT SERVER---- //
        for(String s : response){
            messageByte = s.getBytes();
            packet = new DatagramPacket(messageByte,messageByte.length, packet.getAddress(), packet.getPort());
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private ArrayList<String> findAuthData() {
        authData = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("tld server database/authServerData.txt"));
            String line = reader.readLine();
            while (line != null) {
                authIpAddress = line.split(" ", 2)[0];
                authPortNumber = (line.split(" ", 2)[1]);
                authData.add(authIpAddress);
                authData.add(authPortNumber);
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

}
