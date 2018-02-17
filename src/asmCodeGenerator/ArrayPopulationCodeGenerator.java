package asmCodeGenerator;

import static asmCodeGenerator.runtime.Record.ARRAY_SUBTYPE_NOT_REF_STATUS;
import static asmCodeGenerator.runtime.Record.ARRAY_SUBTYPE_REF_STATUS;
import static asmCodeGenerator.runtime.RunTime.*;

import java.util.List;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import parseTree.ParseNode;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.PrimitiveType;

public class ArrayPopulationCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_ADDRESS);
//		int statusFlags = (node.getType() instanceof Array || node.getType() == PrimitiveType.STRING) 
//				? ARRAY_SUBTYPE_REF_STATUS 
//				: ARRAY_SUBTYPE_NOT_REF_STATUS;
//		int subtypeSize = node.getType().getSize();
//		createEmptyArrayRecord(fragment, statusFlags, subtypeSize);
//		
//		List<ParseNode> children = node.getChildren();
//		populateArray(fragment, children, subtypeSize);		// []
//		
		return fragment;
	}

}
