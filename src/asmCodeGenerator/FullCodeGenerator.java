package asmCodeGenerator;

import parseTree.ParseNode;
import asmCodeGenerator.codeStorage.ASMCodeFragment;

public interface FullCodeGenerator {
	public ASMCodeFragment generate(ParseNode node, ASMCodeFragment... operandCode);
}
