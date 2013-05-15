package com.googlecode.lingwah.parser;

import java.util.Arrays;
import java.util.List;

import com.googlecode.lingwah.Match;
import com.googlecode.lingwah.ParseContext;
import com.googlecode.lingwah.ParseError;
import com.googlecode.lingwah.ParseResults;
import com.googlecode.lingwah.Parser;

public class OptionalParser extends CombinatorParser {
	
	private Parser _matcher;

	public OptionalParser(Parser parser) {
		_matcher= parser;
	}
	
	@Override
	public String getDefaultLabel() {
		return "optional("+_matcher.getLabel()+")";
	}

	@Override
	public List<Parser> getDependencies() {
		return Arrays.asList(new Parser[] { _matcher });
	}

	@Override
	public void startMatching(final ParseContext ctx, final int start, final ParseResults targetResults) {
		
		// immediately add a match of zero length if the parser is recursive
		if (_matcher.isRecursive())
			targetResults.addMatch(Match.create(ctx, this, start, start));
		
		// add any other matches that are found
		ctx.doMatch(_matcher, start).addListener(new ParseResults.Listener() {
			@Override
			public void onMatchFound(ParseResults results, Match node) {
				targetResults.addMatch(node);
			}
			
			@Override
			public void onMatchError(ParseResults results, ParseError parseError) {
				if (!_matcher.isRecursive()) {
					targetResults.addMatch(Match.create(ctx, targetResults.getMatcher(), start, start));
				}
				else
					targetResults.setError(parseError);			
			}
		});
	}
}