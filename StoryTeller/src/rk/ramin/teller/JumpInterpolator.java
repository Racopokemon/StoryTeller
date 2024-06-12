package rk.ramin.teller;

import javafx.animation.Interpolator;;

public class JumpInterpolator extends Interpolator {
	
	private double base, factor, size; 
	
	public JumpInterpolator(double base, double size) {
		if (base <= 1 || size <= 0) {
			throw new RuntimeException("Arguments not in range");
		}
		this.base = base;
		this.size = size*2;
		factor = Math.pow(size, base);
	}
	
	@Override
	protected double curve(double t) {
		t = Math.abs(t-0.5)*size;
		return (factor-Math.pow(t, base))/factor;
	}

}
