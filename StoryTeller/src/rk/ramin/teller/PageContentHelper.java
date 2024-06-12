package rk.ramin.teller;

import java.util.ArrayList;

public class PageContentHelper {
	public static final Object[] oneInstanceOfADefaultPage = getNewDefaultPage();
	
	public static Object[] getNewDefaultPage() {
		return new Object[] { //Container
						new Object[] { //Romans
								new Object[] { //AMC
										new Object[] { //ParagraphBox
												null, //AreaTest
												new Object[] {}, //AreaVars
												new Object[] { //AMC
														""
												}
										}
								}
						},
						new Object[] {
								new Object[] {}, //AreaVars	
								new Object[] {}, //AMO with SingleAnswerBoxes
								new Object[] { //LinkBox
										new Object[] {}, //AreaVars	
										new Object[] {}, //AMO with LinkTextBoxes
										new Object[] { //SingleLinkBox
												new Object[] {}, //AreaVars	
												new Object[] { //ElementChooserLink
														ElementChooserType.NORMAL_LINK, //this is a normal link 
														null //The page (or in this case just null)
												}
										}
								},
						},
						new Object[] {//CustomAnswers
								new Object[] {}, //AMO with SingleCustomAnswerBoxes
								null
						}
				};
	}
	public static int getParagraphCount(Object[] c) {
		return ((Object[]) ((Object[]) c[0])[0]).length;
	}
	public static Object[] getParagraphAt(Object[] c, int pos) {
		return (Object[]) ((Object[]) ((Object[]) c[0])[0])[pos];
	}
	public static int getLineCount(Object[] c) {
		return ((Object[]) c[2]).length;
	}
	public static String getLineAt(Object[] c, int pos) {
		return (String) ((Object[]) c[2])[pos];
	}
	public static Object[] getParagraphVars(Object[] c) {
		return ((Object[]) c[1]);
	}
	public static String getParagraphTest(Object[] c) {
		return ((String) c[0]);
	}
	public static boolean hasParagraphPlaceholder(Object[] c) {
		return ((Object[]) c[0]).length == 2;
	}
	public static Object[] getParagraphPlaceholder(Object[] c) {
		return (Object[]) ((Object[]) c[0])[1];
	}
	public static Object[] getParagraphPlaceholderVars(Object[] c) {
		c = getParagraphPlaceholder(c);
		return (Object[]) c[1];
	}
	public static int getParagraphPlaceholderLineCount(Object[] c) {
		c = getParagraphPlaceholder(c);
		return ((Object[]) c[2]).length;
	}
	public static String getParagraphPlaceholderLineAt(Object[] c, int pos) {
		c = getParagraphPlaceholder(c);
		return (String) ((Object[]) c[2])[pos];
	}
	/**
	 * The ones that are set immediately the answers are shown to the user
	 */
	public static Object[] getAnswerVars(Object[] c) {
		return ((Object[]) ((Object[]) c[1])[0]);
	}
	public static int getAnswerCount(Object[] c) {
		return ((Object[]) ((Object[]) c[1])[1]).length;
	}
	public static Object[] getAnswerAt(Object[] c, int pos) {
		return (Object[]) ((Object[]) ((Object[]) c[1])[1])[pos];
	}
	public static String getAnswerText(Object[] c) {
		return (String) c[0];
	}
	public static String getAnswerTest(Object[] c) {
		return (String) c[1];
	}
	public static Object[] getAnswerLink(Object[] c) {
		return (Object[]) c[2];
	}
	public static boolean hasAnswerPlaceholder(Object[] c) {
		return ((Object[]) c[1]).length == 3;
	}
	public static Object[] getAnswerPlaceholderLink(Object[] c) {
		return (Object[]) ((Object[]) c[1])[2];
	}
	public static int getCustomAnswerCount(Object[] c) {
		return ((Object[]) ((Object[]) c[2])[0]).length;
	}
	public static Object[] getCustomAnswerAt(Object[] c, int pos) {
		return (Object[]) ((Object[]) ((Object[]) c[2])[0])[pos];
	}
	public static Object[] getCustomAnswerLink(Object[] c) {
		return (Object[]) c[3];
	}
	public static String getCustomAnswerTest(Object[] c) {
		return (String) c[2];
	}
	public static byte getCustomAnswerType(Object[] c) {
		return (byte) c[1];
	}
	public static Object[] getCustomAnswerText(Object[] c) {
		return (Object[]) c[0];
	}
	public static Object[] getWholeLinkVars(Object[] c) {
		return (Object[]) c[0];
	}
	public static int getTestLinkCount(Object[] c) {
		return ((Object[]) c[1]).length;
	}
	public static Object[] getTestLinkAt(Object[] c, int pos) {
		return (Object[]) ((Object[]) c[1])[pos];
	}
	public static String getTestLinkTest(Object[] c) {
		return (String) c[0];
	}
	public static Object[] getTestLinkLink(Object[] c) {
		return (Object[]) c[1];
	}
	public static Object[] getMainLink(Object[] c) {
		return (Object[]) c[2];
	}
	public static Object[] getLinkVars(Object[] c) {
		return (Object[]) c[0];
	}
	/**
	 * Bad OO, will always return a link type (normal or custom)
	 * @param c
	 * @return
	 */
	public static ElementChooserType getLinkType(Object[] c) {
		return (ElementChooserType) ((Object[]) c[1]) [0];
	}
	public static Page getLinkPage(Object[] c) {
		return (Page) ((Object[]) c[1]) [1];
	}
	/**
	 * Turns the given link into a normal link, that leads to the given page
	 * @param c
	 * @param p
	 */
	private static void setLinkPage(Object[] c, Page p) {
		//c[0] are its vars
		Object[] linkItself = (Object[]) c[1];
		linkItself[0] = ElementChooserType.NORMAL_LINK;
		linkItself[1] = p;
	}
	public static String getLinkExpression(Object[] c) {
		return (String) ((Object[]) c[1]) [1];
	}
	public static void setLinkToDefaultLinkToPage(Object[] c, Page link) {
		//TODO: Test this by opening a game without a saving file or provoke errors during loading. 
		c[1] = new Object[] {ElementChooserType.NORMAL_LINK, link};
	}
	public static int getVarCount(Object[] o) {
		return o.length;
	}
	public static Object[] getVarAt(Object[] o, int index) {
		return (Object[]) o[index];
	}
	public static String getVarTest(Object[] o) {
		return (String) o[0];
	}
	public static Object[] getVarData(Object[] o) {
		return (Object[]) o[1];
	}
	/*
	public static String getVarExpression(Object[] o) {
		return (String) ((Object[]) o[1]) [1];
	}
	public static String getVarVar(Object[] o) {
		return (String) ((Object[]) o[1]) [0];
	}
	*/
	public static void removeAllReferencesTo(Object[] c, Page link) {
		if (hasAnswerPlaceholder(c)) {
			removeAllReferencesInLink(getAnswerPlaceholderLink(c), link);
		}
		int e = getAnswerCount(c);
		for (int i = 0; i < e; i++) {
			removeAllReferencesInLink(getAnswerLink(getAnswerAt(c, i)), link);
		}
		e = getCustomAnswerCount(c);
		for (int i = 0; i < e; i++) {
			removeAllReferencesInLink(getCustomAnswerLink(getCustomAnswerAt(c, i)), link);
		}
	}
	
