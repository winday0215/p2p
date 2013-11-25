import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class CcThread implements Runnable {   
  private Socket cclient;
  private int ClientID;
  private SharedData shareddata;
  private int totalchunks;
  private ObjectOutputStream out;
  private ObjectInputStream in;
  private ArrayList<String> chunklist;
  private String message;
  private String dir;
  private FileOutputStream fos;
  private String filename;
  boolean[] receivedchunks;

  //constructor
  public CcThread(Socket cclient, int ClientID, SharedData shareddata, int totalchunks, String filename){
    this.cclient = cclient;
    this.ClientID = ClientID;
    this.shareddata = shareddata;
    this.totalchunks = totalchunks;
    this.filename = filename;
  }
  public void run(){
    try {

      int cport = DecidePort(ClientID);
      System.out.println("CLIENT: Trying to Connect to Server on port "+ cport + "...");
      
      //try to connect to server port
      while(true){
        try {
          Thread.sleep(1000);
          cclient = new Socket("localhost",cport);
          break;
        } 
        catch (InterruptedException ie1){}      
        catch (ConnectException ce){ }
      }
    
      out = new ObjectOutputStream(cclient.getOutputStream());
      out.flush();
      in = new ObjectInputStream(cclient.getInputStream());
      System.out.println("CLIENT: Connected to "+ cclient.getRemoteSocketAddress());
      chunklist = shareddata.GetChunkList();
      out.writeUTF("Chunklist "+chunklist);
      out.flush();
      receivedchunks = new boolean[totalchunks];
      for(int i=0; i< chunklist.size(); i++){
        receivedchunks[Integer.parseInt(chunklist.get(i))-1] = true;
      }
      //receive files
      System.out.println("CLIENT: Waiting for server to send chunks");
      dir = "Client_"+ClientID;
      while(true){
        try{
          chunklist = shareddata.GetChunkList();
          System.out.println("CLIENT: Current chunks are "+ chunklist);
          //read filename and filesize
          message = in.readUTF();
          String[] fpara = message.split(" ");
          int filesize = Integer.parseInt(fpara[1]);
          //download chunk
          DownloadChunk(in, dir, fpara[0], filesize);
          System.out.println("CLIENT: Chunk "+ fpara[0]+" has been downloaded");
          
          out.writeUTF(fpara[0]+" Download Completed");
          out.flush();

          //insert downloaded chunk ID into chunk id list
          shareddata.AddChunkID(fpara[0]);
          receivedchunks[Integer.parseInt(fpara[0])-1] = true;

          //check if all chunks have been downloaded
          int ccounter = 0;
          for(int j=0; j< receivedchunks.length; j++){
            if(receivedchunks[j] == true){
              ccounter++;
            }
          }
          if(ccounter == totalchunks) {
            break;
          }
        }
      catch(InterruptedException ie2){} 
      }
      // join chunks into a file
      JoinChunks(filename, dir);
    } catch (IOException e){
      e.printStackTrace();
    }
    finally{
      try{
        cclient.close();  
      }
      catch (IOException io1){}
    }
  }
  public int DecidePort(int ClientID){
    int port = 0;
    switch(ClientID){
      case 1: port = 25002;
              break;
      case 2: port = 25003;
              break;
      case 3: port = 25004;
              break;
      case 4: port = 25005;
              break;
      case 5: port = 25001;
              break;
    }
    return port;
  }


  public void DownloadChunk(ObjectInputStream in, String dir, String filename, int filesize) throws IOException {
    String workingDir, filepath;
    workingDir = System.getProperty("user.dir");
    filepath = workingDir+ File.separator+ dir + File.separator + filename;
    File file = new File(filepath);

    fos = new FileOutputStream(file);
    byte[] buffer = new byte[filesize];
    int bytesRead = 0;

    while(filesize > 0 && (bytesRead = in.read(buffer,0,(int)Math.min(1000, filesize))) != -1){
      filesize -= bytesRead;
      fos.write(buffer, 0, bytesRead);
      fos.flush();
    }
    fos.close();
              
  }

 //join chunks into one file 
  public void JoinChunks(String mergefilename, String dir) throws IOException {

    //merged file-output
    String workingDir, inputfilepath, outputfilepath;
    workingDir = System.getProperty("user.dir");
    outputfilepath = workingDir+File.separator+dir+File.separator+mergefilename;
    File ofile = new File(outputfilepath);
    if(ofile.exists()){
      ofile.delete();
    }
    FileOutputStream mfos;
    FileInputStream mfis;
    byte[] fileBytes;
    int bytesRead = 0;
    mfos = new FileOutputStream(ofile,true);             
    for (int i=1; i<=totalchunks; i++) {
      //input chunks 
      String filename = Integer.toString(i); 
      inputfilepath = workingDir+ File.separator+ dir + File.separator + filename;
      File file = new File(inputfilepath);
      mfis = new FileInputStream(file);
      fileBytes = new byte[(int) file.length()];
      bytesRead = mfis.read(fileBytes, 0,(int)  file.length());
      assert(bytesRead == fileBytes.length);
      assert(bytesRead == (int) file.length());
      mfos.write(fileBytes);
      mfos.flush();
      mfis.close();
    }
    mfos.close();
    System.out.println("CLIENT: File "+mergefilename+" has been created.");
  }
}
