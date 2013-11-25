import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class SharedData {
  
  private ArrayList<String> chunklist;
  private Semaphore available = new Semaphore(1, true);

  //constructor
  public SharedData(ArrayList<String> chunklist){
    this.chunklist = chunklist;
  }

  //add data into chunklist
  public void AddChunkID(String chunkID) throws InterruptedException {
    available.acquire();
    chunklist.add(chunkID);
    available.release();
  }

  //get chunklist
  public ArrayList<String> GetChunkList() {
    ArrayList<String> newchunklist = null; 
    try{
      available.acquire();
      newchunklist =new ArrayList<String>(chunklist);
      available.release();
    } catch (InterruptedException e) {
    }
    return newchunklist; 
  }
}
