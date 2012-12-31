package com.googlecode.lingwah.test;

import com.googlecode.lingwah.Parser;
import com.googlecode.lingwah.parser.ChoiceParser;

public class Metaprogramming {
	
	/**
	 * Returns a parser that will accept the same input as the orignal parser
	 * EXCEPT that the new parser accepts only strings where the letters 
	 * are either all upper case or all lower case.
	 * 
	 * For example, consider this Parser...
	 * Parser original= seq(str("Program"), identifier, str("End.")).separatedBy(opt(ws));
	 * 
	 * The result from this method would be a Parser that is equivalent to this...
	 * Parser original= choice(
	 * 		seq(str("program"), identifier, str("end.")).separatedBy(opt(ws)),
	 * 		seq(str("PROGRAM"), identifier, str("END.")).separatedBy(opt(ws))
	 * );
	 * 		
	 * 
	 * @param original
	 * @return
	 */
	public static Parser allUpperOrAllLowerLetters(Parser original) {
		ChoiceParser newParser= new ChoiceParser();
		newParser.addChoice(allUpperLetters(original));
		newParser.addChoice(allLowerLetters(original));
		return newParser;
	}
	public static Parser allUpperLetters(Parser original) {
		Parser 
		ChoiceParser newParser= new ChoiceParser();
		newParser.addChoice(allUpperLetters(original));
		newParser.addChoice(allLowerLetters(original));
		return newParser;
	}
}
