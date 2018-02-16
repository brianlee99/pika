package parser;

import java.util.ArrayList;
import java.util.Arrays;

import logging.PikaLogger;
import parseTree.*;
import parseTree.nodeTypes.ArrayPopulationNode;
import parseTree.nodeTypes.AssignmentNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.ControlFlowStatementNode;
import parseTree.nodeTypes.BlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TabNode;
import parseTree.nodeTypes.TypeNode;
import tokens.*;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import lexicalAnalyzer.Scanner;


public class Parser {
	private Scanner scanner;
	private Token nowReading;
	private Token previouslyRead;
	
	public static ParseNode parse(Scanner scanner) {
		Parser parser = new Parser(scanner);
		return parser.parse();
	}
	public Parser(Scanner scanner) {
		super();
		this.scanner = scanner;
	}
	
	public ParseNode parse() {
		readToken();
		return parseProgram();
	}

	////////////////////////////////////////////////////////////
	// "program" is the start symbol S
	// S -> EXEC mainBlock
	
	private ParseNode parseProgram() {
		if(!startsProgram(nowReading)) {
			return syntaxErrorNode("program");
		}
		ParseNode program = new ProgramNode(nowReading);
		
		expect(Keyword.EXEC);
		ParseNode mainBlock = parseBlock();
		program.appendChild(mainBlock);
		
		if(!(nowReading instanceof NullToken)) {
			return syntaxErrorNode("end of program");
		}
		
		return program;
	}
	private boolean startsProgram(Token token) {
		return token.isLextant(Keyword.EXEC);
	}
	
	
	///////////////////////////////////////////////////////////
	// mainBlock
	
	// mainBlock -> { statement* }
	private ParseNode parseBlock() {
		if(!startsBlock(nowReading)) {
			return syntaxErrorNode("block");
		}
		ParseNode block = new BlockNode(nowReading);
		expect(Punctuator.OPEN_BRACE);
		
		while(startsStatement(nowReading)) {
			ParseNode statement = parseStatement();
			block.appendChild(statement);
		}
		expect(Punctuator.CLOSE_BRACE);
		return block;
	}
	private boolean startsBlock(Token token) {
		return token.isLextant(Punctuator.OPEN_BRACE);
	}
	
	
	///////////////////////////////////////////////////////////
	// statements
	
	// statement-> declaration | printStmt
	private ParseNode parseStatement() {
		if(!startsStatement(nowReading)) {
			return syntaxErrorNode("statement");
		}
		if(startsDeclaration(nowReading)) {
			return parseDeclaration();
		}
		if(startsAssignment(nowReading)) {
			return parseAssignment();
		}
		if(startsPrintStatement(nowReading)) {
			return parsePrintStatement();
		}
		if(startsBlock(nowReading)) {
			return parseBlock();
		}
		if(startsControlFlowStatement(nowReading)) {
			return parseControlFlowStatement();
		}
		return syntaxErrorNode("statement");
	}
	private boolean startsStatement(Token token) {
		return startsPrintStatement(token) ||
				startsDeclaration(token) ||
				startsAssignment(token) ||
				startsBlock(token) ||
				startsControlFlowStatement(token);
	}
	
	// printStmt -> PRINT printExpressionList .
	private ParseNode parsePrintStatement() {
		if(!startsPrintStatement(nowReading)) {
			return syntaxErrorNode("print statement");
		}
		PrintStatementNode result = new PrintStatementNode(nowReading);
		
		readToken();
		result = parsePrintExpressionList(result);
		
		expect(Punctuator.TERMINATOR);
		return result;
	}
	private boolean startsPrintStatement(Token token) {
		return token.isLextant(Keyword.PRINT);
	}	

	// This adds the printExpressions it parses to the children of the given parent
	// printExpressionList -> printExpression* bowtie (,|;)  (note that this is nullable)

