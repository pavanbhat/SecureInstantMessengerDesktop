/**
 * Foundations of Computer Networks
 * Term Project 
 * Submitted by - Swapnil Kamat(snk6855@rit.edu), Pavan Bhat(pxb8715@rit.edu)
 */


package client;
import com.sun.java.swing.plaf.windows.resources.windows;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.ParseException;

public class Client {
    /**
     * Implements core logic for the client side chat application
     */

    
    private Socket sock;
    private static ServerSocket serv;
    private static int port = 10210;
    private static int msgport = 10211;
    private static String serverIP = "129.21.133.88";
    private static ArrayList<String> users = new ArrayList();  
    protected static HashMap<String,String> chat = new HashMap();
    protected static HashMap<String,String> pub_em = new HashMap();
    protected static HashMap<String,String> pub_nm = new HashMap();
    private static ClientGUI obj;
    protected static String myUsername;
    protected static String currentUserChat;
    protected static String pub_e;
    protected static String pri_d;
    protected static String pub_n;
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
       
        obj = new ClientGUI();
        obj.setVisible(true);
        obj.setResizable(false);
        chatElemVisibility(false);
        Secure.generateRSAKeys();
        
        try {
                serv = new ServerSocket(port);
                Runnable r = new Receiver();
                new Thread(r).start();
                
        } catch (Exception e) {
                System.out.println(e.getMessage());
        }
    }
    
    /**
     * Implements receiver for the incoming packets which are then handed over to packets processor
     */
    static class Receiver implements Runnable{

        @Override
        public void run() {
            ServerSocket sock;
		try {
			sock = new ServerSocket(11210);
			while (true) {
                                System.out.println("Receiver started");
				Socket incoming = sock.accept();
				BufferedReader br = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
				String result = null;
				while ((result = br.readLine()) != null) {
					System.out.println(result);
					Runnable process = new ProcessRecvPacket(result);
					new Thread(process).start();
				}
				System.out.println("Receiver resumed");
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
        }
    }
    
    /**
     * Packet processor that identifies type of the packet and processes accordingly
     */
    static class ProcessRecvPacket implements Runnable{
        
        String pkt;
        
        public ProcessRecvPacket(String pkt)
        {
        this.pkt = pkt;
        }
        
        @Override
        public void run() {
            try {
                processPacket(pkt);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        /**
         * Checks the type of the packet and processes accordingly
         * @param pkt
         * @throws UnsupportedEncodingException 
         */
        public void processPacket(String pkt) throws UnsupportedEncodingException{
            JSONObject root;
            try {
                root = (JSONObject) new JSONParser().parse(pkt);
                if (root.get("type").equals("message")) {
                    System.out.println("client.Client.ProcessRecvPacket.processPacket()");
                    JSONObject message = (JSONObject) root.get("message");
                    String to = (String) message.get("to");
                    String from = (String) message.get("from");
                    String encrBody = (String) message.get("body");
                    
                    String decrBody = Secure.decrypt(encrBody, pub_n, pri_d);
                    System.out.println(from+": "+decrBody);
                    synchronized(chat){
                        if(chat.containsKey(from))
                        {
                            String newC = chat.get(from) + "<b>"+from+": </b>"+decrBody+"<br>";
                            chat.put(from, newC);
                            System.out.println("newC" + newC);
                        }
                    }
                    obj.updateChatArea();
                }
                else if (root.get("type").equals("presence")) {
                    JSONObject message = (JSONObject) root.get("presence");
                    String state = (String) message.get("status");
                    String from = (String) message.get("from");
                    System.out.println("\n" + from + " is now " + state);
                    obj.update(from, state);
                }
            } catch (ParseException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }	
        }
    }
    
    /**
     * Sets the visibility of the chat window elements
     * @param flag false for hiding the elements
     */
    public static void chatElemVisibility(boolean flag){
        
        obj.setChatElemVisibility(flag);
        
    }
    
    /**
     * logs the user out. Makes the user offline
     */
    public static void logout(){
        
        obj.update("","");
        chatElemVisibility(false);
        
    }
    
    /**
     * Creates Message to send across
     * @param to receiver
     * @param from sender
     * @param body message to send
     * @throws IOException 
     */
    public static void createMessage(String to, String from, String body) throws IOException {
		String message = "";
        System.out.println("client.Client.createMessage()");
		JSONObject root = new JSONObject();
		JSONObject msg = new JSONObject();

                // need n and e of receiver
                String encrBody = Secure.encrypt(body, pub_nm.get(to), pub_em.get(to));
                
		msg.put("to", to);
		msg.put("from", from);
		msg.put("body", encrBody);

		root.put("message", msg);
		root.put("type", "message");

		message = root.toJSONString();

		sendMessage(message);
	}
    
    /**
     * Sends the message to the IM Server
     * @param text packet to send
     * @throws IOException 
     */
    public static void sendMessage(String text) throws IOException
    {
        System.out.println(text);
        Socket sock = new Socket(serverIP, 10210);
        PrintWriter dos = new PrintWriter(sock.getOutputStream(), true);
        dos.write(text);
        dos.flush();
        dos.close();
    }
    
    /**
     * Register user to the IM Server
     * @param username username to be registered
     * @throws UnknownHostException 
     */
    public static void register(String username) throws UnknownHostException{
        
        try {
            String regStr = registerJSON(username);
            System.out.println(regStr);
            
            Socket sock = new Socket(serverIP, port);
            PrintWriter dos = new PrintWriter(sock.getOutputStream(), true);
            dos.write(regStr);
            dos.flush();
            dos.close();
            
            Socket incoming = serv.accept();
            BufferedReader br = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
            String result = null;
            while ((result = br.readLine()) != null) {
		if (result.equals("true")){
                    System.out.println("Registered");
                    myUsername = username;
                    chatElemVisibility(true);
                }
                else{
                    System.out.println("Already Registered");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Creates the registration message to send
     * @param username user to be registered
     * @return 
     */
    public static String registerJSON(String username){
        
        String register= "";
        try {
            
            JSONObject root = new JSONObject();
            JSONObject msg = new JSONObject();
            
            InetAddress[] allMyIps = InetAddress.getAllByName(InetAddress.getLocalHost().getCanonicalHostName());
            String address = allMyIps[1].getHostAddress();

            msg.put("username", username);
            msg.put("address", address);
            msg.put("pub_e", pub_e);
            msg.put("pub_n", pub_n);

            root.put("register", msg);
            root.put("type", "register");

            register = root.toJSONString();

            
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return register;
    }
    
    /**
     * Add new user to the buddy list
     * @param budName user to add in the list
     * @throws UnknownHostException
     * @throws ParseException 
     */
    public static void addBuddy(String budName) throws UnknownHostException, ParseException{
        
        try {
            String addBudStr = addBuddyJSON(budName, myUsername);
            System.out.println(addBudStr);
            
            Socket sock = new Socket(serverIP, port);
            PrintWriter dos = new PrintWriter(sock.getOutputStream(), true);
            dos.write(addBudStr);
            dos.flush();
            dos.close();
            
            Socket incoming = serv.accept();
            BufferedReader br = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
            String result = null;
            while ((result = br.readLine()) != null) {
		if (!result.equals("false")){
                    System.out.println("Added new buddy");
                    JSONObject request = (JSONObject) new JSONParser().parse(result);
                           JSONObject req = (JSONObject) request.get("subscribe");
                                String to = (String) req.get("to");
				String pub_es = (String) req.get("pub_e");
				String pub_ns = (String) req.get("pub_n");
                                
                                pub_em.put(to, pub_es);
                                pub_nm.put(to, pub_ns);
                    String status = (String) req.get("status");
                    obj.update(budName,status);
                }
                else{
                    System.out.println("Buddy not added");
                }
            }
            
            obj.clear("buddytxt");
            
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Create add buddy message to be sent to the server
     * @param to user to add
     * @param from requesting user
     * @return 
     */
    public static String addBuddyJSON(String to, String from){
        
        String subscribe= "";
        
        JSONObject root = new JSONObject();
        JSONObject msg = new JSONObject();

        msg.put("to", to);
        msg.put("from", from);

        root.put("subscribe", msg);
        root.put("type", "subscribe");

        subscribe = root.toJSONString();
            
        return subscribe;
    }
    
    /**
     * Logs he user in. Requests for the buddy list from the server corresponding to the user logging in.
     * @param username
     * @throws UnknownHostException 
     */
    public static void login(String username) throws UnknownHostException{
        
        try {
            String loginStr = loginJSON(username);
            System.out.println(loginStr);
            
            Socket sock = new Socket(serverIP, port);
            PrintWriter dos = new PrintWriter(sock.getOutputStream(), true);
            dos.write(loginStr);
            dos.flush();
            dos.close();
            
            Socket incoming = serv.accept();
            BufferedReader br = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
            String result = null;
            while ((result = br.readLine()) != null) {
		if(!result.equals(null) && !result.equals(true) && !result.equals(false)){
                    System.out.println("Logged in");
                    System.out.println(result);
                    myUsername = username;
                    chatElemVisibility(true);
                    
                    JSONObject root = (JSONObject) new JSONParser().parse(result);
                    JSONArray arr = (JSONArray) root.get("list");
                    if (arr!=null){
                        for(int i =0; i<arr.size(); i++){
                            JSONObject o = (JSONObject) arr.get(i);
                            obj.update(o.get("user").toString(), o.get("status").toString());
                        }
                    }
                    else{
                        obj.update("","");
                    }
                }
                else{
                    System.out.println("No such username exists");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Creates login message to be sent to the server
     * @param username user logging in
     * @return 
     */
    public static String loginJSON(String username){
        
        String listreq = "";
       
        JSONObject root = new JSONObject();
        JSONObject msg = new JSONObject();

        msg.put("for", username);

        root.put("listReq", msg);
        root.put("type", "listReq");

        listreq = root.toJSONString();
            
        return listreq;
    }
    
    /**
     * Create and send presence message. Whether the user is going offline or online
     * @param from self
     * @param status online or offline
     * @throws IOException 
     */
    public static void sendPresenceMessage(String from, String status) throws IOException {
            String presence = "";

            JSONObject root = new JSONObject();
            JSONObject msg = new JSONObject();

            msg.put("from", from);
            msg.put("status", status);

            root.put("presence", msg);
            root.put("type", "presence");

            presence = root.toJSONString();

            Socket sock = new Socket(serverIP, 10210);
            PrintWriter dos = new PrintWriter(sock.getOutputStream(), true);
            dos.write(presence);
            dos.flush();
            dos.close();
	}
}
