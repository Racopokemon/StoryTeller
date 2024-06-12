package rk.ramin.teller;

/**
 * * Note * I decided to rename this interface from "Element" to "Saveable", because this is fitting more exactly what this interface does.
 * 			Of course it is right that all elements should provide a possibility to save and load data (otherwise they might be quite useles though),
 * 			but Areas themselves also provide options to save and load data, and of course _they_ are no Elements. 
 * An Element is a node shown in Areas. The special thing about them is, that they provide methods to save their data and load data they saved 
 * once before. Everything in the deep roman-answer graph that has any content, that is changeable and needs to be saved as part of the page, 
 * implements this interface therefore, e.g. ElementTextFields, ElementSetVars, ElementTests and of course Boxes themselves, 
 * as they have to save the structure of areas that they contain. 
 * All Elements should have a constructor only with a reference to a ConstructionData, AreaMultis rely on this as they are dynamically creating 
 * new instances using this constructor, but also all Elements that might be resetted by their ButtonProvider have to implement such a constructor. 
 */
public interface Saveable {
	public Object save();
	public void load(Object o);
}
