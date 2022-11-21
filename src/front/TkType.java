package front;

public enum TkType {
    // free choice of input
    IDENFR, INTCON, STRCON,
    // key word
    MAINTK, CONSTTK, INTTK, BREAKTK, CONTINUETK, IFTK, ELSETK,
    WHILETK, GETINTTK, PRINTFTK, RETURNTK,
    VOIDTK,
    // not identifier
    NOT, AND, OR,
    PLUS, MINU,
    MULT, DIV, MOD, LSS, LEQ, GRE, GEQ, EQL, NEQ,
    ASSIGN, SEMICN, COMMA, LPARENT, RPARENT, LBRACK, RBRACK, LBRACE, RBRACE;
}