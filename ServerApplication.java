import java.io.IOException;

import client.Client;
import client.ClientInterface;
import client.ClientWindow;
import common.Comms;
import server.CommsServer;
import server.Configuration;
import server.Server;
import server.ServerInterface;
import server.ServerWindow;

public class ServerApplication {
	ServerInterface initialise () {
		Server server = new Server ();
		return server;
	}
	
	void launchGUI(ServerInterface serverInterface) {
		ServerWindow serverWindow = new ServerWindow (serverInterface);
	}
	
	public static void main (String[] args) {
		ServerApplication serverApp = new ServerApplication ();
		ServerInterface serverInterface = serverApp.initialise();
		serverApp.launchGUI(serverInterface);
	}
}
