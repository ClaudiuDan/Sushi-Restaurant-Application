package server;

import java.io.BufferedReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection {
	public Socket s;
	public ObjectInputStream in;
	public ObjectOutputStream out;
}
