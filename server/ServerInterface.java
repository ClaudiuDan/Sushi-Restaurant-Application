package server;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import common.*;

public interface ServerInterface {

	//Configuration
	/**
	 * Given a configuration file, populate the module appropriately
	 * @param filename configuration file to load
	 * @throws FileNotFoundException if unable to load given file
	 */
	public void loadConfiguration(String filename) throws FileNotFoundException;
	
	//Stock control
	
	/**
	 * Enable or disable automatic restocking of ingredients. 
	 * When restocking is disabled, no restocking by drones of ingredients should take place.
	 * @param enabled set to true to enable restocking of ingredients, or false to disable.
	 */
	public void setRestockingIngredientsEnabled(boolean enabled);
	
	/**
	 * Enable or disable automatic restocking of dishes. 
	 * When restocking is disabled, no restocking by staff of dishes should take place.
	 * @param enabled set to true to enable restocking of dishes, or false to disable.
	 */
	public void setRestockingDishesEnabled(boolean enabled);
	
	/**
	 * Set the current stock of a given dish directly
	 * @param dish dish to set the stock 
	 * @param stock stock amount
	 */
	public void setStock(Dish dish, Number stock);
	
	/**
	 * Set the current stock of a given ingredient directly
	 * @param ingredient ingredient to set the stock
	 * @param stock stock amount
	 */
	public void setStock(Ingredient ingredient, Number stock);
	
	//Dishes
	
	/**
	 * Return the list of current dishes available to order
	 * @return list of dishes
	 */
	public List<Dish> getDishes();
	
