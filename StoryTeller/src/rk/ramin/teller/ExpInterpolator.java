package rk.ramin.teller;

import javafx.animation.Interpolator;

public class ExpInterpolator extends Interpolator {
	
	private double base, substract, divide, section;
	private boolean backwards;
	
	public ExpInterpolator(double base, double section, boolean inversed) {
		//if (section <= 0 || base <= 1) {
			//throw new RuntimeException("Arguments not in range");
		//}
		backwards = inversed;
		base = 1./base;
		this.base = base;
		this.section = section;
		substract = Math.pow(base, section);
		divide = 1-substract;
	}
	
	@Override
	protected double curve(double x) {
		//double ret = 1+(Math.pow(base, x*section)-base)/divide; Wrong but looks also fancy anyhow
		//divide = base-substract; Also use this
		if (backwards) {
			return 1-((Math.pow(base, (1-x)*section)-1)/-divide);
		} else {
			return (Math.pow(base, x*section)-1)/-divide;
		}
	}

}
