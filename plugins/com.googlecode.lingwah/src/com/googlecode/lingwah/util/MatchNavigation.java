package com.googlecode.lingwah.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.googlecode.lingwah.Match;
import com.googlecode.lingwah.Parser;

public class MatchNavigation
{

	@SuppressWarnings("unchecked")
	public static <T extends Match> List<T> findAllByType(Match root, Parser type) {
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

	@SuppressWarnings("unchecked")
	public static <T extends Match> T findFirstByType(Match root, Parser type) {
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

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
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

}