	private PrintStatementNode parsePrintExpressionList(PrintStatementNode parent) {
		while(startsPrintExpression(nowReading) || startsPrintSeparator(nowReading)) {
			parsePrintExpression(parent);
			parsePrintSeparator(parent);
		}
		return parent;
	}
	

	// This adds the printExpression it parses to the children of the given parent
	// printExpression -> (expr | nl)?     (nullable)
	
	private void parsePrintExpression(PrintStatementNode parent) {
		if(startsExpression(nowReading)) {
			ParseNode child = parseExpression();
			parent.appendChild(child);
		}
		else if(nowReading.isLextant(Keyword.NEWLINE)) {
			readToken();
			ParseNode child = new NewlineNode(previouslyRead);
			parent.appendChild(child);
		}
		else if(nowReading.isLextant(Keyword.TAB)) {
			readToken();
			ParseNode child = new TabNode(previouslyRead);
			parent.appendChild(child);
		}
		// else we interpret the printExpression as epsilon, and do nothing
	}
	private boolean startsPrintExpression(Token token) {
		return startsExpression(token) || token.isLextant(Keyword.NEWLINE, Keyword.TAB) ;
	}
	
	
	// This adds the printExpression it parses to the children of the given parent
	// printExpression -> expr? ,? nl? 
	
	private void parsePrintSeparator(PrintStatementNode parent) {
		if(!startsPrintSeparator(nowReading) && !nowReading.isLextant(Punctuator.TERMINATOR)) {
			ParseNode child = syntaxErrorNode("print separator");
			parent.appendChild(child);
			return;
		}
		
		if(nowReading.isLextant(Punctuator.SPACE)) {
			readToken();
			ParseNode child = new SpaceNode(previouslyRead);
			parent.appendChild(child);
		}
		else if(nowReading.isLextant(Punctuator.SEPARATOR)) {
			readToken();
		}		
		else if(nowReading.isLextant(Punctuator.TERMINATOR)) {
			// we're at the end of the bowtie and this printSeparator is not required.
			// do nothing.  Terminator is handled in a higher-level nonterminal.
		}
	}
	private boolean startsPrintSeparator(Token token) {
		return token.isLextant(Punctuator.SEPARATOR, Punctuator.SPACE) ;
	}
	
	
	// declaration -> CONST (or VAR) identifier := expression .
	private ParseNode parseDeclaration() {
		if(!startsDeclaration(nowReading)) {
			return syntaxErrorNode("declaration");
		}
		Token declarationToken = nowReading;
		readToken();
		
		ParseNode identifier = parseIdentifier();
		expect(Punctuator.ASSIGN);
		ParseNode initializer = parseExpression();
		expect(Punctuator.TERMINATOR);
		
		return DeclarationNode.withChildren(declarationToken, identifier, initializer);
	}
	private boolean startsDeclaration(Token token) {
		return token.isLextant(Keyword.CONST, Keyword.VAR);
	}

	// assignment -> identifier := expression .
	private ParseNode parseAssignment() {
		if (!startsAssignment(nowReading)) {
			return syntaxErrorNode("assignment");
		}
		Token assignmentToken = nowReading;
		
		ParseNode targetExpression = parseTargetExpression();
		expect(Punctuator.ASSIGN);
		ParseNode assignedValue = parseExpression();
		expect(Punctuator.TERMINATOR);
		
		return AssignmentNode.withChildren(assignmentToken, targetExpression, assignedValue);
	}
	private boolean startsAssignment(Token token) {
		return startsTargetExpression(token);
	}
	
	private ParseNode parseTargetExpression() {
		
		if (startsArrayIndexingExpression(nowReading)) {
			return parseArrayIndexingExpression();
		}
		if (startsIdentifier(nowReading)) {
			return parseIdentifier();
		}
		if (startsParenthesesExpression(nowReading)) {
			return parseParenthesesExpression();
		}
		
		return null;
	}
	private boolean startsTargetExpression(Token token) {
		return startsArrayIndexingExpression(token) ||
			   startsIdentifier(token)              ||
			   startsParenthesesExpression(token);
	}
	
