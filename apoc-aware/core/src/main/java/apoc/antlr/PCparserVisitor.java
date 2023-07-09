package apoc.antlr;

// Generated from PCparser.g4 by ANTLR 4.9.2
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link PCparserParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface PCparserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link PCparserParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStat(PCparserParser.StatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Not}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNot(PCparserParser.NotContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Or}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr(PCparserParser.OrContext ctx);
	/**
	 * Visit a parse tree produced by the {@code And}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnd(PCparserParser.AndContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BoolFalse}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoolFalse(PCparserParser.BoolFalseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BoolTrue}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoolTrue(PCparserParser.BoolTrueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BoolVar}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoolVar(PCparserParser.BoolVarContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Paren}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParen(PCparserParser.ParenContext ctx);
}