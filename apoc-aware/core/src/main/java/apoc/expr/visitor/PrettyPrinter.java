// Expr -> infix string

package apoc.expr.visitor;

import apoc.expr.composite.*;

public class PrettyPrinter implements Visitor {
	
	public String infixOutput;
	
	public PrettyPrinter() {
		infixOutput = "";
	}
	
	public void visitBinaryExpr (BinaryExpr b, String op) {
		PrettyPrinter leftPrinter = new PrettyPrinter();
		b.left().accept(leftPrinter);
		
		PrettyPrinter rightPrinter = new PrettyPrinter();
		b.right().accept(rightPrinter);
		infixOutput = infixOutput.concat("(" + leftPrinter.infixOutput + " " + op + " " + rightPrinter.infixOutput + ")");
	}
	
	public void visitUnaryExpr(UnaryExpr u, String op) {
		
		PrettyPrinter p = new PrettyPrinter();
		
		u.child.accept(p);
		
		infixOutput = infixOutput.concat("(" + op + " " + p.infixOutput + ")");
	}

	@Override
	public void visitAnd(Conjunction conjunction) {
		visitBinaryExpr(conjunction, "&&");
	}

	@Override
	public void visitOr(Disjunction disjunction) {
		visitBinaryExpr(disjunction, "||");
	}

	@Override
	public void visitNot(Negation negation) {
		visitUnaryExpr(negation, "!");
	}

	@Override
	public void visitBoolFalse(BoolFalse boolFalse) {
		infixOutput = infixOutput.concat(boolFalse.name);
	}

	@Override
	public void visitBoolTrue(BoolTrue boolTrue) {
		infixOutput = infixOutput.concat(boolTrue.name);
	}

	@Override
	public void visitBoolVar(BoolVar boolVar) {
		infixOutput = infixOutput.concat(boolVar.name);
	}

}
