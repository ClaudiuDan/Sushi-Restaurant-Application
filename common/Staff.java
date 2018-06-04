package common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Staff extends Model implements Runnable{
	
	static Map<Dish, Number> stock = new ConcurrentHashMap<Dish, Number>();
	static CopyOnWriteArrayList<Order> waitingOrders = new CopyOnWriteArrayList<Order>();
	static Object lock = new Object();
	private String status;
	public Staff () {
		Thread t1 = new Thread(this);
		t1.start();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	private boolean automated = true;
	private boolean isWorking = false;
	public boolean fired = false;
	private Random r = new Random();
	@Override
	public void run() {
		Dish restocked = null;
		while (true && !fired) {
			synchronized (lock) {
				Map<Dish, Number> temp;
				
				//looks if there are any orders that can be worked on with the current stock
				for (Order order : waitingOrders) {
					boolean finished = true;
					temp = order.getDetails();
					for (Dish d : temp.keySet()) {
						if (stock.get(d).doubleValue() > temp.get(d).doubleValue()) {
							stock.put(d, stock.get(d).doubleValue() - temp.get(d).doubleValue()); 
							temp.put(d, 0);
						}
						else {
							temp.put(d, temp.get(d).doubleValue() - stock.get(d).doubleValue()); 
							stock.put(d, 0);
							finished = false;
						}
					}
					if (finished == true && order.getStatus().equals("PREPARING")) 
						order.setStatus("READY");
					if (order.getStatus().equals("DELIVERED"))
						waitingOrders.remove(order);
				}
				
				//checks if there are any dishes to restock
				if (automated == true)
					for (Dish d : stock.keySet()) {
						
						if (d.isRestocking == false && stock.get(d).doubleValue() < d.getThreshold().doubleValue()) {
							boolean ok = true;
							
							//checks if ingredients are available
							for (Ingredient i : d.getRecipe().keySet()) {
								if (d.getRecipe().get(i).doubleValue() > Drone.stock.get(i).doubleValue()) {
									ok = false;
									break;
								}}
							if (ok == false)
								break;
							for (Ingredient i : d.getRecipe().keySet()) {
								Drone.stock.put(i, Drone.stock.get(i).doubleValue() - d.getRecipe().get(i).doubleValue());
							}
							d.isRestocking = true;
							d.prepared = true;
							isWorking = true;
							setStatus("WORKING");
							restocked = d;
							break;
						}
						if (d.isRestocking == true && stock.get(d).doubleValue() < d.getThreshold().doubleValue() +
						  d.getRestocking().doubleValue() && !(d.prepared == true && stock.get(d).doubleValue() == d.getThreshold().doubleValue() + 
						  d.getRestocking().doubleValue() - 1))
						{
							boolean ok = true;
							for (Ingredient i : d.getRecipe().keySet())
								if (d.getRecipe().get(i).doubleValue() > Drone.stock.get(i).doubleValue()) {
									ok = false;
									break;
								}
							if (ok == false)
								break;
							
							for (Ingredient i : d.getRecipe().keySet()) {
								Drone.stock.put(i, Drone.stock.get(i).doubleValue() - d.getRecipe().get(i).doubleValue());
							}
							isWorking = true;
							setStatus("WORKING"); 
							restocked = d;
							break;
						}
						d.prepared = false; 
						d.isRestocking = false;
						
					}
			}
			
			//if is it supposed to restock, goes to sleep, then changes the stock
			if (isWorking == true) {
				try {
					Thread.sleep((20 + r.nextInt(40)) * 1000);
					setStatus("IDLE"); 
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				stock.put(restocked, stock.get(restocked).doubleValue() + 1);
				isWorking = false;
			}
		}
		
	}
	
	public static void removeItem (Dish m, Number nr) {
		synchronized (lock) {
			stock.put(m, stock.get(m).doubleValue() - nr.doubleValue());
		}
	}
	
	public void setAutomated (boolean x) {
		automated = x;
	}
	
	public static void setStock (Dish d, Number x) {
		synchronized (lock) {
			stock.put(d, x);
		}
	}
	
	public static Map<Dish, Number> getStock () {
		synchronized (lock) { 
			return stock;
		}
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
