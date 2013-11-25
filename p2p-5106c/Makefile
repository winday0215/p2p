JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
CLASSES = \
				Server.java \
				Client.java \
				CcThread.java \
				CsThread.java \
				ClientThread.java \
				FinishedClientCounter.java \
				SharedData.java
default: classes
classes: $(CLASSES:.java = .class)
clean:
	$(RM) *.class
	$(RM) -rf Client_*
