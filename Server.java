import java.net.*;  
import java.io.*;
import java.nio.*;

public class Server extends Thread {

	//initialize socket and input stream
    private Socket          socket   = null;
    private ServerSocket    server   = null;
    private DataInputStream in       = null;

    private FileOutputStream fileOut = null;
 	private String person;
    private String friend;
	private int self_port;
	public Server (int self_port, String person, String friend) {
		this.self_port = self_port;
		this.person = person;
		this.friend = friend;
	}

	public void run() {
        System.out.println("Server thread - " + self_port);	

        // starts server and waits for a connection
        try
        {
            server = new ServerSocket(self_port);
            System.out.println("Server started");
 
            System.out.println("Waiting for a client ...");
 
            socket = server.accept();
            System.out.println("Client accepted");
 
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
                    System.out.println(friend + ": " + line);
                	
                	int flag = interpretInput(line);
                	if (flag==1) {
                		System.out.print(">>");
                	}
                	else if (flag==2) {
                		// receiving file via tcp
                		saveFileTCP(line);
                		// invoke some tcp receive function
                	}
                	else if (flag==3) {
                		// receiving file via udp
                		saveFileUDP(line);
                		// invoke some udp receive function
                	}
                	else {
                		System.out.print(">>");
                	}
 
                }
                catch(IOException i)
                {
                    System.out.println(i);
                }
            }
            System.out.println("Closing connection");
 			System.out.print(">>");
            // close connection
            socket.close();
            in.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }	
	}

	public int interpretInput (String input) {
		int flag = 1;
		try {
            String[] input_array = input.split(" ");
            int size = input_array.length;

            /// some condition
            /// saveFile(socket);
            if (size==3) {
                for (int i=0; i<size; i++) {
                    input_array[i] = input_array[i].trim();
                    input_array[i] = input_array[i].replaceAll("\n","");
                    //System.out.println("Token:" + i + "::" + input_array[i]+":EOT");
                }
                if (input_array[0].equals("Sending")) {
                    //System.out.print("Parsed string:" + input_array[0] + input_array[1] + input_array[2]);
                    if (input_array[2].equals("TCP")) flag = 2;
                    else if (input_array[2].equals("UDP")) flag = 3;
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
            
            String fSize = in.readUTF();
            long fileSize = Long.parseLong(fSize);
            int sizeOfFile = Integer.parseInt(fSize);
            long packetCount = (fileSize/4096) + 1;

            //// make tons of changes in this function :D
			//DataInputStream dis = new DataInputStream(socket.getInputStream());
			FileOutputStream fileOut = new FileOutputStream(filename);
			byte[] buffer = new byte[4096];
			
			int read = 0;
			int totalRead = 0;
			int remaining = sizeOfFile;
			int transferPercent = 0;
			String progress = ">          ";
			while((read = in.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
				totalRead += read;
				transferPercent = (totalRead/sizeOfFile) * 100;
				remaining -= read;
				progress = displayBar(transferPercent);
				System.out.print("Receiving " + filename + " [" + progress + "] " + transferPercent + "%\r");
				fileOut.write(buffer, 0, read);
			}
            System.out.println("\nReceived file");
			fileOut.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveFileUDP(String command) {
		System.out.println("UDP");
        System.out.println("Received file");
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