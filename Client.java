import java.net.*;  
import java.io.*;
import java.nio.*;

public class Client extends Thread {
    // initialize socket and input output streams
    private Socket socket            = null;
    private DataInputStream  input   = null;
    private DataOutputStream out     = null;

    private FileInputStream fileIn   = null;
    private int peer_port;
    private String person;
    private String friend;
    //constructor
    public Client (int peer_port, String person, String friend) {
        this.peer_port = peer_port;
        this.person = person;
        this.friend = friend;
    }

    public void run() {
        try {
            Thread.sleep(1*10*1000);
        }
        catch (InterruptedException e) {
            System.out.println(e);
        }
        System.out.println("Client thread - " + peer_port);

        // establish a connection
        try {
            socket = new Socket("localhost", peer_port);
            System.out.println("Connected");

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

        // string to read message from input
        String line = "";
 
        // keep reading until "Over" is input
        while (!line.equals("Over"))
        {
            try
            {
                System.out.print(">>");
                line = input.readLine();
                int flag = isInputValid (line);
                if (flag==1) {
                    out.writeUTF(line);
                }
                else if (flag==2) {
                    out.writeUTF(line);
                    sendFileTCP (line);
                }
                else if (flag==3) {
                    out.writeUTF(line);
                    sendFileUDP (line);
                }                
            }
            catch(IOException i)
            {
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
            System.out.println(i);
        }

    }

    public int isInputValid (String input) {
        int flag = 1;
        try {
            String[] input_array = input.split(" ");
            int size = input_array.length;

            if (size==3) {
                for (int i=0; i<size; i++) {
                    input_array[i] = input_array[i].trim();
                    input_array[i] = input_array[i].replaceAll("\n","");
                    //System.out.println("Token:" + i + "::" + input_array[i]+":EOT");
                }
                if (input_array[0].equals("Sending")) {
                    //System.out.print("Parsed string:" + input_array[0] + input_array[1] + input_array[2]);
                    if (input_array[2].equals("TCP") || input_array[2].equals("UDP")) {
                        File tempFile = new File(input_array[1]);
                        boolean exists = tempFile.exists();
                        boolean dir = tempFile.isDirectory();
                        if (exists==false) {
                            System.out.println("File does not exist in specified path!\n");
                            flag = 0;
                        }
                        else {
                            if (dir==true) {
                                System.out.println("Specified path is of a directory!\n");
                                flag = 0;
                            }
                            else {
                                if (input_array[2].equals("TCP")) flag = 2;
                                else if (input_array[2].equals("UDP")) flag = 3;
                                else flag = 0;
                            }
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

    public void sendFileTCP (String command) {
        try {
            String[] input_array = command.split(" ");
            int size = input_array.length;
            for (int i=0; i<size; i++) {
                input_array[i] = input_array[i].trim();
                input_array[i] = input_array[i].replaceAll("\n","");
                //System.out.println("Token:" + i + "::" + input_array[i]+":EOT");
            }
            String filename = input_array[1];
            System.out.print("TCP function\n");

            File tempFile = new File(filename);
            long fileSize = tempFile.length();
            String fSize = Long.toString(fileSize);
            int sizeOfFile = Integer.parseInt(fSize);
            out.writeUTF(fSize);
            long packetCount = (fileSize/4096) + 1;

            fileIn = new FileInputStream(filename);

            byte[] buffer = new byte[4096];
            int transferPercent = 0;
            int count = 0;
            String progress = ">          ";
            int totalRead = 0;
            int remaining = sizeOfFile;
            while ((count = fileIn.read(buffer)) > 0) {
                totalRead += count;
                remaining -= count;
                out.write(buffer, 0, count);
                transferPercent = ( totalRead / sizeOfFile ) * 100;
                progress = displayBar(transferPercent);
                System.out.print("Sending " + filename + " [" + progress + "] " + transferPercent + "%\r");
            }
            System.out.println("\nSent file");            
            fileIn.close();
                
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFileUDP (String command) {
        System.out.print("UDP function\n");

        System.out.println("Sent file");
    }

    public String displayBar (int value) {
        int fraction = value/10;
        String display = ">          ";
        //int remainder = value%10;
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
        return display;
    }
}