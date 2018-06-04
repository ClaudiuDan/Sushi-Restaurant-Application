package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import common.Dish;
import common.Ingredient;
import common.Order;
import common.Postcode;
import common.Supplier;
import common.User;

public class Configuration {
	private File file;
	private FileReader fileReader;
	private BufferedReader reader;
	private Server server;
	public Configuration (String name, Server server) throws IOException {
		file = new File(name);
		this.server = server;
		fileReader = new FileReader(file);
		reader = new BufferedReader(fileReader);
	}
	
	String[] input;
	
	//parses the input
	public void parse () throws IOException {
		String line;
		while (reader.ready()) {
			line = reader.readLine();
			input = line.split(":");
			int i = 0;
			while (i < input.length && input[i].equals("SUPPLIER")) {
				createSupplier();
				i += 3;
			}
			
			while (i < input.length && input[i].equals("INGREDIENT")) {
				createIngredient();
				i += 6;
			}
			
			while (i < input.length && input[i].equals("DISH")) {
				createDish();
				i += 7;
			}
			
			while (i < input.length && input[i].equals("POSTCODE")) {
				createPostcode();
				i += 3;
			}
			
			while (i < input.length && input[i].equals("USER")) {
				createUser();
				i += 5;
			}
			
			while (i < input.length && input[i].equals("ORDER")) {
				createOrder();
				i += 3;
			}
			
			while (i < input.length && input[i].equals("STOCK")) {
				createStock();
				i += 3;
			}
			
			while (i < input.length && input[i].equals("DRONE")) {
				createDrone();
				i += 2;
			}
			
			while (i < input.length && input[i].equals("STAFF")) {
				createStaff();
				i += 3;
			}
		}
	}
	
	
	private void createSupplier () {
		server.addSupplier(input[1], Integer.valueOf(input[2]));
	}
	
	private void createIngredient () {
		Supplier supplier = null;
		for (Supplier s : server.getSuppliers())
			if (s.getName().equals(input[3])) {
				supplier = s; break; }
		
		server.addIngredient(input[1], input[2], supplier, Integer.valueOf(input[4]), Integer.valueOf(input[5]));
	}

	private void createDish () {
		Dish dish = server.addDish(input[1], input[2], Integer.valueOf(input[3]), Integer.valueOf(input[4]), Integer.valueOf(input[5]));
		String[] aux;
		if (input.length > 6) {
			if (input[6].contains("="))
				aux = input[6].split(", ");
			else
				aux = input[6].split(",");
		}
		else
			aux = new String[0];
		int pos;
		for (int i = 0; i < aux.length; i++) {
			if (aux[i].contains("*")) {
				pos = aux[i].indexOf('*');
				for (Ingredient ingr : server.getIngredients()) {
					if (ingr.getName().equals(aux[i].substring(pos + 2, aux[i].length()))) {
						server.addIngredientToDish(dish, ingr, Double.valueOf(aux[i].substring(0, pos - 1)));
						break;
					}}
			}
			else {
				pos = aux[i].indexOf('=');
				for (Ingredient ingr : server.getIngredients()) {
					if (ingr.getName().equals(aux[i].substring(0, pos))) {
						server.addIngredientToDish(dish, ingr, Double.valueOf(aux[i].substring(pos + 1, aux[i].length())));
						break;
					}}
			}
		}
	}
	
	private void createPostcode () {
		server.addPostcode(input[1], Integer.valueOf(input[2]));
	}
	
	private void createUser () {
		for (Postcode p : server.getPostcodes()) {
			if (p.getName().equals(input[4]) || input[4].equals("null")) {
				if (input[4].equals("null"))
					server.addUser(input[1], input[2], input[3], null);
				else
					server.addUser(input[1], input[2], input[3], p);
				break;
			}
		}
	}
	
	private void createOrder () {
		Map<Dish, Number> dishes = new HashMap<Dish, Number>();
		String[] parts;
		if (input[2].contains(", "))
			parts = input[2].split(", ");
		else
			parts = input[2].split(",");
		for (int i = 0; i < parts.length; i++) {
			String[] aux;
			if (parts[i].contains("*")) {
				aux = parts[i].split(" \\* ");
				for (Dish d : server.getDishes())
					if (d.getName().equals(aux[1])) {
						dishes.put(d, Double.valueOf(aux[0])); break;}
				}
			else {
				aux = parts[i].split("=");
				for (Dish d : server.getDishes())
					if (d.getName().equals(aux[0])) {
						dishes.put(d, Double.valueOf(aux[1])); break;}
			}
		}
		
		for (User u : server.getUsers()) { 
			if (u.getName().equals(input[1])) {
				Order o = new Order(u);
				o.updateDetails(dishes);
				if (input.length == 4)
					o.cost = Double.valueOf(input[3]);
				else
					o.setCost();
				server.addOrder(o);
				break;
			}
		}
	}
	
	private void createStock () {
		for (Dish d : server.getDishes()) 
			if (d.getName().equals(input[1])) {
				server.setStock(d, Double.valueOf(input[2])); break;
			}
		for (Ingredient i : server.getIngredients()) 
			if (i.getName().equals(input[1])) {
				server.setStock(i, Double.valueOf(input[2])); break;
			}
	}
	
	private void createStaff () {
		server.addStaff(input[1]);
	}
	
	private void createDrone () {
		server.addDrone(Integer.valueOf(input[1]));
	}
}
