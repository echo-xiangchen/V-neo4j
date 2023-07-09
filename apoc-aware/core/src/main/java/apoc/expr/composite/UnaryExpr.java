package apoc.expr.composite;


public class UnaryExpr extends Expr {

	// attribute for unary expr
	public Expr child;
	
	
	public UnaryExpr(Expr expr) {
		this.child = expr;
	}
}
