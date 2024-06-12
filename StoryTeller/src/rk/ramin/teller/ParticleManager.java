
package rk.ramin.teller;

import java.util.ArrayList;
import java.util.Random;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

public class ParticleManager {

	private static final double BOX_SIZE = 100;
	private static final double TICK_TIME = 0.1;
	private static final int COLOR_FADE_SUBDIVISIONS = 6; //Turns out that an update every 0.1 seconds is enough for a fluent transition at flickering and blur, but not for the change of colors.
															//In combination with 0.1 tick time this now makes 60 frames per second. Even 40 frames still seem stuttering a bit.
	
	private StackPane pane;
	private ArrayList<Particle> parts;
		
	private EventHandler<ActionEvent> nextCaller;
	
	private Timeline ani = new Timeline(20);
	private boolean running = false;
	private Random rand = new Random();
	private ArrayList<SpawnBox> spawners;
	
	private Color color = Color.rgb(255, 0, 0), colorStart, colorGoal, backColorStart, backColorGoal, backColor = Color.BLACK;
	private int colorVal = 255, backColorVal = 0;
	private int sizeMin=10, sizeRand;
	private int spawn = 10, lifetime = 150;
	private int type = 0;
	private double fadingPercentage=0.5, transparency=0.1, flickering, flickeringGoal, flickeringStart;
	private double blurX, blurY, blurXGoal, blurYGoal, blurXStart, blurYStart;
	private double speedMin = 1, speedRand;
	private double angleMin, angleRand;
	private int transSteps = 0;
	private Pane background;
	
	private Transition blurTransition = new Transition(), 
			colorTransition = new Transition(), 
			flickerTransition = new Transition(),
			backColorTransition = new Transition();
	
