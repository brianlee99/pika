package asmCodeGenerator;

import java.util.HashMap;
import java.util.Map;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.runtime.MemoryManager;
import asmCodeGenerator.runtime.Record;
import asmCodeGenerator.runtime.RunTime;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import parseTree.*;
import parseTree.nodeTypes.ArrayPopulationNode;
import parseTree.nodeTypes.AssignmentNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.BreakNode;
import parseTree.nodeTypes.CallNode;
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.ContinueNode;
import parseTree.nodeTypes.IfStatementNode;
import parseTree.nodeTypes.BlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.ForStatementNode;
import parseTree.nodeTypes.FunctionDefinitionNode;
import parseTree.nodeTypes.FunctionInvocationNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.LambdaNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.ReleaseStatementNode;
import parseTree.nodeTypes.ReturnNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TabNode;
import parseTree.nodeTypes.WhileStatementNode;
import semanticAnalyzer.types.ArrayType;
import semanticAnalyzer.types.LambdaType;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
import symbolTable.Scope;

import static asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import static asmCodeGenerator.runtime.RunTime.*;
import static asmCodeGenerator.Macros.*;

// do not call the code generator if any errors have occurred during analysis.
public class ASMCodeGenerator {
	ParseNode root;
	
	public static ASMCodeFragment generate(ParseNode syntaxTree) {
		ASMCodeGenerator codeGenerator = new ASMCodeGenerator(syntaxTree);
		return codeGenerator.makeASM();
	}
	public ASMCodeGenerator(ParseNode root) {
		super();
		this.root = root;
	}
	
	public ASMCodeFragment makeASM() {
		ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);

		code.append( MemoryManager.codeForInitialization() );
		code.append( RunTime.getEnvironment() );
		code.append( globalVariableBlockASM() );
		code.append( programASM() );
		code.append( MemoryManager.codeForAfterApplication() );
		
