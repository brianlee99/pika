package asmCodeGenerator;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import parseTree.ParseNode;
import semanticAnalyzer.types.ArrayType;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;

import static asmCodeGenerator.runtime.Record.*;

public class ArrayCloningCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		// TODO Auto-generated method stub
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VALUE);
		int statusFlags = (node.child(0).getType() instanceof ArrayType || node.getType() == PrimitiveType.STRING) 
				? ARRAY_SUBTYPE_REF_STATUS 
				: ARRAY_SUBTYPE_NOT_REF_STATUS;
		ArrayType arrayType = (ArrayType) node.child(0).getType();
		Type subtype = arrayType.getSubtype();
		int subtypeSize = subtype.getSize();
		
		cloneArrayRecord(fragment, statusFlags, subtypeSize, subtype);
		return fragment;
	}

}
