package rk.ramin.teller;

import java.io.Serializable;

public class AnswerHolder implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String text;
	private RoutingHolder whatFollows;
	
	public AnswerHolder(String t, RoutingHolder r) {
		text = t;
		whatFollows = r;
	}
	
	public RoutingHolder getRouter() {
		return whatFollows;
	}
	public String getText() {
		return text;
	}
	
}
