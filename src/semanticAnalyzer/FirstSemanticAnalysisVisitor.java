package semanticAnalyzer;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import parseTree.nodeTypes.BlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.FunctionDefinitionNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.ProgramNode;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
import symbolTable.Scope;

public class FirstSemanticAnalysisVisitor extends ParseNodeVisitor.Default {
	@Override
	public void visitEnter(ProgramNode node) {
		Scope scope = Scope.createProgramScope();
		node.setScope(scope);
	}
	public void visitLeave(ProgramNode node) {

	}
	

	@Override
	public void visitEnter(FunctionDefinitionNode node) {
		// Set scope for the function definition
		Scope baseScope = node.getLocalScope();
		Scope scope = baseScope.createParameterScope();
		node.setScope(scope);
		
		// Let's set the type of this node
		node.setFunctionSignature();
		
		IdentifierNode identifierNode = (IdentifierNode) node.child(0);
		Type type = node.getType();
		addBinding(identifierNode, type, false);
	}
	public void visitLeave(FunctionDefinitionNode node) {

	}
	
	// Assuming that this is the block of a procedure scope
	public void visitEnter(BlockNode node) {
		Scope baseScope = node.getLocalScope();
		Scope scope = baseScope.createProcedureScope();
		node.setScope(scope);
	}
	
	// Adding bindings for function calls.
	private void addBinding(IdentifierNode identifierNode, Type type, boolean isMutable) {
		Scope scope = identifierNode.getLocalScope();
		Binding binding = scope.createBinding(identifierNode, type, isMutable);
		identifierNode.setBinding(binding);
	}
	                                        
}
