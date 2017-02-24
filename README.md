# SecureInstantMessengerDesktop

Files: 1. Client.java (Contains the client side code)
       2. ClientGUI.java (Contains the GUI of the messenger)
       3. Secure.java (Contains Security module with RSA and OAEP key generation and encryption and decryption code)
       4. IMServer.java (Contains the server side code)
       5. IMProcessPacket.java (Contains processing information on the server side)


Requirement: NETBEANS 8.2 (Java SE) to run the GUI
             json-simple-1.1.1.jar (JSON library in the lib folder of the
             distribution)

Changes to be made on the Client.java file:
As the network IP address changes with the WIFI network it is connected to, the
Server IP address is hardcoded and needs to be changed with the change
in the server computer.

NOTE: Server and Client pcs can't be the same as there will be a conflict of the
      sockets being used by the same pc. Therefore, Server and Clients must be
      run on separate computers.


Instructions to run the project: Add the external jar file (JSON)
                                 Build and Run the project on NETBEANS.

Testing:
Server needs to be started first in order to register the clients.

Next,
Run the project.
Enter a name in the username field.
Click the register button while logging in for the first time. Next time onwards
as the user is already registered the login button would work.
Add a User already connected to the server. Otherwise, the chat will wait until
the user is registered on the server and no action will be taken.

Client A - Sender
Client B - Receiver

Client A adds B to its list
Client B adds A to its list

Now, if both the users are connected, the respective users will be populated in
the online buddies section on the right. Click on the name of the client you
want to chat with and press send to send messages.

Clicking logout would reflect on the client and the server machines and the user
who has logged out will switch from the online buddies section to the offline
buddies section.

Communication:

A registers to the Server
B registers to the Server

A adds B
B adds A

A sends message -> Encryption Module (Secure.java) -> Server
Server then forwards the message to the respective client

XMPP is used to show status messages i.e. Online and Offline status.