	// e.g. if (condition) { block statement }
	// or while (condition { block statement}
	private ParseNode parseControlFlowStatement() {
		if (!startsControlFlowStatement(nowReading)) {
			return syntaxErrorNode("control flow");
		}
		
        Token controlFlowToken = nowReading;
        readToken();
        expect(Punctuator.OPEN_PARENTHESES);
        ParseNode expression = parseExpression();
        expect(Punctuator.CLOSE_PARENTHESES);
        ParseNode thenStatement = parseBlock();
        
        if (controlFlowToken.isLextant(Keyword.IF) && startsElseStatement(nowReading)) {
            // expect(Keyword.ELSE);
            readToken();
            ParseNode elseStatement = parseBlock();
            return ControlFlowStatementNode.withChildren(controlFlowToken, expression, thenStatement, elseStatement);
            
        }
        return ControlFlowStatementNode.withChildren(controlFlowToken, expression, thenStatement);
	}
	
	private boolean startsControlFlowStatement(Token token) {
		return token.isLextant(Keyword.IF, Keyword.WHILE);
	}
	
	private boolean startsElseStatement(Token token) {
		return token.isLextant(Keyword.ELSE);
	}
	///////////////////////////////////////////////////////////
	// expressions
	// expr                     -> comparisonExpression
	// comparisonExpression     -> additiveExpression [> additiveExpression]?
	// additiveExpression       -> multiplicativeExpression [+ multiplicativeExpression]*  (left-assoc)
	// multiplicativeExpression -> atomicExpression [MULT atomicExpression]*  (left-assoc)
	// atomicExpression         -> literal
	// literal                  -> intNumber | identifier | booleanConstant (among others)
	

	// expr  -> disjunctionExpression
	private ParseNode parseExpression() {		
		if(!startsExpression(nowReading)) {
			return syntaxErrorNode("expression");
		}
		return parseDisjunctionExpression();
	}
	private boolean startsExpression(Token token) {
		return startsDisjunctionExpression(token);
	}
	
	// disjunctionExpression -> conjunctionExpression ( || conjunctionExpression)
	private ParseNode parseDisjunctionExpression() {
		if(!startsDisjunctionExpression(nowReading)) {
			return syntaxErrorNode("or");
		}
		ParseNode left = parseConjunctionExpression();
		// readToken();
		if (nowReading.isLextant(Punctuator.OR)) {
			Token compareToken = nowReading;
			readToken();
			ParseNode right = parseConjunctionExpression();
			return OperatorNode.withChildren(compareToken, left, right);
		}
		return left;
	}
	private boolean startsDisjunctionExpression(Token token) {
		return startsConjunctionExpression(token);
	}
	
	// conjunctionExpression -> comparisonExpression ( && comparisonExpression)
	private ParseNode parseConjunctionExpression() {
		if(!startsConjunctionExpression(nowReading)) {
			return syntaxErrorNode("and");
		}
		ParseNode left = parseComparisonExpression();
		//readToken();
		if (nowReading.isLextant(Punctuator.AND)) {
			Token compareToken = nowReading;
			readToken();
			ParseNode right = parseComparisonExpression();
			return OperatorNode.withChildren(compareToken, left, right);
		}
		return left;
	}
	private boolean startsConjunctionExpression(Token token) {
		return startsComparisonExpression(token);
	}

	// comparisonExpression -> additiveExpression [> additiveExpression]?
	private ParseNode parseComparisonExpression() {
		if(!startsComparisonExpression(nowReading)) {
			return syntaxErrorNode("comparison expression");
		}
		
		ParseNode left = parseAdditiveExpression();
		if(nowReading.isLextant(
				Punctuator.GREATER,
				Punctuator.LESS,
				Punctuator.EQUALS,
				Punctuator.NOT_EQUALS,
				Punctuator.GREATER_EQUALS,
				Punctuator.LESS_EQUALS)) {
			Token compareToken = nowReading;
			readToken();
			ParseNode right = parseAdditiveExpression();
			
			return OperatorNode.withChildren(compareToken, left, right);
		}
		return left;

	}
	private boolean startsComparisonExpression(Token token) {
		return startsAdditiveExpression(token);
	}

