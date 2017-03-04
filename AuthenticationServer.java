
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
public class AuthenticationServer {

        static String line, gotLine, remoteIP, remotePort;
        static BufferedReader inFromClient;
        static DataOutputStream outToClient;
        static DatagramSocket peerSendSocket = null; 
        static DatagramPacket receivePacket;
        static String authFile = "authList.txt";
        static Hashtable<String, String> authList = new Hashtable<String, String>();
        static Hashtable<String, String> peerList = new Hashtable<String, String>();
        static StringTokenizer strTok;
        
        
    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception{
        // TODO code application logic here
        
        createAuthList();
        peerSendSocket = new DatagramSocket(9966);

        while(true){
            do{
                byte[] reciveData = new byte[1024]; 
                receivePacket = new DatagramPacket(reciveData, reciveData.length);
                peerSendSocket.receive(receivePacket);
                gotLine = new String(receivePacket.getData());
            }while(gotLine == null);
            
            String replySocket = receivePacket.getSocketAddress().toString().substring(1);
            System.out.println("Socket: " + replySocket + " wants to connect");
            remoteIP = replySocket.split(":")[0];
            remotePort = replySocket.split(":")[1];
            strTok = new StringTokenizer(gotLine);
            String type = strTok.nextToken();
            
            if(type.equals("AUTH")){
                String uname = strTok.nextToken();
                String passwd = strTok.nextToken();
                byte[] sendData = new byte[4096];
                DatagramPacket sendPacket;
                if(authList.containsKey(uname) && authList.get(uname).equals(passwd)){
                    
                    
                    String sendText = "YIP " + remoteIP + "\n"; 
                    sendData = sendText.getBytes();
                    sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(remoteIP), Integer.parseInt(remotePort));
                    peerSendSocket.send(sendPacket);        
                    System.out.println(uname + " authenticated");
                    sendText = "PLIST ";
                    for(Map.Entry entry: peerList.entrySet()){
                        sendText += entry.getKey() + " " + entry.getValue() + " ";
                    }
                    sendText += "\n";
                    sendData = sendText.getBytes();
                    sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(remoteIP), Integer.parseInt(remotePort));
                    peerSendSocket.send(sendPacket);
                    System.out.println("friend list sent ...");
                    sendText = "ADD " + uname + " " + replySocket + "\n";
                    sendData = sendText.getBytes();
                    for(Map.Entry entry: peerList.entrySet()){
                        System.out.println("sending add to peers ...");
                        String sock = entry.getValue().toString();
                        sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(sock.split(":")[0]), Integer.parseInt(sock.split(":")[1]));
                        peerSendSocket.send(sendPacket);
                    }
                    peerList.put(uname, replySocket);
                }
                else{
                    sendData = "BAD\n".getBytes();
                    sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(remoteIP), Integer.parseInt(remotePort));
                    peerSendSocket.send(sendPacket);
                }
            }
            else if(type.equals("CLOSE")){
                String uname = strTok.nextToken();
                String remSocket = strTok.nextToken();                
                byte[] sendData = new byte[1024];
                String sendText = "DEL " + uname + " " + remSocket + "\n";
                sendData = sendText.getBytes();
                peerList.remove(uname, remSocket);
                for(Map.Entry entry: peerList.entrySet()){
                    String sock = entry.getValue().toString();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(sock.split(":")[0]), Integer.parseInt(sock.split(":")[1]));
                    peerSendSocket.send(sendPacket);
                }
                System.out.println(uname + " disconnected ...");
            }
        }
        
    }
    
    public static void createAuthList() throws FileNotFoundException, IOException{
       
        FileInputStream authStream = new FileInputStream(authFile);
            try (BufferedReader authReader = new BufferedReader(new InputStreamReader(authStream))) {
                String readLine = null;
                while((readLine = authReader.readLine()) != null){
                    authList.put(readLine.split(" ")[0], readLine.split(" ")[1]);
                }  
            }
    }    
}
