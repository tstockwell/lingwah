# Lingwah Tutorial #

In this tutorial we will use a classic example, a calculator, to show how lingwah's declarative API can be used to construct a parser.

Our calculator should be able to:

  * support calculation of decimal numbers.
  * support Operators such as '+', '-', '`*`', '/'.
  * group expressions with '(' and ')'.
  * allow java style inline comment enclosed by "/`*`" and "`*`/".
  * allow the use of whitespace.  So "1+2" is equivalent to "1 + 2".

In this tutorial we will develop a class named 'Calculator' with a single static method that parses an expression and returns the decimal value of the expression.
Here is an example of how our calculator will work:
```
// prints "answer=2082.5615313302951609592032881793"
System.out.println("answer="+Calculator.parse("(6543.56 - 1)/ 3.14159265/*Pi*/");
```

# The Lingwah Way #

Lingwah is used in two steps:
  1. Parse some text and create an AST (Abstract Syntax Tree).
> > An AST is an object that represents the structure of the parsed text.
  1. Navigate and process the AST to create a result from the parsed text.

## Lingwah Grammars ##
Lingwah needs a grammar definition in order to parse text.
In lingwah a grammar definition is represented by an instance of the Grammar class.
The details of creating a lingwah grammar are covered later.

## Lingwah ASTs ##
In lingwah an AST is represented by an instance the Match class.
An instance of Match may include:
  * an error message if there was no match.
  * a reference to the Parser that found the Match (the Parser reference is used to identify the Grammar element associated which the Match)
  * a list of sub-Matches that describes the sub-elements of the matched grammar element.
The details of how to navigate and process a lingwah Match are covered later.

## Creating the Calculator ##
To create our Calculator we will be required to create three classes:
  1. A subclass of the lingwah Grammar class that defines the calculator grammar.
  1. A subclass of the lingwah MatchProcessor class that processes Match instances.
  1. The Calculator class itself, which just provides a convenient API parsing text and getting a result.

# The Calculator Grammar #

