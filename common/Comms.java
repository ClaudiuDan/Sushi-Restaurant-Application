package common;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Comms {

	
	protected int port = 5002;
	protected Object lock = new Object();
	protected ObjectOutputStream out;
	
	//first writes a message, then sends the object
	public synchronized void sendMessage (String message, Object o) { 
		try {
			out.writeObject(message);
			out.flush();
			out.writeObject(o);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	protected Object decode (String message) {return null;}
	public Object receiveMessage () {return null;}
}
