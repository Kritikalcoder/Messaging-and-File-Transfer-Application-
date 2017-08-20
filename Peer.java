/* This program creates an instance of a peer which 
 * can connect to another peer. It takes as input, 
 * two parametres: a port for it to run on and the port 
 * of the other peer, assuming both run on localhost. 
 * The two peers can chat (TCP) as well as send files 
 * of any type (TCP and UDP). They follow a half-duplex
 * protocol. 
*/

import java.net.*;  
import java.io.*;
import java.nio.*;

public class Peer {

	public Peer () {

		
	}

	public static void main(String[] args) {
		try {
			String serverPort = args[0];
			int sPort = Integer.parseInt(serverPort);
			String clientPort = args[1];
			int cPort = Integer.parseInt(clientPort);

			String person = args[2];
			String friend = args[3];

			String serverPortUDP = args[4];
			int sPortUDP = Integer.parseInt(serverPortUDP);
			String clientPortUDP = args[5];
			int cPortUDP = Integer.parseInt(clientPortUDP);
			// server port is the port of this system
			try {

				Server myServer = new Server(sPort,sPortUDP,person,friend);
				Client myClient = new Client(cPort,cPortUDP,person,friend);
				myServer.setPriority(Thread.MAX_PRIORITY);
				myClient.setPriority(Thread.NORM_PRIORITY);
				myServer.start();
				myClient.start();

				//myServer.join();
				//myClient.join();
	            
	        }
	        catch (Exception e) {
	        	System.out.println("error");
	        }

		}
		catch(ArrayIndexOutOfBoundsException e) {
            System.out.println("Please enter all four ports and usernames on the command line");
        }
	}
}