/*
* Server.java
*
* Written by : Kritika Prakash
* Written for: Distributed Systems Course, M2017
* Date  	 : August 21st, 2017
* Version    : 1.0
* Modified by: -
* Date       : -
* Version    : -
*/

import java.net.*;  
import java.io.*;
import java.nio.*;

public class Server extends Thread {
	//initialize socket and input stream
    private Socket          socket   = null;
    private ServerSocket    server   = null;
    private DatagramSocket  socketUDP = null;
    private DataInputStream in       = null;
    private FileOutputStream fileOut = null;
 	private String person;
    private String friend;
	private int selfPort;
	private int portUDP;

	//constructor
	public Server (int selfPort, int portUDP, String person, String friend) {
		this.selfPort = selfPort;
		this.portUDP = portUDP;
		this.person = person;
		this.friend = friend;
	}

	public void run() {
        // starts server and waits for a client connection
        try
        {
            server = new ServerSocket(selfPort);
 
            socket = server.accept();
 
            // takes input from the client socket
            in = new DataInputStream(
                new BufferedInputStream(socket.getInputStream()));
 
            String line = "";
 
            // reads message from client until "Over" is sent
            while (!line.equals("Over"))
            {
                try
                {
                    line = in.readUTF();
                    System.out.println("\n" + friend + ": " + line);
                	
                	int flag = interpretInput(line);
                	if (flag == 1) {
                		System.out.print(">>");
                	}
                	else if (flag == 2) {
                		saveFileTCP(line);
                		System.out.print(">>");
                	}
                	else if (flag == 3) {
                		saveFileUDP(line);
                		System.out.print(">>");
                	}
                	else {
                		System.out.print(">>");
                	}
 
                }
                catch(IOException i)
                {
                    //System.out.println(i);
                    System.out.println("Exception in input/output");
                }
            }
            System.out.println(friend + " left the conversation." );
 			System.out.print(">>");
            // close connection
            socket.close();
            in.close();
        }
        catch(IOException i) {
            //System.out.println(i);
            System.out.println("Exception in input/output");
        }
	}

	private int interpretInput (String input) {
		//method used to interpret the input from the client
		int flag = 1;
		try {
            String[] inputArray = input.split(" ");
            int size = inputArray.length;

            if (size == 3) {
                for (int i = 0; i < size; i++) {
                    inputArray[i] = inputArray[i].trim();
                    inputArray[i] = inputArray[i].replaceAll("\n","");
                }
                if (inputArray[0].equals("Sending")) {
                    if (inputArray[2].equals("TCP")) flag = 2;
                    else if (inputArray[2].equals("UDP")) flag = 3;
                    else {
                        System.out.print("Error in format");
                        flag = 0;
                    }
                }
            }
            return flag;
        }
        catch (Exception e) {
            System.out.print("Exception");
        }
        return flag;
	}

	private void saveFileTCP(String command) {
		//method which saves the packets received from the client to a file
		//using TCP network protocol
		try {
			String[] inputArray = command.split(" ");
            int size = inputArray.length;
            for (int i = 0; i < size; i++) {
                inputArray[i] = inputArray[i].trim();
                inputArray[i] = inputArray[i].replaceAll("\n","");
            }
            String filename = inputArray[1];
            
            String fSize = in.readUTF();
            long fileSize = Long.parseLong(fSize);
            int sizeOfFile = Integer.parseInt(fSize);
            long packetCount = (fileSize/4096) + 1;

			FileOutputStream fileOut = new FileOutputStream(filename);
			byte[] buffer = new byte[4096];
			
			int read = 0;
			long totalRead = 0;
			long remaining = fileSize;
			long transferPercent = 0;
			int choice = 0;
			if (remaining > 4096) choice = buffer.length;
			else choice = (int) remaining;
			String progress = ">          ";
			while((read = in.read(buffer, 0, choice)) > 0) {
				totalRead += read;
				transferPercent = ((totalRead * 100)/fileSize);
				remaining -= read;
				if (remaining > 4096) choice = buffer.length;
				else choice = (int) remaining;
				progress = displayBar(transferPercent);
				System.out.print("Receiving " + filename + " [" + progress + "] " + transferPercent + "%\r");
				fileOut.write(buffer, 0, read);
			}
            System.out.println("\nReceived file");
            fileOut.close();
		}
		catch (IOException e) {
			//e.printStackTrace();
			System.out.println("Error in Input/output");
		}
	}

	private void saveFileUDP(String command) {
		//method which saves the packets received from the client into a file
		//using UDP network protocol
		try {
			socketUDP = new DatagramSocket(portUDP,InetAddress.getByName("localhost"));
			socketUDP.setSoTimeout(3*1000);
			String[] inputArray = command.split(" ");
            int size = inputArray.length;
            for (int i = 0; i < size; i++) {
                inputArray[i] = inputArray[i].trim();
                inputArray[i] = inputArray[i].replaceAll("\n","");
            }
            String filename = inputArray[1];
            
            String fSize = in.readUTF();
            long fileSize = Long.parseLong(fSize);
            int sizeOfFile = Integer.parseInt(fSize);
            long packetCount = (fileSize/4096) + 1;

			byte[] buffer = new byte[4096];
			
			fileOut = new FileOutputStream(filename);

			int read = 0;
			long totalRead = 0;
			long remaining = fileSize;
			long transferPercent = 0;
			int choice = 0;
			String progress = ">          ";

			DatagramPacket rcvPkt = new DatagramPacket(buffer,buffer.length);
			
			for (long i = 0; i < packetCount; i++) {
				socketUDP.receive(rcvPkt);
				read = rcvPkt.getLength();
            	totalRead += read;
				transferPercent = ((totalRead * 100 )/fileSize);
				if (transferPercent > 100) transferPercent = 100;
				if (remaining >= read) remaining -= read;
				else remaining = 0;
				progress = displayBar(transferPercent);
				System.out.print("Receiving " + filename + " [" + progress + "] " + transferPercent + "%\r");
				fileOut.write(buffer, 0, read);
			}

            System.out.println("\nReceived file");
            fileOut.close();
			socketUDP.close();
		}
		catch (IOException e) {
			System.out.println("\n UDP transfer incomplete due to packet loss.");
			socketUDP.close();
		}

	}

	private String displayBar (long value) {
        int fraction = (int) value/10;
        String display = ">          ";
        switch (fraction) {
            case 0: display = ">          ";
                break;
            case 1: display = "=>         ";
                break;
            case 2: display = "==>        ";
                break;       
            case 3: display = "===>       ";
                break;
            case 4: display = "====>      ";
                break;           
            case 5: display = "=====>     ";
                break;
            case 6: display = "======>    ";
                break;
            case 7: display = "=======>   ";
                break;
            case 8: display = "========>  ";
                break;
            case 9: display = "=========> ";
                break;
            case 10:display = "==========>";
                break;
        }
        if (fraction > 10) display = "==========>";
        return display;
    }
}