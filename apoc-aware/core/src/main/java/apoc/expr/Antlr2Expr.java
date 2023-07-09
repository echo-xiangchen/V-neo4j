package apoc.expr;

import java.util.*;
import apoc.antlr.*;
import apoc.antlr.PCparserParser.*;
import apoc.path.AwarePathExplorer;

import static info.scce.addlib.cudd.Cudd.*;

public class Antlr2Expr extends PCparserBaseVisitor<Long>{
	
	
	/* *****************************************************************************************
	 * TODO Methods for boolExpr rule
	 * *****************************************************************************************
	 */
	
	// Negation
	@Override
	public Long visitNot(NotContext ctx) {
		long not = Cudd_Not(visit(ctx.boolExpr()));
		
		Cudd_Ref(not);
		return not;
	}
	
	// Conjunction
	@Override
	public Long visitAnd(AndContext ctx) {
		long and = Cudd_bddAnd(AwarePathExplorer.ddManager, visit(ctx.boolExpr(0)), visit(ctx.boolExpr(1)));
		
		Cudd_Ref(and);
		return and;
	}
	
	// Disjunction
	@Override
	public Long visitOr(OrContext ctx) {
		long or = Cudd_bddOr(AwarePathExplorer.ddManager, visit(ctx.boolExpr(0)), visit(ctx.boolExpr(1)));
		
		Cudd_Ref(or);
		return or;
	}
		
	// boolean true declaration
		@Override
		public Long visitBoolTrue(BoolTrueContext ctx) {
			return Cudd_ReadOne(AwarePathExplorer.ddManager);
		}
		
		// boolean false declaration
		@Override
		public Long visitBoolFalse(BoolFalseContext ctx) {
			return Cudd_ReadLogicZero(AwarePathExplorer.ddManager);
		}
		
	// boolean variable verification
	@Override
	public Long visitBoolVar(BoolVarContext ctx) {
		// need to check if the variable has been created
		if (!AwarePathExplorer.exprMap.containsKey(ctx.ID().getText())) {
			// create the variable and then increment the index
			long var = Cudd_bddIthVar(AwarePathExplorer.ddManager, AwarePathExplorer.varIndex);
			AwarePathExplorer.varIndex++;
			Cudd_Ref(var);
			
			// store the variable to the map
			AwarePathExplorer.exprMap.put(ctx.ID().getText(), var);
		}
		
		//address.add(varMap.get(boolVar.name));
		return AwarePathExplorer.exprMap.get(ctx.ID().getText());
	}
	
	
	// parentheses
	@Override
	public Long visitParen(ParenContext ctx) {
		return visit(ctx.boolExpr());
	}
}
