grammar PCparser;

//@header {
//	package antlr;
//}

stat : boolExpr* ;

boolExpr 
	: '(' boolExpr ')'								# Paren
	| NOT boolExpr									# Not
	| boolExpr AND boolExpr							# And
	| boolExpr OR boolExpr							# Or
//	| boolExpr IFF boolExpr							# Iff
	| TRUE 											# BoolTrue
	| FALSE 										# BoolFalse
	| ID 											# BoolVar
//	| relationalExpr								# Relate 
	;



//relationalExpr
//	: arithmeticExpr EQUAL arithmeticExpr			    # Equal
//	| arithmeticExpr GREATERTHAN arithmeticExpr		  	# GreaterThan
//	| arithmeticExpr LESSTHAN arithmeticExpr		    # LessThan
//	| arithmeticExpr GREATEROREQUAL arithmeticExpr		# GreaterOrEqual
//	| arithmeticExpr LESSOREQUAL arithmeticExpr		  	# LessOrEqual
//	;


//arithmeticExpr
//	: arithmeticExpr op=(MUL|DIV) arithmeticExpr		# MulDiv
//	| arithmeticExpr op=(ADD|SUB) arithmeticExpr		# AddSub
//	| ID 												# ArithmeticVar
//	| INTNUM											# Integer
//	| REALNUM											# Real
//	| '(' arithmeticExpr ')' 							# ArithParen
//	;


// boolean constant keywords
TRUE : 'true' | 'TRUE' | 'True';
FALSE : 'false' | 'FALSE' | 'False';

// logical expr keywords
NOT : '!';
AND : '&&';
OR : '||';

// specify ID
ID : [a-zA-Z0-9_]+;

//NOT : '!' | 'not';
//AND : '&&' | 'and' | '/\\';
//OR : '||' | 'or' | '\\/';
// IMPLIES : '=>';
// IFF : '<=>';

// relational expr keywords
// EQUAL : '=';
// GREATERTHAN : '>';
// LESSTHAN : '<';
// GREATEROREQUAL : '>=';
// LESSOREQUAL : '<=';

// arithmetic expr keyworkds
// MUL : '*';
// DIV : '/';
// ADD : '+';
// SUB : '-';

// ignore the comments and whitespace
//COMMENT : '--' ~[\r\n]* -> skip;
WS  :   [ \t\n]+ -> skip ;

// number lexer rules
// INTNUM : '0'|'-'?[1-9][0-9]*;
// REALNUM : '-'?[0-9]* '.' [0-9]+;