	// additiveExpression -> multiplicativeExpression [+ multiplicativeExpression]*  (left-assoc)
	private ParseNode parseAdditiveExpression() {
		if(!startsAdditiveExpression(nowReading)) {
			return syntaxErrorNode("additiveExpression");
		}
		
		ParseNode left = parseMultiplicativeExpression();
		while(nowReading.isLextant(Punctuator.ADD, Punctuator.SUBTRACT)) {
			Token additiveToken = nowReading;
			readToken();
			ParseNode right = parseMultiplicativeExpression();
			
			left = OperatorNode.withChildren(additiveToken, left, right);
		}
		return left;
	}
	private boolean startsAdditiveExpression(Token token) {
		return startsMultiplicativeExpression(token);
	}	

	// multiplicativeExpression -> atomicExpression [MULT atomicExpression]*  (left-assoc)
	private ParseNode parseMultiplicativeExpression() {
		if(!startsMultiplicativeExpression(nowReading)) {
			return syntaxErrorNode("multiplicativeExpression");
		}
		
		ParseNode left = parseUnaryPrefixExpression();
		while(nowReading.isLextant(
				Punctuator.MULTIPLY,
				Punctuator.DIVIDE,
				Punctuator.OVER,
				Punctuator.EXPRESS_OVER,
				Punctuator.RATIONALIZE)) {
			Token multiplicativeToken = nowReading;
			readToken();
			ParseNode right = parseUnaryPrefixExpression();
			
			left = OperatorNode.withChildren(multiplicativeToken, left, right);
		}
		return left;
	}
	private boolean startsMultiplicativeExpression(Token token) {
		//return startsAtomicExpression(token);
		return startsUnaryPrefixExpression(token);
	}
	
	private ParseNode parseUnaryPrefixExpression() {
		if(!startsUnaryPrefixExpression(nowReading)) {
			return syntaxErrorNode("unaryPrefixExpression");
		}
		
		if (startsArrayIndexingExpression(nowReading)) {
			return parseArrayIndexingExpression();
		}
		
		Token token = nowReading;
		readToken();
		ParseNode child = parseUnaryPrefixExpression();
		ParseNode parent = OperatorNode.withChildren(token, child);
		return parent;
	}
	private boolean startsUnaryPrefixExpression(Token token) {
//		return startsAtomicExpression(token) ||
//				token.isLextant(Punctuator.NOT) ||
//				token.isLextant(Keyword.LENGTH);
		return startsArrayIndexingExpression(token) ||
				token.isLextant(Punctuator.NOT) ||
				token.isLextant(Keyword.LENGTH) ||
				token.isLextant(Keyword.CLONE);
	}
	
	
	
	// atomicExpression -> literal
	private ParseNode parseAtomicExpression() {
		if(!startsAtomicExpression(nowReading)) {
			return syntaxErrorNode("atomic expression");
		}
		
		if (startsBracketExpression(nowReading)) {
			return parseBracketExpression();
		}
		
		if (startsParenthesesExpression(nowReading)) {
			return parseParenthesesExpression();
		}
		if (startsNewArrayExpression(nowReading)) {
			return parseNewArrayExpression();
		}
		
		return parseLiteral();
	}
	private boolean startsAtomicExpression(Token token) {
		return startsLiteral(token) 				||
				startsBracketExpression(token) 		||
				startsParenthesesExpression(token) 	||
				startsNewArrayExpression(token);
	}
	
	private ParseNode parseNewArrayExpression() {
		if (!startsNewArrayExpression(nowReading)) {
			return syntaxErrorNode("new array");
		}
		
		Token newToken = nowReading;
		readToken();
		expect(Punctuator.OPEN_BRACKET);
		ParseNode type = parseType();
		expect(Punctuator.CLOSE_BRACKET);
		expect(Punctuator.OPEN_PARENTHESES);
		ParseNode expression = parseExpression();
		expect(Punctuator.CLOSE_PARENTHESES);
		
		return OperatorNode.withChildren(newToken, type, expression);
	}
	private boolean startsNewArrayExpression(Token token) {
		return token.isLextant(Keyword.NEW);
	}
	
