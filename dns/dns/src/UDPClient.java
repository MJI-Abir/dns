import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

class DNSMessage {
    // --------------header fields--------------- //
    String id;
    String flags; /* Bit-mask to indicate request/response */
    String qdcount; /*Stringer of questions */
    String ancount; /* Number of answers */
    String nscount; /* Number of authority records */
    String arcount; /* Number of additional records */

    // question section
    String qname;
    String qtype;
    String qclass;

    public byte[] writeTo() {
        // calculate the length of the message
        int length = 12 + qname.length() + 4;

        // create a byte buffer to store the message
        ByteBuffer buffer = ByteBuffer.allocate(length);

        // write the header fields to the buffer
        buffer.put(id.getBytes());
        buffer.put(flags.getBytes());
        buffer.put(qdcount.getBytes());
        buffer.put(ancount.getBytes());
        buffer.put(nscount.getBytes());
        buffer.put(arcount.getBytes());

        // write the question section to the buffer
        buffer.put(qname.getBytes());
        buffer.put(qtype.getBytes());
        buffer.put(qclass.getBytes());

        // return the buffer as a byte array
        return buffer.array();
    }
}

public class UDPClient {

    public static void main(String[] args) throws Exception {

        InetAddress rootIpAddress = InetAddress.getByName("localhost");
        int rootPort = 1234;
        Scanner scanner = new Scanner(System.in);

        // CREATE A DATAGRAM SOCKET
        DatagramSocket socket = new DatagramSocket();

        // ----DOMAIN NAME AS INPUT---- //
        System.out.println("Type exit to disconnect/ Type domain name: ");
        String query;
        query = scanner.nextLine();
        System.out.println("Requested domain: " + query);
        System.out.println("-------------*********-------------");

        // ----CLOSE CONNECTION IF CLIENT TYPES "exit"---- //

        // Create an instance of the DNS message
        DNSMessage message = new DNSMessage();
        message.id = "01";
        message.flags = "00";
        message.qdcount = "01";
        message.ancount = "00";
        message.nscount = "00";
        message.arcount = "00";
        message.qname = query;
        message.qtype = "01";
        message.qclass = "01";

        // Serialize the DNS message
        byte[] dnsMessageBytes = message.writeTo();

        // ****************************** Iterative DNS Resolution ****************************** //

//
//        // ----SEND QUERY TO ROOT SERVER---- //
        DatagramPacket dnsPacket = new DatagramPacket(dnsMessageBytes, dnsMessageBytes.length, rootIpAddress, rootPort);
        socket.send(dnsPacket);

        // ----RECEIVE IP ADDRESS OF TLD SERVER FROM ROOT SERVER---- //
        byte[] tldmessageBytes = new byte[1024];
        DatagramPacket tldIpPacket = new DatagramPacket(tldmessageBytes, tldmessageBytes.length);
        socket.receive(tldIpPacket);
        InetAddress tldIpAddress = InetAddress.getByName(new String(tldIpPacket.getData(), 0, tldIpPacket.getLength()));
        System.out.println("TLD SERVER IP ADDRESS: " + tldIpAddress);

        // ----RECEIVE PORT NUMBER OF TLD SERVER FROM ROOT SERVER---- //
        tldmessageBytes = new byte[1024];
        DatagramPacket tldPortPacket = new DatagramPacket(tldmessageBytes, tldmessageBytes.length);
        socket.receive(tldPortPacket);
        int tldPortNumber = Integer.parseInt(new String(tldPortPacket.getData(), 0, tldPortPacket.getLength()));
        System.out.println("TLD SERVER PORT NUMBER: " + tldPortNumber);
        System.out.println("-------------*********-------------");

        // ----SEND DOMAIN NAME TO TLD SERVER---- //
        sendQuery(socket, dnsPacket, tldIpAddress, tldPortNumber, dnsMessageBytes);

        // ----RECEIVE IP ADDRESS OF AUTH SERVER FROM TLD SERVER---- //
        byte[] messageBytes = new byte[1024];
        DatagramPacket authIpPacket = new DatagramPacket(messageBytes, messageBytes.length);
        socket.receive(authIpPacket);
        InetAddress authIpAddress = InetAddress.getByName(new String(authIpPacket.getData(), 0, authIpPacket.getLength()));
        System.out.println("AUTH SERVER IP ADDRESS: " + authIpAddress);

        // ----RECEIVE PORT NUMBER OF AUTH SERVER FROM ROOT SERVER---- //
        messageBytes = new byte[1024];
        DatagramPacket authPortPacket = new DatagramPacket(messageBytes, messageBytes.length);
        socket.receive(authPortPacket);
        int authPortNumber = Integer.parseInt(new String(authPortPacket.getData(), 0, authPortPacket.getLength()));
        System.out.println("AUTH SERVER PORT NUMBER: " + authPortNumber);
        System.out.println("-------------*********-------------");

        // ----SEND DOMAIN NAME TO AUTH SERVER---- //
        sendQuery(socket, dnsPacket, authIpAddress, authPortNumber, dnsMessageBytes);

        // ----RECEIVE RESPONSE RECORDS FROM AUTH SERVER---- //
        ArrayList<String> response = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            messageBytes = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(messageBytes, messageBytes.length);
            socket.receive(responsePacket);
            response.add(new String(responsePacket.getData(), 0, responsePacket.getLength()));
            System.out.println("Server response: " + response.get(i));
        }

        socket.close();
    }

    private static void sendQuery(DatagramSocket socket, DatagramPacket packet, InetAddress ipAddress, int portNumber, byte[] messageBytes) throws IOException {

        packet = new DatagramPacket(messageBytes, messageBytes.length, ipAddress, portNumber);
        socket.send(packet);


        // ************ Recursive DNS Resolution ************ //
        // ----SEND QUERY TO ROOT SERVER---- //
//        DatagramPacket dnsPacket = new DatagramPacket(dnsMessageBytes, dnsMessageBytes.length, rootIpAddress, rootPort);
//        socket.send(dnsPacket);
//
//        // ----RECEIVE A and AAAA resource FROM ROOT SERVER---- //
//
    }
}