		return code;
	}
	private ASMCodeFragment globalVariableBlockASM() {
		assert root.hasScope();
		Scope scope = root.getScope();
		int globalBlockSize = scope.getAllocatedSize();
		
		ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);
		code.add(DLabel, RunTime.GLOBAL_MEMORY_BLOCK);
		code.add(DataZ, globalBlockSize);
		
		return code;
	}
	
	private ASMCodeFragment programASM() {
		ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);

		
		code.add(    Label, RunTime.MAIN_PROGRAM_LABEL);
		
		code.append(initFrameStackPointers());
		code.append( programCode());
		code.add(    Halt );
	
		return code;
	}
	private ASMCodeFragment initFrameStackPointers() {
		ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);
		code.add(Memtop);
		storeITo(code, FRAME_POINTER);
		code.add(Memtop);
		storeITo(code, STACK_POINTER);
		return code;
	}
	
	private ASMCodeFragment programCode() {
		CodeVisitor visitor = new CodeVisitor();
		root.accept(visitor);
		return visitor.removeRootCode(root);
	}


	protected class CodeVisitor extends ParseNodeVisitor.Default {
		private Map<ParseNode, ASMCodeFragment> codeMap;
		ASMCodeFragment code;
		
		public CodeVisitor() {
			codeMap = new HashMap<ParseNode, ASMCodeFragment>();
		}


		////////////////////////////////////////////////////////////////////
        // Make the field "code" refer to a new fragment of different sorts.
		private void newAddressCode(ParseNode node) {
			code = new ASMCodeFragment(GENERATES_ADDRESS);
			codeMap.put(node, code);
		}
		private void newValueCode(ParseNode node) {
			code = new ASMCodeFragment(GENERATES_VALUE);
			codeMap.put(node, code);
		}
		private void newVoidCode(ParseNode node) {
			code = new ASMCodeFragment(GENERATES_VOID);
			codeMap.put(node, code);
		}

	    ////////////////////////////////////////////////////////////////////
        // Get code from the map.
		private ASMCodeFragment getAndRemoveCode(ParseNode node) {
			ASMCodeFragment result = codeMap.get(node);
			codeMap.remove(result);
			return result;
		}
	    public  ASMCodeFragment removeRootCode(ParseNode tree) {
			return getAndRemoveCode(tree);
		}		
		ASMCodeFragment removeValueCode(ParseNode node) {
			ASMCodeFragment frag = getAndRemoveCode(node);
			makeFragmentValueCode(frag, node);
			return frag;
		}		
		private ASMCodeFragment removeAddressCode(ParseNode node) {
			ASMCodeFragment frag = getAndRemoveCode(node);
			assert frag.isAddress();
			return frag;
		}		
		ASMCodeFragment removeVoidCode(ParseNode node) {
			ASMCodeFragment frag = getAndRemoveCode(node);
			assert frag.isVoid();
			return frag;
		}
		
	    ////////////////////////////////////////////////////////////////////
        // convert code to value-generating code.
		private void makeFragmentValueCode(ASMCodeFragment code, ParseNode node) {
			assert !code.isVoid();
			
			if(code.isAddress()) {
				turnAddressIntoValue(code, node);
			}	
		}
		private void turnAddressIntoValue(ASMCodeFragment code, ParseNode node) {
			Type nodeType = node.getType();
			if(nodeType == PrimitiveType.INTEGER) {
				code.add(LoadI);
			}
			else if(nodeType == PrimitiveType.FLOATING) {
				code.add(LoadF);
			}
			else if(nodeType == PrimitiveType.BOOLEAN) {
				code.add(LoadC);
			}
			else if (nodeType == PrimitiveType.CHARACTER) {
				code.add(LoadC);
			}
			else if (nodeType == PrimitiveType.RATIONAL) {
				code.add(Duplicate);
				code.add(LoadI);
				code.add(Exchange);
				code.add(PushI, 4);
				code.add(Add);
				code.add(LoadI);
			}
			else if (nodeType == PrimitiveType.STRING) {
				code.add(LoadI);
			}
			else if (nodeType instanceof ArrayType) {
				code.add(LoadI);
			}
			else if (nodeType instanceof LambdaType) {
				code.add(LoadI);
			}
			else {
				assert false : "node " + node;
			}
			code.markAsValue();
		}
		
	    ////////////////////////////////////////////////////////////////////
        // ensures all types of ParseNode in given AST have at least a visitLeave	
		public void visitLeave(ParseNode node) {
			assert false : "node " + node + " not handled in ASMCodeGenerator";
		}
		
		///////////////////////////////////////////////////////////////////////////
		// constructs larger than statements
		public void visitLeave(ProgramNode node) {
			newVoidCode(node);
			for(ParseNode child : node.getChildren()) {
				ASMCodeFragment childCode = removeVoidCode(child);
				code.append(childCode);
			}
		}
		public void visitLeave(BlockNode node) {
			newVoidCode(node);
			for(ParseNode child : node.getChildren()) {
				ASMCodeFragment childCode = removeVoidCode(child);
				code.append(childCode);
			}
		}
		
		///////////////////////////////////////////////////////////////////////////
		// functions
		public void visitLeave(FunctionDefinitionNode node) {
			newVoidCode(node);
			ASMCodeFragment lvalue = removeAddressCode(node.child(0));	
			ASMCodeFragment rvalue = removeValueCode(node.child(1));
			
			code.append(lvalue);
			code.append(rvalue);
			
			Type type = node.getType();
			code.add(opcodeForStore(type));
		}
		public void visitEnter(LambdaNode node) {
			Labeller labeller = new Labeller("func");
			String startLabel = labeller.newLabel("start");
			String exitLabel = labeller.newLabel("exit");
			String endLabel = labeller.newLabel("end");
			node.setStartLabel(startLabel);
			node.setExitLabel(exitLabel);
			node.setEndLabel(endLabel);
		}
		public void visitLeave(LambdaNode node) {
			newValueCode(node); 
			ASMCodeFragment body = removeVoidCode(node.child(1));
			
			String startLabel = node.getStartLabel();
			String exitLabel = node.getExitLabel();
			String endLabel = node.getEndLabel();
			
			int procScopeSize = node.child(1).getScope().getAllocatedSize();
			int paramScopeSize = node.getScope().getAllocatedSize();
			Type returnType = ((LambdaType) node.getType()).getReturnType();
			int returnSize = returnType.getSize();
			
			code.add(Jump, endLabel);
			
			// Function start
			code.add(Label, startLabel);
			Macros.loadIFrom(code, RunTime.FRAME_POINTER);				// [ RA frame_ptr ]
			Macros.loadIFrom(code, RunTime.STACK_POINTER);				// [ RA frame_ptr stack_ptr ]
			code.add(PushI, 4);
			code.add(Subtract);											// [ RA frame_ptr dyn_link_addr ]
			code.add(Exchange); 										// [ RA dyn_link_addr frame_ptr ]
			code.add(StoreI);
			
			Macros.loadIFrom(code, RunTime.STACK_POINTER);				// [ RA stack_ptr ]
			code.add(PushI, 8);
			code.add(Subtract);											// [ RA return_addr ]
			code.add(Exchange);
			code.add(StoreI);
			
			// Set FP <- SP
			loadIFrom(code, STACK_POINTER);								// [ SP ]
			storeITo(code, FRAME_POINTER);
			
			// Subtract stack pointer by the procedure scope size
			loadIFrom(code, STACK_POINTER);
			code.add(PushI, procScopeSize);
			code.add(Subtract);
			storeITo(code, STACK_POINTER);
			
			// Function body
			code.append(body);
			
			if (returnType != PrimitiveType.VOID) {
				code.add(Jump, NO_RETURN_RUNTIME_ERROR);
			}
			
			// Exit Handshake
			code.add(Label, exitLabel);
			
			// RA at FP - 8
			readIPtrOffset(code, FRAME_POINTER, -8);				// [ returnValue returnAddr ]					[ returnAddr ] 
			if (returnType == PrimitiveType.RATIONAL) {
				exchangeWithRational(code);
			} else if (returnType != PrimitiveType.VOID) {
				code.add(Exchange);									// [ returnAddr returnValue ]					[ returnAddr ] 
			}
			
			// Dynamic Link at FP - 4
			readIPtrOffset(code, FRAME_POINTER, -4);				// [ returnAddr returnValue dynamicLink ]		[ returnAddr dynamicLink ] 
			storeITo(code, FRAME_POINTER);							// [ returnAddr returnValue ]					[ returnAddr ] 
			
			// Increase SP by allocatedSize + paramScopeSize
			loadIFrom(code, STACK_POINTER);
			code.add(PushI, procScopeSize + paramScopeSize);
			code.add(Add);
			storeITo(code, STACK_POINTER);
			
			// Decrease SP by return value size
			loadIFrom(code, STACK_POINTER);
			code.add(PushI, returnSize);
			code.add(Subtract);
			storeITo(code, STACK_POINTER);
			
			// store return value onto this address
			if (returnType == PrimitiveType.RATIONAL) {
				loadIFrom(code, STACK_POINTER);						// [ num den SP ]
				code.add(PushI, 4);
				code.add(Add);
				code.add(Exchange);
				code.add(StoreI);
				loadIFrom(code, STACK_POINTER);						// [ num SP ]
				code.add(Exchange);
				code.add(StoreI);
			}
			else if (returnType != PrimitiveType.VOID) {
				loadIFrom(code, STACK_POINTER);
				code.add(Exchange);
				code.add(opcodeForStore(returnType));
			}
			
			// return from the function
			code.add(Return);
			code.add(Label, endLabel);
			
			// Need to return a value (address of the function start)
			code.add(PushD, startLabel);
		}
		
		public void visitLeave(FunctionInvocationNode node) {
			Type returnType = node.getType();
			if (returnType == PrimitiveType.VOID) {
				newVoidCode(node);
			} else {
				newValueCode(node);
			}
			
			ParseNode left = node.child(0);
			
			int nChildren = node.nChildren();
			for (int i = 1; i < nChildren; i++) {
				loadIFrom(code, STACK_POINTER);	
				
				Type type = node.child(i).getType();
				code.add(PushI, type.getSize());
				code.add(Subtract);
				storeITo(code, STACK_POINTER);
				loadIFrom(code, STACK_POINTER);				// [ SP ]
				
				ASMCodeFragment frag = removeValueCode(node.child(i));
				code.append(frag);
				
				if (type == PrimitiveType.RATIONAL ) {
					storeITo(code, DENOMINATOR_1);
					code.add(StoreI);
					loadIFrom(code, STACK_POINTER);
					code.add(PushI, 4);
					code.add(Add);
					loadIFrom(code, DENOMINATOR_1);
					code.add(StoreI);
				} else {
					code.add(opcodeForStore(type));
				}
			}
			
			code.append(removeValueCode(left));
			code.add(CallV);
			
			if (returnType == PrimitiveType.RATIONAL) {
				loadIFrom(code, STACK_POINTER);
				code.add(LoadI); 					// [ num ]
				loadIFrom(code, STACK_POINTER);
				code.add(PushI, 4);
				code.add(Add);
				code.add(LoadI); 					// [ num den ]
			} else if (returnType != PrimitiveType.VOID) {
				loadIFrom(code, STACK_POINTER);
				code.add(opcodeForLoad(returnType));
			}
			
			int returnSize = returnType.getSize();
			
			loadIFrom(code, STACK_POINTER);
			code.add(PushI, returnSize);
			code.add(Add);
			storeITo(code, STACK_POINTER);
		}
		
		public void visitLeave(ReturnNode node) {
			newVoidCode(node);
			if (node.nChildren() == 1) {
				code.append(removeValueCode(node.child(0)));
			}
			LambdaNode lambdaNode = findLambdaNode(node);
			String label = lambdaNode.getExitLabel();
			code.add(Jump, label);
		}
		private LambdaNode findLambdaNode(ParseNode node) {
			for (ParseNode parent : node.pathToRoot()) {
				if (parent instanceof LambdaNode) {
					return (LambdaNode) parent;
				}
			}
			return null; 
		}
		
		public void visitLeave(CallNode node) {
			newVoidCode(node);
			ParseNode child = node.child(0);
			Type returnType = child.getType();
			if (returnType == PrimitiveType.RATIONAL) {
				code.append(removeValueCode(child));
				code.add(Pop);
				code.add(Pop);
			}
			else if (returnType != PrimitiveType.VOID) {
				code.append(removeValueCode(child));
				code.add(Pop);
			}
			else {
				code.append(removeVoidCode(child));
			}
		}

		///////////////////////////////////////////////////////////////////////////
		// statements and declarations

		public void visitLeave(PrintStatementNode node) {
			newVoidCode(node);
			new PrintStatementGenerator(code, this).generate(node);	
		}
		public void visit(NewlineNode node) {
			newVoidCode(node);
			code.add(PushD, RunTime.NEWLINE_PRINT_FORMAT);
			code.add(Printf);
		}
		public void visit(TabNode node) {
			newVoidCode(node);
			code.add(PushD, RunTime.TAB_PRINT_FORMAT);
			code.add(Printf);
		}
		public void visit(SpaceNode node) {
			newVoidCode(node);
			code.add(PushD, RunTime.SPACE_PRINT_FORMAT);
			code.add(Printf);
		}
		
		public void visitLeave(DeclarationNode node) {
			newVoidCode(node);
			ASMCodeFragment lvalue = removeAddressCode(node.child(0));	
			ASMCodeFragment rvalue = removeValueCode(node.child(1));
			
			code.append(lvalue);
			code.append(rvalue);
			
			Type type = node.getType();
			if (type == PrimitiveType.RATIONAL) {
				code.append(rationalFragmentForStore());
			} else {
				code.add(opcodeForStore(type));
			}
		}
		
		private ASMCodeFragment rationalFragmentForStore() {
			ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VOID);	
			Macros.storeITo(fragment, RunTime.DENOMINATOR_1);
			Macros.storeITo(fragment, RunTime.NUMERATOR_1);		// [ addr ]
			fragment.add(Duplicate);							// [ addr addr ]
			Macros.loadIFrom(fragment, RunTime.NUMERATOR_1);	// [ addr addr num ]
			fragment.add(Exchange);								// [ addr num addr ]
			Macros.loadIFrom(fragment, RunTime.DENOMINATOR_1);	// [ addr num addr denom ]
			fragment.add(Exchange);								// [ addr num denom addr ]
			fragment.add(PushI, 4);						
			fragment.add(Add);									// [ addr num denom addr+4 ]
			fragment.add(Exchange);								// [ addr num addr+4 denom ]
			fragment.add(StoreI);
			fragment.add(StoreI);								// [ ]
			return fragment;
		}
		
		public void visitLeave(AssignmentNode node) {
			newVoidCode(node);
			ASMCodeFragment lvalue = removeAddressCode(node.child(0));	
			ASMCodeFragment rvalue = removeValueCode(node.child(1));
			code.append(lvalue);
			code.append(rvalue);
			Type type = node.child(0).getType();
			if (type == PrimitiveType.RATIONAL) {
				code.append(rationalFragmentForStore());
			} else {
				code.add(opcodeForStore(type));
			}
		}
	
		private ASMOpcode opcodeForStore(Type type) {
			if (type == PrimitiveType.INTEGER) {
				return StoreI;
			}
			if (type == PrimitiveType.FLOATING) {
				return StoreF;
			}
			if (type == PrimitiveType.BOOLEAN) {
				return StoreC;
			}
			if (type == PrimitiveType.CHARACTER) {
				return StoreC;
			}
			if (type == PrimitiveType.STRING) {
				return StoreI;
			}
			if (type instanceof ArrayType) {
				return StoreI;
			}
			if (type instanceof LambdaType) {
				return StoreI;
			}
			assert false: "Type " + type + " unimplemented in opcodeForStore()";
			return null;
		}
		private ASMOpcode opcodeForLoad(Type type) {
			if(type == PrimitiveType.INTEGER) {
				return LoadI;
			}
			if(type == PrimitiveType.FLOATING) {
				return LoadF;
			}
			if(type == PrimitiveType.BOOLEAN) {
				return LoadC;
			}
			if(type == PrimitiveType.CHARACTER) {
				return LoadC;
			}
			if (type == PrimitiveType.STRING) {
				return LoadI;
			}
			if (type instanceof ArrayType) {
				return LoadI;
			}
			if (type instanceof LambdaType) {
				return LoadI;
			}
			assert false: "Type " + type + " unimplemented in opcodeForLoad()";
			return null;
		}

		///////////////////////////////////////////////////////////////////////////
		// if / while
		public void visitLeave(IfStatementNode node) {
			if (!node.getToken().isLextant(Keyword.IF)) {
				assert false;
			}
			ASMCodeFragment conditionCode = removeValueCode(node.child(0));
			ASMCodeFragment thenCode = removeVoidCode(node.child(1));
			
			Labeller labeller = new Labeller("if");
			String falseLabel = labeller.newLabel("false");
			String endLabel   = labeller.newLabel("end");
			
			newVoidCode(node);
			code.append(conditionCode);
			code.add(JumpFalse, falseLabel);
			code.append(thenCode);
			code.add(Jump, endLabel);
			code.add(Label, falseLabel);
			if (node.nChildren() == 3) {
				code.append(removeVoidCode(node.child(2)));
			}
			code.add(Label, endLabel);
		}
		
		public void visitEnter(WhileStatementNode node) {
			Labeller labeller 	= new Labeller("while");
			String loopLabel 	= labeller.newLabel("loop");
			String endLabel   	= labeller.newLabel("end");
			node.setLoopLabel(loopLabel);
			node.setEndLabel(endLabel);
		}
		public void visitLeave(WhileStatementNode node) {
			if (!node.getToken().isLextant(Keyword.WHILE)) {
				assert false;
			}
			ASMCodeFragment conditionCode = removeValueCode(node.child(0));
			ASMCodeFragment loopCode = removeVoidCode(node.child(1));
			
			String loopLabel = node.getLoopLabel();
			String endLabel  = node.getEndLabel();
			
			newVoidCode(node);
			code.add(Label, loopLabel);
			code.append(conditionCode);
			code.add(JumpFalse, endLabel);
			code.append(loopCode);
			code.add(Jump, loopLabel);
			code.add(Label, endLabel);
		}
		
		///////////////////////////////////////////////////////////////////////////
		// for
		public void visitEnter(ForStatementNode node) {
			Labeller labeller 	= new Labeller("for");
			String loopLabel 	= labeller.newLabel("loop");
			String endLabel   	= labeller.newLabel("end");
			node.setLoopLabel(loopLabel);
			node.setEndLabel(endLabel);
		}
		public void visitLeave(ForStatementNode node) {
			if (!node.getToken().isLextant(Keyword.INDEX, Keyword.ELEM)) {
				assert false;
			}
			
			ASMCodeFragment identifier = removeAddressCode(node.child(0));
			ASMCodeFragment array = removeValueCode(node.child(1));
			ASMCodeFragment blockCode = removeVoidCode(node.child(2));
//			
			// here is the length
			code.append(array);
			
			code.add(PushI, Record.ARRAY_LENGTH_OFFSET);
			code.add(Add);
			code.add(LoadI);
			
			
			String loopLabel = node.getLoopLabel();
			String endLabel  = node.getEndLabel();
//			
			newVoidCode(node);
			code.add(Label, loopLabel);
			
			// increment i by 1
			
			// if i >= length then jump to end
			code.add(JumpFalse, endLabel);
			
			code.append(blockCode);
			
			
			// compare i to length
			
//			code.append(conditionCode);
//			code.add(JumpFalse, endLabel);
//			code.append(loopCode);
			code.add(Jump, loopLabel);
			code.add(Label, endLabel);
		}
		
		///////////////////////////////////////////////////////////////////////////
		// break and continue
		public void visit(BreakNode node) {
			ParseNode controlNode = getClosestWhileOrFor(node);
			if (controlNode instanceof WhileStatementNode) {			
				String endLabel = ((WhileStatementNode) controlNode).getEndLabel();
				newVoidCode(node);
				code.add(Jump, endLabel);
			}
			else if (controlNode instanceof ForStatementNode) {
				String endLabel = ((ForStatementNode) controlNode).getEndLabel();
				newVoidCode(node);
				code.add(Jump, endLabel);
			}

		}
		public void visit(ContinueNode node) {
			ParseNode controlNode = getClosestWhileOrFor(node);
			if (controlNode instanceof WhileStatementNode) {
				String loopLabel = ((WhileStatementNode) controlNode).getLoopLabel();
				newVoidCode(node);
				code.add(Jump, loopLabel);
			}
			else if (controlNode instanceof ForStatementNode) {
				String loopLabel = ((ForStatementNode) controlNode).getLoopLabel();
				newVoidCode(node);
				code.add(Jump, loopLabel);
			}
		}
		private ParseNode getClosestWhileOrFor(ParseNode node) {
			for (ParseNode parent : node.pathToRoot()) {
				if ((parent instanceof WhileStatementNode) || (parent instanceof ForStatementNode)) {
					return parent;
				}
			}
			return null;
		}
		
		///////////////////////////////////////////////////////////////////////////
		// release
		public void visitLeave(ReleaseStatementNode node) {
			if (!node.getToken().isLextant(Keyword.RELEASE)) {
				assert false;
			}
			newVoidCode(node);
			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			code.append(arg1);
			Type type = node.getType();
			code.append(Record.releaseRecord(type));
		}
		
		///////////////////////////////////////////////////////////////////////////
		// expressions                                         
		public void visitLeave(OperatorNode node) {
			Lextant operator = node.getOperator();
			if(operator == Punctuator.GREATER 				||
					operator == Punctuator.LESS 			||
					operator == Punctuator.EQUALS 			||
					operator == Punctuator.NOT_EQUALS 		||
					operator == Punctuator.GREATER_EQUALS 	||
					operator == Punctuator.LESS_EQUALS ) {
				visitComparisonOperatorNode(node, operator);
			}
			else if (operator == Punctuator.NOT) {
				visitNotOperatorNode(node);
			}
			else if (operator == Keyword.LENGTH 			|| 
						operator == Keyword.CLONE			||
						operator == Keyword.REVERSE) {
				visitUnaryOperatorNode(node, operator);
			}
			else if (operator == Punctuator.CASTING) {
				visitCastingOperatorNode(node);
			}
			else if (operator == Keyword.NEW) {
				visitNewArrayNode(node);
			}
			else {
				visitNormalBinaryOperatorNode(node);
			}
		}
		
		private void visitComparisonOperatorNode(OperatorNode node,
				Lextant operator) {
			
			Type leftNodeType = node.child(0).getType();

			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			ASMCodeFragment arg2 = removeValueCode(node.child(1));				
			
			Labeller labeller = new Labeller("compare");
			String startLabel = labeller.newLabel("arg1");
			String arg2Label  = labeller.newLabel("arg2");
			String subLabel   = labeller.newLabel("sub");
			String trueLabel  = labeller.newLabel("true");
			String falseLabel = labeller.newLabel("false");
			String joinLabel  = labeller.newLabel("join");

			newValueCode(node);
			code.add(Label, startLabel);
			code.append(arg1);
			code.add(Label, arg2Label);
			code.append(arg2);										
			code.add(Label, subLabel);
			 
			if (leftNodeType == PrimitiveType.RATIONAL) {			// [ .. num1 den1 num2 den2 ]
				code.add(Call, RATIONAL_SUBTRACT);					// [ .. resultNum resultDen ]
				// Assume for now that the denominator is positive
				code.add(Pop);										// [ .. resultNum ]
			}
			else if (leftNodeType == PrimitiveType.INTEGER)
				code.add(Subtract);
			else if (leftNodeType == PrimitiveType.CHARACTER)
				code.add(Subtract);
			else if (leftNodeType == PrimitiveType.BOOLEAN)
				code.add(Subtract);
			else if (leftNodeType == PrimitiveType.STRING)
				code.add(Subtract);
			else if (leftNodeType instanceof ArrayType)
				code.add(Subtract);
			else if (leftNodeType == PrimitiveType.FLOATING)
				code.add(FSubtract);
			else
				assert false : "unknown type";
			
			// we need to check the node signatures
			if (operator == Punctuator.GREATER) {
				if (leftNodeType == PrimitiveType.INTEGER)
					code.add(JumpPos, trueLabel);
				else if(leftNodeType == PrimitiveType.CHARACTER)
					code.add(JumpPos, trueLabel);
				else if (leftNodeType == PrimitiveType.RATIONAL)
					code.add(JumpPos, trueLabel);
				else if (leftNodeType == PrimitiveType.FLOATING)
					code.add(JumpFPos, trueLabel);
				else
					assert false : "type not supported for operation";
				
				code.add(Jump, falseLabel);
			}
			else if (operator == Punctuator.LESS) {
				if (leftNodeType == PrimitiveType.INTEGER)
					code.add(JumpNeg, trueLabel);
				else if(leftNodeType == PrimitiveType.CHARACTER)
					code.add(JumpNeg, trueLabel);
				else if (leftNodeType == PrimitiveType.RATIONAL)
					code.add(JumpNeg, trueLabel);
				else if (leftNodeType == PrimitiveType.FLOATING)
					code.add(JumpFNeg, trueLabel);
				else
					assert false : "type not supported for operation";
				
				code.add(Jump, falseLabel);
			}
			else if (operator == Punctuator.EQUALS) {
				if (leftNodeType == PrimitiveType.INTEGER)
					code.add(JumpFalse, trueLabel);
				else if (leftNodeType == PrimitiveType.CHARACTER)
					code.add(JumpFalse, trueLabel);
				else if (leftNodeType == PrimitiveType.BOOLEAN)
					code.add(JumpFalse, trueLabel);
				else if (leftNodeType == PrimitiveType.RATIONAL)
					code.add(JumpFalse, trueLabel);
				else if (leftNodeType == PrimitiveType.FLOATING)
					code.add(JumpFZero, trueLabel);
				else if (leftNodeType == PrimitiveType.STRING)
					code.add(JumpFalse, trueLabel);
				else if (leftNodeType instanceof ArrayType){
					code.add(JumpFalse, trueLabel);
				}
				else
					assert false : "type not supported for operation";
				
				code.add(Jump, falseLabel);
			}
			else if (operator == Punctuator.NOT_EQUALS) {
				if (leftNodeType == PrimitiveType.INTEGER) {
					code.add(JumpTrue, trueLabel);
					code.add(Jump, falseLabel);
				}
				else if (leftNodeType == PrimitiveType.CHARACTER) {
					code.add(JumpTrue, trueLabel);
					code.add(Jump, falseLabel);
				}
				else if (leftNodeType == PrimitiveType.BOOLEAN) {
					code.add(JumpTrue, trueLabel);
					code.add(Jump, falseLabel);
				}
				else if (leftNodeType == PrimitiveType.RATIONAL) {
					code.add(JumpTrue, trueLabel);
					code.add(Jump, falseLabel);
				}
				else if (leftNodeType == PrimitiveType.FLOATING) {
					code.add(JumpFZero, falseLabel);
					code.add(Jump, trueLabel);
				}
				else if (leftNodeType == PrimitiveType.STRING) {
					code.add(JumpTrue, trueLabel);
					code.add(Jump, falseLabel);
				}
				else if (leftNodeType instanceof ArrayType) {
					code.add(JumpTrue, trueLabel);
					code.add(Jump, falseLabel);
				}
				else {
					assert false : "type not supported for operation";
				}
			}
			else if (operator == Punctuator.GREATER_EQUALS) {
				if (leftNodeType == PrimitiveType.INTEGER)
					code.add(JumpNeg, falseLabel);
				else if (leftNodeType == PrimitiveType.CHARACTER)
					code.add(JumpNeg, falseLabel);
				else if (leftNodeType == PrimitiveType.RATIONAL)
					code.add(JumpNeg, falseLabel);
				else if (leftNodeType == PrimitiveType.FLOATING) 
					code.add(JumpFNeg, falseLabel);
				else
					assert false : "type not supported for operation";
				
				code.add(Jump, trueLabel);
			}
			else if (operator == Punctuator.LESS_EQUALS) {
				if (leftNodeType == PrimitiveType.INTEGER)
					code.add(JumpPos, falseLabel);
				else if (leftNodeType == PrimitiveType.CHARACTER)
					code.add(JumpPos, falseLabel);
				else if (leftNodeType == PrimitiveType.RATIONAL)
					code.add(JumpPos, falseLabel);
				else if (leftNodeType == PrimitiveType.FLOATING) 
					code.add(JumpFPos, falseLabel);
				else
					assert false : "type not supported for operation";
				
				code.add(Jump, trueLabel);
			}
			else {
				assert false : "unrecognized operator";
			}

			code.add(Label, trueLabel);
			code.add(PushI, 1);
			code.add(Jump, joinLabel);
			code.add(Label, falseLabel);
			code.add(PushI, 0);
			code.add(Jump, joinLabel);
			code.add(Label, joinLabel);
		}
		
		private void visitNotOperatorNode(OperatorNode node) {
			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			
			Labeller labeller = new Labeller("compare");
			String startLabel = labeller.newLabel("arg1");
			String trueLabel  = labeller.newLabel("true");
			String falseLabel = labeller.newLabel("false");
			String joinLabel  = labeller.newLabel("join");
			String opLabel    = labeller.newLabel("op");

			newValueCode(node);
			code.add(Label, startLabel);
			code.append(arg1);
			code.add(Label, opLabel);
			code.add(BNegate);
			code.add(JumpTrue, trueLabel);
			code.add(Jump, falseLabel);
			code.add(Label, trueLabel);
			code.add(PushI, 1);
			code.add(Jump, joinLabel);
			code.add(Label, falseLabel);
			code.add(PushI, 0);
			code.add(Jump, joinLabel);
			code.add(Label, joinLabel);
		}
		                                                      
		private void visitUnaryOperatorNode(OperatorNode node, Lextant operator) {
			newValueCode(node);
			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			
			Object variant = node.getSignature().getVariant();
			if(variant instanceof ASMOpcode) {
				code.append(arg1);
				ASMOpcode opcode = (ASMOpcode) variant;
				code.add(opcode);
			}
			else if (variant instanceof SimpleCodeGenerator) {
				code.append(arg1);
				SimpleCodeGenerator generator = (SimpleCodeGenerator) variant;
				ASMCodeFragment fragment = generator.generate(node);
				code.append(fragment);
				
				if (fragment.isAddress()) {
					code.markAsAddress();
				}
			}
			else if (variant instanceof FullCodeGenerator) {
				FullCodeGenerator generator = (FullCodeGenerator) variant;
				ASMCodeFragment fragment = generator.generate(node);
				code.append(fragment);
				
				if (fragment.isAddress()) {
					code.markAsAddress();
				}
			}
			else {
				assert false : "unknown variant in UnaryOperatorNode";
			}
		}
		
		private void visitCastingOperatorNode(OperatorNode node) {
			newValueCode(node);
			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			code.append(arg1);
			
			Object variant = node.getSignature().getVariant();
			
			if(variant instanceof ASMOpcode) {
				ASMOpcode opcode = (ASMOpcode) variant;
				code.add(opcode);
			}
			else if (variant instanceof SimpleCodeGenerator) {
				SimpleCodeGenerator generator = (SimpleCodeGenerator) variant;
				ASMCodeFragment fragment = generator.generate(node);
				code.append(fragment);
				if (fragment.isAddress()) {
					code.markAsAddress();
				}
			}
			else {
				assert false : "unknown variant in CastingExpressionNode";
			}
		}
		
		public void visitLeave(ArrayPopulationNode node) {
			newValueCode(node);
			int length = node.nChildren();
			int subtypeSize = node.child(0).getType().getSize();
			Type type = node.child(0).getType();
			code.add(PushI, length);												// [ ... length ]

			SimpleCodeGenerator generator = new NewArrayCodeGenerator();
			ASMCodeFragment fragment = generator.generate(node);
			code.append(fragment);													// [ ... &arr ]
			
			for (int i = 0; i < length; i++) {
				code.add(Duplicate); 												// [ ... &arr &arr ]
				ASMCodeFragment childFragment = removeValueCode(node.child(i));
				code.append(childFragment);											// [ ... &arr &arr item ]
				int offset = i * subtypeSize;
				Record.populateArray(code, offset, type);							// [ ... &arr ]
			}
		}
		public void visitNewArrayNode(OperatorNode node) {
			newValueCode(node);
			code.append(removeValueCode(node.child(1)));
			
			SimpleCodeGenerator generator = (SimpleCodeGenerator) node.getSignature().getVariant();
			ASMCodeFragment fragment = generator.generate(node);
			code.append(fragment);
			if (fragment.isAddress()) {
				code.markAsAddress();
			}
		}
		
		private void visitNormalBinaryOperatorNode(OperatorNode node) {
			newValueCode(node);
			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			ASMCodeFragment arg2 = removeValueCode(node.child(1));
			
			Object variant = node.getSignature().getVariant();
			if(variant instanceof ASMOpcode) {
				code.append(arg1);
				code.append(arg2);
				
				ASMOpcode opcode = (ASMOpcode) variant;
				code.add(opcode);
			}
			else if (variant instanceof SimpleCodeGenerator) {
				code.append(arg1);
				code.append(arg2);
				
				SimpleCodeGenerator generator = (SimpleCodeGenerator) variant;
				ASMCodeFragment fragment = generator.generate(node);
				code.append(fragment);
				
				if (fragment.isAddress()) {
					code.markAsAddress();
				}
			}
			else if (variant instanceof FullCodeGenerator) {
				FullCodeGenerator generator = (FullCodeGenerator) variant;
				ASMCodeFragment fragment = generator.generate(node, arg1, arg2);
				code.append(fragment);
				
				if (fragment.isAddress()) {
					code.markAsAddress();
				}
			}
			else {
				assert false : "unknown variant in BinaryOperatorNode";
			}
			
//			ASMOpcode opcode = opcodeForOperator(node.getOperator());
//			code.add(opcode);							// type-dependent! (opcode is different for floats and for ints)
		}
		