	// Casting expression is treated as atomic
	private ParseNode parseBracketExpression() {
		if(!startsBracketExpression(nowReading)) {
			return syntaxErrorNode("brackets");
		}
		
		Token token = nowReading;						// [
		readToken();
		ParseNode expression = parseExpression();		// [ expr
		
		if (nowReading.isLextant(Punctuator.BAR)) {
			return parseCastingExpression(expression);
		}
		
		return parseArrayPopulationExpression(expression);
	}
	
	private boolean startsBracketExpression(Token token) {
		return (token.isLextant(Punctuator.OPEN_BRACKET));
	}
	
	private ParseNode parseCastingExpression(ParseNode expressionNode) {
		Token realToken = nowReading;
		Token castingToken = LextantToken.artificial(realToken, Punctuator.CASTING);
		
		readToken();
		ParseNode targetType = parseType();
		expect(Punctuator.CLOSE_BRACKET);
		
		return OperatorNode.withChildren(castingToken, expressionNode, targetType);
	}
	
	private ParseNode parseArrayPopulationExpression(ParseNode expressionNode) {
		Token realToken = nowReading;
		Token arrayPopulationToken = LextantToken.artificial(realToken, Punctuator.ARRAY_POPULATION);
		
		ArrayList<ParseNode> arrayNodes = new ArrayList<>();
		arrayNodes.add(expressionNode);
		
		while (nowReading.isLextant(Punctuator.SEPARATOR)) {
			expect(Punctuator.SEPARATOR);
			ParseNode node = parseExpression();
			arrayNodes.add(node);
		}

		expect(Punctuator.CLOSE_BRACKET);
		return ArrayPopulationNode.withChildren(arrayPopulationToken, arrayNodes);
	}
	
	// Parsing a Type Node (right side of a casting expression)
	private ParseNode parseType() {
		if(!startsType(nowReading)) {
			return syntaxErrorNode("type");
		}
		readToken();
		return new TypeNode(previouslyRead);
	}
	
	private boolean startsType(Token token) {
		return token.isLextant(
				Keyword.BOOL,
				Keyword.CHAR,
				Keyword.INT,
				Keyword.FLOAT,
				Keyword.STRING,
				Keyword.RAT);
	}
	
	
	private ParseNode parseArrayIndexingExpression() {
		if(!startsArrayIndexingExpression(nowReading)) {
			return syntaxErrorNode("array indexing");
		}
		
		ParseNode base = parseAtomicExpression();
		while(nowReading.isLextant(Punctuator.OPEN_BRACKET)) { 
			Token realToken = nowReading;
			Token indexToken = LextantToken.artificial(realToken, Punctuator.ARRAY_INDEXING);
			readToken();
			ParseNode index = parseExpression();
			expect(Punctuator.CLOSE_BRACKET);
			base = OperatorNode.withChildren(indexToken, base, index);
		}
		return base;
	}
	private boolean startsArrayIndexingExpression(Token token) {
		return startsAtomicExpression(token);
	}
	
	// Parsing a parentheses-enclosed expression
	private ParseNode parseParenthesesExpression() {
		if (!startsParenthesesExpression(nowReading)) {
			return syntaxErrorNode("parentheses");
		}
		readToken();
		ParseNode expression = parseExpression();
		expect(Punctuator.CLOSE_PARENTHESES);
		
		return expression;
	}
	
