package client;

import java.util.List;
import java.util.Map;

import common.*;

public interface ClientInterface {

	//Users
	
	/**
	 * Register a new user on the system and return it, or return null if registration fails.
	 * @param username username
	 * @param password password
	 * @param address address 
	 * @param postcode valid postcode
	 * @return newly registered user, or null if registration fails
	 */
	public User register(String username, String password, String address, Postcode postcode);
	
	/**
	 * Login a previously registered user on the system and return it, or return null if login fails.
	 * @param username username
	 * @param password password
	 * @return logged in user, or null if login fails
	 */
	public User login(String username, String password);
	
	/**
	 * Return a list of available postcodes in the system
	 * @return list of postcodes
	 */
	public List<Postcode> getPostcodes();
	
	//Dishes
	
	/**
	 * Return a list of all available dishes
	 * @return list of available dishes
	 */
	public List<Dish> getDishes();
	
	/**
	 * Return the description for a given dish
	 * @param dish Dish to lookup
	 * @return description of dish
	 */
	public String getDishDescription(Dish dish);
	
	/**
	 * Return the price for a given dish
	 * @param dish Dish to lookup
	 * @return the price of given fish
	 */
	public Number getDishPrice(Dish dish);
	
	//Basket
	
	/**
	 * Get the basket for a given user
	 * @param user user to lookup
	 * @return a map of dishes to quantities
	 */
	public Map<Dish,Number> getBasket(User user);
	
	/**
	 * Get the total cost of the current basket
	 * @param user user to lookup basket
	 * @return total cost of basket
	 */
	public Number getBasketCost(User user);
		
	/**
	 * Add a quantity of given dish to the basket
	 * @param user user of basket
	 * @param dish dish to change
	 * @param quantity quantity to set
	 */
	public void addDishToBasket(User user, Dish dish, Number quantity);
	
	/**
	 * Update a dish in the basket with a new quantity. If the quantity is 0, the item should be removed.
	 * @param user user of basket
	 * @param dish dish to change
	 * @param quantity quantity to set. 0 should remove.
	 */
	public void updateDishInBasket(User user, Dish dish, Number quantity);
	
	/**
	 * Checkout a basket into a new order and return it
	 * @param user user of basket
	 * @return new order
	 */
	public Order checkoutBasket(User user);
	
	/**
	 * Clear the basket of a given user
	 * @param user user of basket
	 */
	public void clearBasket(User user);

	//Orders
	
	/**
	 * Get the current orders for a given user
	 * @param user user to lookup
	 * @return list of orders
	 */
	public List<Order> getOrders(User user);
	
	/**
	 * Return whether an order is complete or is still in progress
	 * @param order order to lookup
	 * @return true if the order is complete, false otherwise
	 */
	public boolean isOrderComplete(Order order);
	
	/**
	 * Return a text representation of the status of a given order
	 * @param order order to lookup
	 * @return status text as string
	 */
	public String getOrderStatus(Order order);
	
	/**
	 * Return the cost of an order based on the makeup of the order
	 * @param order to lookup
	 * @return cost of order
	 */
	public Number getOrderCost(Order order);
	
	/**
	 * Cancel a given order
	 * @param order to cancel
	 */
	public void cancelOrder(Order order);
	
	//Listeners
	
	/**
	 * Add a new update listener to the client. This should be notified when any model changes occur that require the UI to update.
	 * @param listener An update listener to be informed of all model changes.
	 */
	public void addUpdateListener(UpdateListener listener);
	
	/**
	 * Notify all listeners of a model update. This is primarily used to update the UI after changes have been made.
	 */
	public void notifyUpdate();
	
}