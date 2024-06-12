package rk.ramin.teller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.paint.Color;

public class SaveStat implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	private Map<String, Integer> init = new HashMap<>(), current = new HashMap<>();
	private Page initPage, currentPage;
	/** Only set in saveStats, that are actually saved as file for really games, in other cases this is null and the currentPage is used to perform a start */
	//private Object[] betterSave;
	//How saves work now: There are four variables added, beginning with a 0 (the user cant add such a name, so its not used yet) and in those ones we save the game
	
	private static final ArrayList<EssentialVar> essentialVars = new ArrayList<>();
	static {
		essentialVars.add(new EssentialVar("_pSizeMin", 1, 50, 8));
		essentialVars.add(new EssentialVar("_pSizeMax", 1, 50, 15));
		essentialVars.add(new EssentialVar("_pLifetime", 10, 150, 50));
		essentialVars.add(new EssentialVar("_pSpawn", 0, 10, 0));
		essentialVars.add(new EssentialVar("_pType", 0, 2, 2));
		essentialVars.add(new EssentialVar("_pTransparency", 1, 100, 10));
		essentialVars.add(new EssentialVar("_pFading", 1, 100, 100));
		essentialVars.add(new EssentialVar("_pFlickering", 0, 100, 30));
		essentialVars.add(new EssentialVar("_pBlurX", 0, 50, 3));
		essentialVars.add(new EssentialVar("_pBlurY", 0, 50, 1));
		essentialVars.add(new EssentialVar("_pSpeedMin", -100, 100, -1));
		essentialVars.add(new EssentialVar("_pSpeedMax", -100, 100, 5));
		essentialVars.add(new EssentialVar("_pAngle", -180, 180, 0));
		essentialVars.add(new EssentialVar("_pAngleVar", 0, 360, 360));
		essentialVars.add(new EssentialVar("_pTransitionTime", 0, 200, 50)); //10ths of seconds. I mean, a smaller scale does not matter anymore.
		essentialVars.add(new EssentialVar("_pColor", Integer.MIN_VALUE, Integer.MAX_VALUE, 255));
		essentialVars.add(new EssentialVar("_backColor", Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
		essentialVars.add(new EssentialVar("_textColor", Integer.MIN_VALUE, Integer.MAX_VALUE, 255+255*256+256*256*255));
		essentialVars.add(new EssentialVar("_textFont", 0, 255, 0));
		essentialVars.add(new EssentialVar("_textSize", 8, 100, 24));
	}
	
	public SaveStat(String name) {
		this.name = name;
		for (EssentialVar v : essentialVars) {
			init.put(v.getName(), v.getDefaultValue());
			current.put(v.getName(), v.getDefaultValue());
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String n) {
		name = n;
	}
	
	public SaveStat clone() {
		SaveStat c = new SaveStat(this.name);
		c.init = cloneMap(this.init);
		c.current = cloneMap(this.current);
		c.initPage = initPage;
		c.currentPage = currentPage;
		return c;
	}
	
	private Map<String, Integer> cloneMap(Map<String, Integer> m) {
		HashMap<String, Integer> h = new HashMap<String, Integer>();
		h.putAll(m);
		return h;
	}
	
	/**
	 * You are free to look into the maps, but please do not edit anything - you might change values that are limited.
	 * Instead, you should to use the shortcuts I provide for this. 
	 */
	public Map<String, Integer> getInit() {
		return init;
	}
	/**
	 * You are free to look into the maps, but please do not edit anything - you might change values that are limited.
	 * Instead, you should to use the shortcuts I provide for this. 
	 */
	public Map<String, Integer> getCurrent() {
		return current;
	}
	
	public void addVar(String varName) {
		init.put(varName, 0);
		current.put(varName, 0);
	}
	
	public void changeVarInitValue(String varName, int initValue) {
		EssentialVar v = getEssentialVarWithName(varName);
		if (v == null) {
			init.put(varName, initValue);
		} else {
			init.put(varName, v.limitToBounds(initValue));
		}
	}
	
	public void changeVarCurrentValue(String varName, int currentValue) {
		EssentialVar v = getEssentialVarWithName(varName);
		if (v == null) {
			current.put(varName, currentValue);
		} else {
			current.put(varName, v.limitToBounds(currentValue));
		}
	}
	
	public void removeVar(String name) {
		init.remove(name);
		current.remove(name);
	}
	
	public int getCurrent(String name) {
		return current.get(name);
	}
	
	public int getInit(String name) {
		return init.get(name);
	}
	
	public void reset(boolean toCurrent) {
		Map<String, Integer> from, to;
		if (toCurrent) {
			from = current;
			to = init;
			initPage = currentPage;
		} else {
			from = init;
			to = current;
			currentPage = initPage;
		}
		to.clear();
		to.putAll(from);
		
	}
	
	public void removeAllReferencesTo(Page p) {
		if (initPage == p) {
			initPage = null;
		}
		if (currentPage == p) {
			currentPage = null;
		}
	}
	
	public Page getInitPage() {
		return initPage;
	}
	
	public Page getCurrentPage() {
		return currentPage;
	}
	
	public void setInitPage(Page p) {
		initPage = p;
	}
	
	public void setCurrentPage(Page p) {
		currentPage = p;
	}
	
	/**
	 * As the vars are not case sensitive (just to keep things easier for the authors), 
	 * we first need to find out, whether the given var name is really a var name and how its true case is written.
	 * If there exists a fitting var, we return its name in right case, else we return null;
	 */
	public String getVarName(String s) {
		for (String st : current.keySet()) {
			if (st.equalsIgnoreCase(s)) {
				return st;
			}
		}
		return null;
	}
	
	public void makeCompatible() {
		//We cant give a version number, because we dont't know it if we only loaded the save file. 
		for (EssentialVar v : essentialVars) {
			String n, m = v.getName();
			if ((n = getVarName(m)) == null) {
				init.put(m, v.getDefaultValue());
				current.put(m, v.getDefaultValue());
			} else {
				if (!n.equals(m)) {
					//I assume this was to correct different cases within the names (_PsPaWn is also recognized as _pSpawn)
					int i = init.get(n), c = current.get(n);
					init.remove(n);
					current.remove(n);
					init.put(m, i);
					current.put(m, c);
				}
				init.put(m, v.limitToBounds(init.get(m)));
				current.put(m, v.limitToBounds(current.get(m)));
			}
		}
		/**
		if (version < ?) { //oldest, makes it compatible to the next version number.
			
		}
		if (version < ?) { //then this follows
			
		}
		//and so on
		 */
	}
	
	public static boolean mayDelete(String name) {
		return getEssentialVarWithName(name) == null;
	}
	
	/**
	 * If there is no essential var with this name (so it might be a simple user defined one)
	 * null will be returned;
	 */
	private static EssentialVar getEssentialVarWithName(String s) {
		for (EssentialVar v : essentialVars) {
			if (v.isRightName(s)) {
				return v;
			}
		}
		return null;
	}
	
	private static class EssentialVar {
		private int min, max, def;
		private String name;
		public EssentialVar(String n, int mn, int mx, int defaultValue) {
			name = n;
			min = mn;
			max = mx;
			def = defaultValue;
		}
		public EssentialVar(String n, int mn, int mx) {
			this(n, mn, mx, 0);
			if (min == Integer.MIN_VALUE) {
				if (max < 0) {
					def = max;
				} else {
					def = 0;
				}
			} else {
				def = min;
			}
		}
		public EssentialVar(String n) {
			this(n, Integer.MIN_VALUE, Integer.MAX_VALUE);
		}
		public String getName() {
			return name;
		}
		public int limitToBounds(int val) {
			if (val < min) {
				return min;
			} else if (val > max) {
				return max;
			} else {
				return val;
			}
		}
		public int getDefaultValue() {
			return def;
		}
		public boolean isRightName(String s) {
			return s.equalsIgnoreCase(name);
		}
	}
	
	public static Color intToColor(int c) {
		int r = c%256, g = ((c%(256*256))/256), b = ((c%(256*256*256))/(256*256));
		return Color.rgb(r, g, b);
	}

	public static int colorToInt(Color c) {
		return (int) (Math.round(c.getRed()*255) + Math.round(c.getGreen()*255)*256 + Math.round(c.getBlue()*255)*256*256);
	}
}
