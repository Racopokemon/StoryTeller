package rk.ramin.teller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Story implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Short version history
	 * 1 Start
	 * 2 Added the variables array
	 * 3 Complete rework of the page structure, from now on everything is saved as huge arrays of objects
	 * 4 Added ElementChoosers, this affects the setVars and links
	 * 5 Added the pageMap
	 */
	public static final int CURRENT_VERSION = 5;
	
	private int version = CURRENT_VERSION;
	
	private ArrayList<Chapter> chapters = new ArrayList<Chapter>();
	/*Not used any longer, as we now have save stats*/@Deprecated
	private Page entrancePage; 
	//private Variable[] variables = {};
	private ArrayList<SaveStat> saveStats;
	
	private int upcomingPageNumber = 0;
	
	private Map<Integer, Page> pageMap;

	public Story() {
		chapters.add(new Chapter("Type a better name"));
		saveStats = new ArrayList<>();
		saveStats.add(new SaveStat("Launch"));
		pageMap = new HashMap<>();
	}
	
	public ArrayList<Chapter> getChapterList() {
		return chapters;
	}
	
	public Page getAnyPage() {
		Page ret;
		for (Chapter c : chapters) {
			for (Page p : c.getPages()) {
				ret = PageContentHelper.getGeneratedPageLinkingTo("There is no page to start. \nYou will now start anywhere in the story. \n(You can set a start page in the menu at 'Story')", p);
				return ret;
			}
		}
		ret = PageContentHelper.getGeneratedPageLinkingTo("Sadly there is no story to tell. \nThere is not a single page is in this whole story. \n(Double click the center area in the editor to add your first page)", null);
		return ret;
	}
	
	public void deletePage(Page p) {
		if (entrancePage == p) {
			entrancePage = null;
		}
		for (SaveStat s : saveStats) {
			s.removeAllReferencesTo(p);
		}
		for (Page pg : pageMap.values()) {
			pg.removeAllReferencesTo(p);
		}
		pageMap.remove(p.getPageNumber());
	}
	
	/**
	 * Call this when you add a page to any chapter in this story. 
	 * (We need this to assign the page its unique number)
	 * @param p
	 */
	public void addPage(Page p) {
		while(getPageWithNumber(upcomingPageNumber) != null) {
			upcomingPageNumber++;
		}
		p.setPageNumber(upcomingPageNumber);
		pageMap.put(upcomingPageNumber, p);
		upcomingPageNumber++;
	}
	
	public void changePageNumber(int from, int to) {
		Page pa = pageMap.get(from), pb = pageMap.get(to);
		if (pa == null) {
			throw new RuntimeException("You requested to change the page number "+from+", but this page does not even exist ...");
		}
		if (pb == null) {
			pageMap.remove(from);
			pageMap.put(to, pa);
			pa.setPageNumber(to);
		} else {
			pageMap.put(from, pb);
			pageMap.put(to, pa);
			pa.setPageNumber(to);
			pb.setPageNumber(from);
		}
	}
	
	/**
	 * Returns the page with the given page number, or null if there is no page with that number.
	 * @param number
	 * @return
	 */
	public Page getPageWithNumber(int number) {
		return pageMap.get(number);
	}
	
	public int getUpcomingPageNumber() {
		return upcomingPageNumber;
	}
	public void setUpcomingPageNumber(int i) {
		upcomingPageNumber = i;
	}
	
	public boolean isEmpty() {
		for (Chapter c : chapters) {
			if (c.getPages().length > 0) {
				return false;
			}
		}
		return true;
	}
	
	/*
	public Variable[] getVars() {
		return variables;
	}
	
	public void setVars(Variable[] v) {
		variables = v;
	}
	*/
	public ArrayList<SaveStat> getSaveStats() {
		return saveStats;
	}
	
	public void makeCompatible() {
		/*
		if (variables == null) {
			variables = new Variable[0];
		}
		*/
		if (saveStats == null) {
			saveStats = new ArrayList<SaveStat>();
			saveStats.add(new SaveStat("Launch"));
		} else {
			for (SaveStat s : saveStats) {
				s.makeCompatible();
			}
		}
		if (entrancePage != null) {
			for (SaveStat s : saveStats) {
				s.setCurrentPage(entrancePage);
				s.setInitPage(entrancePage);
			}
			entrancePage = null;
		}
		
		if (version < 5) {
			upcomingPageNumber = 0;
			pageMap = new HashMap<>();
			for (Chapter c : chapters) {
				for (Page p : c.getPages()) {
					p.setPageNumber(upcomingPageNumber);
					pageMap.put(upcomingPageNumber, p);
					upcomingPageNumber++;
				}
			}
		}
		
		for (Chapter c : chapters) {
			c.makeCompatible(version);
		}
		
		version = CURRENT_VERSION;
	}
}
