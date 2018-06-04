package server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import client.CommsClient;
import common.Dish;
import common.Drone;
import common.Ingredient;
import common.Order;
import common.Postcode;
import common.Staff;
import common.Supplier;
import common.UpdateEvent;
import common.UpdateListener;
import common.User;

public class Server implements ServerInterface, Serializable {

	private ArrayList<Postcode> postcodes = new ArrayList<Postcode>();
	private ArrayList<Dish> dishes = new ArrayList<Dish>();
	private ArrayList<User> users = new ArrayList<User>();
	private ArrayList<Order> orders = new ArrayList<Order>();
	private ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
	private ArrayList<Drone> drones = new ArrayList<Drone>();
	private ArrayList<Staff> staff = new ArrayList<Staff>();
	private ArrayList<Supplier> suppliers = new ArrayList<Supplier>();
	
	//static DishTracker dishTracker;
	//static IngredientTracker ingredientTracker;
	private CommsServer commsServer;
	public Server() {
		commsServer = new CommsServer();
		commsServer.init(this);
		Configuration config;
		try {
			config = new Configuration("backup.txt", this);
			config.parse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DataPersistence d = new DataPersistence(this);
		
	}
	
	
	public User addUser (String name, String password, String address, Postcode postcode) {
		User user = new User(name, password, address, postcode);
		users.add(user);
		notifyUpdate();
		return user;
	}
	
	public void addOrder (Order order) {
		orders.add(order); 
		order.getUser().addOrder(order);
	}
	
	@Override
	public void loadConfiguration(String filename) throws FileNotFoundException {
		Configuration config;
		try {
			//clears the current data
			orders.clear();
			users.clear();
			dishes.clear();
			ingredients.clear();
			postcodes.clear();
			suppliers.clear();
			for (Staff s : staff)
				s.fired = true;
			for (Drone d : drones)
				d.fired = true;
			staff.clear();
			drones.clear();
			config = new Configuration(filename, this);
			config.parse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setRestockingIngredientsEnabled(boolean enabled) {
		for (Drone d : drones)
			d.setAutomated(enabled);
	}

	@Override
	public void setRestockingDishesEnabled(boolean enabled) {
		for (Staff s : staff)
			s.setAutomated(enabled);
	}

	@Override
	public void setStock(Dish dish, Number stock) {
		Staff.setStock(dish, stock);
		
	}

	@Override
	public void setStock(Ingredient ingredient, Number stock) {
		Drone.setStock(ingredient, stock);		
	}

	@Override
	public List<Dish> getDishes() {
		return dishes;
	}

	@Override
	public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
		Dish d = new Dish ();
		d.setName(name);
		d.setDescription(description);
		d.setPrice(price);
		d.setRestocking(restockAmount);
		d.setThreshold(restockThreshold);
		dishes.add(d);
		Staff.setStock(d, 0);
		return d;
	}

	@Override
	public void removeDish(Dish dish) throws UnableToDeleteException {
		dishes.remove(dish);
	}

	@Override
	public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
		dish.addIngredient(ingredient, quantity);
		
	}

	@Override
	public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
		dish.removeIngredient(ingredient);
	}

	@Override
	public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
		dish.setRecipe(recipe);
	}

	@Override
	public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {
		dish.setRestocking(restockAmount);
		dish.setThreshold(restockThreshold);
	}

	@Override
	public Number getRestockThreshold(Dish dish) {
		return dish.getThreshold();
	}

	@Override
	public Number getRestockAmount(Dish dish) {
		return dish.getRestocking();
	}

	@Override
	public Map<Ingredient, Number> getRecipe(Dish dish) {
		return dish.getRecipe();
	}

	@Override
	public Map<Dish, Number> getDishStockLevels() {
		return Staff.getStock();
	}

	@Override
	public List<Ingredient> getIngredients() {
		return ingredients;
	}

	@Override
	public Ingredient addIngredient(String name, String unit, Supplier supplier, Number restockThreshold,
			Number restockAmount) {
		Ingredient i = new Ingredient();
		i.setName(name);
		i.setUnit(unit);
		i.setSupplier(supplier);
		i.setRestocking(restockAmount);
		i.setThreshold(restockThreshold);
		Drone.setStock(i, 0);
		ingredients.add(i);
		return i;
	}

	@Override
	public void removeIngredient(Ingredient ingredient) throws UnableToDeleteException {
		ingredients.remove(ingredient);
	}

	@Override
	public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
		ingredient.setRestocking(restockAmount);
		ingredient.setThreshold(restockThreshold);
	}

	@Override
	public Number getRestockThreshold(Ingredient ingredient) {
		return ingredient.getThreshold();
	}

	@Override
	public Number getRestockAmount(Ingredient ingredient) {
		return ingredient.getRestocking();
	}

	@Override
	public Map<Ingredient, Number> getIngredientStockLevels() {
		return Drone.getStock();
	}

	@Override
	public List<Supplier> getSuppliers() {
		return suppliers;
	}

	@Override
	public Supplier addSupplier(String name, Number distance) {
		Supplier supplier = new Supplier();
		supplier.setName(name); supplier.setDistance(distance);
		suppliers.add(supplier);
		return supplier;
		
	}

	@Override
	public void removeSupplier(Supplier supplier) throws UnableToDeleteException {
		suppliers.remove(supplier);
	}

	@Override
	public Number getSupplierDistance(Supplier supplier) {
		return supplier.getDistance();
	}

	@Override
	public List<Drone> getDrones() {
		return drones;
	}
	private int ct = 0;
	@Override
	public Drone addDrone(Number speed) {
		Drone d = new Drone();
		d.setSpeed(speed);
		ct++;
		d.setName("Drone " + ct);
		d.setStatus("IDLE");
		drones.add(d);
		return d;
	}

	@Override
	public void removeDrone(Drone drone) throws UnableToDeleteException {
		drones.remove(drone);
		drone.fired = true;
	}

	@Override
	public Number getDroneSpeed(Drone drone) {
		return drone.getSpeed();
	}

	@Override
	public String getDroneStatus(Drone drone) {
		return drone.getStatus();
	}

	@Override
	public List<Staff> getStaff() {
		return staff;
	}

	@Override
	public Staff addStaff(String name) {
		Staff s = new Staff();
		s.setName(name);
		staff.add(s);
		return s;
	}

	@Override
	public void removeStaff(Staff staff) throws UnableToDeleteException {
		staff.fired = true;
		this.staff.remove(staff);
	}

	@Override
	public String getStaffStatus(Staff staff) {
		return staff.getStatus();
	}

	@Override
	public List<Order> getOrders() {
		return orders;
	}

	@Override
	public void removeOrder(Order order) throws UnableToDeleteException {
		orders.remove(order);
	}

	@Override
	public Number getOrderDistance(Order order) {
		return order.getUser().getPostcode().getDistance();
	}

	@Override
	public boolean isOrderComplete(Order order) {
		if (order.getStatus().equals("READY"))
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
	public List<Postcode> getPostcodes() {
		return postcodes;
	}

	@Override
	public void addPostcode(String code, Number distance) {
		Postcode p = new Postcode();
		p.setName(code);
		p.setDistance(distance);
		postcodes.add(p);
	}

	@Override
	public void removePostcode(Postcode postcode) throws UnableToDeleteException {
		postcodes.remove(postcode);
	}

	@Override
	public List<User> getUsers() {
		return users;
	}

	@Override
	public void removeUser(User user) throws UnableToDeleteException {
		users.remove(user);
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

}
