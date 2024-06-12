package rk.ramin.teller;

/**
 * Every element that users can select with some elementChooser has to implement this interface.
 * ou should also have a look at the ElementChooserType enumeration, that contains an enum for every
 * element implementing this interface, and provides necessary information of each (as the class of them and their description)
 * 
 * The ElementChooserEventHelper stores the knowledge of how the var-items save their data, and how to process them within the game.  
 */
public interface ElementChooserItem extends Saveable, BriefedNode {}
