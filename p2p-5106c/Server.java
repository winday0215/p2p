import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Server {
	int sPort = 8000;    //The server will be listening on this port number
	ServerSocket sSocket;   //serversocket used to lisen on port number 8000
	Socket connection = null; //socket for the connection with the client
	String message;    //message received from the client
	String MESSAGE;    //uppercase message send to the client
//	ObjectOutputStream out;  //stream write to the socket
//	ObjectInputStream in;    //stream read from the socket
  int ClientID = 1; //assign and track client ID
  int chunkcounter = 1; //count total chunk numbers
  int[][] clientdwlist = new int[5][10]; //list to track downloaded chunk IDs for each client
//constructor
  int MaxClientNumber = 5;
  public void Server() {}
 
  
  //split file into chunks, each chunk is 100kB 
  public void splitFile(String filename){
    File file = new File(filename);
    int sizeofChunk = 100 * 1000; //100kB
    byte[] bufferofChunk = new byte[sizeofChunk];
    FileInputStream fis = null;
    FileOutputStream fos = null;
    int tmp = 0;
  
  
    try {
      fis = new FileInputStream(file);
      BufferedInputStream bis = new BufferedInputStream(fis);
      System.out.println("Total file size:"+fis.available()/1000 + " kB");
      File dir = new File("chunks");
      dir.mkdir();

      //read 100kB each time, and create a new file for each read.
      while ((tmp = bis.read(bufferofChunk)) > 0){
        File newFile = new File(dir, Integer.toString(chunkcounter));
        System.out.println("Chunk "+ chunkcounter +": " + tmp/1000 + "kB");
        newFile.createNewFile();
        chunkcounter++;
        fos = new FileOutputStream(newFile);
        fos.write(bufferofChunk, 0, tmp);
        fos.close();
      }
      chunkcounter--;
      System.out.println("Total number of chunks: "+ chunkcounter);
      int result = chunkcounter/5;
      int remainder = chunkcounter % 5;

      //assign chunks to each client, use 2 dimensional array
      for(int i=0; i<5 ; i++){
        for(int j=0; j<result; j++){
          clientdwlist[i][j] = result*i+j+1; 
        }
      }
      for(int i=0; i<remainder; i++){
        clientdwlist[i][result] = 5*result+i+1;
      }

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if(fis != null){
          fis.close();
        }
      } catch (IOException ex) {
          ex.printStackTrace();
        } 
      
    }

  }
	
  void run()
	{
		try{
      //create a serversocket
			sSocket = new ServerSocket(sPort, 5);
			//Wait for connection
			System.out.println("Waiting for connection...");
		  //accept a connection from the client
		  FinishedClientCounter fcc;
      fcc = new FinishedClientCounter(MaxClientNumber);
      while(true) {
        connection = sSocket.accept();
        ClientThread th;
        th = new ClientThread(fcc, connection,clientdwlist,ClientID++, chunkcounter);
        Thread t = new Thread(th);
        t.start(); 
        if(ClientID > MaxClientNumber){
          break;
        }        
      }
      while(!fcc.isFinished()){
        try {
          Thread.sleep(1000);
        } catch(InterruptedException ex) {
          Thread.currentThread().interrupt();
        } 
      }
      System.out.println("All files have been dispatched to clients, Server is closing...");  


    }	catch(IOException ioException){
			ioException.printStackTrace();
    }
    finally{
        //Close connections
        try{
          sSocket.close();
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
}
	//send a message to the output stream

 	public static void main(String args[]) {
      Server s = new Server();
      String filename = "a.JPG";
      s.splitFile(filename);  
      s.run();
 
    }

}
