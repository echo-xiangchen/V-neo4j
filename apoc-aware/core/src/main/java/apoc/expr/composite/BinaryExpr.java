package apoc.expr.composite;

import java.util.ArrayList;
import java.util.List;

public abstract class BinaryExpr extends Expr {
	
	// attribute for binary expr
	public List<Expr> children;
		
	public Expr left() {
		return children.get(0);
	}
	
	public Expr right() {
		return children.get(1);
	}
	
	public BinaryExpr(Expr expr1, Expr expr2) {
		children = new ArrayList<Expr>();
		children.add(expr1);
		children.add(expr2);
	}
}
