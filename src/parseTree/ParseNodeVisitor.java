package parseTree;

import parseTree.nodeTypes.ArrayPopulationNode;
import parseTree.nodeTypes.AssignmentNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.ParameterListNode;
import parseTree.nodeTypes.ParameterSpecificationNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CallNode;
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.ControlFlowStatementNode;
import parseTree.nodeTypes.BlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.FunctionDefinitionNode;
import parseTree.nodeTypes.FunctionInvocationNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.LambdaNode;
import parseTree.nodeTypes.LambdaParamTypeNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.ReleaseStatementNode;
import parseTree.nodeTypes.ReturnNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TabNode;
import parseTree.nodeTypes.TypeNode;

// Visitor pattern with pre- and post-order visits
public interface ParseNodeVisitor {
	
	// non-leaf nodes: visitEnter and visitLeave
	void visitEnter(OperatorNode node);
	void visitLeave(OperatorNode node);
	
	void visitEnter(BlockNode node);
	void visitLeave(BlockNode node);

	void visitEnter(DeclarationNode node);
	void visitLeave(DeclarationNode node);

	void visitEnter(AssignmentNode node);
	void visitLeave(AssignmentNode node);
	
	void visitEnter(ParseNode node);
	void visitLeave(ParseNode node);
	
	void visitEnter(PrintStatementNode node);
	void visitLeave(PrintStatementNode node);
	
	void visitEnter(ProgramNode node);
	void visitLeave(ProgramNode node);
	
	void visitEnter(FunctionDefinitionNode node);
	void visitLeave(FunctionDefinitionNode node);
	
	void visitEnter(LambdaNode node);
	void visitLeave(LambdaNode node);
	
	void visitEnter(LambdaParamTypeNode node);
	void visitLeave(LambdaParamTypeNode node);
	
	void visitEnter(ParameterListNode node);
	void visitLeave(ParameterListNode node);
	
	void visitEnter(ParameterSpecificationNode node);
	void visitLeave(ParameterSpecificationNode node);
	
	void visitEnter(ReturnNode node);
	void visitLeave(ReturnNode node);
	
	void visitEnter(CallNode node);
	void visitLeave(CallNode node);

	void visitEnter(FunctionInvocationNode node);
	void visitLeave(FunctionInvocationNode node);
	
	void visitEnter(ControlFlowStatementNode node);
	void visitLeave(ControlFlowStatementNode node);

	void visitEnter(ArrayPopulationNode node);
	void visitLeave(ArrayPopulationNode node);
	
	void visitEnter(ReleaseStatementNode node);
	void visitLeave(ReleaseStatementNode node);
	
	void visitEnter(TypeNode node);
	void visitLeave(TypeNode node);

	// leaf nodes: visitLeaf only
	void visit(BooleanConstantNode node);
	void visit(ErrorNode node);
	void visit(IdentifierNode node);
	void visit(IntegerConstantNode node);
	void visit(FloatingConstantNode node);
	void visit(CharacterConstantNode node);
	void visit(StringConstantNode node);
	void visit(NewlineNode node);
	void visit(SpaceNode node);
	void visit(TabNode node);
	
	public static class Default implements ParseNodeVisitor
	{
		public void defaultVisit(ParseNode node) {	}
		public void defaultVisitEnter(ParseNode node) {
			defaultVisit(node);
		}
		public void defaultVisitLeave(ParseNode node) {
			defaultVisit(node);
		}		
		public void defaultVisitForLeaf(ParseNode node) {
			defaultVisit(node);
		}
		public void visitEnter(OperatorNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(OperatorNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(DeclarationNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(DeclarationNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(AssignmentNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(AssignmentNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(BlockNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(BlockNode node) {
			defaultVisitLeave(node);
		}				
		public void visitEnter(ParseNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ParseNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(PrintStatementNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(PrintStatementNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(ProgramNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ProgramNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(FunctionDefinitionNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(FunctionDefinitionNode node) {
			defaultVisitLeave(node);
		}
		

		public void visitEnter(LambdaNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(LambdaNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(LambdaParamTypeNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(LambdaParamTypeNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(ParameterListNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ParameterListNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(ParameterSpecificationNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ParameterSpecificationNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(ReturnNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ReturnNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(CallNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(CallNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(FunctionInvocationNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(FunctionInvocationNode node) {
			defaultVisitLeave(node);
		}
		
		public void visitEnter(ControlFlowStatementNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ControlFlowStatementNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(ArrayPopulationNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ArrayPopulationNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(ReleaseStatementNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ReleaseStatementNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(TypeNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(TypeNode node) {
			defaultVisitLeave(node);
		}
		
		public void visit(BooleanConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(ErrorNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(IdentifierNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(IntegerConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(FloatingConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(CharacterConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(StringConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(TabNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(NewlineNode node) {
			defaultVisitForLeaf(node);
		}	
		public void visit(SpaceNode node) {
			defaultVisitForLeaf(node);
		}
	}


}
