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

		if(ch.isDigit()) {
			return scanNumber(ch);
		}
		else if(ch.isIdentifierStart()) {
			return scanIdentifier(ch);
		}
		else if(isCommentStart(ch)) {
			scanComment(ch);
			return findNextToken();
		}
		else if(isStringStart(ch)) {
			return scanString(ch);
		}
		else if(ch.isCharacterStart()) {
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
	private Token scanNumber(LocatedChar firstChar) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(firstChar.getCharacter());
		appendSubsequentDigits(buffer);
		
		return IntegerToken.make(firstChar.getLocation(), buffer.toString());
		
	}

//	private Token scanNumber(LocatedChar firstChar) {
//		StringBuffer buffer = new StringBuffer();
//		char ch = firstChar.getCharacter();
//		buffer.append(ch);
//		
//		if (ch == '.') {
//			LocatedChar c = firstChar;
//			LocatedChar c2 = input.next();
//			if (c2.isWhitespace()) {
//				input.pushback(c2);
//				input.pushback(c);
//				return PunctuatorScanner.scan(firstChar, input);
//			} else {
//				
//			}
//		}
//		
//		// step 1: finish reading all digits to the left of the decimal
//		if (ch == '+' || ch == '-' || firstChar.isDigit()) {
//			appendSubsequentDigits(buffer);
//			LocatedChar c = input.next();
//		}
//		
//		
//		if (c.getCharacter() == '.') {
//			LocatedChar c2 = input.next();
//			if (c2.isDigit()) {
//				buffer.append('.');
//				buffer.append(c2.getCharacter());
//				appendSubsequentDigits(buffer);
//			}
//			else {
//				input.pushback(c2);
//				input.pushback(c);
//				return IntegerToken.make(firstChar.getLocation(), buffer.toString());
//			}
//		}
//		else {
//			input.pushback(c);
//			return IntegerToken.make(firstChar.getLocation(), buffer.toString());
//		}
//		
//		// step 3. look for the E
//		c = input.next();
//		if (c.getCharacter() == 'E') {
//			c = input.next();
//			ch = c.getCharacter();
//			if (c.isDigit()) {
//				buffer.append('E');
//				buffer.append(c.getCharacter());
//				appendSubsequentDigits(buffer);
//			}
//			else if (ch == '+' || ch == '-') {
//				c = input.next();
//				if (c.isDigit()) {
//					buffer.append('E');
//					buffer.append(ch);
//					buffer.append(c.getCharacter());
//					appendSubsequentDigits(buffer);
//				}
//			}
//			return FloatingToken.make(firstChar.getLocation(), buffer.toString());
//		}
//		else {
//			input.pushback(c);
//			return FloatingToken.make(firstChar.getLocation(), buffer.toString());
//		}
//
////		buffer.append(firstChar.getCharacter());
////		appendSubsequentDigits(buffer);					// may append 0 digits
////		
////		// look for a decimal point
////		LocatedChar c = input.next();
////		if (c.getCharacter() == '.') {
////			c = input.next();
////			if (c.isDigit()) {
////				// do valid digit stuff
////				buffer.append('.');
////				buffer.append(c.getCharacter());
////				appendSubsequentDigits(buffer);
////				
////				return FloatingToken.make(firstChar.getLocation(), buffer.toString());
////			} else {
////				// do nonvalid stuff
////				input.pushback(c);
////				return IntegerToken.make(firstChar.getLocation(), buffer.toString());
////			}
////		} else {
////			input.pushback(c);
////			return IntegerToken.make(firstChar.getLocation(), buffer.toString());
////		}
//	}
	
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
	private boolean isCommentStart(LocatedChar lc) {
		return lc.getCharacter() == '#';
	}

	private void scanComment(LocatedChar firstChar) {
		LocatedChar c = input.next();
		while(c.getCharacter() != '#' && c.getCharacter() != '\n') {
			c = input.next();
		}
		input.pushback(c);
	}

	//////////////////////////////////////////////////////////////////////////////
	// String lexical analysis
	private boolean isStringStart(LocatedChar lc) {
		return lc.getCharacter() == '"';
	}
	
	private Token scanString(LocatedChar firstChar) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(firstChar.getCharacter());
		appendToString(buffer);
		
		return StringToken.make(firstChar.getLocation(), buffer.toString());
	}
	
	private void appendToString(StringBuffer buffer) {
		LocatedChar c = input.next();
		while(c.getCharacter() != '"' && c.getCharacter() != '\n') {
			buffer.append(c.getCharacter());
			c = input.next();
		}
		input.pushback(c);
	}
	
	//////////////////////////////////////////////////////////////////////////////
	// Character lexical analysis
	private Token scanCharacter(LocatedChar firstChar) {
		StringBuffer buffer = new StringBuffer();
		char ch = input.next().getCharacter();
		assert (ch >= 32 && ch <= 126);
		buffer.append(ch);
		input.next(); // read the ^ symbol
		
		return CharacterToken.make(firstChar.getLocation(), buffer.toString());
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
