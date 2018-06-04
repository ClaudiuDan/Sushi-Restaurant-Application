package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import common.Comms;

public class ServerAcceptThread implements Runnable{

	private ServerSocket serverSocket;
	private CommsServer comms;
	ServerAcceptThread (int port, CommsServer comms) {
		this.comms = comms;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void run() {
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				comms.addSocket(socket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