	private boolean startsParenthesesExpression(Token token) {
		return (token.isLextant(Punctuator.OPEN_PARENTHESES));
	}
	

	
	// literal -> number | identifier | booleanConstant | characterConstant
	// | stringConstant | identifier
	private ParseNode parseLiteral() {
		if(!startsLiteral(nowReading)) {
			return syntaxErrorNode("literal");
		}
		
		if(startsIntNumber(nowReading)) {
			return parseIntNumber();
		}
		if(startsFloatNumber(nowReading)) {
			return parseFloatNumber();
		}
		if(startsCharacter(nowReading)) {
			return parseCharacter();
		}
		if(startsString(nowReading)) {
			return parseString();
		}
		if(startsIdentifier(nowReading)) {
			return parseIdentifier();
		}
		if(startsBooleanConstant(nowReading)) {
			return parseBooleanConstant();
		}

		return syntaxErrorNode("literal");
	}
	
	private boolean startsLiteral(Token token) {
		return startsIntNumber(token) ||
				startsFloatNumber(token) ||
				startsCharacter(token) ||
				startsString(token) ||
				startsIdentifier(token) ||
				startsBooleanConstant(token);
	}

	// integer (terminal)
	private ParseNode parseIntNumber() {
		if(!startsIntNumber(nowReading)) {
			return syntaxErrorNode("integer constant");
		}
		readToken();
		return new IntegerConstantNode(previouslyRead);
	}
	private boolean startsIntNumber(Token token) {
		return token instanceof IntegerToken;
	}
	
	// floating number (terminal)
	private ParseNode parseFloatNumber() {
		if(!startsFloatNumber(nowReading)) {
			return syntaxErrorNode("float constant");
		}
		readToken();
		return new FloatingConstantNode(previouslyRead);
	}
	private boolean startsFloatNumber(Token token) {
		return token instanceof FloatingToken;
	}
	
	// character (terminal)
	private ParseNode parseCharacter() {
		if(!startsCharacter(nowReading)) {
			return syntaxErrorNode("character constant");
		}
		readToken();
		return new CharacterConstantNode(previouslyRead);
	}
	private boolean startsCharacter(Token token) {
		return token instanceof CharacterToken;
	}
	
	// character (terminal)
	private ParseNode parseString() {
		if(!startsString(nowReading)) {
			return syntaxErrorNode("string constant");
		}
		readToken();
		return new StringConstantNode(previouslyRead);
	}
	private boolean startsString(Token token) {
		return token instanceof StringToken;
	}
	
	// identifier (terminal)
	private ParseNode parseIdentifier() {
		if(!startsIdentifier(nowReading)) {
			return syntaxErrorNode("identifier");
		}
		readToken();
		return new IdentifierNode(previouslyRead);
	}
	private boolean startsIdentifier(Token token) {
		return token instanceof IdentifierToken;
	}

	// boolean constant (terminal)
	private ParseNode parseBooleanConstant() {
		if(!startsBooleanConstant(nowReading)) {
			return syntaxErrorNode("boolean constant");
		}
		readToken();
		return new BooleanConstantNode(previouslyRead);
	}
	private boolean startsBooleanConstant(Token token) {
		return token.isLextant(Keyword.TRUE, Keyword.FALSE);
	}

	private void readToken() {
		previouslyRead = nowReading;
		nowReading = scanner.next();
	}	
	
	// if the current token is one of the given lextants, read the next token.
	// otherwise, give a syntax error and read next token (to avoid endless looping).
	private void expect(Lextant ...lextants ) {
		if(!nowReading.isLextant(lextants)) {
			syntaxError(nowReading, "expecting " + Arrays.toString(lextants));
		}
		readToken();
	}	
	private ErrorNode syntaxErrorNode(String expectedSymbol) {
		syntaxError(nowReading, "expecting " + expectedSymbol);
		ErrorNode errorNode = new ErrorNode(nowReading);
		readToken();
		return errorNode;
	}
	private void syntaxError(Token token, String errorDescription) {
		String message = "" + token.getLocation() + " " + errorDescription;
		error(message);
	}
	private void error(String message) {
		PikaLogger log = PikaLogger.getLogger("compiler.Parser");
		log.severe("syntax error: " + message);
	}	
}