	public ParticleManager(double w, double h, Pane backgroundPane, Color currentBackgroundColor) {
		background = backgroundPane;
		backColor = currentBackgroundColor;
		backColorVal = SaveStat.colorToInt(currentBackgroundColor);
		initSpawner(w, h);
		parts = new ArrayList<>();
		nextCaller = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent ae) {
				next();
			}
		};
		ani.setOnFinished(nextCaller);
	}
	
	public StackPane getContent() {
		pane = new StackPane();
		//pane.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
		return pane;
	}
	
	/**
	 * Make sure any changes you make here are also done at the SaveStat, where all names and limits to these values are defined. 
	 */
	public void updateValues(SaveStat s) {
		
		double flickeringGoalOld = flickeringGoal;
		int colorValOld = colorVal, backColorValOld = backColorVal;
		double blurXGoalOld = blurXGoal, blurYGoalOld = blurYGoal;
		
		sizeMin = s.getCurrent("_pSizeMin");
		sizeRand = s.getCurrent("_pSizeMax")-sizeMin;
		lifetime = s.getCurrent("_pLifetime");
		spawn = s.getCurrent("_pSpawn");
		type = s.getCurrent("_pType");
		transparency = s.getCurrent("_pTransparency")*0.01;
		fadingPercentage = s.getCurrent("_pFading")*0.01;
		flickeringGoal = s.getCurrent("_pFlickering")*0.01*0.03; //0.025
		blurXGoal = s.getCurrent("_pBlurX");
		blurYGoal = s.getCurrent("_pBlurY");
		speedMin = s.getCurrent("_pSpeedMin")*0.02;
		speedRand = s.getCurrent("_pSpeedMax")*0.02-speedMin;
		angleRand = s.getCurrent("_pAngleVar")/180.*Math.PI;
		angleMin = s.getCurrent("_pAngle")/180.*Math.PI-angleRand*0.5;
		colorVal = s.getCurrent("_pColor");
		backColorVal = s.getCurrent("_backColor");
		transSteps = (int) Math.round((((double)s.getCurrent("_pTransitionTime"))*0.1)/TICK_TIME); //At the moment, this is just an 1 to 1 scaling with 1 ticks per second.

		
		{
			if (flickeringGoalOld != flickeringGoal) {
				if (running && transSteps > 1) {
					flickeringStart = flickering;
					flickerTransition.start(transSteps);
				} else {
					flickering = flickeringGoal;
					flickerTransition.reset();
				}
			}
			if (blurXGoal != blurXGoalOld || blurYGoal != blurYGoalOld) {
				if (running && transSteps > 1) {
					blurXStart = blurX;
					blurYStart = blurY;
					blurTransition.start(transSteps);
				} else {
					blurX = blurXGoal;
					blurY = blurYGoal;
					blurTransition.reset();
					updateBlur();
				}
			}
			if (colorVal != colorValOld) {
				if (running && transSteps > 1) {
					if (colorTransition.isActive()) {
						colorStart = colorStart.interpolate(colorGoal, colorTransition.getTransitionValueAtSubstep(0, 1));
						//See the explanation for the backColor below
					} else {
						colorStart = color;
					}
					colorGoal = SaveStat.intToColor(colorVal);
					colorTransition.start(transSteps);
				} else {
					color = colorGoal = SaveStat.intToColor(colorVal);
					colorTransition.reset();
				}
			}
			if (backColorValOld != backColorVal) {
				if (transSteps > 1) {
					if (backColorTransition.isActive()) {
						backColorStart = backColorStart.interpolate(backColorGoal, backColorTransition.getTransitionValueAtSubstep(0, 1));
						//Manually calculating the color that is reached at the end of the transition, 
						//the updateValues call can arrive at every time within the fading animation.
						//With the substep call we can get the current transition value without changing the transition.  
					} else {
						backColorStart = backColor;
					}
					backColorGoal = SaveStat.intToColor(backColorVal);
					backColorTransition.start(transSteps);
				} else {
					backColor = backColorGoal = SaveStat.intToColor(backColorVal);
					updateBackground();
				}
			}
		}
		
		if (!running && (spawn > 0 || backColorTransition.isActive())) {
			colorTransition.reset();
			blurTransition.reset();
			flickerTransition.reset();
			running = true;
			next();
		}
	}
	
	public void destroy() {
		running = false;
		ani.stop();
		ani.setOnFinished(null);
	}
	
	private void next() {
		if (!running) {
			return;
		}
		
		ani.stop();
		ani.getKeyFrames().clear();
		
		if (flickerTransition.isActive()) {
			double factor = flickerTransition.update();
			flickering = flickeringGoal*factor+flickeringStart*(1-factor);
		}
		if (blurTransition.isActive()) {
			double factor = blurTransition.update();
			blurX = blurXGoal*factor+blurXStart*(1-factor);
			blurY = blurYGoal*factor+blurYStart*(1-factor);
			updateBlur();
		}
		if (colorTransition.isActive()) {
			for (int i = 1; i <= COLOR_FADE_SUBDIVISIONS; i++) {
				ani.getKeyFrames().add(
						new KeyFrame(Duration.seconds((TICK_TIME/COLOR_FADE_SUBDIVISIONS)*i), 
								new ColorChangeHandler(
										colorStart.interpolate(
												colorGoal, colorTransition.getTransitionValueAtSubstep(i, COLOR_FADE_SUBDIVISIONS)))));
			}
			
			double factor = colorTransition.update();
			//color = colorStart.interpolate(colorGoal, factor); done in the last key frame created above.
		}
		boolean backColorTransitionActive;
		if (backColorTransition.isActive()) {
			backColorTransitionActive = true;
			for (int i = 1; i <= COLOR_FADE_SUBDIVISIONS; i++) {
				double transitionFactor = backColorTransition.getTransitionValueAtSubstep(i, COLOR_FADE_SUBDIVISIONS);
				Color changeTo = backColorStart.interpolate(backColorGoal, transitionFactor);
				ani.getKeyFrames().add(
						new KeyFrame(Duration.seconds((TICK_TIME/COLOR_FADE_SUBDIVISIONS)*i), 
								new EventHandler<ActionEvent>() {
									@Override
									public void handle(ActionEvent event) {
										backColor = changeTo;
										updateBackground();
									}
								}));
			}
			backColorTransition.update();
		} else {
			backColorTransitionActive = false;
		}
		
		ObservableList<Node> l = pane.getChildren();
		for (int i = 0; i < parts.size(); ) {
			Particle p = parts.get(i);
			if (p.shouldDelete()) {
				parts.remove(i);
				l.remove(p.getParticle());
			} else {
				i++;
			}
		}

		for (int i = 0; i < spawn; i++) {
			Particle p = new Particle();
			parts.add(p);
			l.add(p.getParticle());
		}
		if (parts.isEmpty() && !backColorTransitionActive) {
			if (spawn <= 0) {
				running = false;
			}
		} else {
			ArrayList<KeyValue> v = new ArrayList<>();
			for (int i = 0; i < parts.size(); i++) {
				parts.get(i).addNewValues(v);
			}
			KeyFrame f = new KeyFrame(Duration.seconds(TICK_TIME), v.toArray(new KeyValue[v.size()]));
			ani.getKeyFrames().add(f);
			ani.playFromStart();
		}
	}
	
	private void initSpawner(double w, double h) {
		spawners = new ArrayList<>();
		w/=2.;
		h/=2.;
		//spawners.add(new SpawnBox(-w, -h, w*2, h*2, 1.3));
		/*
		spawners.add(new SpawnBox(-w, h, w*2, BOX_SIZE, 0.55));
		spawners.add(new SpawnBox(-w, -h-BOX_SIZE, w*2, BOX_SIZE, 0.55));
		spawners.add(new SpawnBox(w, -h, BOX_SIZE, h*2, 0.55));
		spawners.add(new SpawnBox(-w-BOX_SIZE, -h, BOX_SIZE, h*2, 0.55));
		
		spawners.add(new SpawnBox(w, h, BOX_SIZE, BOX_SIZE, 0.33));
		spawners.add(new SpawnBox(-w-BOX_SIZE, h, BOX_SIZE, BOX_SIZE, 0.33));
		spawners.add(new SpawnBox(w, -h-BOX_SIZE, BOX_SIZE, BOX_SIZE, 0.33));
		spawners.add(new SpawnBox(-w-BOX_SIZE, -h-BOX_SIZE, BOX_SIZE, BOX_SIZE, 0.33));
		*/
		spawners.add(new SpawnBox(-w, -h, w*2, h/3., 0.5));
		spawners.add(new SpawnBox(-w, -h/3.*2, w*2, h/3., 0.8));
		spawners.add(new SpawnBox(-w, -h/3., w*2, h/3.*2., 1.0));
		spawners.add(new SpawnBox(-w, h/3., w*2, h/3., 0.8));
		spawners.add(new SpawnBox(-w, h/3.*2, w*2, h/3., 0.5));
		double v = 0, pos = 0;
		for (SpawnBox s : spawners) {
			v += s.getValue();
		}
		for (int i = 0; i < spawners.size(); i++) {
			SpawnBox s = spawners.get(i);
			pos += s.getValue()/v;
			s.setStart(i == spawners.size()-1 ? 1 : pos);
		}
	}
	
	private double[] getStartingPos() {
		double p = rand.nextDouble();
		int i = 0;
		double[] ret;
		while ((ret = spawners.get(i).spawnParticle(p)) == null) {
			i++;
		}
		return ret;
	}
	
	private void updateBlur() {
		pane.setEffect(new BoxBlur(blurX, blurY, 1));
	}
	
	private void updateBackground() {
		background.setBackground(new Background(new BackgroundFill(backColor, null, null)));
	}

	private class Particle {
		private double x, y, dir, speed, velX, velY, op, maxOp;
		private int lifetime, fadeIn, fade;
		private ObjectProperty<Paint> paint;
		
		private Node shape;
		
		public Particle() {
			double[] d = getStartingPos();
			x = d[0];
			y = d[1];
			
			dir = angleMin + angleRand*rand.nextDouble();
			speed = speedMin + speedRand*rand.nextDouble();
			
			velX = Math.sin(dir)*speed;
			velY = -Math.cos(dir)*speed;
			
			op = 0;
			maxOp = transparency;
			lifetime = ParticleManager.this.lifetime;
			fade = (int) (lifetime*0.5*fadingPercentage);
			fadeIn = lifetime-fade;
		}
		
		private void createShape() {
			double size = sizeMin + rand.nextDouble()*sizeRand;
			if (type == 2) {
				double[] v = new double[12];
				double r = Math.PI/3;
				for (int i = 0; i < 6; i++) {
					v[i*2] = Math.cos(r*i)*size;
					v[i*2+1] = Math.sin(r*i)*size;
				}
				Polygon p = new Polygon(v);
				paint = p.fillProperty();
				shape = p;
			} else if (type == 1) {
				Circle c = new Circle(0, 0, size);
				paint = c.strokeProperty();
				c.setFill(null);
				c.setStrokeWidth(6);
				shape = c;
			} else {
				Circle c = new Circle(0, 0, size);
				shape = c;
				paint = c.fillProperty();
			}
			shape.setTranslateX(x);
			shape.setTranslateY(y);
		}
		
		public Node getParticle() {
			if (shape == null) {
				createShape();
			}
			shape.setOpacity(op);
			//shape.
			return shape;
		}
		
		private void tick() {
			x+=velX;
			y+=velY;
			
			/*
			if (lifetime < 50) {
				op = (lifetime/50.)*0.1;
			} else if (lifetime > 50) {
				op = ((100-lifetime)/50.)*0.1;
			} else {
				op = Math.random() < 0.01 ? 0.5 : 0.1;
			}
			op +=Math.random()*0.02-0.01;*/
			
			if (lifetime > fadeIn) {
				op = (1-((lifetime-fadeIn)/(double)fade))*maxOp;
			} else if (lifetime < fade) {
				op = (lifetime/(double)fade)*maxOp;
			} else {
				op = maxOp;
			}
			op +=Math.random()*flickering-(flickering*0.5);
			
			updateColor();
			
			//op = ((lifetime)/100.)*0.1;
			lifetime--;
		}
		
		public void updateColor() {
			paint.set(color);
		}
		
		/**
		 * This is the public "tick" method.
		 */
		public void addNewValues(ArrayList<KeyValue> v) {
			tick();
			v.add(new KeyValue(shape.translateXProperty(), x));
			v.add(new KeyValue(shape.translateYProperty(), y));
			v.add(new KeyValue(shape.opacityProperty(), op));
		}
		
		/**
		 * True, if this particle can be removed.
		 * Call this before updating.
		 */
		public boolean shouldDelete() {
			return lifetime <= 0;
		}
	}

	private class SpawnBox {
		private double x, y, w, h, d, s;
		public SpawnBox(double x, double y, double width, double height, double density) {
			w = width;
			h = height;
			d = density;
			this.x = x;
			this.y = y;
		}
		public double getValue() {
			return w*h*d;
		}
		public void setStart(double start) {
			s = start;
		}
		public double[] spawnParticle(double val) {
			if (val <= s) {
				return new double[] {rand.nextDouble()*w+x, rand.nextDouble()*h+y};
			} else {
				return null;
			}
		}
	}
	
	private class Transition {
		private int goal, current;
		
		public boolean isActive() {
			return goal > 0;
		}
		
		/**
		 * Update if active() is true.
		 * Returns the transition value (0 to 1, 1 means that the transition will be not longer active with the next update)
		 */
		public double update() {
				if (++current >= goal) {
					goal = 0;
					return 1;
				}
				return (double)current / goal;
		}
		
		public double getTransitionValueAtSubstep(int step, int stepCount) {
			return ((current+((double)step/stepCount))/ goal);
		}
		
		public void reset() {
			goal = 0;
			current = 0;
		}
		
		public void start(int steps) {
			goal = steps;
			current = 0;
		}
	}
	
	private class ColorChangeHandler implements EventHandler<ActionEvent> {

		public ColorChangeHandler(Color c) {
			newColor = c;
		}
		
		private Color newColor;
		
		@Override
		public void handle(ActionEvent event) {
			color = newColor;
			for (Particle p : parts) {
				p.updateColor();
			}
		}
		
	}
}
