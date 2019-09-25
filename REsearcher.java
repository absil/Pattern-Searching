/*
Abbey Silson - 1315323
Curtis Barnes - 1299191
*/

import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.lang.*;
import java.io.IOException;

public class REsearcher{
	public static void main(String[] args){		
		ArrayList<String> symbolArr = new ArrayList<String>();
		ArrayList<Integer> n1Arr = new ArrayList<Integer>();
	 	ArrayList<Integer> n2Arr = new ArrayList<Integer>();
		ArrayList<Integer> considered = new ArrayList<Integer>();	//array list to store the states that have already been considered
		boolean linematch = false;					//boolean to store whether there was a match on the line or not
		Scanner scan = new Scanner(System.in); 				//scanner to read in the fsm 
		Scanner fScan;							//scanner to read in the file to search
		int currState = 0;						//holds the current state
		int pointer = 0;						//points to the char on the line of the text file that is currently being considered
		boolean initState = true;					//boolean to store whether it is the inital state being looked for in a line
		boolean stillLooking = false;					//boolean to store if the line still needs to be checked further
		try{
			if(args.length == 1){					//if there is a file passed in through a command line argument
				File searchFile = new File(args[0]);	//get the name of the file to search through from the command line
				fScan = new Scanner(searchFile);	//set the scanner to search the file
				String[] line;				//variable to store the current line (state from FSM) being read
					
			while(scan.hasNext()){	//read through all the lines (states) from the REcompiler output
				line = scan.nextLine().split(",");	//split the line by commas
				if(line.length == 4){	//if there are 4 items on the line
					symbolArr.add(line[1]);	//add the symbol to look for to the symbol array
					n1Arr.add(Integer.parseInt(line[2]));	//add the next state to the next state array
					n2Arr.add(Integer.parseInt(line[3]));	//add the alt. next state to the alt. next state array
				}
			}

			String sLine = "";				//variable to store the line/part of line being checked
			String fullLine = "";				//variable to store the full line being checked
			while(fScan.hasNext()){
				boolean eol = false;			//bool to store if the end of the line has been reached
				initState = true;			//set init state to true so the line is looked through to find the first instance of the current state
				if(stillLooking){			//if the first instance of the state didn't match the pattern, but there is more line to check - split the line to check the rest
					sLine = sLine.substring(pointer);
					stillLooking = false;
					pointer = 0;			//reset the pointer
				}
				else{					//read in a new line to check
					pointer = 0;
					linematch = false;
					sLine = fScan.nextLine();
					fullLine = sLine;
				}
				Deque dq = new Deque();			//create a new deque
				dq.put(n1Arr.get(0).toString());	//put the first state onto the deque
				considered.clear();			//clear the considered states array
				while(dq.nsEmpty() == false){		//while there are still possible next states to check	
					//pop off the scan, add a new scan to the end
					if(linematch == true || stillLooking == true || eol == true){	//if the line has already been matched, the line needs to be split (stillLooking) or the end of line has been reached, break the loop
						break;
					}
					dq.pop();			//pop off the previous scan
					dq.put("SCAN");			//put a "SCAN" seperator onto the end of the queue
					considered.clear();		//clear the considered array
					while(dq.csEmpty() == false){	//IF THERE ARE CURRENT STATES TO BE CHECKED	
						currState = Integer.parseInt(dq.pop());	//set current state to head of deque
						if(n1Arr.get(currState) == 0 && n2Arr.get(currState) == 0){	//if the end of the regexp has been reached, a match has been found
							System.out.println("REGEXP match found in line: " + fullLine);	//printout the full line that has been matched
							linematch = true;
							break;
						}
						//check if the text matches curr state symbol
						if(n1Arr.get(currState) == n2Arr.get(currState)){	//if the state is not a branching state, check the character against the file to search			
							//System.out.println("looking for" + symbolArr.get(currState));
							if(pointer >= sLine.length()){ //if there is no more line to be checked
								eol = true;
								break;
							}			 
							boolean match = false;			//if the pointer is pointig at a char that matches the current state symbol to find
							boolean notContains = false;		//if it should be checking there ISN"T the characters
							boolean wild = false;			//if the symbol to find is a wild card
							if(symbolArr.get(currState).charAt(0) == '^'){	//if the collection of chars shouldn't be matched
								notContains = true;
							}	
							else if(symbolArr.get(currState).equals("..")){	//if the char to match is a wildcard
								wild = true;
							}
							if(initState){	//check through the whole line looking for the a match to the initial state
								initState = false;								
								for(int i = pointer; i < sLine.length(); i++){ 	//for each char in the line to check								
									if(notContains == false){	//if the symbol is to be matched
										if(wild == false){	//if the symbol isn't a wildcard
											if(symbolArr.get(currState).contains(Character.toString(sLine.charAt(i)))){	//if it matches
												match = true;
												pointer = i + 1;	//increment the pointer;
												break;
											}
										}
										else{	//if the char to find is a wild card(matches anything), set match to true
											match = true;
											pointer = i + 1;	//increment the pointer
											break;
										}
									}
									else{	//if the symbol matches anything but current state symbols
										String notFind = symbolArr.get(currState).substring(1);
										if(notFind.contains(Character.toString(sLine.charAt(i)))){	//if it matches
											match = false;
										}
										else{ 
											match = true; 
											pointer = i + 1; 	//increment pointer
											break;
										}
									}
								}
								if(match == false){	//if there is no match for the first char in the regex anywhere in the expression, move to the next line
									eol = true;
									break;
								}									
							}
							else{		//if it isn't the inital state, and should only check the next symbol in the line				
								if(notContains == false){
									if(wild == false){
										if(symbolArr.get(currState).contains(Character.toString(sLine.charAt(pointer)))){	//if it matches
											match = true;
											pointer++;
										}
									}
									else{	//if the char to find is a wild card & matches anything, set match to true
										match = true;
										pointer++;
									}
								}
								else{
									String notFind = symbolArr.get(currState).substring(1);
									if(notFind.contains(Character.toString(sLine.charAt(pointer)))){	//if it matches
										match = false;
									}
									else{ 
										match = true; 
										pointer++; 
									}
								}
							}
							if(match == false && dq.csEmpty() == false){	//if there was no match and there are current states, continue loop
								continue;
							}
							else if(match == false && pointer <= sLine.length() && pointer != 0){	//if no match was found on the current line, move to the next line
								stillLooking = true;								
								break;
							}
							else if(match == false && dq.csEmpty() == true){ //if there was no match and there are no more current states, break from loop	
								break;
							}
							
							else if(match == true){ //match found, move to next state in the FSM
								//clear all the states in the current states 
								dq.clearCurr();
								if(n1Arr.get(currState) == n2Arr.get(currState)){	//if the state is not a branching state
									dq.put(n1Arr.get(currState).toString());
								}
								else{	//if the state is a branching state
									dq.put(n1Arr.get(currState).toString());
									dq.put(n2Arr.get(currState).toString());
								}
								break;						
							}						
						}else{	//if branching state, push possible states onto current states
							//if state has already been considered, continue loop;
							
							if(considered.indexOf(n1Arr.get(currState)) == -1){
								dq.push(n1Arr.get(currState).toString());
								considered.add(n1Arr.get(currState));						
							}
					
							if (considered.indexOf(n2Arr.get(currState)) == -1){
								dq.push(n2Arr.get(currState).toString());
								considered.add(n2Arr.get(currState));
							}
						}				
					}
				}
			}
			}
			else{	//if there is an incorrect number of command line arguments, print a usage statement
				System.out.println("Usage: filename to search, < fsm output");
				System.exit(0);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/* implementation of a Deque data structure*/
	static class Deque {
		ArrayList<String> deque = new ArrayList<String>();

		public Deque(){
			deque.add("SCAN");	//add a marker to seperate the current states from the possible next states
		}

		/* push (add) a state onto the front of the deque*/
		public void push(String state){
			deque.add(0, state);
		}

		/* remove the state off the head of the deque, return the removed state*/
		public String pop(){
			String front = deque.get(0);
			deque.remove(0);
			return front;
		}

		/* add the state to the end of the queue*/
		public void put(String state){
			deque.add(state);
		}

		public void print(){
			System.out.println(deque);
		}

		/* check if there are next states in the deque*/
		public boolean nsEmpty(){
			//find the index of scan, check if there are any states after this
			int scan = deque.indexOf("SCAN");
			if(deque.size() - 1 > scan){
				return false;
			}
			return true;
		}

		/* check if there are current states in the deque*/
		public boolean csEmpty(){
			int scan = deque.indexOf("SCAN");
			if(scan == 0){ //if scan is the first element in the list (there are no current states) return true
				return true;
			}
			return false;
		}

		/* remove all the current states from the queue*/
		public void clearCurr(){
			int scan = deque.indexOf("SCAN");
			for(int i = 0; i < scan; i++){
				deque.remove(0);
			}
		}
	}
}
