/**
 * Foundations of Computer Networks
 * Term Project 
 * Submitted by - Swapnil Kamat(snk6855@rit.edu), Pavan Bhat(pxb8715@rit.edu)
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Starts the server. Activates receiver. Receives packet and assigns a processing thread to every packet received
 *
 */
public class IMServer {

	protected static HashMap<String, String> users = new HashMap<>();
	protected static int port = 10210;
	protected static HashMap<String, ArrayList<String>> buddyList = new HashMap<>();
	protected static HashMap<String, ArrayList<String>> presNotiList = new HashMap<>();
	protected static HashMap<String, ArrayList<String>> msgStorage = new HashMap<>();
	protected static HashMap<String, String> status = new HashMap<>();
	protected static HashMap<String, String> key_n = new HashMap<>();
	protected static HashMap<String, String> key_e = new HashMap<>();

	public static void main(String[] args) throws UnknownHostException, IOException {

		System.out.println("IM Server is running...");

		ServerSocket sock;

		try {
			sock = new ServerSocket(port);
			while (true) {
				Socket incoming = sock.accept();
				BufferedReader br = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
				String result = null;
				while ((result = br.readLine()) != null) {
					// create a processing thread for every packet received
					Runnable process = new IMProcessPacket(result, users, buddyList, presNotiList, msgStorage, status,
							key_n, key_e);
					new Thread(process).start();
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