	private static void removeAllReferencesInLink(Object[] c, Page link) {
		clearLinkIfItIs(getMainLink(c), link);
		int e = getTestLinkCount(c);
		for (int i = 0; i < e; i++) {
			clearLinkIfItIs(getTestLinkLink(getTestLinkAt(c, i)), link);
		}
	}
	private static void clearLinkIfItIs(Object[] c, Page link) {
		if (getLinkType(c) == ElementChooserType.NORMAL_LINK && getLinkPage(c) == link) {
			setLinkPage(c, null);
		}
	}
	
	public static Object[] recreatePage(String[] romans, Object answers, TypingHolder[] customAnswers) {
		if (romans == null) {
			romans = new String[] {""};
		}
		if (customAnswers == null) {
			customAnswers = new TypingHolder[0];
		}
		Page placeholder = null;
		Object[] ans;
		if (answers instanceof AnswerHolder[]) {
			AnswerHolder[] a = (AnswerHolder[]) answers;
			ans = new Object[a.length];
			for (int i = 0; i < a.length; i++) {
				ans[i] = 
					new Object[] {
							a[i].getText(), //Text
							null, //AreaTest
							new Object[] { //LinkBox
									new Object[] {}, //AreaVars	
									new Object[] {}, //AMO with LinkTextBoxes
									new Object[] { //SingleLinkBox
											new Object[] {}, //AreaVars	
											a[i].getRouter().getBasicPage()
									}
							}
					};
			}
		} else {
			ans = new Object[0];
			RoutingHolder r = (RoutingHolder) answers;
			placeholder = r.getBasicPage();
		}
		Object[] ca = new Object[customAnswers.length];
		for (int i = 0; i < customAnswers.length; i++) {
			ca[i] = 
					new Object[] {
							new Object[] { //AMC with different possible custom answers
									customAnswers[i].getText() //Text
							},
							customAnswers[i].getType(), //CustomAnswer type (matches exactly / contains / ...)
							null, //AreaTest
							new Object[] { //LinkBox
									new Object[] {}, //AreaVars	
									new Object[] {}, //AMO with LinkTextBoxes
									new Object[] { //SingleLinkBox
											new Object[] {}, //AreaVars	
											customAnswers[i].getRouting().getBasicPage()
									}
							}
					};
		}
		return new Object[] { //Container
				new Object[] { //Romans
						new Object[] { //AMC
								new Object[] { //ParagraphBox
										null, //AreaTest
										new Object[] {}, //AreaVars
										romans
								}
						}
				},
				new Object[] {
						new Object[] {}, //AreaVars	
						ans, //AMO with SingleAnswerBoxes
						new Object[] { //LinkBox
								new Object[] {}, //AreaVars	
								new Object[] {}, //AMO with LinkTextBoxes
								new Object[] { //SingleLinkBox
										new Object[] {}, //AreaVars	
										placeholder
								}
						},
				},
				new Object[] {//CustomAnswers
						ca, //AMO with SingleCustomAnswerBoxes
						null
				}
		};
	}
	
