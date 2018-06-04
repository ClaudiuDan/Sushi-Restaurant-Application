package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import common.Comms;
import common.Order;
import common.Postcode;
import common.User;

public class CommsServer extends Comms{
	private Server server;
	
	public void init (Server server) {
		this.server = server;
		(new Thread(new ServerAcceptThread(port, this))).start();
	}
	
	public void addSocket (Socket socket) {
		MessageReceiver receiver = new MessageReceiver(socket);
		receiver.start();
	}
	
	public void receiveMessage (Socket s, ObjectInputStream in, ObjectOutputStream out) throws java.io.EOFException {
		String msg = null;
		try {
			Object obj = in.readObject();
			msg = (String) obj;
			decode(msg, in, out);
		}
		catch (ClassNotFoundException e) {} catch (java.io.EOFException e) {throw new java.io.EOFException();} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new java.io.EOFException();
		}
	}
	public synchronized void decode (String msg, ObjectInputStream in, ObjectOutputStream out) {
		if (msg != null) {
			super.out = out;
			String[] s = msg.split(":");
			
			//checks what message was receive and acts accordingly
			if (s[0].equals("REGISTER")) {
				try {
					Postcode postcode = (Postcode) in.readObject();
					if (findUser(s[1], s[2]) == null)
						server.addUser(s[1], s[2], s[3], postcode);
				} catch (ClassNotFoundException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (s[0].equals("LOGIN")) {
				try {
					String string = (String) in.readObject();
					String[] userData = string.split(" ");
					for (User u : server.getUsers())
						if (u.getName().equals(userData[0]) && u.getPassword().equals(userData[1])) {
							sendMessage("LOGIN", u);
							return;
						}
					sendMessage("LOGIN", null);
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
				
			}
			if (s[0].equals("GET DISHES")) {
				sendMessage("DISHES", server.getDishes());
			}
			if (s[0].equals("ORDER")) {
				try {
					Order order = (Order) in.readObject();
					order.setUser(findUser(order.getUser().getName(), order.getUser().getPassword()));
					server.addOrder(order);
				} catch (ClassNotFoundException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (s[0].equals("GET ORDERS")) {
				try {
					User user = (User) in.readObject(); 
					user = findUser(user.getName(), user.getPassword());
					sendMessage("ORDERS", findUser(user.getName(), user.getPassword()).getOrders());
					for (Order o : user.getOrders())
						sendMessage("ORDERS", o.getStatus());
				} catch (ClassNotFoundException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (s[0].equals("CANCELLED")) {
				try {
					Order p = (Order) in.readObject(); 
					User user;
					user = findUser(p.getUser().getName(), p.getUser().getPassword());
					for (Order o : server.getOrders()) 
						if (o.getName().equals(p.getName()) && user.equals(o.getUser())){
							o.setStatus("CANCELLED");
							break;
						}
				} catch (ClassNotFoundException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (s[0].equals("GET POSTCODES")) {
				sendMessage("POSTCODES", server.getPostcodes());
			}
			
		}
	}
	
	private User findUser (String name, String pass) {
		for (User u : server.getUsers())
			if (u.getName().equals(name) && u.getPassword().equals(pass)) {
				return u;
			}
		return null;
	}
	
	// permanently waits for new messages
	class MessageReceiver extends Thread {
		Connection conn = new Connection();
		MessageReceiver (Socket socket) {
			OutputStream outStream;
			try {
				conn.s = socket;
				outStream = socket.getOutputStream();
				InputStream inStream = socket.getInputStream();
				conn.out = new ObjectOutputStream(outStream);
				conn.out.flush();
				conn.in = new ObjectInputStream(inStream);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		@Override
		public void run() {
			while (true) {
				try {
					receiveMessage(conn.s, conn.in, conn.out);
				} catch (EOFException e) {
					break;
				}
			}
		}
	}	
}

