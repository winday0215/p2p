//package p2p;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class ClientThread implements Runnable {   
  private Socket client;   
  private int ClientID;
  private int[][] clientdwlist;
  private FinishedClientCounter fcc;
  //Constructor   
  ObjectInputStream in = null;     
  ObjectOutputStream out = null;     
  FileInputStream fis = null;
  private String message;
  private int totalchunks;
  private String filename;

  public ClientThread (FinishedClientCounter fcc, Socket client, int[][] clientdwlist,int ClientID, int totalchunks, String filename) {     
    this.fcc = fcc;
    this.client = client;     
    this.ClientID = ClientID;
    this.clientdwlist = clientdwlist;
    this.totalchunks = totalchunks;
    this.filename = filename;
  }    
  
  public void run(){     
  
  try{
    System.out.println("Connection established from "+ client.getRemoteSocketAddress());    
    out = new ObjectOutputStream(client.getOutputStream());
    out.flush();
    in = new ObjectInputStream(client.getInputStream()); 
    //send ClientID to each Client
    String ClientIDmessage = "ClientID:" + ClientID;
    out.writeUTF(ClientIDmessage); 
    out.flush();
    System.out.println("Assigned Client ID as Client "+ ClientID);

    //create file stream
    int dwcounter = 0;
    int i=0;
    while(clientdwlist[ClientID-1][i] != 0){
      dwcounter++;
      i++;
    }
    
    out.writeUTF(filename);
    out.flush();
    out.writeUTF(Integer.toString(totalchunks)+"/"+Integer.toString(dwcounter));
    out.flush();
    System.out.println("Prepare to send "+dwcounter+" file chunks to Client "+ClientID);

    message = in.readUTF();
    System.out.println("Receive message: "+message);
    if(message.equals("READY")){
      String filename,workingDir, filepath;
      for(int j=0; j < dwcounter; j++){
        filename = Integer.toString(clientdwlist[ClientID-1][j]);
        workingDir = System.getProperty("user.dir");
        filepath = workingDir+ File.separator+"chunks" + File.separator + filename;
         
        File file = new File(filepath);
        fis = new FileInputStream(file);
        int filesize = (int)file.length();
        //send filename and filesize
        out.writeUTF("SENDING_FILE "+filename+" "+filesize);
        out.flush();
 
        byte[] buffer = new byte[filesize];
        fis.read(buffer, 0,  filesize);
        //send file data
        out.write(buffer);
        out.flush();
     
        fis.close();

        message = in.readUTF();
        System.out.println("Receive message: "+message);
       }
    }
    message = in.readUTF();
    System.out.println("Receive message: "+message);
    if(message.equals("Connection Close")){
      try{
        fcc.IncreaseCounter();
      } catch (InterruptedException e){
        e.printStackTrace();
      }
      System.out.println("Client "+ClientID+" connection has been closed!");
      in.close();
      out.close();
      client.close();
      return;
    }
  } catch (IOException e) {       
    System.out.println("in or out failed");      
    System.exit(-1);     
    }      
  
  }
}
