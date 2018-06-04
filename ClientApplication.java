import java.io.IOException;

import client.Client;
import client.ClientInterface;
import client.ClientWindow;
import client.CommsClient;
import common.Comms;

public class ClientApplication {
	ClientInterface initialise () {
		Client client = new Client ();
		return client;
	}
	
	void launchGUI(ClientInterface clientInterface) {
		ClientWindow clientWindow = new ClientWindow (clientInterface);
	}
	
	public static void main (String[] args) {
		ClientApplication clientApp = new ClientApplication ();
		ClientInterface clientInterface = clientApp.initialise();
		clientApp.launchGUI(clientInterface);
	}
}
