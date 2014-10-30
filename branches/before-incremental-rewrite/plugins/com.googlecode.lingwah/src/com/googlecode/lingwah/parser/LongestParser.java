/**
 * 
 */
package com.googlecode.lingwah.parser;


import java.util.Arrays;
import java.util.List;

import com.googlecode.lingwah.Match;
import com.googlecode.lingwah.ParseContext;
import com.googlecode.lingwah.ParseError;
import com.googlecode.lingwah.ParseResults;
import com.googlecode.lingwah.Parser;
import com.googlecode.lingwah.Parsers;
import com.googlecode.lingwah.exception.RecursiveMatchersNotSupported;

/**
 * Similar to RepetitionParser but only returns the longest possible match.
 * May not be used with recursive parsers.
 * 
 * @author Ted Stockwell
 */
public final class LongestParser extends CombinatorParser
{
	private final Parser _matcher;

	public LongestParser(Parser parser)
	{
		_matcher= parser;
	}
	
	@Override
	public String getDefaultLabel() {
		return "longest("+_matcher.getLabel()+")";
	}

	@Override
	public void startMatching(final ParseContext ctx, final int start, final ParseResults targetResults) {
		if (_matcher.isRecursive())
			throw new RecursiveMatchersNotSupported(this);
		
		
		class RepetitionListener implements ParseResults.Listener {
			private Match _previousMatch;
			RepetitionListener(Match previousMatch) {
				_previousMatch= previousMatch;
			}
			@Override
			public void onMatchFound(ParseResults results, final Match nextNode) {
				Match match= nextNode;
				if (_previousMatch != null)
					match= Match.create(ctx, targetResults.getMatcher(), _previousMatch.getStart(), nextNode.getEnd());
				ctx.doMatch(_matcher, nextNode.getEnd()).addListener(new RepetitionListener(match));
			}
			@Override
			public void onMatchError(ParseResults results, ParseError parseError) {
				if (_previousMatch != null) {
					targetResults.addMatch(_previousMatch);
				}
				else
					targetResults.setError(parseError);
			}
		};
		
		ctx.doMatch(_matcher, start).addListener(new RepetitionListener(null));
	}
	
	@Override
	public List<Parser> getDependencies() {
		return Arrays.asList(new Parser[] { _matcher });
	}
	
	public Parser separatedBy(Parser separator) {
		return Parsers.seq(_matcher, Parsers.opt(Parsers.rep(Parsers.seq(separator, _matcher))));
	}
	public final Parser sep(Parser separator) {
		return separatedBy(separator);
	}
}