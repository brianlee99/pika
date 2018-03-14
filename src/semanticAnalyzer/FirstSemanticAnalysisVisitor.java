package semanticAnalyzer;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import parseTree.nodeTypes.BlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.FunctionDefinitionNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.LambdaNode;
import parseTree.nodeTypes.ProgramNode;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
import symbolTable.Scope;

public class FirstSemanticAnalysisVisitor extends ParseNodeVisitor.Default {
	@Override
	public void visitEnter(ProgramNode node) {
		enterProgramScope(node);
	}
	public void visitLeave(ProgramNode node) {

	}
	
	private void enterProgramScope(ParseNode node) {
		Scope scope = Scope.createProgramScope();
		node.setScope(scope);
	}
	

	@Override
	public void visitEnter(FunctionDefinitionNode node) {
		node.setFunctionSignature();
		IdentifierNode identifierNode = (IdentifierNode) node.child(0);
		Type type = node.getType();
		addBinding(identifierNode, type, false);
	}
	
	public void visitLeave(FunctionDefinitionNode node) {

	}
	
	public void visitEnter(LambdaNode node) {
		// Set scope for the function definition
		Scope baseScope = node.getLocalScope();
		Scope scope = baseScope.createParameterScope();
		node.setScope(scope);
	}
	
	// Assuming that this is the block of a procedure scope
	public void visitEnter(BlockNode node) {
		ParseNode parent = node.getParent();
		if (parent instanceof LambdaNode) {
			Scope baseScope = node.getLocalScope();
			Scope scope = baseScope.createProcedureScope();
			node.setScope(scope);
		}
	}
	
	// Adding bindings for function calls.
	private void addBinding(IdentifierNode identifierNode, Type type, boolean isMutable) {
		Scope scope = identifierNode.getLocalScope();
		Binding binding = scope.createBinding(identifierNode, type, isMutable);
		identifierNode.setBinding(binding);
	}
	                                        
}
