package com.googlecode.lingwah.parser;

import java.util.Collections;
import java.util.List;

import com.googlecode.lingwah.ParseContext;
import com.googlecode.lingwah.ParseResults;
import com.googlecode.lingwah.Parser;

/**
 * Base class for 'combinator' parsers.
 * Combinator parsers are parsers that are used to combine 
 * parsers into new parsers.
 * Sequence, Optional, Choice, and Repetition are the usual combinator types.
 * 
 * Knowing that a parser is a combinator is useful when it comes to finding matches.
 *  
 * @author Ted Stockwell
 */
abstract public class CombinatorParser extends Parser {
	
	@Override
	public boolean isCombinator() {
		return true;
	}

}