	/**
	 * Add a new dish to the system
	 * @param name name of dish
	 * @param description description of dish
	 * @param price price of dish
	 * @param restockThreshold minimum threshold to reach before restocking
	 * @param restockAmount amount to restock by
	 * @return newly created dish
	 */
	public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount);
	
	/**
	 * Remove a dish from the system
	 * @param dish dish to remove
	 * @throws UnableToDeleteException if unable to delete a dish
	 */
	public void removeDish(Dish dish) throws UnableToDeleteException;	
	
	/**
	 * Add an ingredient to a dish with the given quantity. If the ingredient exists already, update with the newly given values.
	 * @param dish dish to edit the recipe of
	 * @param ingredient ingredient to add/update
	 * @param quantity quantity to set. Should update and replace, not add to.
	 */
	public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity);
	
	/**
	 * Remove an ingredient from a dish
	 * @param dish dish to edit the recipe of
	 * @param ingredient ingredient to completely remove
	 */
	public void removeIngredientFromDish(Dish dish, Ingredient ingredient);
	
	/**
	 * Set the recipe for a given dish to the suppliest map of ingredients and quantity numbers
	 * @param dish dish to modify the recipe of
	 * @param recipe map of ingredients and quantity numbers to update
	 */
	public void setRecipe(Dish dish,Map<Ingredient,Number> recipe);
	
	/**
	 * Set restocking levels for the given dish
	 * @param dish dish to modify the restocking levels of
	 * @param restockThreshold new amount at which to restock
	 * @param restockAmount new amount to restock by when threshold is reached
	 */
	public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount);
	
	/**
	 * Get the restock threshold for a given dish
	 * @param dish dish to query restock threshold of
	 * @return the restock threshold
	 */
	public Number getRestockThreshold(Dish dish);
	
	/**
	 * Get the restock amount for a given dish
	 * @param dish dish to query restock amount of
	 * @return the restock amount, which will be restocked on reaching threshold
	 */
	public Number getRestockAmount(Dish dish);
	
	/**
	 * Get the recipe of a given dish, by returning a map of ingredients to quantity amounts
	 * @param dish dish to query the recipe of
	 * @return the mapping of ingredients to quantity amounts
	 */
	public Map<Ingredient,Number> getRecipe(Dish dish);
	
	/**
	 * Get the current stock levels of dishes
	 * @return map of dish to quantity numbers
	 */
	public Map<Dish,Number> getDishStockLevels();
	
	//Ingredients
	
	/**
	 * Get the current list of ingredients in the system
	 * @return a list of all current ingredients
	 */
	public List<Ingredient> getIngredients();
	
	/**
	 * Add a new ingredient
	 * @param name name
	 * @param unit unit
	 * @param supplier supplier
	 * @param restockThreshold when amount reaches restockThreshold restock
	 * @param restockAmount when threshold is reached, restock with this amount
	 * @return new ingredient
	 */
	public Ingredient addIngredient(String name, String unit, Supplier supplier, Number restockThreshold, Number restockAmount);
	
	/**
	 * Remove the given ingredient
	 * @param ingredient ingredient to remove
	 * @throws UnableToDeleteException if unable to remove the ingredient
	 */
	public void removeIngredient(Ingredient ingredient) throws UnableToDeleteException;
	
	/**
	 * Set the restock levels of the given ingredient
	 * @param ingredient ingredient to modify the restocking levels of
	 * @param restockThreshold new amount at which to restock
	 * @param restockAmount new amount to restock by when threshold is reached
	 */
	public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount);
	
	/**
	 * Get the restock threshold for a given ingredient
	 * @param ingredient ingredient to query restock threshold of
	 * @return the restock threshold
	 */
	public Number getRestockThreshold(Ingredient ingredient);
	
	/**
	 * Get the restock amount for a given ingredient
	 * @param ingredient ingredient to query restock amount of
	 * @return the restock amount, which will be restocked on reaching threshold
	 */
	public Number getRestockAmount(Ingredient ingredient);
	
	/**
	 * Get the current stock levels of ingredients
	 * @return map of ingredient to quantity numbers
	 */
	public Map<Ingredient,Number> getIngredientStockLevels();
	
	//Suppliers
	
	/**
	 * Get a list of all current suppliers
	 * @return list of suppliers
	 */
	public List<Supplier> getSuppliers();
	
	/**
	 * Add a new supplier
	 * @param name name of supplier
	 * @param distance from restaurant
	 * @return newly created supplier
	 */
	public Supplier addSupplier(String name, Number distance);
	
	/**
	 * Remove the given supplier
	 * @param supplier supplier to remove
	 * @throws UnableToDeleteException if unable to remove this supplier
	 */
	public void removeSupplier(Supplier supplier) throws UnableToDeleteException;
	
	/**
	 * Get the distance of a given supplier
	 * @param supplier supplier to query
	 * @return distance from the restaurant
	 */
	public Number getSupplierDistance(Supplier supplier);
	
	//Drones
	
	/**
	 * Get a list of all the drones in the system
	 * @return list of drones
	 */
	public List<Drone> getDrones();
	
	/**
	 * Add a new drone to the system with the given speed
	 * @param speed speed of drone
	 * @return the newly created drone
	 */
	public Drone addDrone(Number speed);
	
	/**
	 * Remove a given drone from the system
	 * @param drone drone to remove
	 * @throws UnableToDeleteException if unable to remove the drone
	 */
	public void removeDrone(Drone drone) throws UnableToDeleteException;
	
	/**
	 * Get the speed of a given drone
	 * @param drone drone to query
	 * @return the speed of the drone
	 */
	public Number getDroneSpeed(Drone drone);
	
	/**
	 * Get the text status of a given drone and what it is currently doing
	 * @param drone drone to query
	 * @return a text description of the current status of the drone
	 */
	public String getDroneStatus(Drone drone);
	
	//Staff
	
	/**
	 * Get the current staff in the system
	 * @return list of staff
	 */
	public List<Staff> getStaff();
	
	/**
	 * Add a new staff member to the system with the given name
	 * @param name name of staff member
	 * @return newly created staff member
	 */
	public Staff addStaff(String name);
	
	/**
	 * Remove a staff member from the system
	 * @param staff staff member to remove
	 * @throws UnableToDeleteException if unable to remove staff
	 */
	public void removeStaff(Staff staff) throws UnableToDeleteException;
	
	/**
	 * Get the current status of a given staff member
	 * @param staff member to query
	 * @return a text description of the current status of the staff member
	 */
	public String getStaffStatus(Staff staff);
	
	//Orders
	
	/**
	 * Get a list of all the orders in the system
	 * @return list of orders
	 */
	public List<Order> getOrders();
	
	/**
	 * Remove an order from the system
	 * @param order order to remove
	 * @throws UnableToDeleteException if unable to remove the order
	 */
	public void removeOrder(Order order) throws UnableToDeleteException;
	
	/**
	 * Get the distance of an order based on the delivery location
	 * @param order order to query
	 * @return the distance of the order
	 */
	public Number getOrderDistance(Order order);
	
	/**
	 * Return whether an order is complete
	 * @param order order to query
	 * @return true if the order is complete, false otherwise
	 */
	public boolean isOrderComplete(Order order);
	
	/**
	 * Get the text status of a given order
	 * @param order order to query
	 * @return a text status of the given order
	 */
	public String getOrderStatus(Order order);
	
	/**
	 * Get the current cost of an order
	 * @param order order to query
	 * @return the cost of the order
	 */
	public Number getOrderCost(Order order);
	
	//Postcodes
	
	/**
	 * Get all postcodes in the system
	 * @return a list of all postcodes
	 */
	public List<Postcode> getPostcodes();
	
	/**
	 * Add a new postcode to the system
	 * @param code postcode string representation
	 * @param distance distance from the restaurant
	 */
	public void addPostcode(String code,Number distance);
	
	/**
	 * Remove a postcode from the system
	 * @param postcode postcode to remove
	 * @throws UnableToDeleteException if unable to remove the postcode
	 */
	public void removePostcode(Postcode postcode) throws UnableToDeleteException;
	
	//Users
	
	/**
	 * Get all users in the system
	 * @return list of all users
	 */
	public List<User> getUsers();
	
	/**
	 * Remove a user from the system
	 * @param user to remove
	 * @throws UnableToDeleteException if unable to remove the user
	 */
	public void removeUser(User user) throws UnableToDeleteException;

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
	
	//Exceptions
	
	//Unable to delete exception
	
	/**
	 * Throw an unable to delete exception with a given message if a deletion cannot occur
	 *
	 */
	public class UnableToDeleteException extends Exception {
	    public UnableToDeleteException(String message) {
	        super(message);
	    }
	}
		
}

