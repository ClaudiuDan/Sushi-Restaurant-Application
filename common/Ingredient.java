package common;

import java.io.Serializable;
import java.util.Map;

public class Ingredient extends Model implements Serializable {

	private String unit;
	private Supplier supplier;
	private Number restocking, threshold;
	
	@Override
	public String getName() {
		return name;
	}
	public String getUnit () {
		return unit;
	}
	
	public void setUnit (String unit) {
		this.unit = unit;
	}
	
	public Supplier getSupplier () {
		return supplier;
	}
	
	public void setSupplier (Supplier supplier2) {
		this.supplier = supplier2;
	}
	
	public Number getThreshold () {
		return threshold;
	}
	
	public void setThreshold (Number threshold) {
		this.threshold = threshold;
	}
	
	public Number getRestocking () {
		return restocking;
	}
	
	public void setRestocking (Number restockAmount) {
		this.restocking = restockAmount;
	}
}
