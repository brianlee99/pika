package asmCodeGenerator;

import static asmCodeGenerator.runtime.Record.*;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import parseTree.ParseNode;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;

public class NewArrayCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VALUE);
		Type subtype = ((Array) node.getType()).getSubtype();
		int statusFlags = (subtype instanceof Array || subtype == PrimitiveType.STRING) 
				? ARRAY_SUBTYPE_REF_STATUS 
				: ARRAY_SUBTYPE_NOT_REF_STATUS;
		int subtypeSize = subtype.getSize();
		createEmptyArrayRecord(fragment, statusFlags, subtypeSize);
		
		return fragment;
		
	}

}
