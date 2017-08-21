/*
* Client.java
*
* Written by : Kritika Prakash
* Written for: Distributed Systems Course, M2017
* Date       : August 21st, 2017
* Version    : 1.0
* Modified by: -
* Date       : -
* Version    : -
*/

import java.net.*;  
import java.io.*;
import java.nio.*;

public class Client extends Thread {
    // initialize socket and input output streams
    private Socket socket            = null;
    private DatagramSocket socketUDP = null;
    private DataInputStream  input   = null;
    private DataOutputStream out     = null;
    private FileInputStream fileIn   = null;
    private int peerPort;
    private int peerUDP;
    private String person;
    private String friend;

    //constructor
    public Client (int peerPort, int peerUDP ,String person, String friend) {

        this.peerPort = peerPort;
        this.peerUDP = peerUDP;
        this.person = person;
        this.friend = friend;

    }

    public void run() {
        // string to read message from input
        String line = "";

        try {
            Thread.sleep(1*10*1000);
        }

        catch (InterruptedException e) {
            System.out.println(e);
        }

        // establish a connection with server
        try {
            socket = new Socket("localhost", peerPort);
            System.out.println("Connected to " + friend);
            // takes input from terminal
            input  = new DataInputStream(System.in);
            // sends output to the socket
            out    = new DataOutputStream(socket.getOutputStream());
        }

        catch(UnknownHostException u) {
            System.out.println(u);
        }

        catch(IOException i) {
            System.out.println(i);
        }

        System.out.print(">>");

        // keep reading until "Over" is input from standard input
        while (!line.equals("Over")) {

            try {

                line = input.readLine();
                int flag = isInputValid (line);

                if (flag == 1) {
                    out.writeUTF(line);
                    System.out.print(">>");
                }

                else if (flag == 2) {
                    out.writeUTF(line);
                    sendFileTCP (line);
                    System.out.print(">>");
                }

                else if (flag == 3) {
                    out.writeUTF(line);
                    sendFileUDP (line);
                    System.out.print(">>");
                }  

            }

            catch(IOException i) {
                System.out.println(i);
            }

        }

        // close the connection
        try {
            input.close();
            out.close();
            socket.close();
        }

        catch(IOException i) {
            //System.out.println(i);
            System.out.println("Error in input/output");
        }

    }

    private int isInputValid (String input) {
        // method to check the validity of user input
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
                    if (inputArray[2].equals("TCP") || inputArray[2].equals("UDP")) {

                        File tempFile = new File(inputArray[1]);
                        boolean exists = tempFile.exists();
                        boolean dir = tempFile.isDirectory();

                        if (exists == false || dir == true) {
                            System.out.println("Path entered is not a file!");
                            flag = 0;
                        }
                        else {
                            if (inputArray[2].equals("TCP")) flag = 2;
                            else if (inputArray[2].equals("UDP")) flag = 3;
                            else flag = 0;
                        }

                    }

                    else {
                        System.out.println("Error in format\n");
                        flag = 0;
                    }

                }

            }

            return flag;
        }

        catch (Exception e) {
            System.out.println("Exception\n");

        }

        return flag;
    }

    private void sendFileTCP (String command) {
        //method which sends the packets to the server from a file
        //using TCP network protocol
        try {

            String[] inputArray = command.split(" ");
            int size = inputArray.length;

            for (int i = 0; i < size; i++) {
                inputArray[i] = inputArray[i].trim();
                inputArray[i] = inputArray[i].replaceAll("\n","");
            }

            String filename = inputArray[1];

            File tempFile = new File(filename);
            long fileSize = tempFile.length();
            String fSize = Long.toString(fileSize);
            int sizeOfFile = Integer.parseInt(fSize);
            out.writeUTF(fSize);
            long packetCount = (fileSize/4096) + 1;

            fileIn = new FileInputStream(filename);

            byte[] buffer = new byte[4096];
            long transferPercent = 0;
            int count = 0;
            String progress = ">          ";
            long totalRead = 0;
            long remaining = fileSize;

            while ((count = fileIn.read(buffer)) > 0) {
                totalRead += count;
                remaining -= count;
                out.write(buffer, 0, count);
                transferPercent = ( (totalRead * 100 )/ fileSize );
                progress = displayBar(transferPercent);
                System.out.print("Sending " + filename + " [" + progress + "] " + transferPercent + "%\r");
            }

            System.out.println("\nSent file");            
            fileIn.close();
                
        }
        catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Exception in input/output");
        }
    }

    private void sendFileUDP (String command) {
        //method which sends the packets to the server from a file
        //using UDP network protocol
        try {

            socketUDP = new DatagramSocket(); // UDP socket to connect to server
            String[] inputArray = command.split(" ");
            int size = inputArray.length;

            for (int i = 0; i < size; i++) {
                inputArray[i] = inputArray[i].trim();
                inputArray[i] = inputArray[i].replaceAll("\n","");
            }

            String filename = inputArray[1];
            File tempFile = new File(filename);
            long fileSize = tempFile.length();
            String fSize = Long.toString(fileSize);
            int sizeOfFile = Integer.parseInt(fSize);
            out.writeUTF(fSize);          
            long packetCount = (fileSize/4096) + 1;

            fileIn = new FileInputStream(filename);

            byte[] buffer = new byte[4096];
            long transferPercent = 0;
            int count = 0;
            String progress = ">          ";
            long totalRead = 0;
            long remaining = fileSize;

            DatagramPacket sendPkt = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), peerUDP);
            
            for(long i = 0; i < packetCount; i++) {
                count = fileIn.read(buffer);
                totalRead += count;
                remaining -= count;
                socketUDP.send(sendPkt);
                transferPercent = ((totalRead * 100) / fileSize );

                if (transferPercent > 100) {
                    transferPercent = 100;
                }          

                progress = displayBar(transferPercent);
                System.out.print("Sending " + filename + " [" + progress + "] " + transferPercent + "%\r");
            }

            System.out.println("\nSent file");
            fileIn.close();
            socketUDP.close();
                
        }

        catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Error in input/output");
            socketUDP.close();
        }
    }

    private String displayBar (long value) {
        //method for creating the display of the progress bar
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