To understand how to specify a grammar using parser combinators it helps to have a passing knowledge of [Extended Backus-Naur Form (EBNF)](http://en.wikipedia.org/wiki/Extended_Backus%E2%80%93Naur_Form), a way of specifying how a language will look.
Our calculator language is described by the following EBNF:

```
inline_comment = "/*", {?any_character?}, "/*"; 
blank	= ' ' | '\t' | '\n' | '\r';
ws	= (blank, {blank}) | inline_comment;
digit = '0'|'1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9';
number = digit, { digit };
decimal = [ '-' ], (number, ['.', [number]] | '.', number);
addition = expr, [ws], '+', [ws], expr;
subtraction = expr, [ws], '-', [ws], expr;
multiplication = (expr, [ws], '*', [ws], expr) | (expr, [ws], expr);
division = expr, [ws], '\', [ws], expr;
group = '(', [ws], expr, [ws], ')';
expr = decimal | addition | subtraction | multiplication | division | group;
```


# Defining a Grammar in Lingwah #

Here is how the EBNF grammar from the previous section is written in Java using the lingwah API:
```
import com.googlecode.lingwah.Grammar;
import com.googlecode.lingwah.Parser;
import com.googlecode.lingwah.parser.ParserReference;

public class CalculatorGrammar extends Grammar {
	public final Parser inline_comment = seq(str("/*"), zeroOrMore(anyChar()), str("*/")); 
	public final Parser ws	= oneOrMore(cho(oneOrMore(regex("[ \t\n\f\r]")), inline_comment));
	public final Parser digit = regex("[0-9]");
	public final Parser number = oneOrMore(digit);
	public final Parser decimal = seq(opt(str('-')), seq(number, opt(seq(str('.'), number))));
	public final ParserReference expr = ref();
	public final Parser addition = seq(expr, str('+'), expr).separatedBy(opt(ws));
	public final Parser subtraction = seq(expr, str('-'), expr).separatedBy(opt(ws));
	public final Parser multiplication = seq(expr, str('*'), expr).separatedBy(opt(ws));
	public final Parser division = seq(expr, str('/'), expr).separatedBy(opt(ws));
	public final Parser group = seq(str('('), expr, str(')')).separatedBy(opt(ws));
	{
		expr.define(cho(decimal, addition, subtraction, multiplication, division, group));
	}
	
	private CalculatorGrammar() {
		init();
	}
	public static final CalculatorGrammar INSTANCE = new CalculatorGrammar();
}
```

Note the following features of this class:
  * For each element of the EBNF grammar there is an equivalent lingwah Parser definition.
> > The lingwah Grammer class includes many functions for creating terminal Parsers and many types of non-terminal parsers.
> > The lingwah Grammer class includes functions for creating all types of non-terminal rules allowed in EBNF such as sequences, choices, and repetitions.
  * The expr rule is recursive.  That is, the expr rule uses rules which in turn use the expr rule.
> > In order to define recursive rules a 'rule reference' is created using the ref() method.
> > Later, after all the dependent rules have been defined the reference may be set using the reference's `define` method.
  * A constructor is created at the bottom of the class definition which calls the Grammar.init() method.
> > It is important that the constructor be declared **at the bottom** of the class and that the init method is called.
> > The init method is responsible for initialing all the declared Parsers, and this can only be done **after** the parsers have been created.
  * For convenience, rules can be defined with short names like 'seq' instead of 'sequence'.
> > However, if you prefer you may use the longer names.
> > The following short names are the most commonly used:
> > <table border='1'>
<blockquote><thead><td><b>long name</b></td><td><b>short name</b></td></thead>
<tr><td>sequence</td><td>seq</td></tr>
<tr><td>choice</td><td>choice</td></tr>
<tr><td>repetition</td><td>rep, or zeroOrMore</td></tr>
<tr><td>optional</td><td>opt</td></tr>
<tr><td>reference</td><td>reference</td></tr>
<tr><td>separatedBy</td><td>sep</td></tr>
</table>
</blockquote>  * When supporting whitespace, instead of polluting a rule definition with a lot whitespace rules we can often just use the 'separatedBy' method to add support for whitespace to a rule.
> > For instance, these two definitions are equivalent:
```
	Parser subtraction = seq(expr, str('-'), expr).sepBy(opt(ws));
	Parser subtraction = seq(expr, opt(ws), str('-'), opt(ws), expr); }}}
```
  * Lingwah includes a regular expression parser that is very convenient for declaring terminal rules.
> > For instance, this EBNF...
> > ` digit = '0'|'1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9'; `
> > ...can be written in lingwah like this...
> > ` Parser digit = regex("[0-9]"); `

# Parsing text and creating an AST #

Parsing text is easy.
A ParseContext is created and then the ParseContext.getParseResults method is invoked with a Parser.
Here is an example:
```
		Document doc= new StringDocument("text to parse");
		ParseContext ctx= new ParseContext(doc);
		ParseResults parseResults= ctx.getParseResults(PARSER);
		if (!parseResults.success())
			throw parseResults.getError();
```

The ParseContext class manages the parsing process.
Among other things, the ParseContext class caches Matches produces by Parsers in order to avoid parsing the same part of a Document with the same Parser more than once.
This makes linwah's parsing process efficient.
The Document class provides an abstract interface that provides access to text.

The ParseContext and Document can work together to allow lingwah to be used in sophisticated situations where the Document is being changed.
When a Document is changed the associated ParseContext is notified of the changes.
The ParseContext then invalidates its' internal cache associated with the changed text and only reparses text that has changed.
This makes lingwah an efficient parser to use for creating text editors.

# Processing an AST in Lingwah #

As mentioned previously, lingwah parses text and creates an AST (and an AST is represented by a Match).
Lingwah provides a convenient API for processing ASTs, the MatchProcessor API.
It is not necessary to use this processor API, ASTs may be navigated and processed in many ways.
The MatchProcessor API is convenient becauses it provides functionality which is commonly needed when processing an AST,
such as managing the navigation of nodes in the AST, caching results associated with nodes, etc.

## The MatchProcessor API ##

Lingwah MatchProcessor API is an implementation of the [Vistor pattern](http://en.wikipedia.org/wiki/Visitor_pattern), and provides a convenient API for processing Match ASTs.

The Match class has a method called accept(MatchProcessor) which accepts a MatchProcessor, calls the MatchProcessor's
process methods, optionally passes the process to all of its children (and so on and so on), and then finally calls the
MatchProcessor's complete method.

Here is the interface that is implemented by all MatchProcessors:
```
public interface MatchProcessor {
	/**
	 * Invoked when visiting a node in the tree
	 * @return true if children of this node should be visited
	 */
	public boolean process(Match node);
	
	/**
	 * Invoked after invoking the visit method and visiting all children nodes   
	 */
	public void complete(Match node);
}
```

A MatchProcessor is very much like a Visitor, except it has two methods:
  * the process method, like the visit method in the Visitor pattern, is invoked when visiting a node in the AST tree, and
  * the complete method, which is invoked after invoking the visit method and visiting all children nodes.
> > The complete method is convenient for computing results which require children nodes to be processed before a result can be calculated.

Lingwah has a default implementation of this interface named AbstractProcessor that provides a convenient implementation of the MatchProcessor interface.
AbstractProcessor allows for subclasses to declare process and complete methods for processing Matches found by specific Parsers.
For instance, our CalculatorGrammar class declares a parser named 'decimal'.
A processor can declare a method specifically for processing Matches produced by the decimal Parser by declaring a method named 'processDecimal'.
Whenever the AbstractProcessor.process(Match) method is invoked with a Match that was produced by the decimal Parser, AbstractProcessor will forward the processing to the processDecimal method.

For instance, below is shown the processor for our calculator example.
The goal of this processor is to calculate a value from a calculator expression.
This process does that by creating a value for every element of the AST produced by the parser.
The value associated with the root Match of the AST will be the value of the expression.
The rest of this section will explain how the processor works.
```
import java.math.*;
import java.util.List;

import com.googlecode.lingwah.*;

@Processes(CalculatorGrammar.class)
public class CalculatorProcessor extends AbstractProcessor {
	
	static final CalculatorGrammar grammar= CalculatorGrammar.INSTANCE;
	
	public void completeAddition(Match addition) {
		List<Match> children= addition.getChildrenByType(grammar.expr);
		BigDecimal left= getResult(children.get(0));
		BigDecimal right= getResult(children.get(1));
		putResult(left.add(right));
	}
	public void completeSubtraction(Match subtraction) {
		List<Match> children= subtraction.getChildrenByType(grammar.expr);
		BigDecimal left= getResult(children.get(0));
		BigDecimal right= getResult(children.get(1));
		putResult(left.subtract(right));
	}
	public void completeMultiplication(Match multiplication) {
		List<Match> children= multiplication.getChildrenByType(grammar.expr);
		BigDecimal left= getResult(children.get(0));
		BigDecimal right= getResult(children.get(1));
		putResult(left.multiply(right));
	}
	public void completeDivision(Match division) {
		List<Match> children= division.getChildrenByType(grammar.expr);
		BigDecimal left= getResult(children.get(0));
		BigDecimal right= getResult(children.get(1));
		putResult(left.divide(right, 28, RoundingMode.HALF_UP));
	}
	public void completeGroup(Match group) {
		putResult(getResult(group.getChildByType(grammar.expr)));
	}
	public void completeDecimal(Match decimal) {
		putResult(new BigDecimal(decimal.getText()));
	}
	public void completeExpr(Match expr) {
		putResult(getResult(expr.getFirstChild()));
	}

	public static BigDecimal process(ParseResults results) {
		CalculatorProcessor processor= new CalculatorProcessor();
		results.getLongestMatch().accept(processor);
		return processor.getResult(results.getLongestMatch());
	}

}
```

First, notice that all the methods begin with 'complete' instead of 'process'.
This is because we want these methods to be invoked **after** their associated children have been processed instead of before thier children are processed.
In this way an element can use the values associated with their subelements to create a new value.

Consider the completeDecimal(Match decimal) method in the processor shown above.
```
	public void completeDecimal(Match decimal) {
		putResult(new BigDecimal(decimal.getText()));
	}
```
When this method is invoked the Match passed will represent an instance found by the CalculatorGrammar.decimal Parser.
To create a value for this Match we simply want to get the text associated with the decimal and create a BigDecimal instance from it.
The BigDecimal value created from the Match text is associated with the given Match instance by calling putResult method and passing the value
(the AbstractProcessor class knows what Match is currently being processed so there is no need to also pass the Match).
Saving the value in this way allows Matches further up the AST tree to use the value associated with the decimal element to create more results.

Next consider the completeAddition(Match addition) method.
When this method is invoked the Match passed will represent an instance found by the CalculatorGrammar.addition Parser.
Recall the definition of the CalculatorGrammar.addition Parser:
```
	public final Parser addition = seq(expr, str('+'), expr).separatedBy(opt(ws));
```
A Match produced the the addition Parser will have these children:
  * The first child will be a Match produced by the expr Parser.
  * The last child will be a Match produced by the expr Parser.
  * In between the first and last children will be a Match produced by the unnamed parser produced by the call to str('+') in the addition Parser definition.
  * There may also be children that represent white space between the '+' operator and the expressions.
In order to produce a value for the addition element we need to get the values associated with the left and right expressions and add those values together.
Here is the code to the completeAddition method again:
```
	public void completeAddition(Match addition) {
		List<Match> children= addition.getChildrenByType(grammar.expr);
		BigDecimal left= getResult(children.get(0));
		BigDecimal right= getResult(children.get(1));
		putResult(left.add(right));
	}
```
The first line calls the getChildrenByType method to get all the children of the given Match produced by the grammar.expr.
The 2nd lines gets the value associated with the left side of the addition and the 3rd line gets the value associated with the left side of the addition.
The last lines adds the values together and saves the result for later.
All the methods that process operators work in the same fashion.

Note that we could have also written the completeAddition this way:
```
	public void completeAddition(Match addition) {
		BigDecimal left= getResult(addition.getFirstChild());
		BigDecimal right= getResult(children.getLastChild());
		putResult(left.add(right));
	}
```
The Match class provides several convenient methods for accessing children.

Now consider the completeExpr(Match expr) method.
When this method is invoked the Match passed will represent an instance found by the CalculatorGrammar.expr Parser:
```
	{
		expr.define(cho(decimal, addition, subtraction, multiplication, division, group));
	}
```
The Match passed to the completeExpr method will have only one child, and that child will be a Match that represents either a decimal, addition, subtraction, multiplication, division, or group.
So, to create a value for an expr element all the completeExpr must do is the the value associated with its child and associate that value with the given expr Match:
```
	public void completeExpr(Match expr) {
		putResult(getResult(expr.getFirstChild()));
	}
```

Finally, our calculator process includes the following convenience method:
```
	public static BigDecimal process(ParseResults results) {
		CalculatorProcessor processor= new CalculatorProcessor();
		Match expr= results.getLongestMatch();
		expr.accept(processor);
		return processor.getResult(expr);
	}
```
This method illustrates how to process an AST.


# Finally, create the Calculator #

Now that we have defined our Grammar and created a corresponding MatchProcessor it is a simple matter to create our Calculator class:

```
public class Calculator {
	static final Parser PARSER= CalculatorGrammar.INSTANCE.expr;
	public static BigDecimal parse(String expression) 
	{
		ParseResults parseResults= ParseContext.parse(PARSER, expression);
		if (!parseResults.success())
			throw parseResults.getError();
		return CalculatorProcessor.process(parseResults);
	}
}
```

This class simply performs the two steps that comprise the typical linwah workflow, parsing some text and then processing the AST.
First the ParseContext.parse method (a convenience method) is invoked to parse an expression.
Then the CalculatorProcessor.process is invoked to calculate a value from the results.


That's it!
Welcome to lingwah.



