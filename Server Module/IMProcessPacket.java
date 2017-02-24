/**
 * Foundations of Computer Networks
 * Term Project 
 * Submitted by - Swapnil Kamat(snk6855@rit.edu), Pavan Bhat(pxb8715@rit.edu)
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Processes packet based on type of the packet
 * 
 */
public class IMProcessPacket implements Runnable {

	protected HashMap<String, String> users;
	protected HashMap<String, ArrayList<String>> buddyList;
	protected HashMap<String, ArrayList<String>> presNotiList;
	protected HashMap<String, ArrayList<String>> msgStorage;
	protected HashMap<String, String> status;
	protected HashMap<String, String> key_n;
	protected HashMap<String, String> key_e;
	private int port = 10210;
	private int cliPort = 11210;
	protected String packet;

	public IMProcessPacket(String packet, HashMap<String, String> users, HashMap<String, ArrayList<String>> buddyList,
			HashMap<String, ArrayList<String>> presNotiList, HashMap<String, ArrayList<String>> msgStorage,
			HashMap<String, String> status, HashMap<String, String> key_n, HashMap<String, String> key_e) {
		this.users = users;
		this.packet = packet;
		this.buddyList = buddyList;
		this.presNotiList = presNotiList;
		this.msgStorage = msgStorage;
		this.status = status;
		this.key_n = key_n;
		this.key_e = key_e;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		try {
			JSONObject root = (JSONObject) new JSONParser().parse(packet);

			if (root.get("type").equals("message")) {
				System.out.println(root.toString());
				JSONObject message = (JSONObject) root.get("message");
				String destination = (String) message.get("to");
				System.out.println(destination);
				String destip = users.get(destination);

				Socket sock = new Socket(destip, cliPort);
				PrintWriter dos = new PrintWriter(sock.getOutputStream(), true);
				dos.write(packet);
				dos.flush();
				dos.close();
			} else if (root.get("type").equals("listReq")) {
				JSONObject obj = (JSONObject) root.get("listReq");
				String destination = (String) obj.get("for");
				System.out.println("\n" + destination);

				if (!users.containsKey(destination)) {
				} else {
					String destip = users.get(destination);

					ArrayList<String> buds = new ArrayList<>();

					synchronized (buddyList) {
						buds = buddyList.get(destination);
					}

					JSONArray arr = new JSONArray();
					if (buds != null) {
						for (String i : buds) {
							JSONObject obj1 = new JSONObject();
							obj1.put("user", i);
							synchronized (status) {
								obj1.put("status", status.get(i));
							}
							arr.add(obj1);
						}
					}

					JSONObject list = new JSONObject();
					list.put("list", arr);

					System.out.println(list.toString());

					Socket sock = new Socket(destip, port);
					PrintWriter dos = new PrintWriter(sock.getOutputStream(), true);
					dos.write(list.toString());
					dos.flush();
					dos.close();
				}
			} else if (root.get("type").equals("register")) {
				JSONObject info = (JSONObject) root.get("register");
				String username = (String) info.get("username");
				String address = (String) info.get("address");
				System.out.println(address);
				String pub_e = (String) info.get("pub_e");
				String pub_n = (String) info.get("pub_n");
				key_e.put(username, pub_e);
				key_n.put(username, pub_n);
				status.put(username, "online");

				synchronized (users) {
					if (users.containsKey(username)) {
						System.out.println("\n" + username + " is already registered");
						Socket sock = new Socket(address, port);
						PrintWriter dos = new PrintWriter(sock.getOutputStream(), true);
						dos.write("false");
						dos.flush();
						dos.close();
					} else {
						System.out.println("\n" + username + " is registered");
						users.put(username, address);
						Socket sock = new Socket(address, port);
						PrintWriter dos = new PrintWriter(sock.getOutputStream(), true);
						dos.write("true");
						dos.flush();
						dos.close();
					}
				}
				displayRegStatus();

			} else if (root.get("type").equals("subscribe")) {
				JSONObject request = (JSONObject) root.get("subscribe");
				String subTo = (String) request.get("to");
				String subFrom = (String) request.get("from");
				System.out.println("To" + subTo);
				System.out.println("From" + subFrom);
				boolean flag = false;

				request.put("pub_e", key_e.get(subTo));
				request.put("pub_n", key_n.get(subTo));
				request.put("status", status.get(subTo));

				root.replace("subscribe", request);

				synchronized (users) {
					if (users.containsKey(subTo)) {
						flag = true;
					} else {
						System.out.println("\nUser " + subTo + " not found");
						Socket sock = new Socket(users.get(subFrom), port);
						PrintWriter dos = new PrintWriter(sock.getOutputStream(), true);
						dos.write("false");
						dos.flush();
						dos.close();
					}
				}

				if (flag) {
					synchronized (buddyList) {
						ArrayList<String> buddies;
						if (buddyList.get(subFrom) != null) {
							buddies = buddyList.get(subFrom);
						} else {
							buddies = new ArrayList<String>();
						}
						if (buddies.contains(subTo)) {
							System.out.println("\n" + subTo + " already in buddy list of " + subFrom);

							Socket sock = new Socket(users.get(subFrom), port);
							PrintWriter dos = new PrintWriter(sock.getOutputStream(), true);
							dos.write("false");
							dos.flush();
							dos.close();

						} else {
							System.out.println("\n" + subFrom + " added " + subTo);
							buddies.add(subTo);
							buddyList.put(subFrom, buddies);

							Socket sock = new Socket(users.get(subFrom), port);
							PrintWriter dos = new PrintWriter(sock.getOutputStream(), true);
							dos.write(root.toString());
							dos.flush();
							dos.close();

							synchronized (presNotiList) {
								ArrayList<String> notifyList;
								if (presNotiList.get(subTo) != null) {
									notifyList = presNotiList.get(subTo);
								} else {
									notifyList = new ArrayList<String>();
								}
								if (!(notifyList.contains(subFrom))) {
									notifyList.add(subFrom);
									presNotiList.put(subTo, notifyList);
								}
							}
						}
					}
					displaybudList();
					displayNotifyList();
				}
			} else if (root.get("type").equals("presence")) {
				JSONObject message = (JSONObject) root.get("presence");
				String state = (String) message.get("status");
				String from = (String) message.get("from");
				System.out.println("\n" + from + " is now " + state);

				status.put(from, state);

				synchronized (presNotiList) {
					if (presNotiList.containsKey(from)) {
						ArrayList<String> notifyList = presNotiList.get(from);
						if (notifyList != null) {
							System.out.print("Notified to - [  ");
							for (String i : notifyList) {
								System.out.print(i + "  ");
								// Send presence notification to client here
								// (from, status)
								synchronized (users) {
									Socket sock = new Socket(users.get(i), cliPort);
									PrintWriter dos = new PrintWriter(sock.getOutputStream(), true);
									dos.write(packet);
									dos.flush();
									dos.close();
								}
							}
							System.out.print("]\n");
						}
					}
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Display the Registered users list on Server's console
	 */
	public void displayRegStatus() {

		synchronized (users) {
			System.out.println("\nUsers registered on IM server : ");
			for (Entry<String, String> entry : users.entrySet()) {
				System.out.println(entry.getKey());
			}
		}
	}

	/**
	 * Display the buddy lists of all registered users on Server's console
	 */
	public void displaybudList() {

		synchronized (buddyList) {
			System.out.println("\nBuddy lists- ");
			for (Entry<String, ArrayList<String>> entry : buddyList.entrySet()) {
				System.out.print(entry.getKey() + ": \t");
				ArrayList<String> buddies = entry.getValue();
				if (buddies != null) {
					System.out.print("[  ");
					for (String i : entry.getValue()) {
						System.out.print(i + "  ");
					}
					System.out.print("]\n");
				} else {
					System.out.print(entry.getKey() + ": \t[ ]");
				}
			}
		}
	}

	/**
	 * Display the notification lists for all the users on Server's console
	 */
	public void displayNotifyList() {

		synchronized (presNotiList) {
			System.out.println("\nNotification lists- ");
			for (Entry<String, ArrayList<String>> entry : presNotiList.entrySet()) {
				System.out.print(entry.getKey() + ": \t");
				ArrayList<String> notifyTo = entry.getValue();
				if (notifyTo != null) {
					System.out.print("[  ");
					for (String i : entry.getValue()) {
						System.out.print(i + "  ");
					}
					System.out.print("]\n");
				} else {
					System.out.print(entry.getKey() + ": \t[ ]");
				}
			}
		}
	}
}
