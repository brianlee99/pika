package lexicalAnalyzer;


import logging.PikaLogger;

import inputHandler.InputHandler;
import inputHandler.LocatedChar;
import inputHandler.LocatedCharStream;
import inputHandler.PushbackCharStream;
import inputHandler.TextLocation;
import tokens.CharacterToken;
import tokens.FloatingToken;
import tokens.IdentifierToken;
import tokens.LextantToken;
import tokens.NullToken;
import tokens.StringToken;
import tokens.IntegerToken;
import tokens.Token;

import static lexicalAnalyzer.PunctuatorScanningAids.*;

public class LexicalAnalyzer extends ScannerImp implements Scanner {
	public static LexicalAnalyzer make(String filename) {
		InputHandler handler = InputHandler.fromFilename(filename);
		PushbackCharStream charStream = PushbackCharStream.make(handler);
		return new LexicalAnalyzer(charStream);
	}

	public LexicalAnalyzer(PushbackCharStream input) {
		super(input);
	}

	
	//////////////////////////////////////////////////////////////////////////////
	// Token-finding main dispatch	

	@Override
	protected Token findNextToken() {
		LocatedChar ch = nextNonWhitespaceChar();

		if(isNumberStart(ch)) {
			return scanNumber(ch);
		}
		else if(ch.isIdentifierStart()) {
			return scanIdentifier(ch);
		}
		// simply scan by all characters; don't make a token
		else if(ch.isCommentStart()) {
			scanComment(ch);
			return findNextToken();
		}
		else if(ch.isStringStartOrEnd()) {
			return scanString(ch);
		}
		else if(ch.isCharacterStartOrEnd()) {
			return scanCharacter(ch);
		}
		else if(isPunctuatorStart(ch)) {
			return PunctuatorScanner.scan(ch, input);
		}
		else if(isEndOfInput(ch)) {
			return NullToken.make(ch.getLocation());
		}
		else {
			lexicalError(ch);
			return findNextToken();
		}
	}


