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

public class PreSemanticAnalysisVisitor extends ParseNodeVisitor.Default {
	@Override
	public void visitEnter(ProgramNode node) {
		enterProgramScope(node);
	}
	public void visitLeave(ProgramNode node) {
		leaveScope(node);
	}
	
	private void enterProgramScope(ParseNode node) {
		Scope scope = Scope.createProgramScope();
		node.setScope(scope);
	}		
	private void enterSubscope(ParseNode node) {
		Scope baseScope = node.getLocalScope();
		Scope scope = baseScope.createSubscope();
		node.setScope(scope);
	}
	private void leaveScope(ParseNode node) {
		node.getScope().leave();
	}
	
	@Override
	public void visitEnter(FunctionDefinitionNode node) {
		// enterProgramScope(node);
		Scope baseScope = node.getLocalScope();
		Scope scope = baseScope.createParameterScope();
		node.setScope(scope);
	}
	public void visitLeave(FunctionDefinitionNode node) {
		// leaveScope(node);
	}
	
	// Assuming that this is the block of a procedure scope
	public void visitEnter(BlockNode node) {
		Scope baseScope = node.getLocalScope();
		Scope scope = baseScope.createProcedureScope();
		node.setScope(scope);
	}
	
//	@Override
//	public void visit(IdentifierNode node) {
//		if(!isBeingDeclared(node)) {		
//			Binding binding = node.findVariableBinding();
//			
//			node.setType(binding.getType());
//			node.setBinding(binding);
//		}
//		// else parent DeclarationNode does the processing.
//	}
//	private boolean isBeingDeclared(FunctionDefinitionNode node) {
//		ParseNode parent = node.getParent();
//		return (parent instanceof DeclarationNode) && (node == parent.child(0));
//	}
	
	// Adding bindings for function calls.
	private void addBinding(IdentifierNode identifierNode, Type type, boolean isMutable) {
		Scope scope = identifierNode.getLocalScope();
		Binding binding = scope.createBinding(identifierNode, type, isMutable);
		identifierNode.setBinding(binding);
	}
	                                        
}
