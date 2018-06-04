package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import common.Comms;
import common.Order;
import common.User;

public class CommsClient extends Comms {
	private Socket socket;
	private ObjectInputStream reader;
	private MessageReceiver receiver;
	public void init () {
		try {
			
			// closes the socket on close 
			socket = new Socket("localhost", port);
			Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
			    try {
			    	socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			});
			reader = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			receiver = new MessageReceiver();
			receiver.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public Object receiveMessage () {
		String msg;
		try {
			msg = (String)reader.readObject();
			return decode (msg);
		} catch (SocketException s) {} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override 
	protected Object decode (String msg) {
		
		// checks if it received a valid message
		if (msg.equals("LOGIN") || msg.equals("DISHES") || msg.equals("ORDERS") || msg.equals("POSTCODES")) {
			try { 
				return reader.readObject();
				
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}
			
		return null;
	}
	
	class MessageReceiver extends Thread {
		@Override
		public void run() {
			//receiveMessage();
		}
	}
}
