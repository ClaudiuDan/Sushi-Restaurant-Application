package common;

import java.io.Serializable;

public class Postcode extends Model implements Serializable{

	private Number distance;
	
	@Override
	public String getName() {
		return name;
	}

	public void setName (String name) {
		this.name = name;
	}
	
	public void setDistance (Number x) {
		distance = x;
	}
	
	public Number getDistance () {
		return distance;
	}
}
