package semanticAnalyzer;

import java.util.ArrayList;
import java.util.List;

import asmCodeGenerator.Labeller;
import lexicalAnalyzer.Keyword;
import logging.PikaLogger;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import parseTree.nodeTypes.BlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ForStatementNode;
import parseTree.nodeTypes.FunctionDefinitionNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.LambdaNode;
import parseTree.nodeTypes.ParameterSpecificationNode;
import parseTree.nodeTypes.ProgramNode;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
import symbolTable.Scope;

public class FirstSemanticAnalysisVisitor extends ParseNodeVisitor.Default {
	@Override
	public void visitEnter(ProgramNode node) {
		createProgramScope(node);
		
		// sort the children nodes
		List<ParseNode> children = node.getChildren();
		for (int i = 0; i < node.nChildren() ; i++) {
			if (node.child(i) instanceof FunctionDefinitionNode) {
				ParseNode targetNode = node.child(i);
				children.remove(i);
				children.add(targetNode);

			}
		}
		for (int i = 0; i < node.nChildren() ; i++) {
			if (node.child(i) instanceof BlockNode) {
				ParseNode targetNode = node.child(i);
				children.remove(i);
				children.add(targetNode);
				break;
			}
		}
		
	}


	private void createProgramScope(ParseNode node) {
		Scope scope = Scope.createProgramScope();
		node.setScope(scope);
	}
	
	@Override
	public void visitLeave(FunctionDefinitionNode node) {
		node.setFunctionSignature();
		IdentifierNode identifierNode = (IdentifierNode) node.child(0);
		Type type = node.getType();
		addBinding(identifierNode, type, false);
	}
	
	@Override
	public void visitLeave(LambdaNode node) {
		Scope baseScope = node.getLocalScope();
		Scope scope = baseScope.createParameterScope();
		node.setScope(scope);
	}
	
	@Override
	// Assuming that this is the block of a procedure scope
	public void visitLeave(BlockNode node) {
		ParseNode parent = node.getParent();
		if (parent instanceof LambdaNode) {
			Scope baseScope = node.getLocalScope();
			Scope scope = baseScope.createProcedureScope();
			node.setScope(scope);
		}
	}
	
	private void addBinding(IdentifierNode identifierNode, Type type, boolean isMutable) {
		Scope scope = identifierNode.getLocalScope();
		Binding binding = scope.createBinding(identifierNode, type, isMutable);
		identifierNode.setBinding(binding);
	}
	
}
