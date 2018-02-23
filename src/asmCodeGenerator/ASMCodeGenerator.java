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
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.ControlFlowStatementNode;
import parseTree.nodeTypes.BlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.ReleaseStatementNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TabNode;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
import symbolTable.Scope;
import static asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import static asmCodeGenerator.runtime.RunTime.RATIONAL_SUBTRACT;

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

		code.append( MemoryManager.codeForInitialization());
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
		code.append( programCode());
		code.add(    Halt );
	
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
				code.add(LoadI);			// load numerator
				code.add(Exchange);
				code.add(PushI, 4);
				code.add(Add);
				code.add(LoadI);			// load denominator
			}
			else if (nodeType == PrimitiveType.STRING) {
				code.add(LoadI);
			}
			else if (nodeType instanceof Array) {
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
			}
			else {
				code.add(opcodeForStore(type));
			}
		}
		
		private ASMCodeFragment rationalFragmentForStore() {
			ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VOID);

			// [addr num denom] -> [addr num denom addr]
			// save num and denom in temp variables
			Macros.storeITo(fragment, RunTime.DENOMINATOR_1);
			Macros.storeITo(fragment, RunTime.NUMERATOR_1);
			// make a copy of addr
			fragment.add(Duplicate);
			// bring back num
			Macros.loadIFrom(fragment, RunTime.NUMERATOR_1);

			// swap					[addr num addr]
			fragment.add(Exchange);
			
			// bring back denom     [addr num addr denom]
			Macros.loadIFrom(fragment, RunTime.DENOMINATOR_1);
			
			// swap                 [addr num denom addr]
			fragment.add(Exchange);
			
			// add 4                [addr num denom addr+4]
			fragment.add(PushI, 4);
			fragment.add(Add);
			
			// swap                 [addr num addr+4 denom]
			fragment.add(Exchange);

			fragment.add(StoreI);
			fragment.add(StoreI);
			
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
			}
			else {
				code.add(opcodeForStore(type));
			}
		}
	
		private ASMOpcode opcodeForStore(Type type) {
			if(type == PrimitiveType.INTEGER) {
				return StoreI;
			}
			if(type == PrimitiveType.FLOATING) {
				return StoreF;
			}
			if(type == PrimitiveType.BOOLEAN) {
				return StoreC;
			}
			if(type == PrimitiveType.CHARACTER) {
				return StoreC;
			}
			if (type == PrimitiveType.STRING) {
				return StoreI;
			}
			if (type instanceof Array) {
				return StoreI;
			}

			assert false: "Type " + type + " unimplemented in opcodeForStore()";
			return null;
		}

		

		///////////////////////////////////////////////////////////////////////////
		// expressions
		public void visitLeave(ControlFlowStatementNode node) {
			if (!node.getToken().isLextant(Keyword.IF, Keyword.WHILE)) {
				assert false;
			}
			Lextant controlFlowType = node.getControlFlowType();
			
			if (controlFlowType == Keyword.IF) {
				ASMCodeFragment conditionCode = removeValueCode(node.child(0));
				ASMCodeFragment thenCode = removeVoidCode(node.child(1));
				
				Labeller labeller = new Labeller("if");
				String falseLabel = labeller.newLabel("false");
				String endLabel   = labeller.newLabel("end");
				
				newVoidCode(node);
				code.append(conditionCode);
				code.add(JumpFalse, falseLabel);
				
				// true code
				code.append(thenCode);
				code.add(Jump, endLabel);
				
				// false code
				code.add(Label, falseLabel);
				if (node.nChildren() == 3) {
					code.append(removeVoidCode(node.child(2)));
				}
				
				code.add(Label, endLabel);
			}
			else if (controlFlowType == Keyword.WHILE) {
				ASMCodeFragment conditionCode = removeValueCode(node.child(0));
				ASMCodeFragment loopCode = removeVoidCode(node.child(1));
				
				Labeller labeller = new Labeller("while");
				String loopLabel = labeller.newLabel("loop");
				String endLabel   = labeller.newLabel("end");
				
				newVoidCode(node);
				code.add(Label, loopLabel);
				code.append(conditionCode);
				code.add(JumpFalse, endLabel);
				
				// loop body
				code.append(loopCode);
				code.add(Jump, loopLabel);

				code.add(Label, endLabel);
			}
			else {
				assert false;
			}
		}
		
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
			else if (operator == Keyword.LENGTH || operator == Keyword.CLONE) {
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
			else if (leftNodeType == PrimitiveType.BOOLEAN)
				code.add(Subtract);
			else if (leftNodeType == PrimitiveType.STRING)
				code.add(Subtract);
			else if (leftNodeType instanceof Array)
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
				else if (leftNodeType instanceof Array){
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
				else if (leftNodeType instanceof Array) {
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
			// Type leftNodeType = node.child(0).getType();
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
		                                                      
		private void visitUnaryOperatorNode(OperatorNode node,
				Lextant operator) {
			
			// NOT
			if (operator == Punctuator.NOT) {
				visitNotOperatorNode(node);
			}
			else {
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
			if (fragment.isAddress()) {
				code.markAsAddress();
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
