package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Dish extends Model implements Serializable{
	public boolean isRestocking = false;
	private String description;
	private Map<Ingredient, Number> recipe = new ConcurrentHashMap<Ingredient, Number>();
	private Number threshold, restocking;
	private Number price;
	public boolean prepared = false;
	@Override
	public String getName() {
		return name;
	}

	
	public Map<Ingredient, Number> getRecipe () {
		return recipe;
	}
	
	public String getDescription () {
		return description;
	}
	
	public Number getPrice () {
		return price;
	}
	
	public Number getThreshold () {
		return threshold;
	}
	
	public void setThreshold (Number threshold) {
		this.threshold = threshold;
	}
	
	public void setPrice (Number price) {
		this.price = price;
	}
	
	public void setDescription (String descr) {
		description = descr;
	}
	
	public void addIngredient (Ingredient i, Number quantity) {
		recipe.put(i, quantity);
	}
	
	public Number getRestocking () {
		return restocking;
	}
	
	public void setRestocking (Number restocking) {
		this.restocking = restocking;
	}
	
	public void removeIngredient (Ingredient i) {
		recipe.remove(i);
	}
	
	public void setRecipe (Map<Ingredient, Number> recipe) {
		this.recipe = recipe;
	}
}
