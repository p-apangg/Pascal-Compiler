# **Pascal Compiler**

The work are seperated into 4 subparts: lexer, parser, checker and compiler. The code is in src folder and reports in japanese language are in report folder.

**1. Lexer**
   
   This phase of compiler extract token sequences from program written in Pascal-like languages (pas file) and save it as ts file.

**2. Parser (構文解析器)**

  This phase of compiler take the output extracted from the first phase, which are received as input from a ts file, and produces the syntax judgment results.
   

**3. Checker (意味解析器)**

The semantic analyzer involves building variable and function tables, checking for each call if they are defined, and verifying if there are any semantic errors
   

**4. Compiler**

This task involves creating a compiler for the objective code written in CASL-II. This compiler takes Pascal-like programs, tokenized from Task 1 as input. If a file is not found, it outputs 'File not found' to standard error. In case of syntax errors during parsing, it outputs 'Syntax error: line X' for the line X where the error is found. Similarly, for semantic errors, it outputs 'Semantic error: line X'. However, if there are no errors, it generates a program written in CASL-II (cas file)
