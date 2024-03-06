import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class VoiceDuplex {


    static InetAddress clientIP = null;
    static int port = 55555;
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        // Check for IP
        clientIP = getValidIPAddress();
        port = getValidPORT();

        VoiceSender sender = new VoiceSender(clientIP, port);
        VoiceReceiver receiver = new VoiceReceiver(port);
    }

    public static InetAddress getValidIPAddress() {

        InetAddress tempIP = null;

        while (tempIP == null) {
            System.out.print("Enter IP: ");
            String ipString = scanner.next();

            if (ipString.equalsIgnoreCase("E")) {
                System.exit(0);
            }

            try {
                tempIP = InetAddress.getByName(ipString);

            } catch (UnknownHostException e) {
                System.out.println("ERROR: Invalid IP Address");
            }
        }
        return tempIP;
    }

    public static int getValidPORT(){

        int tempPort = -1;

        while (tempPort == -1){
            System.out.print("Enter PORT: ");
            String PORTString = scanner.next();

            if (PORTString.equalsIgnoreCase("E")){
                System.exit(0);
            }

            try {
                tempPort = Integer.parseInt(PORTString);

            } catch (NumberFormatException e) {
                System.out.println("ERROR: Invalid port Entered.");
            }
        }
        return tempPort;
    }
}
