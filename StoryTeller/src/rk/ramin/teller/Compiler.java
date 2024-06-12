package rk.ramin.teller;

import java.util.*;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
/*
Scanner sc = new Scanner(System.in);
String in;
Compiler m = new Compiler(new SaveStat("Johnnes (wie die Nase eines Mannes ...)"));
System.out.println("You may write now: ");
while (!(in = sc.nextLine()).equals("exit")) {
    String[] c = m.compile(in);
    if (c == null) {
        System.out.println("Works!");
        System.out.println("Result: "+m.calculate(in));
    } else {
        if (c[0] != null) {
            System.out.println("Error: "+c[0]);
        }
        if (c[1] != null) {
            System.out.println("Warning: "+c[1]);
        }
    }
}
System.out.println("terminated");
*/
public class Compiler {
    
	public static final Color COLOR_OK = Color.MEDIUMSEAGREEN.darker();
	public static final Color COLOR_WARNING = Color.DARKORANGE;
	public static final Color COLOR_ERROR = Color.DARKRED;
	
    private SaveStat vars;
    
    private String error = null;
    private String warning = null;
    
    private Label label;
    private VBox pane;
    private TextField text;
    private boolean showingLabel;
    private ObservableList<String> list;
    
    private boolean editor;
    
    public Compiler(SaveStat s) {
        vars = s;
        editor = false;
    }
    public Compiler(VBox p, TextField f, ObservableList<String> l) {
    	label = new Label();
    	pane = p;
    	text = f;
    	editor = true;
    	showingLabel = false;
    	list = l;
    	ListChangeListener<String> c = new ListChangeListener<String>() {
    		@Override
    		public void onChanged(Change<? extends String> c) {
    			compile();
    		}
    	};
    	text.textProperty().addListener(new ChangeListener<String>() {
    		@Override
    		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
    			compile();
    		}
		});
    	text.sceneProperty().addListener(new ChangeListener<Scene>() {
    		public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
    			if (newValue == null) {
					list.removeListener(c);
					//System.out.println("Listener removed. ");
    			} else {
    				list.addListener(c);
    				compile();
    				//System.out.println("Listener added.");
    			}
    		};
		});
    }
    
    public int calculate(String s) {
        return doCalc(new StringBuilder(s), false);
    }
    private int doCalc(StringBuilder b, boolean compile) {
        ArrayList<Object> parts = new ArrayList<Object>();
        int[] functionArgs = null;
        int functionArgsIndex = 0;
        //parts.add(b);
        int expressionStart = -1;
        int bracketStart = -1;
        boolean inExpression = false;
        int brackets = 0; 
        String functionName = null; //Use this to check whether we are currently reading a function (or expecting its brackets to start now)
        
        int i = 0;
        while (i <= b.length()) {
            char c = i == b.length() ? ' ' : b.charAt(i);
            if (brackets == 0) {
                if (c == '(') {
                    inExpression = false;
                    bracketStart = i+1;
                    brackets = 1;
                } else if (c == ')') {
                    error = "Unbalanced brackets";
                    return 0;
                } else if (Character.isWhitespace(c)) {
                    inExpression = false;
                } else if (isOperator(c)) {
                    inExpression = false;
                    if (functionName != null) {
                    	handleVar(functionName, parts, compile);
                    	functionName = null;
                    }
                    
                } else if (c == '^') {
                    error = "Use pow(base, exp) instead of ^";
                    return 0;
                } else if (c == ',' || c == ';')  {
                	error = "Only use "+c+" in functions";
                    return 0;
                } else {
                    //Digit or Var.
                    if (functionName != null) {
                    	handleVar(functionName, parts, compile);
                    	functionName = null;
                    }
                    if (!inExpression) {
                        inExpression = true;
                        expressionStart = i;
                    }
                }
                if (expressionStart > -1 && !inExpression) {
                    //Analyze expression
                    int expressionValue;
                    if (Character.isDigit(b.charAt(expressionStart))) { 
                        //Its a number.
                        //Try to analyze it, end with an error else
                        String s = b.subSequence(expressionStart,i).toString();
                        try {
                            expressionValue = Integer.parseInt(s);
                        } catch (Exception e) {
                            error = "Not a valid number: "+s;
                            return 0;
                        }
                        parts.add(expressionValue);
                    } else {
                        //Its a (trap) var or function
                        String v = b.subSequence(expressionStart,i).toString();
                        int args = getArgCount(v.toLowerCase());
                        if (args > 0 && !isOperator(c)) {
                            //its a function
                            functionName = v;
                            functionArgs = new int[args];
                            functionArgsIndex = 0;
                        } else {
                            //its a var
                        	handleVar(v, parts, compile);
                        }
                    }
                    expressionStart = -1;
                }
                if (isOperator(c)) {
                    parts.add(c);
                }
            } else {
                if (functionName != null && brackets == 1 && (c == ',' || c == ';')) {
                    if (functionArgsIndex+1 >= functionArgs.length) {
                        error = "Too many times "+c+" for "+functionName;
                        return 0;
                    }
                    functionArgs[functionArgsIndex++] = doCalc(new StringBuilder(b.subSequence(bracketStart,i)), compile);
                    bracketStart = i+1;
                    if (error != null) {
                        return 0;
                    }
                }
                if (c == '(') {
                    brackets++;
                } else if (c == ')') {
                    if (--brackets<=0) {
                        if (functionName != null) {
                            functionArgs[functionArgsIndex++] = doCalc(new StringBuilder(b.subSequence(bracketStart,i)),compile);
                            if (error != null) {
                                return 0;
                            }
                            if (functionArgsIndex < functionArgs.length) {
                                error = "Too less arguments for "+functionName;
                                return 0;
                            }
                            if (compile) {
                                parts.add(1);
                            } else {
                                parts.add(calcFunction(functionName, functionArgs));
                            }
                            functionName = null;
                        } else {
                            parts.add(doCalc(new StringBuilder(b.subSequence(bracketStart,i)),compile));
                            if (error != null) {
                                return 0;
                            }
                        }
                    }
                }
            }
            i++;
        }
        if (functionName != null) {
        	handleVar(functionName, parts, compile);
        }
        if (brackets != 0) {
            error = "Unbalanced brackets";
            return 0;
        }
        if (parts.isEmpty()) {
        	error = "Empty or incomplete expression";
        	return 0;
        }
        //------------------------------------------------------------------------------------------
        //LOOP 1: Solving brackets, functions, whitespace into an array (parts) of operators (chars) 
        //and numbers (ints)
        //----Finished!
        
        /*
        System.out.println(b.toString() + " becomes ");
        for (Object o : parts) {
        	System.out.print(o+" ");
        }
        System.out.println();
        
        if (true) return 1;*/
        
        char lastOp = '#'; //' ' means: number number was there, op expected
        for (i = parts.size()-1; i >= 0; ) {
            if (parts.get(i) instanceof Integer) {
                if (lastOp == ' ') {
                    error = "No operator between two numeric expressions";
                    return 0;
                } else {
                    i--; //Everything alright here
                    lastOp = ' ';
                }
            } else {
            	if (lastOp == '#') {
            		error = "Expression has to end with an numeric expression";
            		return 0;
            	}
                //Operator here
                char op = ((Character)parts.get(i)).charValue();
                if (lastOp == ' ') {
                    if (op == '!') { //This will also accept things like 5 + !!-!--8, but if you read from right to left it is clear what that does.
                        parts.remove(i);
                        parts.set(i, ((Integer)parts.get(i)).intValue() == 0 ? 1 : 0);
                        op = ' '; //Preparation for the lastOp = op at the end
                    } else if (i == 0 && op == '-') {
                        parts.remove(0);
                        parts.set(0, -((Integer)parts.get(0)).intValue());
                        op = ' ';
                    }
                    i--; //Everything alright here
                } else {
                    //&& || >= <= == !=
                    if (lastOp == '-') {
                        parts.set(i+2, -((Integer)parts.get(i+2)).intValue());
                        parts.remove(i+1);
                        op = ' '; //To be prepared for the lastsOp = op
                        i++;
                    } else if (lastOp == '=') {
                        if (op == '=') {
                        	op = 'e';
                        } else if (op == '!') {
                        	op = 'n';
                        } else if (op == '>') {
                        	op = 'b';
                        } else if (op == '<') {
                        	op = 'd';                        
                        } else {
                            error = meaningOfOperator(op)+" can not be followed by =";
                            return 0;
                        }
                        parts.remove(i+1);
                        parts.set(i, op); //Another symbol with the same meaning to prevent things like ========= being accepted
                    } else if (lastOp == op && op == '|') {
                        parts.remove(i+1);
                        parts.set(i, 'o');
                        op = 'o';
                    } else if (lastOp == op && op == '&') {
                        parts.remove(i+1);
                        parts.set(i, 'a');
                        op = 'a';
                    } else {
                        error = meaningOfOperator(op)+" can not be followed by "+meaningOfOperator(lastOp);
                        return 0;
                    } 
                    i--;
                    //Or: o |, And: a, &, Equals: =, e, not equal: n, >=: b, <=: d
                }
                lastOp = op;
            }
        }
        if (lastOp != ' ') {
            error = "No start with a numeric expression";
            return 0;
        }
        //------------------------------------------------------------------------------------------
        //LOOP 2: Counting operators and numbers, solving operators consisting of more than one char
        //Now operators and numbers are alternating with every new index now
        //----Finished!
        
        if (compile) {
            return 1;
        }
        
        for (i = 1; i < parts.size(); ) {
            char op = (Character)parts.get(i);
            if (op == '*') {
                parts.set(i-1, (Integer)parts.get(i-1) * (Integer)parts.get(i+1));
                parts.remove(i+1);
                parts.remove(i);
            } else if (op == '/' || op == ':') {
                if (((Integer)parts.get(i+1)).intValue() == 0) {
                    parts.set(i-1, 0);
                } else {
                    parts.set(i-1, (Integer)parts.get(i-1) / (Integer)parts.get(i+1));
                }
                parts.remove(i+1);
                parts.remove(i);
            } else if (op == '%') {
                if (((Integer)parts.get(i+1)).intValue() == 0) {
                    parts.set(i-1, 0);
                } else {
                    parts.set(i-1, (Integer)parts.get(i-1) % (Integer)parts.get(i+1));
                }
                parts.remove(i+1);
                parts.remove(i);
            } else {
                i+=2;
            }
        }
        for (i = 1; i < parts.size(); ) {
            char op = (Character)parts.get(i);
            if (op == '+') {
                parts.set(i-1, (Integer)parts.get(i-1) + (Integer)parts.get(i+1));
                parts.remove(i+1);
                parts.remove(i);
            } else if (op == '-') {
                parts.set(i-1, (Integer)parts.get(i-1) - (Integer)parts.get(i+1));
                parts.remove(i+1);
                parts.remove(i);
            } else {
                i+=2;
            }
        }
        for (i = 1; i < parts.size(); ) {
            char op = (Character)parts.get(i);
            if (op == '&' || op == 'a') {
                parts.set(i-1, 
                    (Integer)parts.get(i-1)!=0&&(Integer)parts.get(i+1)!=0 ? 1 : 0);
                parts.remove(i+1);
                parts.remove(i);
            } else if (op == '|' || op == 'o') {
                parts.set(i-1, (Integer)parts.get(i-1)==(Integer)parts.get(i+1)&&(Integer)parts.get(i+1)==0 ? 0 : 1);
                parts.remove(i+1);
                parts.remove(i);
            } else {
                i+=2;
            }
        }
        for (i = 1; i < parts.size(); ) {
            char op = (Character)parts.get(i);
            boolean rem = true;
            if (op == 'e' || op == '=') {
                parts.set(i-1, (Integer)parts.get(i-1)==(Integer)parts.get(i+1) ? 1 : 0);
            } else if (op == 'n') {
                parts.set(i-1, (Integer)parts.get(i-1)==(Integer)parts.get(i+1) ? 0 : 1);
            } else if (op == 'd') {
                parts.set(i-1, (Integer)parts.get(i-1)<=(Integer)parts.get(i+1) ? 1 : 0);
            } else if (op == 'b') {
                parts.set(i-1, (Integer)parts.get(i-1)>=(Integer)parts.get(i+1) ? 1 : 0);
            } else if (op == '>') {
                parts.set(i-1, (Integer)parts.get(i-1)>(Integer)parts.get(i+1) ? 1 : 0);
            } else if (op == '<') {
                parts.set(i-1, (Integer)parts.get(i-1)<(Integer)parts.get(i+1) ? 1 : 0);
            } else {
                i+=2; //Why did I even implement this skip?
                rem = false;
            }
            if (rem) {
                parts.remove(i+1);
                parts.remove(i);
            }
        }
        if (parts.size() > 1) {
            System.out.println("What da ... fuck - - - did just happen? \nAfter calculating all operators, there are still some left ...\nListing them:");
            for (Object o : parts) {
                System.out.println(o);
            }
            return 0;
        }
        return ((Integer)parts.get(0)).intValue();
    }
    private void handleVar(String name, ArrayList<Object> parts, boolean compile) {
    	if (editor) {
    		for (String s : list) {
    			if (s.equalsIgnoreCase(name)) {
    				parts.add(1);
    				return;
    			}
    		}
    		parts.add(0);
    		warning = "Var not defined: "+name;
    	} else {
        	String s = vars.getVarName(name);
            if (compile) {
                parts.add(1);
                if (s == null) {
                    warning = "Var not defined: "+name;
                }
            } else {
            	if (s == null) {
            		parts.add(0);
            	} else {
            		parts.add(vars.getCurrent(s));
            	}
                 //If the var is not defined, 0 will be returned from the manager
            }    
    	}
    }
    
    private static boolean isOperator(char c) {
        return c=='*'||c=='/'||c==':'||c=='+'||c=='-'||c=='%'||c=='!'||c=='>'||c=='<'||c=='='||c=='|'||c=='&';
    }
    
    /**
     * Just for error logs to the user if compilation fails.
     */
    private String meaningOfOperator(char c) {
    	switch (c) {
    	case ' ':
    		return "a numeric expression";
    	case 'a':
    		return "&& (and)";
    	case 'o':
    		return "|| (or)";
    	case '%':
    		return "% (modulo)";
    	case '&':
    		return "& (and)";
    	case '|':
    		return "| (or)";
    	case 'n':
    		return "!= (not equal)";
    	case 'd':
    		return "<= (greater or equal)";
    	case 'b':
    		return ">= (lesser or equal)";
    	case 'e':
    		return "== (equal)";
    	default:
    		return ""+c;
    	}
    }
    
    /**
     * True if the given expression (please without any whitespace etc) will be interpreted as function in the formula
     */
    public static boolean isFunction(String s) {
        return getArgCount(s.toLowerCase()) != 0;
    }
    
    /**
     * Checks whether a given string will be understood as a function name (returns 0 if not)
     * and if so it returns how many arguments are expected for it.
     */
    private static int getArgCount(String s) {
        //All functions: min(,) max(,) abs(), sgn(), rnd()
        //With more to come - playing time, date, links?
        switch (s) {
            case "min":
            case "max":
            case "pow":
                return 2;
            case "abs":
            case "sgn":
            case "rnd":
            case "rand":
            case "sqrt":
                return 1;
            default: 
                return 0;
        }
    }
    
    /**
     * Calculates the result of the given function with the given arguments. 
     * If an error occurs, 0 will be returned and nothing else happens. 
     */
    private static int calcFunction(String name, int[] args) {
        switch (name) {
            case "min":
                return java.lang.Math.min(args[0],args[1]);
            case "max":
                return java.lang.Math.max(args[0],args[1]);
            case "abs":
                return java.lang.Math.abs(args[0]);
            case "sgn":
                return args[0] > 0 ? 1 : args[0] < 0 ? -1 : 0;
            case "rnd":
            case "rand":
                return args[0]<1?0:(int)(java.lang.Math.random()*args[0]);
            case "pow":
            	return (int) java.lang.Math.pow(args[0],args[1]);
            case "sqrt":
            	return (int) java.lang.Math.sqrt(args[0]);
            default: 
                return 0;
        }
    }
    
    /*
    private static int invert(int i) {
        return i==0?1:0;
    }
    */
    
    /**
     * Reads the inserted String and checks its syntax. 
     * The label and the textField are set up automatically to fit. 
     * Call this, if this compiler is used in the editor.
     */
    public void compile() {
        error = null;
        warning = null;
        doCalc(new StringBuilder(text.getText()),true);
        Color c;
        if (error == null && warning == null) {
        	if (showingLabel) {
        		showingLabel = false;
        		pane.getChildren().remove(label);
        	}
        	c = COLOR_OK;
        } else {
        	if (error != null) {
        		label.setText(error);
        		c = COLOR_ERROR;
        	} else {
        		label.setText(warning);
        		c = COLOR_WARNING;
        	}
        	if (!showingLabel) {
        		pane.getChildren().add(label);
        		showingLabel = true;
        	}
        }
        if (showingLabel) {
        	label.setTextFill(c);
        }
        setTextFieldColor(text, c);
        //System.out.println("compiled");
    }
    public static void setTextFieldColor(TextField f, Color c) {
    	f.setStyle("-fx-text-fill: rgb("+(int)Math.round(c.getRed()*255)+","+(int)Math.round(c.getGreen()*255)+","+(int)Math.round(c.getBlue()*255)+");");
    }
    
}