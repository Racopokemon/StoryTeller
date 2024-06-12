package rk.ramin.teller;

import java.io.Serializable;
import java.util.ArrayList;

import javafx.scene.paint.Color;

public class Page implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/** Center position of the page */
	private double x, y;
	private transient Color color = Color.LIGHTCORAL;
	private double colorR, colorG, colorB;
	private String shortcut = "";
	private String name = "";
	private @Deprecated String[] romans; //If romans == null, we have a "special page"
	private @Deprecated Object answers; //AnswerHolder[] for one or more answers, if there is no answer and only a basic link where to continue (a routingHolder)
	private @Deprecated TypingHolder[] customAnswers; //null or an array[] with at least one TypingHolder
	private transient PageHolder wrapper;
	private Object[] content = PageContentHelper.getNewDefaultPage();
	private transient byte marker;
	/** 
	 * Some pages are generated during the game to give some information to the user (no starting page), but are not saved in the story. Those
	 * pages are temp ones, that MUST not be saved.
	 */
	private transient boolean isTemp = false;
	
	private int pageNumber = -1;
	
	public int getPageNumber() {
		return pageNumber;
	}
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	
	public void setName(String nm) {
		name = nm;
	}
	public String getName() {
		return name;
	}
	public void setShortcut(String sc) {
		shortcut = sc;
	}
	public String getShortcut() {
		return shortcut;
	}
 /**public void setRomans(String[] rm) {
		romans = rm;
	}
	public void setAnswers(Object o) {
		answers = o;
	}
	public Object getAnswers() {
		return answers;
	}
	public TypingHolder[] getCustomAnswers() {
		return customAnswers;
	}
	public void setCustomAnswers(TypingHolder[] th) {
		customAnswers = th;
	}
	public String[] getRomans() {
		return romans;
	}*/
	public void setContent(Object[] c) {
		content = c;
	}
	public Object[] getContent() {
		return content;
	}

	public void setPosition(double x, double y) {
		this.x = x;
		this.y = y;
	}
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public Color getColor() {
		return color;
	}
	public void setColor(Color c) {
		color = c;
		colorR = c.getRed();
		colorG = c.getGreen();
		colorB = c.getBlue();
	}
	public void setWrapper(PageHolder wr) {
		wrapper = wr;
	}
	public PageHolder getWrapper() {
		return wrapper;
	}
	
	/**
	 * Helps to draw the arrows
	 */
	public ArrayList<PageLink> getAllLinks() {
		/*
		ArrayList<PageLink> ret = new ArrayList<PageLink>();
		if (answers != null) {
			if (answers instanceof RoutingHolder) {
				addAllPageLinks(ret,(((RoutingHolder)answers).getAllLinks()),false);
			} else {
				AnswerHolder[] a = (AnswerHolder[])answers;
				for (AnswerHolder h : a) {
					addAllPageLinks(ret,(h.getRouter().getAllLinks()),false);
				}
			}
		}
		if (customAnswers != null) {
			for (TypingHolder th : customAnswers) {
				addAllPageLinks(ret,(th.getRouting().getAllLinks()),true);
			}
		}
		return ret;
		*/
		ArrayList<PageLink> ret = new ArrayList<PageLink>();
		if (PageContentHelper.hasAnswerPlaceholder(content)) {
			addAllLinksFromLink(PageContentHelper.getAnswerPlaceholderLink(content), ret, false);
		}
		int e = PageContentHelper.getAnswerCount(content);
		for (int i = 0; i < e; i++) {
			addAllLinksFromLink(PageContentHelper.getAnswerLink(PageContentHelper.getAnswerAt(content, i)), ret, false);
		}
		e = PageContentHelper.getCustomAnswerCount(content);
		for (int i = 0; i < e; i++) {
			addAllLinksFromLink(PageContentHelper.getCustomAnswerLink(PageContentHelper.getCustomAnswerAt(content, i)), ret, true);
		}
		return ret;
	}
	
	private void addAllLinksFromLink(Object[] c, ArrayList<PageLink> whereToAdd, boolean customAnswer) {
		addSingleLinkToArray(PageContentHelper.getMainLink(c), whereToAdd, customAnswer);
		int e = PageContentHelper.getTestLinkCount(c);
		for (int i = 0; i < e; i++) {
			addSingleLinkToArray(PageContentHelper.getTestLinkLink(PageContentHelper.getTestLinkAt(c, i)), whereToAdd, customAnswer);
		}
	}
	private void addSingleLinkToArray(Object[] link, ArrayList<PageLink> whereToAdd, boolean customAnswer) {
		if (PageContentHelper.getLinkType(link) == ElementChooserType.NORMAL_LINK) {
			Page p = PageContentHelper.getLinkPage(link);
			if (p != null) {
				whereToAdd.add(new PageLink(p, customAnswer, false));
			}
		} else {
			whereToAdd.add(new PageLink(null, customAnswer, true));
		}
	}

	/*
	private void addAllPageLinks(ArrayList<PageLink> whereToAdd, ArrayList<Page> whatToAdd, boolean custom) {
		for (Page p : whatToAdd) {
			whereToAdd.add(new PageLink(p, custom));
		}
	}
	*/
	
	public void removeAllReferencesTo(Page p) {
		/*
		if (answers instanceof RoutingHolder) {
			((RoutingHolder)answers).removeAllLinksTo(p);
		} else {
			for (AnswerHolder a : ((AnswerHolder[])answers)) {
				a.getRouter().removeAllLinksTo(p);
			}
		}
		*/
		PageContentHelper.removeAllReferencesTo(content, p);
	}
	
	public void makeCompatible(int version) {
		marker = 0;
		isTemp = false;
		if (shortcut == null) shortcut = "";
		if (version == 0) {
			x += PageHolder.RADIUS;
			y += PageHolder.RADIUS;
			setColor(Color.LIGHTCORAL);
		} else {
			color = Color.color(colorR,colorG,colorB);
		}
		if (version < 3) {
			content = PageContentHelper.recreatePage(romans, answers, customAnswers);
			romans = null;
			answers = null;
			customAnswers = null;
		}
		if (version < 4) {
			PageContentHelper.makeCompatibleToVersion4(content);
		}
		if (version < 5) {
			
		}
		//Add future compatibility updates after this
	}
	
	public boolean isDark() {
		return colorR+colorB+colorG<1.2;
	}
	
	public void setMarker(byte b) {
		marker = b;
	}
	
	public byte getMarker() {
		return marker;
	}
	
	public void setTemp() {
		isTemp = true;
	}
	
	public boolean isTemp() {
		return isTemp;
	}
}
