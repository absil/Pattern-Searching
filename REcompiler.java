/*
Abbey Silson - 1315323
Curtis Barnes - 1299191

E->V
E->(E)

T->F*
T->F?
T->F|T

F-> \char
F-> (E)
F->[LITERALS]
F->^[LITERALS]
F-> . 	(wildcard)
F-> Vocab

*/

import java.util.Scanner;
import java.io.File;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class REcompiler{

	private static ArrayList<String> characters = new ArrayList<String>(); //Stores characters of interest
	private static ArrayList<Integer> nextState1 = new ArrayList<Integer>(); //Stores next state to jump to
	private static ArrayList<Integer> nextState2 = new ArrayList<Integer>(); //Stores alternative state to jump to
	private static int currentState = 1; //Index for the current state
	private static String regexp = ""; //Stores the regular expression
	private static int index = 0; //Index used to read through regular expression
	private static int bStart = 0; //keeps track of the first state inside ()
	private static int bEnd = 0; //keeps track of the end state inside ()
	private static boolean b = false;	//if the index before is a bracker
	private static int bChange = 0; //state to change
	private static int bChange1 = 0;	


	public static void main(String[] args){
		try{

			if(args.length == 1){ //If the user has supplied a regular expression
				regexp = args[0]; //Regular expression = user input
				setState(0, " ", 1, 1);	//set a start state
			
				int initial = expression();	//call the expression method to parse the regexp
				setState(currentState, " ", 0, 0);	//set finish state

				if(nextState1.get(1) == 2 && nextState2.get(1) == 2){	//if the first state = 2, set start state to state 1
					nextState1.set(0, 1);
					nextState2.set(0, 1);
				}
					

				for(int i = 0; i < nextState1.size(); i++){	//print the FSM to standard out
					System.out.println(i + "," + characters.get(i) + "," + nextState1.get(i) + "," + nextState2.get(i));
				}

			}else{	//provide a usage message
				System.out.println("Usage: Please provide regular expression to validate");
			}					
		}
		catch(Exception e){
			e.printStackTrace(); //Displays error message
		}

	}

	
	/*
		This method adds a state to the finite state machine
	*/
	public static void setState(int state, String ch, int n1, int n2){
		characters.add(ch); //Sets the character we are interested in
		nextState1.add(n1); //Sets the next state
		nextState2.add(n2); //Sets the next alternative state		
	}

	/*
		This method processes an expression
	*/
	private static int expression(){
		int r = term();				//set r to the return of a term method
		if(index == regexp.length()){		//if index is equal to the length of the regexp, return -1
			return -1;
		}
		if(isVocab(regexp.charAt(index)) || regexp.charAt(index) == '(' || isFactor(regexp.charAt(index))){	//if the char being looked at is vocab, and opening bracket or is a factor
			expression();	//call the expression method
		}		
		else if(regexp.charAt(index) == ')'){	//if the char is a closing bracket, return r
			return r;
		}
		else{
			error();	//create an error message - invalid regexp
		}
		return r;
		
	}

	/* this method processes a factor*/
	private static int factor(){
		int r;
		String chars = "";
		if(index >= regexp.length()) error();	//if the end of the regexp has been reached, error
		if(regexp.charAt(index) == '\\'){	//if the char is an escape char
			index++; //move past the escape char
			setState(currentState, String.valueOf(regexp.charAt(index)), currentState+1, currentState+1);	//create a state = to the char being escaped
			index++;
			r = currentState;
			currentState++;
			return r;
		}		
		else if(regexp.charAt(index) == '('){	//if the char is an opening bracket
			index++;	//move past the (
			bStart = currentState;	//set the start state of the brackets to current state
			r = expression();	//set r = expression method
			//bStart = r;

			if(index > regexp.length()){	//if the end of the regexp has been reached, error
				error();
			}			
			else if(regexp.charAt(index) == ')'){	//if the char is a closing bracket
				index++;	//increment index
			}
			else{	//invalid regexp, error
				error();
			}

			if(b == true){	//if b is true, chnages need to be made
				int c = currentState;
				//reset states for bracket usage
				nextState1.set(bChange, c);	
				nextState2.set(bChange, c);
				nextState1.set(bChange1, c);
				nextState2.set(bChange1, c);
				b = false;
			}

			bEnd = currentState - 1;	//set bEnd to end state of brackets
			return r;
		}else if(regexp.charAt(index) == '['){ //Checks for match from list of literals
			index++;	//move past [
			if(index >= regexp.length()) error();
			if(regexp.charAt(index) == ']'){	//if first char in list is ], add to list
				chars += ']';
				index++;
			}

			while(regexp.charAt(index) != ']' && index < regexp.length()-1){	//while there are chars to add
				chars += regexp.charAt(index);	//add to string
				index++;
			}
			if(regexp.charAt(index) != ']'){	//if there isn't a closing ]
				error();	//invalid regexp
			}

			setState(currentState, chars, currentState + 1, currentState + 1);	//create state using list of chars
			r = currentState;
			currentState++;
			index++;
			
			return r;		
		}else if(regexp.charAt(index) == '^'){	//if char is ^ ie should be followed by list of literals enclosed in []
			index++;	//move past ^

			if(index >= regexp.length()) error();

			if(regexp.charAt(index) == '['){	//if next char is [
				index++;	//move past [
			}else{	//invalid regexp
				error();
			}

			chars += '^';	//add a ^ on the front of a string so searcher knows to NOT find list of chars

			if(regexp.charAt(index) == ']'){	//if first is a ], add to list
				chars += ']';
				index++;
			}

			while(regexp.charAt(index) != ']' && index < regexp.length()-1){	//add all chars to string
				chars += regexp.charAt(index);
				index++;
			}

			if(regexp.charAt(index) != ']'){
				error();
			}
			setState(currentState, chars, currentState + 1, currentState + 1);	//create state with list
			r = currentState;
			currentState++;
			index++;
			
			return r;
		}else if(isVocab(regexp.charAt(index)) || regexp.charAt(index) == '.'){	//if the character is a wild card or is vocab
			String symbol = String.valueOf(regexp.charAt(index));
			if(regexp.charAt(index) == '.'){	//if the char is a wild card, set the char to look for to ".." so it can diferentiated from an escaped "."
				symbol = "..";
			}
			setState(currentState, symbol, currentState+1, currentState+1);
			index++;
			r = currentState;
			currentState++;
			return r;	
		}else{
			error();
			return -1;
		}		
	}

	/* this method processes a term*/
	private static int term(){
		int prevState = currentState - 1;
		int r = factor();
		int t1 = r;
		
		if(index == regexp.length()){	//if end of regexp is reached, error	
 			return r;
		}
		if(regexp.charAt(index) == '*'){ //Closure, occurs none or many times
			prevState = currentState-1;
			if(regexp.charAt(index-1) != ')'){	//if brackets before
				if(nextState1.get(prevState-1) == nextState2.get(prevState-1)){	//if the machine is non-branching, reset state 2
					nextState2.set(prevState-1, currentState);
				}
				nextState1.set(prevState-1, currentState);
			}
			else {
				int change = bStart -1;
				if(nextState1.get(change) == nextState2.get(change)){	//if the machine is non-branching, reset state 2
					nextState2.set(change, currentState);
				}
				nextState1.set(change, currentState);
			} 
			index++;
			setState(currentState, " ", r, currentState + 1);
			r = currentState;
			currentState++;
		}else if(regexp.charAt(index) == '?'){	//occurs one or no times
			prevState = currentState - 1;
			if(regexp.charAt(index-1) != ')'){	//if brackets before
				setState(currentState, " ", prevState, currentState+1);	//make a branching machine
				if(nextState1.get(prevState-1) == nextState2.get(prevState-1)){	//if the machine is non-branching, reset state 2
					nextState2.set(prevState-1, currentState);
				}
				nextState1.set(prevState-1, currentState); 
			}
			else{
				int change = bStart - 1;
				setState(currentState, " ", bStart, currentState+1);
				if(nextState1.get(change) == nextState2.get(change)){	//if the machine is non-branching, reset state 2
					nextState2.set(change, currentState);
				}
				nextState1.set(change, currentState);
			}

			nextState1.set(prevState, currentState+1);
			nextState2.set(prevState, currentState+1);
	
			index++;
			r = currentState;
			currentState++;
		}else if(regexp.charAt(index) == '|'){	//alternation, either or
			if(regexp.charAt(index+1) == '(' && regexp.charAt(index-1) == ')'){	//if either and or are brackets
				b = true;	//set b to true so brackets know to alter states
				bChange = bEnd;			//set state to change to end state of brackets
				bChange1 = bStart;		//set other state to change to bStart
			}

			prevState = currentState - 1;	//set the previous state
			int state1 = currentState - 1;
				
			if(regexp.charAt(index-1) == ')'){	//if there is a set of brackets before the infix
				state1 = bStart + 1;
				nextState1.set(bStart-1, currentState);
				nextState2.set(bStart-1, currentState);	
				nextState1.set(bStart, currentState+2);
				nextState2.set(bStart, currentState+2);								
			}				

			//build a branching machine
			setState(currentState, " ", currentState+1, state1);
			int oState = currentState - 2;
			if(regexp.charAt(index-1) != ')'){	//if there isn't a set of brackets before
				if(nextState1.get(oState) == nextState2.get(oState)){	//check if the state before the first infix char was branching
					nextState2.set(oState, currentState);	//set the prev -1 state to current state
				}
				nextState1.set(oState, currentState);
			}

			if(nextState1.get(prevState) == nextState2.get(prevState)){	//if the machine is non-branching, reset state 2
				nextState2.set(prevState, currentState+2);
			}
			nextState1.set(prevState, currentState+2);	

			if(regexp.charAt(index+1) == '(' && regexp.charAt(index-1) != ')'){
				bChange = currentState -1;
				b = true;
			}		
			
			index++;
			r = currentState;
			currentState++;

			int t2 = term();
		}
		return r;
	}
	
	/* method to check if the character is a vocab*/
	public static boolean isVocab(Character c){
		String notVocab = "*?|()[]^\\";
		if(notVocab.contains(Character.toString(c))){
			return false;
		}
		else{
			return true;
		}
	}

	/* check if the char is a special char for factor*/
	public static boolean isFactor(Character c){
		String notVocab = "[^(\\";
		if(notVocab.contains(Character.toString(c))){
			return true;
		}
		else{
			return false;
		}
	}
	
	/* called when regexp is invalid, prints message*/
	private static void error(){
		System.out.println("Invalid Regular expression at index : " + index);
		System.exit(0);
	}	
}
