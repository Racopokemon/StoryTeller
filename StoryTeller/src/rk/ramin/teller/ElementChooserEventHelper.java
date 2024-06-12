package rk.ramin.teller;

/**
 * Another helper class.
 * Here we store all the knowledge about eventChooserItems that can be chosen instead of setting variables. 
 * The difference to the PageContentHelper is that we don't provide methods to access single values, 
 * instead we instantly process their functionality. (It would be too complicated to get every single value in the pageContentHelper, 
 * especially for stuff like setting up the environment, and it is not very necessary.)  
 * @author Der Guru
 *
 */
public class ElementChooserEventHelper {
	private SaveStat save;
	private Compiler comp;
	public ElementChooserEventHelper(Compiler c, SaveStat save) {
		this.save = save;
		comp = c;
	}
	/**
	 * Handles a single event (setting a var, environment, playing sounds, etc)
	 * Does NOT perform the test of it and other stuff, just instantly runs the varData.
	 * @param varData
	 */
	public void handleEvent(Object[] varData) {
		ElementChooserType type = (ElementChooserType) varData[0];
		Object[] data = (Object[]) varData[1];
		switch (type) {
		case SET_VAR:
			String varName = (String) data[0], expression = (String) data[1];
			if ((varName = save.getVarName(varName)) != null) {
				//System.out.println("Var "+varName+" set to "+comp.calculate(expression));
				save.changeVarCurrentValue(varName, Integer.valueOf(comp.calculate(expression))); //Wrap an COMPILE around the expression
			} else {
				System.out.println("Could not set var "+varName+" set to "+expression);
			}
			break;
		case SET_COLOR:
			varName = (String) data[0];
			int colorValue = (int) data[1];
			if ((varName = save.getVarName(varName)) != null) {
				System.out.println("Var "+varName+" set to color "+colorValue);
				save.changeVarCurrentValue(varName, colorValue);
			} else {
				System.out.println("Could not set var "+varName+" set to color "+colorValue);
			}
		default:
			System.out.println("Could not handle / not implemented Event of type "+type);
			break;
		}
	}
}
