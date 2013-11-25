import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*; 
import java.util.concurrent.Semaphore;

public class FinishedClientCounter{
  private int counter = 0; //count finished thread
  private Semaphore available = new Semaphore(1, true);
  private int MaxClientNumber;

  //constructor
  public FinishedClientCounter(int MaxClientNumber){
    this.MaxClientNumber = MaxClientNumber;
  }
  
  //counter+1 for each finished thread
  public void IncreaseCounter() throws InterruptedException {
    available.acquire();
    counter++;
    available.release();
  }

  public boolean isFinished(){
    return counter == MaxClientNumber;
  }
}
