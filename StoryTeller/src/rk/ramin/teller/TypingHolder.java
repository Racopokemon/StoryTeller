package rk.ramin.teller;

import java.io.Serializable;

public class TypingHolder implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private RoutingHolder routing;
	private String text;
	private byte type;
	
	public TypingHolder(String input, byte tp, RoutingHolder routing) {
		text = input;
		type = tp;
		this.routing = routing; 
	}
	
	public String getText() {
		return text;
	}
	
	public byte getType() {
		return type;
	}
	
	public RoutingHolder getRouting() {
		return routing;
	}
	
	public boolean isInputValid(String input) {
		switch (type) {
		case 0:
			return text.equals(input);
		case 1:
			return input.replaceAll("\\s", "").equalsIgnoreCase(text.replaceAll("\\s", ""));
		case 2:
			return input.replaceAll("\\s", "").toLowerCase().contains(text.replaceAll("\\s", "").toLowerCase());
		}
		return false;
	}
}
