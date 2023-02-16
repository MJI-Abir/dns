import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
        System.out.println("Type the domain name: ");
        String domain;
        domain = scanner.nextLine();
        System.out.println("Requested domain: " + domain);
        System.out.println("-------------*********-------------");

        // Create an instance of the DNS message
        DNSMessage message = new DNSMessage();
        message.id = "01";
        message.flags = "00";
        message.qdcount = "01";
        message.ancount = "00";
        message.nscount = "00";
        message.arcount = "00";
        message.qname = domain;
        message.qtype = "01";
        message.qclass = "01";

        // Serialize the DNS message
        byte[] dnsMessageBytes = message.writeTo();

        // ----SEND QUERY TO ROOT SERVER---- //
        DatagramPacket dnsPacket = new DatagramPacket(dnsMessageBytes, dnsMessageBytes.length, rootIpAddress, rootPort);
        socket.send(dnsPacket);

        // ----RECEIVE IP ADDRESS OF AUTH SERVER FROM ROOT SERVER---- //
        byte[] messageBytes = new byte[1024];
        DatagramPacket authIpPacket = new DatagramPacket(messageBytes, messageBytes.length);
        socket.receive(authIpPacket);
        String authIpAddress = new String(authIpPacket.getData(), 0, authIpPacket.getLength());
        System.out.println("AUTH SERVER IP ADDRESS: " + authIpAddress);

        // ----RECEIVE PORT NUMBER OF AUTH SERVER FROM ROOT SERVER---- //
        messageBytes = new byte[1024];
        DatagramPacket authPortPacket = new DatagramPacket(messageBytes, messageBytes.length);
        socket.receive(authPortPacket);
        String authPortNumber = new String(authPortPacket.getData(), 0, authPortPacket.getLength());
        System.out.println("AUTH SERVER PORT NUMBER: " + authPortNumber);

        // ----SEND DOMAIN NAME TO AUTH SERVER---- //
        dnsPacket = new DatagramPacket(dnsMessageBytes, dnsMessageBytes.length, InetAddress.getByName(authIpAddress), Integer.parseInt(authPortNumber));
        socket.send(dnsPacket);

        // ----RECEIVE RESPONSE RECORDS FROM AUTH SERVER---- //
        ArrayList<String> response = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            messageBytes = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(messageBytes, messageBytes.length);
            socket.receive(responsePacket);
            response.add(new String(responsePacket.getData(), 0, responsePacket.getLength()));
            System.out.println("Server response: " + response.get(i));
        }

        // ----Close the socket---- //
        socket.close();
    }


}