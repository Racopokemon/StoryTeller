package rk.ramin.teller;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;

/**
 * 
 * @author Der Guru
 * Perfect OO as always, but ...
 * An arrow represents a single arrow to another page, this arrow can be hovered with the help of "hasThisId",
 * or all arrows representing customLinks, which cannot be hovered (but selected), as their hasThisId always returns false
 */
public class Arrow extends Group {
	public static final byte NORMAL = 0;
	public static final byte SELECTED = 1;
	public static final byte HOVER = 2;
	
	private Page identification;
	private boolean isCustomLink;
	
	public static Arrow getArrow(double sx, double sy, double ex, double ey, byte type, Page id, boolean customAnswer) {
		try {
			return new Arrow(sx, sy, ex, ey, type, id, customAnswer);
		} catch (Exception e) {
			return null;
		}
	}
	
	public Arrow(double sx, double sy, double ex, double ey, byte type, Page id, boolean customAnswer) throws RuntimeException {
		isCustomLink = false;
		setMouseTransparent(true);
		identification = id;
		Line l = new Line();
		double wdt = ex-sx, hgh = ey-sy;
		double len = Math.sqrt(wdt*wdt+hgh*hgh);
		if (len<PageHolder.RADIUS*2) {throw new RuntimeException("Distance too small for an arrow. ");}
		double rx = wdt/len*PageHolder.RADIUS, ry = hgh/len*PageHolder.RADIUS;
		sx+=rx;
		sy+=ry;
		ex-=rx;
		ey-=ry;
		l.setStartX(sx);
		l.setStartY(sy);
		l.setEndX(ex);
		l.setEndY(ey);
		if (customAnswer) {
			setCustomAnswerDashes(l);
		}
		//formatArrows(l,sel);
		this.getChildren().add(l);
		double dir = Math.atan2(-wdt, -hgh);
		Polyline pl = new Polyline(
				ex+Math.sin(dir+0.5)*6,
				ey+Math.cos(dir+0.5)*6,
				ex, ey,
				ex+Math.sin(dir-0.5)*6,
				ey+Math.cos(dir-0.5)*6
				);
		//formatArrows(pl,sel);
		this.getChildren().add(pl);
		setType(type);
	}
	
	private void setCustomAnswerDashes(Shape s) {
		s.getStrokeDashArray().addAll(Double.valueOf(4));
	}
	
	public Arrow(double sx, double sy, byte type, int answerLinks, int customAnswerLinks) {
		//Not well intelligible, but ... i mean its graphics code. Draws one line with a circle, if there are answerLinks, and if there are customAnswerLinks.
		//If there are both, it draws two ones, and else it just draws one. 
		isCustomLink = true;
		setMouseTransparent(true);
		boolean twoOnes = answerLinks > 0 && customAnswerLinks > 0;
		int count = twoOnes ? 2 : 1;
		double step = 0.3, start = twoOnes ? -step*0.5 : 0;
		boolean drawCustomAnswerNow = twoOnes ? false : answerLinks <= 0;
		for (int i = 0; i < count; i++) {
			double angle = start+step*i;
			double x = Math.sin(angle), y = Math.cos(angle);
			Line l = new Line(sx+x*PageHolder.RADIUS, sy+y*PageHolder.RADIUS, sx+x*PageHolder.RADIUS*2, sy+y*PageHolder.RADIUS*2);
			Circle c = new Circle(sx+x*(PageHolder.RADIUS*2+2.5), sy+y*(PageHolder.RADIUS*2+2.5), 2.5);
			c.setFill(null);
			if (drawCustomAnswerNow) {
				setCustomAnswerDashes(l);
			}
			drawCustomAnswerNow = true;
			this.getChildren().addAll(l, c);
		}
		setType(type);
	}
	
	public void setType(byte type) {
		for (Node p : getChildren()) {
			((Shape)p).setStroke(Color.rgb(40, 40, 40, type == NORMAL ? 0.4 : 1));
			((Shape)p).setStrokeWidth(type == HOVER ? 3 : type == NORMAL ? 1 : 1.3);	
		}
	}
	
	/**
	 * Theo one arrow that represents 
	 */
	public boolean hasThisId(Page p) {
		return !isCustomLink && (p == identification);
	}
}
