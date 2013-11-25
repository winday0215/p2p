import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class CsThread implements Runnable {

  private ServerSocket cserver;
  private int ClientID;
  private Socket clientconnection;
  private SharedData shareddata;
  private ArrayList<String> serverchunklist;
  private boolean[] clientchunklist;
  private int totalchunks;
  private String message;
  private ObjectOutputStream out;
  private ObjectInputStream in;
  private FileInputStream fis;

  //constructor
  public CsThread(ServerSocket cserver, int ClientID, SharedData shareddata, int totalchunks){
    this.cserver = cserver;
    this.ClientID = ClientID;
    this.shareddata = shareddata;
    this.totalchunks = totalchunks;
  }

  public void run(){
    try{

      int sport = 25000+ClientID;
      cserver = new ServerSocket(sport, 1);
      System.out.println("SERVER: Server is listening on port "+sport + "...");
      //show current chunk list
      serverchunklist = shareddata.GetChunkList();
      System.out.println("SERVER: Current chunk list is "+serverchunklist);

      //accept client connection
      clientconnection = cserver.accept();
      out = new ObjectOutputStream(clientconnection.getOutputStream());
      out.flush();
      in = new ObjectInputStream(clientconnection.getInputStream());
      System.out.println("SERVER: Connection from "+ clientconnection + " has been accepted.");
      message = in.readUTF();
      String ms = message.substring(11, message.length()-1);
      String[] clientclist = ms.split(", ");
      clientchunklist = new boolean[totalchunks];
      for(int i=0; i<clientclist.length; i++){
        int index = Integer.parseInt(clientclist[i]);
        clientchunklist[index-1] = true;
      }
      String dir = "Client_"+ClientID;
      while(true){
        serverchunklist = shareddata.GetChunkList();
        
        for(int j=0; j< serverchunklist.size(); j++){
        
          String filename = serverchunklist.get(j);
          int k = 1;
          
          try {
            k = Integer.parseInt(filename);
          } catch(Exception e1){
            System.out.println("!!!!!" + serverchunklist.size() + " " + j + " " + filename);
            System.out.println(serverchunklist);
            while(true);
          }

         
          if(clientchunklist[k-1] == false){
            SendChunk(out, filename, dir);//send chunks to client
            clientchunklist[k-1] = true;
          }
        }
        int scounter = 0;
        for(int m=0; m < clientchunklist.length; m++){
          if(clientchunklist[m] == true){
            scounter++;
          }
        }
        if(scounter == totalchunks){
          break;
        }
      }
    } catch (IOException e){
      e.printStackTrace();
    }
    finally{
      try{
        clientconnection.close();
        System.out.println("SERVER: Connection to "+clientconnection+ " has been closed.");
        cserver.close();
      } catch (IOException e1){
        e1.printStackTrace();
      }
    }
  }

  //send chunk
  public void SendChunk(ObjectOutputStream out, String filename, String dir) throws IOException {
    String workingDir, filepath;
    workingDir = System.getProperty("user.dir");
    filepath = workingDir+ File.separator+ dir + File.separator + filename;
                            
    File file = new File(filepath);
    fis = new FileInputStream(file);
    int filesize = (int)file.length();
    //send filename and filesize

    out.writeUTF(filename+" "+filesize);
    out.flush();
    System.out.println("SERVER: "+filename +" is sending to client");
    byte[] buffer = new byte[filesize];
    fis.read(buffer, 0,  filesize);
    //send file data
    out.write(buffer);
    out.flush();
    fis.close();

    message = in.readUTF();
    System.out.println("SERVER Receive: "+message);
  }
}  
