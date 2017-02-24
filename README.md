# SecureInstantMessengerDesktop
<p align="justify">Secure Instant Messenger is a Mobile / Desktop application which has been implemented over Extensible Messaging and Presence Protocol (XMPP) which supports displaying status for an individual such as online, away, offline etc. XMPP was executed using encrypted JSON structure, unlike the traditional XML format. The application was built using a Client-Server architecture using TCP/IP for transfer of messages instantly. The transfer of messages in the instant messenger were encrypted using the end-to-end principle. The principle was adhered to with a dual layered protection in messages using RSA and OAEP algorithm.</p>

<ol><strong>Features Supported by the Messenger:</strong>
<li>Login and Signup for users of the instant messenger.</li>
<li>Allocation and Deallocation of users from the Server.</li>
<li>Adding buddies over a network and storing their chat messages.</li>
<li>Displaying Status of a user and his buddies.</li>
<li>Security Module with End to End encryption using RSA and OAEP algorithms.</li>
</ol>

<p align="justify">
<strong>Changes to be made on the Client.java file:</strong><br>
As the network IP address changes with the WIFI network it is connected to, the Server IP address is hardcoded and needs to be changed with the change in the server computer.</p>
<br>
<strong>NOTE:</strong> <br>
<p align="justify">Server and Client pcs can't be the same as there will be a conflict of the sockets being used by the same pc. Therefore, Server and Clients must be run on separate computers.</p>

<ul>
<strong>Instructions to run the project:</strong>
<li>Add the external jar file (JSON)</li>
<li>Build and Run the project on NETBEANS.</li>
</ul>

<strong>Testing Instructions:</strong><br>
Server needs to be started first in order to register the clients.<br>

<ul>Next,
<li>Run the project.</li>
<li>Enter a name in the username field.</li>
<li>Click the register button while logging in for the first time. Next time onwards as the user is already registered the login button would work.</li>
<li>Add a User already connected to the server. Otherwise, the chat will wait until the user is registered on the server and no action will be taken.</li>
</ul>
<br>
<p align="justify">
Client A - Sender<br>
Client B - Receiver<br>
<br>
Client A adds B to its list<br>
Client B adds A to its list<br>
<br>
Now, if both the users are connected, the respective users will be populated in the online buddies section on the right. Click on the name of the client you want to chat with and press send to send messages.<br>
<br>
Clicking logout would reflect on the client and the server machines and the user who has logged out will switch from the online buddies section to the offline buddies section.</p>
<br>
<strong>Communication:</strong><br>
<br>
A registers to the Server<br>
B registers to the Server<br>
<br>
A adds B<br>
B adds A<br>
<br>
A sends message -> Encryption Module (Secure.java) -> Server<br>
Server then forwards the message to the respective client<br>
<br>
XMPP is used to show status messages i.e. Online and Offline status.<br>

<strong>Technologies Used:</strong> Java, JFrames (GUI), Android, JSON<br>
<br>
<strong>Domains:</strong> Networking, Security <br>
<br>
<ol>
Important Files:
<li>Client.java (Contains the client side code)</li>
<li>ClientGUI.java (Contains the GUI of the messenger)</li>
<li>Secure.java (Contains Security module with RSA and OAEP key generation and encryption and decryption code)</li>
<li>IMServer.java (Contains the server side code)</li>
<li>IMProcessPacket.java (Contains processing information on the server side)</li>
</ol>
 <br>
<ul>
Requirements:
<li>NETBEANS 8.2 (Java SE) to run the GUI using JFrames</li>
<li>json-simple-1.1.1.jar (JSON library in the lib folder of the distribution)</li>
</ul>
