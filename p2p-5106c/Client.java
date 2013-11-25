import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Client {
	Socket requestSocket;           //socket connect to the server
	ServerSocket serverSocket;      //socket opens as server
  ObjectOutputStream out;         //stream write to the socket
 	ObjectInputStream in;          //stream read from the socket
	FileOutputStream fos;
  String ClientID;
  String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server
  File dir;
  ArrayList<String> chunklist;//save obtained chunks
  private int totalchunks;

	public Client() {}

	void run()
	{
		try{
			//create a socket to connect to the server
			requestSocket = new Socket("localhost", 8000);
			System.out.println("Connected to localhost in port 8000");
			//initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			
			//get Input from standard input
		//	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
  	 // while(true){
        
        message  = in.readUTF();
        System.out.println("Receive message: "+ message);
        if(message.substring(0,8).equals("ClientID")){
			    System.out.println("Been assigned Client ID "+ message.substring(9));
          ClientID = message.substring(9);
          dir = new File("Client_"+ClientID);
          dir.mkdir();
        } else if(message.equals("Maximum Client number reaches, Can not Connect")){
          requestSocket.close();
          return;
        }
        message = in.readUTF();
        String[] cm = message.split("/");
        totalchunks = Integer.parseInt(cm[0]);
        int receivedchunks = Integer.parseInt(cm[1]);
        System.out.println("Prepare to receive "+receivedchunks+" file chunks.....");

        out.writeUTF("READY");
        out.flush();
        
        chunklist = new ArrayList<String>();
        for(int i=0; i < receivedchunks; i++){
          message = in.readUTF();
          System.out.println("Receive message: "+ message);
          String[] filepara = message.split(" ");
        
          chunklist.add(filepara[1]); //push obtained chunk ID to chunklist

          File file = new File(dir,filepara[1]);  //test
          int filesize = Integer.parseInt(filepara[2]);
       
          fos = new FileOutputStream(file);
          byte[] buffer = new byte[filesize];
          int bytesRead = 0;
        
          while(filesize > 0 && (bytesRead = in.read(buffer,0,(int)Math.min(1000, filesize))) != -1){
            filesize -= bytesRead;
            fos.write(buffer, 0, bytesRead);
            fos.flush();
          } 

        
       
          fos.close();
          message = filepara[1]+" Download Completed";
          out.writeUTF(message);
			    out.flush();
          System.out.println("Current Chunk List: "+chunklist);
        }
        out.writeUTF("Connection Close");
        out.flush();  
     }

		catch (ConnectException e) {
    			System.err.println("Connection refused. You need to initiate a server first.");
		}  
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
    //TODO:open server thread and client thread, write a SharedData class, use semaphore
    try{

      SharedData sd = new SharedData(chunklist);

      int iClientID = Integer.parseInt(ClientID);//transfer clientid from string to int
      CsThread serverthread;
      serverthread = new CsThread(serverSocket, iClientID, sd, totalchunks);
      Thread ts = new Thread(serverthread);
      ts.start();

      CcThread clientthread;
      clientthread = new CcThread(requestSocket, iClientID, sd, totalchunks);
      Thread tc = new Thread(clientthread);
      tc.start();
    }

    finally{
    }
	}
	//send a message to the output stream
	void sendMessage(String msg)
	{
		try{
			//stream write the message
			out.writeObject(msg);
			out.flush();
			System.out.println("Send message: " + msg);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	//main method
	public static void main(String args[])
	{
		Client client = new Client();
		client.run();
	}

}
