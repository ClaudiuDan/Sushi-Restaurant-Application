package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.ClientInterface;

public class User extends Model implements Serializable {
	
	private String password, address;
	private Postcode postcode;
	private Map<Dish, Number> basket;
	private double price; 
	private ArrayList<Order> orders;
	public User (String username, String password, String address, Postcode postcode) {
		this.setUsername(username);
		this.setPassword(password);
		this.setAddress(address);
		this.setPostcode(postcode);
		basket = new HashMap<Dish, Number>();
		orders = new ArrayList<Order>();
		price = new Double(0);
	}
	
	@Override
	public String getName() {
		return super.name;
	}
	
	// basket
	public void addToBasket(Dish dish, Number quantity) {
		if (quantity.intValue() == 0)
			return;
		if (basket.get(dish) == null)
			basket.put(dish, quantity);
		else
			basket.put(dish, basket.get(dish).intValue() + quantity.intValue());
		price = price + dish.getPrice().doubleValue() * quantity.doubleValue();
	}
	
	public void removeFromBasket(Dish dish, Number quantity) {
		basket.put(dish, basket.get(dish).intValue() - quantity.intValue());
		price = price - dish.getPrice().doubleValue() * quantity.doubleValue();
	}
	
	public void updateBasket (Dish dish, Number quantity) {
		if (quantity.intValue() == 0) {
			price = price - basket.get(dish).doubleValue() * dish.getPrice().doubleValue(); 
			basket.remove(dish);
			return;
		}
		Number aux = quantity;
		quantity = quantity.intValue() - basket.get(dish).intValue();
		basket.put(dish, aux);
		price = price + dish.getPrice().doubleValue() * quantity.doubleValue();
	}
	
	public Map<Dish, Number> getBasket () {
		return basket;
	}
	
	public double getPrice () {
		return price;
	}
	
	public void clearBasket () {
		basket.clear();
	}
	
	// orders
	
	public ArrayList<Order> getOrders () {
		return orders;
	}
	
	public void addOrder (Order order) {
		
		//for deserialization, objects have the same members but are different so it takes care of this
		for (Dish d : order.getDetails().keySet())
			for (Dish d2 : Staff.getStock().keySet())
				if (d.getName().equals(d2.getName())) {
					try {
					Number aux = order.getDetails().get(d);
					order.getDetails().remove(d);
					order.getDetails().put(d2, aux);
					}
					catch (NullPointerException e) {};
				}
		orders.add(order);
		order.setStatus("PREPARING");
		if (order.getName() == null)
			order.setName("ORDER " + order.getUser() + " " + order.getUser().getOrders().size());
		Staff.waitingOrders.add(order);
	}


	public void setUsername(String username) {
		super.name = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Postcode getPostcode() {
		return postcode;
	}

	public void setPostcode(Postcode postcode) {
		this.postcode = postcode;
	}
	
	public void setOrders (ArrayList<Order> orders) {
		this.orders = orders;
	}
}
