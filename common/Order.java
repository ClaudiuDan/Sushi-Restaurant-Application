package common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Order extends Model implements Serializable{

	private String status;
	public Double cost = new Double(0);
	private User user;
	private Map<Dish, Number> orderDetails = new ConcurrentHashMap<Dish, Number>();
	public Order (User user) {
		this.user = user;
	}
	
	@Override
	public String getName () {
		return name;
	}

	public void setUser (User user) {
		this.user = user;
	}
	
	public String getStatus () {
		return status;
	}
	
	public Double getCost () {
		return cost;
	}
	public void setCost () {
		for (Dish d : orderDetails.keySet()) {
			cost += d.getPrice().doubleValue() * orderDetails.get(d).doubleValue();
		}
	}
	public void cancelOrder () {
		status = "CANCELLED";
	}
	
	public Map<Dish, Number> getDetails () {
		return orderDetails;
	}
	
	public void updateDetails (Map<Dish, Number> toUpdate) {
		for (Dish d : toUpdate.keySet())
			orderDetails.put(d, toUpdate.get(d));
	}
	
	public User getUser () {
		return user;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
}
