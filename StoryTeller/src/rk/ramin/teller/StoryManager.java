package rk.ramin.teller;

import java.util.ArrayList;
import java.util.Map;

import jdk.nashorn.internal.runtime.regexp.joni.constants.Arguments;
import rk.ramin.teller.PageContentHelper.*;

/**
 * Does what its name says - manages the story, reads out the next page, holds all changeable variables, etc.
 * 
 * Returns Strings[] as "pages", that are shown to the player. Here is how they are supposed to be handled:
 * null: Invalid option, does nothing (makes sense by the text-input if you type something that has no effect)
 * {} Exit the game. 
 * {null,...} Special option like wait, change layout or exit
 * //{null,"w",t,} Waits 
 * {"title"} Shows the title with only one choice to continue.
 * {"title","choice",...} Regular case - shows the title, giving all of the choices 
 */
public class StoryManager {
	
	private Story story; //(Otherwise I don't see any place where we actually need this)
	private SaveStat save;
	
	private Page page;
	/** -1: Unset / Idle / no one, -2: The placeholder paragraph, <= 0: the paragraphs index */
	private int paragraphIndex;
	private int lineIndex;
	private boolean answers;
	
	private Compiler comp;
	private ElementChooserEventHelper eventHelper;
	
	public StoryManager(Story s, SaveStat save) {
		story = s;
		this.save = save;
		comp = new Compiler(save);
		eventHelper = new ElementChooserEventHelper(comp, save);
	}
	
	/**
	 * Called by the MainWindow to return the players choice. (0 is the first answer, 1 the second etc.)
	 * Already awaits the next page.
	 */
	public String[] choice(int choice) {
		if (answers) {
			Object[] link = null;
			int c = PageContentHelper.getAnswerCount(page.getContent());
			for (int i = 0; i < c; i++) {
				if (performTest(PageContentHelper.getAnswerTest(PageContentHelper.getAnswerAt(page.getContent(), i)))) {
					if (--choice < 0) {
						link = PageContentHelper.getAnswerLink(PageContentHelper.getAnswerAt(page.getContent(), i));
						break;
					}
				}
			}
			if (link == null) {
				link = PageContentHelper.getAnswerPlaceholderLink(page.getContent());
			}
			if (followLink(link)) {
				return nextOutput();
			} else {
				return null;
			}
		} else {
			return nextOutput();
		}
		//If answers: follow the link, reset paragraph und line index
		//Finalyy: return nextOutput
		
		
		
		/*
		if (romanIndex+1 == page.getRomans().length) {
			if (page.getAnswers() instanceof RoutingHolder) {
				return handleRouter((RoutingHolder) page.getAnswers());
			} else {
				return handleRouter(((AnswerHolder[]) page.getAnswers())[choice].getRouter());
			}
		} else {
			romanIndex++;
			return goOn();
		}
		*/
	}
	
