# Why Lingwah? #

Lingwah is a tool for creating [parsers](http://en.wikipedia.org/wiki/Parsing).
There are many such tools currently available, most of them emphasize parser
performance over ease of development.  Most importantly, almost all available
parsing tools require the developer to refactor their grammar to remove
[left recursion](http://en.wikipedia.org/wiki/Left_recursion).
This can be a significant burden when developing a non-trivial grammar.
Lingwah was developed with an emphasis on ease of use over performance and is
built to parse context-free grammars without requiring the grammars to be refactored.

Now, just because Lingwah purposely takes an approach that is known to not be
the most performant does not mean that Lingwah is slow.  Lingwah is 'fast enough'.
Lingwah uses sophisticated memoization and backtracking techniques to accomplish
its' goals.  The techniques used by Linqwah have been around for decades but
have not been favored for performance reasons.
Lingwah uses the memoization and 'continuation-passing' techniques described
in the paper ["Memoization in Top-Down Parsing"](http://arxiv.org/PS_cache/cmp-lg/pdf/9504/9504016v1.pdf) by Mark Johnson.
Computers are now many magnitudes of order faster than they were when
the LR parser was invented by Donald Knuth, and the amount of memory available has also increased
proportionately.
It's time to increase our productivity by trading some parsing speed for some
convenience.

Besides providing a significant advantage of convenience over other parser combinator tools,
Lingwah also provides the advantages of parser combinators over of 'old-school' parser generators (like ANTLR):

  * Does not complicate the parsing process by dividing it into in lexing (token generation) and token parsing phases
  * No special, non-java grammar syntax in separate project files (i.e. an external DSL)
  * No special build steps required
  * No 'untouchable', generated java source files in your project which need to be kept in sync with the grammar specification.