//		private ASMOpcode opcodeForOperator(Lextant lextant) {
//			assert(lextant instanceof Punctuator);
//			Punctuator punctuator = (Punctuator)lextant;
//			switch(punctuator) {
//			case ADD: 	   		return Add;				// type-dependent!
//			case MULTIPLY: 		return Multiply;		// type-dependent!
//			default:
//				assert false : "unimplemented operator in opcodeForOperator";
//			}
//			return null;
//		}

		///////////////////////////////////////////////////////////////////////////
		// leaf nodes (ErrorNode not necessary)
		public void visit(BooleanConstantNode node) {
			newValueCode(node);
			code.add(PushI, node.getValue() ? 1 : 0);
		}
		public void visit(IdentifierNode node) {
			newAddressCode(node);
			Binding binding = node.getBinding();
			binding.generateAddress(code);
		}		
		public void visit(IntegerConstantNode node) {
			newValueCode(node);
			code.add(PushI, node.getValue());
		}
		public void visit(FloatingConstantNode node) {
			newValueCode(node);
			code.add(PushF, node.getValue());
		}
		public void visit(CharacterConstantNode node) {
			newValueCode(node);
			code.add(PushI, node.getValue());
		}
		public void visit(StringConstantNode node) {
			newValueCode(node);
			String string = node.getValue();
			code.add(PushI, string.length());
			int statusFlags = Record.STRING_STATUS;
			Record.createStringRecord(code, statusFlags, string);
		}
	}

}
