import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

public class AuthServer extends Thread {
    final DatagramSocket socket;
    byte[] messageByte;
    DatagramPacket packet;
    String name, value, type, ttl;
    ArrayList<String> response = new ArrayList<>();

    // ----constructor---- //
    public AuthServer(DatagramSocket socket, byte[] messageByte, DatagramPacket packet) {
        this.socket = socket;
        this.messageByte = messageByte;
        this.packet = packet;
    }

    public ArrayList<String> lookup(String request) {
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader("src/dnsRecords/dns_records.txt"));
            String line = reader.readLine();

            while (line != null) {
                name = line.split(" ", 2)[0];
                line = line.split(" ", 2)[1].trim();

                value = line.split(" ", 2)[0];
                line = line.split(" ", 2)[1].trim();

                type = line.split(" ", 2)[0];
                line = line.split(" ", 2)[1].trim();

                ttl = line.split(" ", 2)[0];

                String domainName = request.substring(12, request.length() - 4);

                // ----update the response if found---- //
                if (name.substring(0, name.length() - 1).equals(domainName)) {
                    if (type.equals("A") || type.equals("AAAA")) {
                        response.add(request + "    " + value + "    " + type + "    " + ttl);
                    }
                }

                line = reader.readLine();

                // ----send NOT FOUND message---- //
                if(line == null) {
                    response.add("SORRY");
                    response.add("DOMAIN NOT FOUND!");
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    @Override
    public void run() {
        // ********* Iterative DNS Resolution ********* //
        // ----Client Message---- //
        String message = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Client Request: " + message);

        // ----Send Records to Client---- //
        ArrayList<String> response = lookup(message);
        for (String s : response) {
            messageByte = s.getBytes();
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
//        String message = new String(packet.getData(), 0, packet.getLength());
//        System.out.println("Client Request: " + message);
//
//        // ----SEND RECORDS TO TLD SERVER---- //
//        ArrayList<String> response = lookup(message);
//        for (String s : response) {
//            messageByte = s.getBytes();
//            packet = new DatagramPacket(messageByte, messageByte.length, packet.getAddress(), packet.getPort());
//            try {
//                socket.send(packet);
//            } catch (IOException e) {
//                e.printStackTrace();
//                throw new RuntimeException(e);
//            }
//        }

    }
}
