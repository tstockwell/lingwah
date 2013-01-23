package com.googlecode.lingwah.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.googlecode.lingwah.Match;
import com.googlecode.lingwah.Parser;

@SuppressWarnings("unchecked")
public class MatchNavigation
{

	public static Match findFirstDescendantByType(Match root, Parser type) {
		List<Match> todo = new ArrayList<Match>();
		HashSet<Match> done = new HashSet<Match>();
		todo.addAll(root.getChildren());

		while (!todo.isEmpty()) 
		{
			Match node= todo.remove(0);
			if (done.contains(node))
				continue;
			
			if (node.getParser() == type) {
				return node;
			}

			// add children in reverse order to beginning of list
			// so that final results will be in same order as a depth
			// first search of the root match
			List<Match> children= node.getChildren();
			for (int i= children.size(); 0 < i--;) {
				Match child= children.get(i);
				todo.add(0, child);
			}
		}

		return null;
	}	
	public static Match findFirstDescendantByLabel(Match root, String label) {
		List<Match> todo = new ArrayList<Match>();
		HashSet<Match> done = new HashSet<Match>();
		todo.addAll(root.getChildren());

		while (!todo.isEmpty()) 
		{
			Match node= todo.remove(0);
			if (done.contains(node))
				continue;
			
			if (label.equals(node.getParser().getLabel())) {
				return node;
			}

			// add children in reverse order to beginning of list
			// so that final results will be in same order as a depth
			// first search of the root match
			List<Match> children= node.getChildren();
			for (int i= children.size(); 0 < i--;) {
				Match child= children.get(i);
				todo.add(0, child);
			}
		}

		return null;
	}	

	public static <T extends Match> List<T> findDescendantsByType(Match root, Parser type) {
		List<T> matches = new ArrayList<T>();
		List<Match> todo = new ArrayList<Match>();
		HashSet<Match> done = new HashSet<Match>();
		todo.addAll(root.getChildren());

		while (!todo.isEmpty()) 
		{
			Match node= todo.remove(0);
			if (done.contains(node))
				continue;
			
			if (node.getParser() == type) {
				matches.add((T)node);
				continue;
			}

			// add children in reverse order to beginning of list
			// so that final results will be in same order as a depth
			// first search of the root match
			List<Match> children= node.getChildren();
			for (int i= children.size(); 0 < i--;) {
				Match child= children.get(i);
				todo.add(0, child);
			}
		}

		return matches;
	}

	public static <T extends Match> T findFirstChildByType(Match root, Parser type) {
		List<Match> todo = new ArrayList<Match>();
		HashSet<Match> done = new HashSet<Match>();
		todo.addAll(root.getChildren());

		while (!todo.isEmpty()) 
		{
			Match node= todo.remove(0);
			if (done.contains(node))
				continue;
			
			if (node.getParser() == type)
				return (T)node;
		}

		return null;
	}

	public static <T extends Match> T findChildByType(Match match, Parser parser) {
		List<Match> todo = new ArrayList<Match>();
		todo.addAll(match.getChildren());

		while (!todo.isEmpty()) 
		{
			Match node= todo.remove(0);
			
			if (node.getParser() == parser)
				return (T)node;
		}

		return null;
	}

	public static <T extends Match> List<T> findChildrenByType(Match match, Parser parser) {
		List<T> matches = new ArrayList<T>();
		List<Match> todo = new ArrayList<Match>();
		todo.addAll(match.getChildren());

		while (!todo.isEmpty()) 
		{
			Match node= todo.remove(0);
			
			if (node.getParser() == parser)
				matches.add((T)node);
		}

		return matches;
	}

	public static Match findFirstSiblingByType(Match match, Parser type) {
		List<Match> todo = new ArrayList<Match>();
		HashSet<Match> done = new HashSet<Match>();
		for (Match parent:match.getParents()) {
			todo.addAll(parent.getChildren());
		}

		while (!todo.isEmpty()) 
		{
			Match node= todo.remove(0);
			if (node == match)
				continue;
			
			if (done.contains(node))
				continue;
			
			if (node.getParser() == type) {
				return node;
			}

			todo.addAll(node.getChildren());
		}
		
		return null;
	}
	
	
	/**
	 * This method finds a match with the given parser type.
	 * A match is found by only considering the elements defined by the parser associated with the match to search.
	 *
	 * For instance, consider the following Parser...
	 * 
	 * 	public final Parser ClosureLiteral= sseq(opt(sseq(PIPE, opt(TypeParameterDeclaration), PIPE)), LBRACE, opt(ClosureBody), RBRACE);
	 * 
	 * where PIPE, LBRACE, and RBRACE are terminals.  
	 * ClosureBody is a Parser that defines elements that may recursively contain a TypeParameterDeclaration.
	 * 
	 * If we try to find the TypeParameterDeclaration associated with a ClosureLiteral 
	 * by searching an occurence of a TypeParameterDeclaration then we may end up finding
	 * a TypeParameterDeclaration instance that actually occurs in the closure body.
	 * 
	 * This method directs its search based on the structure of the parser associated with the match to search.
	 * The search will search the tree of matches but will only consider the 
	 * children of matches whose parser type is a 'combinator' type.
	 * sequence, optional, choice, and repetition parsers are combinator parsers (parsers used to construct other parsers).
	 * All other parsers are non-combinator parsers.
	 * 
	 * In the example above example, by not considering the children of the ClosureBody parser (because ClosureBody is NOT a combinator parser)
	 * then we can be sure that when we search for a match created by the TypeParameterDeclaration parser we will not find a 
	 * match that is actually inside the ClosureBody element.
	 * 
	 */
	public static Match findMatchByType(Match root, Parser type) {
		List<Match> todo = new ArrayList<Match>();
		HashSet<Match> done = new HashSet<Match>();
		todo.addAll(root.getChildren());

		while (!todo.isEmpty()) 
		{
			Match node= todo.remove(0);
			if (done.contains(node))
				continue;
			
			Parser parser= node.getParser();
			if (parser == type) {
				return node;
			}

			// add children in reverse order to beginning of list
			// so that final results will be in same order as a depth
			// first search of the root match
			if (parser.isCombinator()) {
				List<Match> children= node.getChildren();
				for (int i= children.size(); 0 < i--;) {
					Match child= children.get(i);
					todo.add(0, child);
				}
			}
		}

		return null;
	}
	public static <T extends Match> List<T> findAllMatchByType(Match root, Parser type) {
		List<T> matches = new ArrayList<T>();
		List<Match> todo = new ArrayList<Match>();
		HashSet<Match> done = new HashSet<Match>();
		todo.addAll(root.getChildren());

		while (!todo.isEmpty()) 
		{
			Match node= todo.remove(0);
			if (done.contains(node))
				continue;
			
			Parser parser= node.getParser();
			if (parser == type) {
				matches.add((T)node);
				continue;
			}

			// add children in reverse order to beginning of list
			// so that final results will be in same order as a depth
			// first search of the root match
			if (parser.isCombinator()) {
				List<Match> children= node.getChildren();
				for (int i= children.size(); 0 < i--;) {
					Match child= children.get(i);
					todo.add(0, child);
				}
			}
		}

		return matches;
	}

}
