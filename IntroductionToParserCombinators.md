Many software developers occasionally need to parse data or information out of files.
Perhaps you have written a parser yourself or have used an off-the-shelf tool like [ANTLR](http://www.antlr.org/).
It is likely that you found the process of creating a parser to be tedious, frustrating, and error-prone.
Parser combinators make it almost too easy to create a parser for a complex language without ever leaving the comfortable play-pen afforded by your Java IDE.
Incidentally, if you aren't familiar with the fundamentals of text parsing, context-free grammars and/or parser generators, then you might want to [do](http://en.wikipedia.org/wiki/Context-free_grammar) [some](http://en.wikipedia.org/wiki/LL_parsing) [reading](http://en.wikipedia.org/wiki/Recursive_descent) before you continue.

# Intro to Parser Combinators #
In most languages, the process of creating a text parser is usually an arduous and clumsy affair involving a parser generator (such as ANTLR, JavaCC, Beaver or ScalaBison) and (usually) a lexer generator such as JFlex.
These tools do a very good job of generating sources for efficient and powerful parsers, but they aren't exactly the easiest tools to use.
They generally have a very steep learning curve, and due to their unique status as compiler-compilers, an unintuitive architecture.
Additionally, these tools can be somewhat rigid, making it very difficult to implement unique or experimental features.
For this reason alone, many real world compilers and interpreters (such as javac, ruby, jruby and scalac) actually use hand-written parsers.
These are usually easier to tweak, but they can be very difficult to develop and test.

When creating a compiler in Java, it is perfectly acceptable to make use of these conventional Java-generating tools like ANTLR or Beaver, but we do have other options.
Parser combinators are a domain-specific language baked into a standard Java library.
Using this internal DSL, we can create an instance of a parser for a given grammar using Java.
What's more, we can do this in a very declarative fashion.
Thanks to the magic of DSLs, our sources will actually look like a plain-Jane context-free grammar for our language.
This means that we get most of the benefits of a hand-written parser without losing the maintainability afforded by parser generators like bison.

In this tutorial we will use a classic example, a calculator, to show how lingwah's declarative API can be used to construct a parser.

Our calculator should be able to:

  * support calculation of decimal numbers.
  * support Operators such as '+', '-', '**', '/'.
  * group expressions with '(' and ')'.
  * allow java style inline comment enclosed by "/**" and "**/".
  * allow the use of whitespace.  So "1+2" is equivalent to "1 + 2".**

In this tutorial we will develop a class named 'Calculator' with a single static method that parses an expression and returns the decimal value of the expression.
Here is an example of how our calculator will work:
```
// prints "answer=2082.5615313302951609592032881793"
System.out.println("answer="+Calculator.parse("(6543.56 - 1)/ 3.14159265/*Pi*/");
```


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



EBNF code expresses the grammar of a computer language.
EBNF consists production rules which determine how symbols can be combined into a legal sequence.
An EBNF production rules can be divided into type types: terminals and non-terminals.
Examples of terminal symbols include alphanumeric characters, punctuation marks, and white space characters.
Non-terminal rules are combinations of terminal rules.
EBNF allows the the following kinds of non-terminal rules:
> | Rule | Notation |
|:-----|:---------|
> | concatenation | , |
> | termination | ; |
> | alternation	 | |  |
> | option | [... ](.md) |
> | repetition (zero or more) | { ... } |
> | grouping | ( ... ) |
> | special sequence | ? ... ? |

So for instance, this rule..
` ws	= ' ' | '\t' | '\n' | '\r' | inline_comment; `
...defines a production rule named 'ws'.
This rule will match any text that:
  * consists of a single ' ' character.
  * consists of a single '\t' character.
  * consists of a single '\n' character.
  * consists of a single '\r' character.
  * matches the inline\_comment rule.

This rule...
` number = digit, { digit }; `
...defines a production rule named 'number' that will match any text that consists of one or more instances of text that matches the 'digit' rule.






For example, here is a very simple grammar for a Pascal-like programming language that allows only assignments, defined in [Extended Backus–Naur Form](http://en.wikipedia.org/wiki/Extended_Backus%E2%80%93Naur_Form):
```
(* a simple program syntax in EBNF − Wikipedia *)
all characters = ? all visible characters ? ;
digit = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ;
alphabetic character = "A" | "B" | "C" | "D" | "E" | "F" | "G"
                     | "H" | "I" | "J" | "K" | "L" | "M" | "N"
                     | "O" | "P" | "Q" | "R" | "S" | "T" | "U"
                     | "V" | "W" | "X" | "Y" | "Z" ;
identifier = alphabetic character , { alphabetic character | digit } ;
number = [ "-" ] , digit , { digit } ;
string = '"' , { all characters − '"' } , '"' ;
assignment = identifier , ":=" , ( number | identifier | string ) ;
white space = ? white space characters ? ;
program = 'PROGRAM' , white space , identifier , white space ,
           'BEGIN' , white space ,
           { assignment , ";" , white space } ,
           'END.' ;
```

Here is the same grammar expressed in Java using Lingwah:
```
/**
 * A simple grammar similar to Pascal
 * @author Ted Stockwell
 */
public class SimplePascalGrammar extends Grammar {
	public final Parser all= anyChar();
	public final Parser digit = cho(str("0"), str("1"), str("2"), str("3"), str("4"), str("5"), str("6"), str("7"), str("8"), str("9"));
	public final Parser character = cho(str("A"), str("B"), str("C"), str("D"), str("E"), str("F"), str("G"),
			str("H"), str("I"), str("J"), str("K"), str("L"), str("M"), str("N"), 
			str("O"), str("P"), str("Q"), str("R"), str("S"), str("T"), str("U"), 
			str("V"), str("W"), str("X"), str("Y"), str("Z"));
	public final Parser identifier = seq(character, zeroOrMore(cho(character,digit)));
	public final Parser number = seq(opt(str("-")), digit , zeroOrMore(digit));
	public final Parser string = seq(str("\"") , zeroOrMore(all.excluding(str("\""))), str("\""));
	public final Parser assignment = seq(identifier, str(":=") , cho( number, identifier, string ));
	public final Parser whitespace = new WhitespaceParser();
	public final Parser program = seq(str("PROGRAM") , whitespace , identifier , whitespace ,
	           str("BEGIN") , whitespace ,
	           zeroOrMore(seq(assignment, str(";"), whitespace )),
	           str("END."));
	
	public SimplePascalGrammar() {
		init();
	}
}
```
This is all valid and correct Java.
An instance of the SimplePascalGrammar class can then be used to process a java.io.Reader, producing some result if the input is valid, otherwise producing an error.

Here is code that shows how to use the SimplePascalGrammar class:
```
Reader input= new FileReader("example.scala");
Grammar grammar= new SimplePascalGrammar();
ParseResults results= new ParseContext().parse(grammar.program, input);
assert results.success();
```

The important thing to note in the previous example is that the developer is not required to implement anything to create a parser with lingwah.
Instead, the developer merely describes the grammar to be parsed by creating a class that describes the grammar.
The SimplePascalGrammar class does not actually do anything - it is merely a description of a grammar using basic constructs.
Each parser declared by SimplePascalGrammar class describes some element of the associated grammar.
To parse some input a ParseContext (the context manages the parsing process) is created and a parser and some input is passed to it.
In the above example we are parsing a complete document so we use the grammar.program parser.
If we wanted to just parse an expression then we would use the grammar.expr parser.
