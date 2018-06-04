package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import common.Dish;
import common.Drone;
import common.Ingredient;
import common.Order;
import common.Postcode;
import common.Staff;
import common.Supplier;
import common.User;

public class DataPersistence {
	Server server;
	BufferedWriter writer;
	File backup = new File ("backup.txt");
	public DataPersistence (Server server) {
		this.server = server;
		if (!backup.exists())
			try {
				backup.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		(new Backer()).start();
	}
	
	//saves all the data to the harddisk
	private void save () {
		try {
			writer = new BufferedWriter (new FileWriter(backup));
			saveSuppliers();
			saveIngredients();
			saveDishes();
			savePostcodes();
			saveUsers();
			saveOrders();
			saveStocks();
			saveStaff();
			saveDrones();
			writer.flush();
			writer.close();
			//backup.
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void saveSuppliers () throws IOException {
		for (Supplier supplier : server.getSuppliers())
			writer.write("SUPPLIER:" + supplier.getName() + ":" + supplier.getDistance() + "\n");
	}
	
	private void saveIngredients () throws IOException {
		for (Ingredient i : server.getIngredients())
			writer.write("INGREDIENT:" + i.getName() + ":" + i.getUnit() + ":" +
						i.getSupplier() + ":" + i.getThreshold() + ":" + i.getRestocking() +"\n");
	}
	
	private void saveDishes () throws IOException {
		for (Dish d : server.getDishes()) {
			String s = new String(d.getRecipe().toString());
			writer.write("DISH:" + d.getName() + ":" + d.getDescription() + ":" +
					d.getPrice() + ":" + d.getThreshold() + ":" + d.getRestocking() + ":" + 
					s.substring(1, s.length() - 1) + "\n");
		}
	}
	
	private void savePostcodes () throws IOException {
		for (Postcode p : server.getPostcodes()) 
			writer.write("POSTCODE:" + p.getName() + ":" + p.getDistance() + "\n");
	}
	
	private void saveUsers () throws IOException {
		for (User u : server.getUsers()) {
			writer.write("USER:" + u.getName() + ":" + u.getPassword() + ":" + u.getAddress() + ":" + u.getPostcode() + "\n");
		}
	}
	
	private void saveOrders () throws IOException {
		for (Order o : server.getOrders()) {
			String s = new String(o.getDetails().toString());
			writer.write("ORDER:" + o.getUser() + ":" + s.substring(1, s.length() - 1) + ":" + o.getCost() + "\n");
		}
	}
	
	private void saveStocks () throws IOException {
		for (Dish d : server.getDishStockLevels().keySet()) 
			writer.write("STOCK:" + d.getName() + ":" + server.getDishStockLevels().get(d) + "\n");
		for (Ingredient i : server.getIngredientStockLevels().keySet()) 
			writer.write("STOCK:" + i.getName() + ":" + server.getIngredientStockLevels().get(i) + "\n");
	}
	
	private void saveStaff () throws IOException {
		for (Staff s : server.getStaff())
			writer.write("STAFF:" + s.getName() + "\n");
	}
	
	private void saveDrones () throws IOException {
		for (Drone d : server.getDrones())
			writer.write("DRONE:" + d.getSpeed() + "\n");
	}
	
	//continuosly saves the data
	class Backer extends Thread {
		
		@Override
		public void run() {
			while (true) {
				save();
				try {
					Thread.sleep (1 * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
