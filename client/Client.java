package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import common.Comms;
import common.Dish;
import common.Drone;
import common.Ingredient;
import common.Order;
import common.Postcode;
import common.Staff;
import common.UpdateEvent;
import common.UpdateListener;
import common.User;

public class Client implements ClientInterface, UpdateListener {

	private ArrayList<Postcode> postcodes = new ArrayList<Postcode>();
	private ArrayList<Dish> dishes = new ArrayList<Dish>();
	private Object lock = new Object();
	private CommsClient commsClient;
	public Client () {
		commsClient = new CommsClient();
		commsClient.init();
		//(new Refresher()).start();
	}
	
	
	private void initDrones() {
		Drone d = new Drone ();
		d.setName("RoboIonut");
	}
	
	@Override
	public User register(String username, String password, String address, Postcode postcode) {
		User user = new User (username, password, address, postcode);
		if (username.equals("") || password.equals("") || address.equals(""))
			return null;
		commsClient.sendMessage("REGISTER:" + username + ":" + password + ":" + address, postcode);
		return user;
	}

	@Override
	public User login(String username, String password) {
		commsClient.sendMessage("LOGIN", username + " " + password);
		User u = (User) commsClient.receiveMessage();
		return (User) u;
				
	}
	
	@Override
	public List<Postcode> getPostcodes() {
		
		commsClient.sendMessage("GET POSTCODES", null);
		postcodes = (ArrayList<Postcode>) commsClient.receiveMessage();
		return postcodes;
	}

	@Override
	public List<Dish> getDishes() {
		commsClient.sendMessage("GET DISHES", null);
		dishes = (ArrayList<Dish>) commsClient.receiveMessage();
		return dishes;
	}

	@Override
	public String getDishDescription(Dish dish) {
		return dish.getDescription();
	}

	@Override
	public Number getDishPrice(Dish dish) {
		return dish.getPrice();
	}

	@Override
	public Map<Dish, Number> getBasket(User user) {
		return user.getBasket();
	}

	@Override
	public Number getBasketCost(User user) {
		return user.getPrice();
	}

	@Override
	public void addDishToBasket(User user, Dish dish, Number quantity) {
		user.addToBasket(dish, quantity);
		
	}

	@Override
	public void updateDishInBasket(User user, Dish dish, Number quantity) {
		user.updateBasket(dish, quantity);
	}

	@Override
	public Order checkoutBasket(User user) {
		Order o = new Order(user);
		o.updateDetails(user.getBasket());
		o.setCost();
		user.clearBasket();
		user.getOrders().add(o);
		o.setName("Order " + user.getName() + " " + user.getOrders().size());
		commsClient.sendMessage("ORDER", o);
		return o;
	}

	@Override
	public void clearBasket(User user) {
		user.clearBasket();
	}

	@Override
	public List<Order> getOrders(User user) { 
		if (user == null) {
			return new ArrayList<Order>();
		}
		commsClient.sendMessage("GET ORDERS", user);
		ArrayList<Order> temp =  (ArrayList<Order>) commsClient.receiveMessage();
		String string;
		for (Order o : temp) {
			string =  (String) commsClient.receiveMessage();
			o.setStatus(string);
		}
		user.setOrders(temp);
		return temp;
	}

	@Override
	public boolean isOrderComplete(Order order) {
		if (order.getStatus().equals("COMPLETED"))
			return true;
		return false;
	}

	@Override
	public String getOrderStatus(Order order) {
		return order.getStatus();
	}

	@Override
	public Number getOrderCost(Order order) {
		return order.getCost();
	}

	@Override
	public void cancelOrder(Order order) {
		commsClient.sendMessage("CANCELLED", order);
		//order.cancelOrder();
	}
	
	private List<UpdateListener> updateListeners = new ArrayList<UpdateListener>();
	@Override
	public void addUpdateListener(UpdateListener listener) {
		updateListeners.add(listener);
	}

	@Override
	public void notifyUpdate() {
		for(UpdateListener listener : updateListeners) {
			listener.updated(new UpdateEvent());
		}	
	}

	@Override
	public void updated(UpdateEvent updateEvent) {
		for (UpdateListener u : updateListeners)
			u.updated(updateEvent);
	}
	class Refresher extends Thread {
		@Override
		public void run () {
			while (true) {
				dishes = (ArrayList<Dish>) getDishes();
				for (Dish d : dishes) {
					System.out.println("aici");
					d.notifyUpdate();
				}
				//notifyUpdate();
				try {
					Thread.sleep(2 * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
