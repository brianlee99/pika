package asmCodeGenerator;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import parseTree.ParseNode;
import semanticAnalyzer.types.ArrayType;
import semanticAnalyzer.types.Type;

import static asmCodeGenerator.runtime.RunTime.*;
import static asmCodeGenerator.Macros.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import static asmCodeGenerator.runtime.Record.*;

public class SubstringCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		// TODO Auto-generated method stub
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_ADDRESS);
		
		storeITo(fragment, ARRAY_INDEXING_INDEX);
		storeITo(fragment, ARRAY_INDEXING_ARRAY);
		
		// Check for a legal array
		loadIFrom(fragment, ARRAY_INDEXING_ARRAY);
		fragment.add(JumpFalse, NULL_ARRAY_RUNTIME_ERROR);
		
		loadIFrom(fragment, ARRAY_INDEXING_INDEX);
		fragment.add(JumpNeg, INDEX_OUT_OF_BOUNDS_RUNTIME_ERROR);
		
		loadIFrom(fragment, ARRAY_INDEXING_INDEX);
		loadIFrom(fragment, ARRAY_INDEXING_ARRAY);
		readIOffset(fragment, ARRAY_LENGTH_OFFSET);
		
		fragment.add(Subtract);
		
		// Jump on non-negative values
		Labeller labeller = new Labeller("array-indexing");
		String label = labeller.newLabel("in-bounds");
		fragment.add(JumpNeg, label);
		fragment.add(Jump, INDEX_OUT_OF_BOUNDS_RUNTIME_ERROR);
		fragment.add(Label, label);
		fragment.add(Nop);
		
		// last box
		loadIFrom(fragment, ARRAY_INDEXING_ARRAY);
		fragment.add(PushI, ARRAY_HEADER_SIZE);
		fragment.add(Add);
		loadIFrom(fragment, ARRAY_INDEXING_INDEX);
		
		ArrayType arrayType = (ArrayType) (node.child(0).getType());
		Type subtype = arrayType.getSubtype();
		fragment.add(PushI, subtype.getSize());
		fragment.add(Multiply);
		fragment.add(Add);
		
		return fragment;
	}

}
