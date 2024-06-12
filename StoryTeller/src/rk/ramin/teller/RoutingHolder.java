package rk.ramin.teller;

import java.io.Serializable;
import java.util.ArrayList;

public class RoutingHolder implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Page basicLink;
	//... more to come
	
	/**
	 * Creates a completely empty routingHolder, only containing an empty default link.
	 */
	public RoutingHolder() {
		// Allright here
	}
	
	public RoutingHolder(Page basicLink) {
		this.basicLink = basicLink;
	}
	
	public void setBasicLink(Page p) {
		basicLink = p;
	}
	public Page getBasicPage() {
		return basicLink;
	}
	
	public ArrayList<Page> getAllLinks() {
		ArrayList<Page> p = new ArrayList<Page>();
		if (basicLink != null) {p.add(basicLink);}
		return p;
	}
	
	public void removeAllLinksTo(Page p) {
		if (basicLink == p) {
			basicLink = null;
		}
	}
	
	//Maybe something like getLinkWithConditions(Conditions ...) or so
}
