package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Drone extends Model implements Runnable, Serializable {

	private Number speed;
	private String status;
	private boolean automated = true;
	public boolean fired = false;
	static Object lock = new Object();
	static Map<Ingredient, Number> stock = new ConcurrentHashMap<Ingredient, Number>();
	public Drone () {
		Thread t1 = new Thread(this);
		t1.start();
	}
	
	@Override
	public void run() {
		Ingredient ingr = null;
		Order order = null;
		boolean gathering, delivering;
		while (true && !fired) {
			gathering = false;
			delivering = false;
			synchronized (lock) {
				
				// checks if there are any available orders to be delivered
				for (Order o : Staff.waitingOrders) {
					if (o.getStatus().equals("READY")) {
						order = o; 
						order.setStatus("DELIVERING");
						delivering = true;
					}
				}
				
				// checks if ingredients should  be restocked
				if (automated == true) 
					if (delivering == false)
						for (Ingredient i : stock.keySet()) {
							if (stock.get(i).doubleValue() < i.getThreshold().doubleValue()) {
								ingr = i;
								gathering = true;
								break;
							}
						}
			}
			
			//if it should gather it goes to sleep and changes the stock amount
			if (gathering == true) {
				status = "GATHERING INGREDIENTS";
				try {
					Thread.sleep((long) (ingr.getSupplier().getDistance().doubleValue() / speed.doubleValue()) * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				stock.put(ingr, ingr.getThreshold().doubleValue() + ingr.getRestocking().doubleValue());
				gathering = false;
				status = "IDLE";
			}
			
			//if it should deliver, it goes to sleep and then delivers
			if (delivering == true) {
				status = "DELIVERING";
				
				try {
					Thread.sleep((long) (order.getUser().getPostcode().getDistance().doubleValue() 
										/ speed.doubleValue()) * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				status = "IDLE";
				delivering = false;
				order.setStatus("DELIVERED");
			}
		}
	}

	@Override
	public String getName () {
		return name;
	}

	public void setAutomated (boolean x) {
		automated = x;
	}
	
	public void setSpeed (Number speed) {
		this.speed = speed;
	}

	public Number getSpeed() {
		return speed;
	}

	public String getStatus() {
		return status;
	}
	public static void removeItem (Ingredient m, Number nr) {
		synchronized (lock) {
			stock.put(m, stock.get(m).doubleValue() - nr.doubleValue());
		}
	}
	
	public static Map<Ingredient, Number> getStock () {
		synchronized (lock) {
			return stock;
		}
	}

	public static void setStock(Ingredient ingredient, Number x) {
		synchronized (lock) {
			stock.put(ingredient, x);
		}
	}

	public void setStatus(String string) {
		status = string;
	}
}