	/**
	 * Called by the MainWindow to return that the player typed something instead of making a choice.
	 * Already awaits the next page - or you return null to show the player that the text had no effect. 
	 */
	public String[] type(String type) {
		//Typing is only called when there are different answers. 
		//So when this method is called, it is already sure, that we are at the end of all lines, 
		//and we don't have to check for this with lineIndex+1 == story.lines().size()
		if (type == null || type == "") {
			return null;
		}
		int c = PageContentHelper.getCustomAnswerCount(page.getContent());
		for (int i = 0; i < c; i++) {
			Object[] ca = PageContentHelper.getCustomAnswerAt(page.getContent(), i);
			if (performTest(PageContentHelper.getCustomAnswerTest(ca))) {
				Object[] s = PageContentHelper.getCustomAnswerText(ca);
				byte t = PageContentHelper.getCustomAnswerType(ca);
				for (Object v : s) {
					if (checkCustomAnswer(t, replaceVarsInString((String) v), type)) {
						if (followLink(PageContentHelper.getCustomAnswerLink(ca))) {
							return nextOutput();
						} else {
							return null;
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Not anymore up to date. Just give us a saveStat and we will be fine.
	 * 
	 * Launches the given story by following the given launch arguments. This means: 
	 * * If you did not give any arguments (null), we start from the saved start page.
	 * * If you gave a page (a simple save as it is used in the editor), we launch from the beginning of this page
	 * * If you gave an advanced save state (An Object[] array, as it is used for final games), we continue the game at exactly the given save state.
	 * 
	 * (As you started the story, you will already receive the first lines to show)
	 */
	public String[] launch() {
		
		if (save.getVarName("0_save_pageNumber") == null) {
			Page p = save.getCurrentPage();
			if (p == null) {
				save.setCurrentPage(story.getAnyPage());
			}
			return startFromPage(save.getCurrentPage());
		} else {
			return continueGame();
		}
	}
	
	/**
	 * Called by the MainWindow to start the game. (Is this even correct?)
	 */
	public String[] startFromPage(Page p) {
		/*
		return loadPage(story.getEntrancePage());
		*/
		lineIndex = -1;
		paragraphIndex = -1;
		page = p;
		return nextOutput();
		//Initiate both indices with -1, set the page and then return nextOutput
	}
	
	/**
	 * Loads a game that was saved before and continues at exactly that state. 
	 * Please only use this during a real playthrough and not during testing.
	 */
	public String[] continueGame() {
		page = story.getPageWithNumber(save.getCurrent("0_save_pageNumber"));
		if (page == null) {
			page = PageContentHelper.getGeneratedPageLinkingTo("Could not find the page you gave me, it seems like your story file has either changed or does not fit to this save. ", null);
		} else {
			lineIndex = save.getCurrent("0_save_lineIndex");
			paragraphIndex = save.getCurrent("0_save_paragraphIndex");
			answers = save.getCurrent("0_save_answers") == 1;
		}
		String text = getLine(paragraphIndex, lineIndex);
		if (answers) {
			String[] s = getAnswers();
			s[0] = text;
			return s;
		} else {
			return new String[] {text};
		}
		//set all values. if answers, then calculate them (getAnswers). if not, just show the >> (??)
	}
	
	private String[] nextOutput() {
		lineIndex++;
		if (paragraphIndex == -1 || lineIndex >= getLineCount(paragraphIndex)) {
			lineIndex = 0;
			paragraphIndex = nextParagraph();
			if (paragraphIndex == -1) {
				paragraphIndex = -2; //This can only happen if we started at this page, all paragraphs had a test and all returned false. In this case we take the placeholder paragraph.
				setVars(PageContentHelper.getParagraphPlaceholderVars(page.getContent()));
			} else {
				setVars(PageContentHelper.getParagraphVars(PageContentHelper.getParagraphAt(page.getContent(), paragraphIndex)));
			}
		}
		if (lineIndex+1 >= getLineCount(paragraphIndex) && nextParagraph() == -1) {
			setVars(PageContentHelper.getAnswerVars(page.getContent()));
			answers = true;
			String[] r = getAnswers();
			r[0] = getLine(paragraphIndex, lineIndex);
			return r;
		} else {
			answers = false;
			return new String[] {getLine(paragraphIndex, lineIndex)};
		}
		//line++
		//if lineIndex out of bounds (or paragraph index == -1) find the next text to show with nextParagraph()
		//set its vars, set the lineIndex to 0, return it (later!)
		//check how it would continue: if lines would be out of bounds find again the next paragraph with nextParagraph() 
		//IF THERE IS NO ONE, set answers to true ELSE to FALSE
		//build the syntax for the answer with getAnswers(
		
		//The optional paragraph is chosen if we dont find another paragraph here. 
		//Dont forget that there is an optional paragraph if there are only tests - this should be also part of the nextParagraph call and should be recognized anyhow
	}
	
	/**
	 * Checks, whether there is another paragraph after this, that has to be shown (tests all conditions of the following paragraphs)
	 * Returns:
	 * -1 if there is no paragraph after this one
	 * the index of the next paragraph
	 * 
	 * Does not check for the placeholder paragraph.
	 */
	private int nextParagraph() {
		if (paragraphIndex == -2) {
			return -1;
		}
		int i = paragraphIndex, c = PageContentHelper.getParagraphCount(page.getContent());
		while (++i < c) {
			String test = PageContentHelper.getParagraphTest(PageContentHelper.getParagraphAt(page.getContent(), i));
			if (performTest(test)) {
				return i;
			}
		}
		return -1;
		//Finds the next paragraph and returns its index (so we start searching from current +1)
		//Any communication if the placeholder is there
		//any comm if there is no one
	}
	
	/**
	 * Does not set any vars, this has been already done.
	 * Just calculates, which answers are available and returns an array containing all of them. 
	 * (If there are no answers, an array with only one element, which is empty, is returned, this will result 
	 * in an arrow >> to just show the next page being shown)
	 */
	private String[] getAnswers() {
		ArrayList<String> s = new ArrayList<>();
		int c = PageContentHelper.getAnswerCount(page.getContent());
		for (int i = 0; i < c; i++) {
			Object[] ans = PageContentHelper.getAnswerAt(page.getContent(), i);
			if (performTest(PageContentHelper.getAnswerTest(ans))) {
				s.add(replaceVarsInString(PageContentHelper.getAnswerText(ans)));
			}
		}
		if (s.isEmpty()) {
			return new String[] {""};
		} else {
			String[] r = new String[s.size()+1];
			for (int i = 0; i < s.size(); i++) {
				r[i+1] = s.get(i);
			}
			return r;
		}
		//DOES NOT SET ANY VARS
		//Calculates the answers and returns them so that they fit to the syntax
	}
	
	//private String[] goOn() {
		/*
		if (romanIndex+1==page.getRomans().length) {
			if (page.getAnswers() instanceof RoutingHolder) {
				String[] ret = new String[] {page.getRomans()[romanIndex]};
				return ret;
			} else {
				AnswerHolder[] h = (AnswerHolder[])page.getAnswers();
				String[] ret = new String[h.length+1];
				ret[0]=page.getRomans()[romanIndex];
				int i = 1;
				for (AnswerHolder a : h) {
					ret[i++] = a.getText();
				}
				return ret;
			}
		} else {
			return new String[] {page.getRomans()[romanIndex]};
		}
		*/
	//}
	
	/**
	 * Follows the given link, changes vars if its connected to following them, resets paragraph- and lineIndex and saves the new page.
	 * If there is no link set however, nothing changes and false is returned (true if a link was followed)
	 */
	private boolean followLink(Object[] link) {
		setVars(PageContentHelper.getWholeLinkVars(link));
		int i = PageContentHelper.getTestLinkCount(link);
		for (int j = 0; j < i; j++) {
			Object[] l = PageContentHelper.getTestLinkAt(link, j);
			if (performTest(PageContentHelper.getTestLinkTest(l))) {
				return actuallyFollowLink(PageContentHelper.getTestLinkLink(l));
			}
		}
		return actuallyFollowLink(PageContentHelper.getMainLink(link));
	}
	
	/**
	 * Dont call his one from anywhere outside, is a subroutine of followLink.
	 */
	private boolean actuallyFollowLink(Object[] link) {
		setVars(PageContentHelper.getLinkVars(link));
		Page p;
		if (PageContentHelper.getLinkType(link) == ElementChooserType.NORMAL_LINK) {
			p = PageContentHelper.getLinkPage(link);
		} else {
			p = story.getPageWithNumber(comp.calculate(PageContentHelper.getLinkExpression(link)));
		}
		
		if (p == null) {
			return false;
		} else {
			paragraphIndex = -1;
			lineIndex = -1;
			page = p;
			//answers = false;
			return true;
		}
	}
	
	/*private String[] loadPage(Page p) {
		
		if (p == null || p.getRomans() == null) { //2nd part is temp, just to avoid some errors for the moment
			return null;
		} else {
			page = p;
			romanIndex = 0;
			return goOn();
		}
		
	}*/
	
	/**
	 * If the test is null, we instantly return true.
	 */
	private boolean performTest(String s) {
		if (s == null) { //If there is no test set, in other words
			return true;
		}
		//System.out.println("Test returned "+ comp.calculate(s));
		return (comp.calculate(s) != 0);
	}
	
	private void setVars(Object[] vars) {
		int c = PageContentHelper.getVarCount(vars);
		for (int i = 0; i < c; i++) {
			Object[] v = PageContentHelper.getVarAt(vars, i);
			if (performTest(PageContentHelper.getVarTest(v))) {
				eventHelper.handleEvent(PageContentHelper.getVarData(v));
			}
		}
	}
	
	private int getLineCount(int p) {
		if (p == -2) {
			return PageContentHelper.getParagraphPlaceholderLineCount(page.getContent());
		} else {
			return PageContentHelper.getLineCount(PageContentHelper.getParagraphAt(page.getContent(), p));
		}
	}

	private String getLine(int p, int l) {
		if (p == -2) {
			return replaceVarsInString(PageContentHelper.getParagraphPlaceholderLineAt(page.getContent(), l));
		} else {
			return replaceVarsInString(PageContentHelper.getLineAt(PageContentHelper.getParagraphAt(page.getContent(), p), l));
		}
	}
	
	public static boolean checkCustomAnswer(byte type, String compareText, String input) {
		switch (type) {
		case 0:
			return compareText.equals(input);
		case 1:
			return input.replaceAll("\\s", "").equalsIgnoreCase(compareText.replaceAll("\\s", ""));
		case 2:
			return input.replaceAll("\\s", "").toLowerCase().contains(compareText.replaceAll("\\s", "").toLowerCase());
		}
		return false;
	}
	
	/**
	 * Replaces var names, surrounded by #, with the current var values.
	 * Everything that makes no sense is simply ignored, were reading from start to the end end watch every pair of # followed by each other.
	 * ## will be replaced with # and ignored after that. 
	 */
	private String replaceVarsInString(String s) {
		String[] p = s.split("#", -1);
		if (p.length < 3) {
			return s;
		}
		StringBuilder sb = new StringBuilder(p[0]);
		int i;
		for (i = 1; i < p.length - 1;i++) {
			if (p[i].equals("")) {
				sb.append("#");
				sb.append(p[++i]);
			} else {
				String n = save.getVarName(p[i]);
				if (n == null) {
					sb.append("#");
					sb.append(p[i]);
				} else {
					sb.append(save.getCurrent(n));
					sb.append(p[++i]);
				}
			}
		}
		if (i < p.length) {
			sb.append("#");
			sb.append(p[i]);
		}
		return sb.toString();
	}
	
	/**
	 * Stores the page we are currently showing in the save stat.
	 * This might be enough to save for editor test runs, and is better to understand for authors when a save state always starts at the very
	 * beginning of the saved page. (So use this way to save for test runs in the editor)
	 * You can continue by simply calling startFromPage() with the given page. 
	 */
	public void saveTesting() {
		if (!page.isTemp()) {
			save.setCurrentPage(page);
		}
	}
	
	/**
	 * Saves the complete game in an array and stores it in the saveStat, so that, if the game data (vars, pages, etc.) is NOT changed in any way,
	 * the game can be continued at exactly the line where it is during this call
	 * You then can continue with continueGame.
	 * This is supposed to be only used for finished games with no editor opened anywhere.
	 * 
	 * ----
	 * Now up to date: Prepares the saveStat for saving and returns it. If we should better not save, it returns null;
	 */
	public SaveStat savePlaying() {
		if (page.isTemp()) {
			return null;
		}
		save.setInitPage(null);
		save.setCurrentPage(null); //Added this. We have to avoid any pages stored in here as they and all linked pages will be also stored in the file [and we dont want that, we want to continue in our game file]
		//save.getInit().clear(); //FALSE: Wont need it, so why save it? It also doesnt matter which values we are deleting - this save stat will never be in the editor
		//The true thing about is, that we wont need any information from the init map - but a SaveStat expects both of its maps to have the same structure. 
		//If we keep this, it would cause errors during loading. 
		//The true problem is that the structure of a saveStat does not completely fit a IngameSaveStat [what has to stay: * Limitations of predefined vars * ignoreCase var names]
		//and maybe one should consider exporting another, new class IngameSaveStat as a save. 
		if (save.getVarName("0_save_pageNumber") == null) {
			save.addVar("0_save_pageNumber");
			save.addVar("0_save_lineIndex");
			save.addVar("0_save_paragraphIndex");
			save.addVar("0_save_answers");
		}
		save.changeVarCurrentValue("0_save_pageNumber", page.getPageNumber());
		save.changeVarCurrentValue("0_save_lineIndex", lineIndex);
		save.changeVarCurrentValue("0_save_paragraphIndex", paragraphIndex);
		save.changeVarCurrentValue("0_save_answers", answers ? 1 : 0);
		return save;
		//And then already save it to file. If there occur errors ... ? What then?
		//Attention: If the page isTemp(), you wont save anything! In the cases temp pages are currently used (error report, no pages, 
		//no starting page) there has no progress been made at all that should be saved. 
	}
	
	/**
	 * Just returns the current save stat as it is, with no changes, page updates,
	 * or information whether were currentl on a temp page, and whether you should save or not. 
	 * (Used to be bypassed to the particle manager to stay up to date)
	 */
	public SaveStat getSave() {
		return save;
	}
	
	/*
	 * Es kann immer nur ein StoryManager gleichzeitig laufen?
	 * ...
	 * start (startpunkt, vars (die dann auch gespeichert werden))
	 * und beim Ende dann auch nen Call, der die neue Position zurückgibt
	 * Man bräuchte einen statischen Compiler, der die Datenstruktur annimmt und den Text und false und true ausgibt
	 */

	
	
}
