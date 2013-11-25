JFLAGS = -g
JC = javac
default: FinishedClientCounter.class SharedData.class ClientThread.class CcThread.class CsThread.class Server.class Client.class
FinishedClientCounter.class: FinishedClientCounter.java
	$(JC) $(JFLAGS) FinishedClientCounter.java
SharedData.class: SharedData.java
	$(JC) $(JFLAGS) SharedData.java
ClientThread.class: ClientThread.java
	$(JC) $(JFLAGS) ClientThread.java
CcThread.class: CcThread.java
	$(JC) $(JFLAGS) CcThread.java
CsThread.class: CsThread.java
	$(JC) $(JFLAGS) CsThread.java
Server.class: Server.java
	$(JC) $(JFLAGS) Server.java
Client.class: Client.java
	$(JC) $(JFLAGS) Client.java
clean:
	$(RM) *.class
	$(RM) -rf Client_*
	$(RM) -rf chunks