	private LocatedChar nextNonWhitespaceChar() {
		LocatedChar ch = input.next();
		while(ch.isWhitespace()) {
			ch = input.next();
		}
		return ch;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Integer/floating lexical analysis
	public boolean isNumberStart(LocatedChar firstChar) {
		char ch = firstChar.getCharacter();
		if (firstChar.isDigit())
			return true;
		else if (ch == '+' || ch == '-')
			return numberFollows() || decimalNumberFollows();
		else if (ch == '.')
			return numberFollows();
		else
			return false;
	}
	
	public boolean decimalNumberFollows() {
		// peek at the next character
		LocatedChar c = input.next();
		boolean isDecimal = c.isDecimal();
		boolean hasNumberAfterDecimal = isDecimal ? numberFollows() : false;
		input.pushback(c);
		return isDecimal && hasNumberAfterDecimal;
	}
	
	public boolean numberFollows() {
		LocatedChar c = input.next();
		boolean result = c.isDigit();
		input.pushback(c);
		return result;
	}
	
	private Token scanNumber(LocatedChar firstChar) {
		StringBuffer buffer = new StringBuffer();
		LocatedChar c = firstChar;
		
		// Append everything to the left of the first decimal
		if (!c.isDecimal()) {
			buffer.append(c.getCharacter());
			appendSubsequentDigits(buffer);
			c = input.next();
		}
		
		if (!c.isDecimal() || !numberFollows()) {
			input.pushback(c);
			return IntegerToken.make(firstChar.getLocation(), buffer.toString());
		}
		
		// Append the decimal point (and all digits after)
		buffer.append(c.getCharacter());
		appendSubsequentDigits(buffer);

		c = input.next();
		if (!c.isExponent()) {
			input.pushback(c);
			return FloatingToken.make(firstChar.getLocation(), buffer.toString());
		}
		
		// Append the E
		buffer.append(c.getCharacter());
		c = input.next();
		if (c.isSign()) {
			// Append the sign (if there is one)
			buffer.append(c.getCharacter());
			c = input.next();
		}
		
		if (!c.isDigit()) {
			// ERROR: the exponent must have a number or sign following E
			PikaLogger log = PikaLogger.getLogger("compiler.lexicalAnalyzer");
			log.severe("Lexical error: E must be followed by a number or sign");
			return findNextToken();
		}
		
		// Append the first digit after the E
		buffer.append(c.getCharacter());
		appendSubsequentDigits(buffer);

		return FloatingToken.make(firstChar.getLocation(), buffer.toString());

	}
	

	
	private void appendSubsequentDigits(StringBuffer buffer) {
		LocatedChar c = input.next();
		while(c.isDigit()) {
			buffer.append(c.getCharacter());
			c = input.next();
		}
		input.pushback(c);
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Identifier and keyword lexical analysis	

	private Token scanIdentifier(LocatedChar firstChar) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(firstChar.getCharacter());
		appendToIdentifier(buffer);
		String lexeme = buffer.toString();
		
		if (lexeme.length() > 32) {
			PikaLogger log = PikaLogger.getLogger("compiler.lexicalAnalyzer");
			log.severe("Lexical error: identifier must be at most 32 characters");
			return findNextToken();
		}
		
		if(Keyword.isAKeyword(lexeme)) {
			return LextantToken.make(firstChar.getLocation(), lexeme, Keyword.forLexeme(lexeme));
		}
		else {
			return IdentifierToken.make(firstChar.getLocation(), lexeme);
		}
	}
	
	private void appendToIdentifier(StringBuffer buffer) {
		LocatedChar c = input.next();
		while(c.isIdentifierContinue()) {
			buffer.append(c.getCharacter());
			c = input.next();
		}
		input.pushback(c);
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Punctuator lexical analysis	
	// old method left in to show a simple scanning method.
	// current method is the algorithm object PunctuatorScanner.java

	@SuppressWarnings("unused")
	private Token oldScanPunctuator(LocatedChar ch) {
		TextLocation location = ch.getLocation();
		
		switch(ch.getCharacter()) {
		case '*':
			return LextantToken.make(location, "*", Punctuator.MULTIPLY);
		case '+':
			return LextantToken.make(location, "+", Punctuator.ADD);
		case '>':
			return LextantToken.make(location, ">", Punctuator.GREATER);
		case ':':
			if(ch.getCharacter()=='=') {
				return LextantToken.make(location, ":=", Punctuator.ASSIGN);
			}
			else {
				throw new IllegalArgumentException("found : not followed by = in scanOperator");
			}
		case ',':
			return LextantToken.make(location, ",", Punctuator.SEPARATOR);
		case ';':
			return LextantToken.make(location, ";", Punctuator.TERMINATOR);
		default:
			throw new IllegalArgumentException("bad LocatedChar " + ch + "in scanOperator");
		}
	}
	//////////////////////////////////////////////////////////////////////////////
	// Comment lexical analysis
	private void scanComment(LocatedChar firstChar) {
		// firstChar should be a #, so skip it
		LocatedChar c = input.next();
		while (c.isCommentContinue()) {
			c = input.next();
		}
		// skip the \n or # (which ends a comment)
		// input.next();
	}

	//////////////////////////////////////////////////////////////////////////////
	// String lexical analysis
	private Token scanString(LocatedChar firstChar) {
		StringBuffer buffer = new StringBuffer();
		LocatedChar c = input.next();
		while (c.isStringContinue()) {
			buffer.append(c.getCharacter());
			c = input.next();
		}
		
		// valid string
		if (c.isStringStartOrEnd()) {
			return StringToken.make(firstChar.getLocation(), buffer.toString());
		}
		// invalid string, wasn't terminated with "
		else {
			PikaLogger log = PikaLogger.getLogger("compiler.lexicalAnalyzer");
			log.severe("Lexical error: string must terminate with \"");
			return findNextToken();
		}
		
	}
	
	//////////////////////////////////////////////////////////////////////////////
	// Character lexical analysis
	private Token scanCharacter(LocatedChar firstChar) {
		
		StringBuffer buffer = new StringBuffer();
		LocatedChar c = input.next();
		
		if (!c.isValidCharacter()) {
			PikaLogger log = PikaLogger.getLogger("compiler.lexicalAnalyzer");
			log.severe("Lexical error: character must be between ASCII 32 and 126");
			return findNextToken();
		}
		
		char ch = c.getCharacter();
		c = input.next();
		if (c.isCharacterStartOrEnd()) {
			buffer.append(ch);
			return CharacterToken.make(firstChar.getLocation(), buffer.toString());
		}
		else {
			PikaLogger log = PikaLogger.getLogger("compiler.lexicalAnalyzer");
			log.severe("Lexical error: character must terminate with ^");
			return findNextToken();
		}
		

	}
	
	//////////////////////////////////////////////////////////////////////////////
	// Character-classification routines specific to Pika scanning.	

	
	private boolean isPunctuatorStart(LocatedChar lc) {
		char c = lc.getCharacter();
		return isPunctuatorStartingCharacter(c);
	}

	private boolean isEndOfInput(LocatedChar lc) {
		return lc == LocatedCharStream.FLAG_END_OF_INPUT;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Error-reporting	

	private void lexicalError(LocatedChar ch) {
		PikaLogger log = PikaLogger.getLogger("compiler.lexicalAnalyzer");
		log.severe("Lexical error: invalid character " + ch);
	}

	
}
