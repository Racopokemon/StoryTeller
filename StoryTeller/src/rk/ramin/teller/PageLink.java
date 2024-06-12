package rk.ramin.teller;

public class PageLink {
	
	private Page link;
	private boolean isCustomAnswer;
	private boolean isCustomLink;
	
	public PageLink(Page p, boolean customAnswer, boolean customLink) {
		isCustomAnswer = customAnswer;
		isCustomLink = customLink;
		link = p;
	}
	
	public Page getLink() {
		return link;
	}
	
	public boolean isCustomLink() {
		return isCustomLink;
	}
	public boolean isCustomAnswer() {
		return isCustomAnswer;
	}
}
