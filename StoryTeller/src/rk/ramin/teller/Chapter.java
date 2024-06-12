package rk.ramin.teller;

import java.io.Serializable;
import java.util.ArrayList;

public class Chapter implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String name;
	private Page[] pages = new Page[0];
	
	public Chapter(String name) {
		this.name = name;
	}
	public void setName(String n) {
		name = n;
	}
	public String getName() {
		return name;
	}
	/**
	 * Yes, this removes all the pages saved in here before.
	 */
	public void setPages(ArrayList<Page> h) {
		pages = new Page[h.size()];
		int index = 0;
		for (Page p : h) {
			pages[index++] = p;
		}
	}
	public Page[] getPages() {
		return pages;
	}
	
	public void makeCompatible(int version) {
		for (Page p : pages) {
			p.makeCompatible(version);
		}
	}
}
