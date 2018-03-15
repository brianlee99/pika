package asmCodeGenerator;

import static asmCodeGenerator.runtime.RunTime.*;
import static asmCodeGenerator.runtime.Record.*;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import parseTree.ParseNode;
import semanticAnalyzer.types.ArrayType;
import semanticAnalyzer.types.PrimitiveType;

public class StringInitializerCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_ADDRESS);
		int statusFlags = (node.getType() instanceof ArrayType || node.getType() == PrimitiveType.STRING) 
				? ARRAY_SUBTYPE_REF_STATUS 
				: ARRAY_SUBTYPE_NOT_REF_STATUS;
		int subtypeSize = node.child(0).getType().getSize();
		createEmptyArrayRecord(fragment, statusFlags, subtypeSize);
		
		return fragment;
		
	}

}