	/**
	 * Updates the page representation from version 3 to version 4.
	 * (Here the ElementChoosers for links and setVars were introduced.)
	 * This method has no return value, as we change the array itself to be compatible afterwards. 
	 * @param o
	 */
	public static void makeCompatibleToVersion4(Object[] o) {
		//TODO: update the getGeneratedPageLinkingTo method. 
		//Find all vars and update them
		int c = getParagraphCount(o);
		for (int i = 0; i < c; i++) {
			makeSetVarCompatibleToVersion4(getParagraphVars(getParagraphAt(o, i)));
		}
		if (hasParagraphPlaceholder(o)) {
			makeSetVarCompatibleToVersion4(getParagraphPlaceholderVars(o));
		}
		makeSetVarCompatibleToVersion4(getAnswerVars(o));
		
		//Find all links and update them
		//Part 1: Answers
		if (hasAnswerPlaceholder(o)) {
			makeEntireLinkCompatibleToVersion4(getAnswerPlaceholderLink(o));
		}
		c = getAnswerCount(o);
		for (int i = 0; i < c; i++) {
			makeEntireLinkCompatibleToVersion4(getAnswerLink(getAnswerAt(o, i)));
		}
		//Part 2: CustomAnswers
		c = getCustomAnswerCount(o);
		for (int i = 0; i < c; i++) {
			makeEntireLinkCompatibleToVersion4(getCustomAnswerLink(getCustomAnswerAt(o, i)));
		}
	}
	
	private static void makeSetVarCompatibleToVersion4(Object[] o) {
		/*
		 * Previous structure: 
		{optionalTest,
			{Var, expression}
		}
		 */
		int varCount = getVarCount(o);
		for (int i = 0; i < varCount; i++) {
			Object[] v = getVarAt(o, i);
			v[1] = new Object[] {ElementChooserType.SET_VAR, v[1]};
		}
	}
	
	private static void makeEntireLinkCompatibleToVersion4(Object[] o) {
		makeSetVarCompatibleToVersion4(getLinkVars(o)); 
		makeLinkCompatibleToVersion4(getMainLink(o));
		int links = getTestLinkCount(o);
		for (int i = 0; i < links; i++) {
			Object[] b = getTestLinkAt(o, i);
			makeLinkCompatibleToVersion4(getTestLinkLink(b));
		}
	}
	private static void makeLinkCompatibleToVersion4(Object[] o) {
		//Index 0 are the vars, index 1 was the page and is now another array containing first the ELementChooserType and then its data
		makeSetVarCompatibleToVersion4((Object[]) o[0]);
		o[1] = new Object[] {ElementChooserType.NORMAL_LINK, o[1]};
	}
	
	/**
	 * If you give null as page, you just have a dead end page. 
	 * Already adds the TEMP-flag to the page. 
	 */
	public static Page getGeneratedPageLinkingTo(String text, Page link) {
		Object[] r = getNewDefaultPage(), s = getParagraphAt(r, 0), l = getMainLink(getAnswerPlaceholderLink(r));
		((Object[]) s[2])[0] = text;
		setLinkPage(l, link);
		Page p = new Page();
		p.setContent(r);
		p.setTemp();
		return p;
	}
}