import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a Mini program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Kids
//     --------            ----
//     ProgramNode         DeclListNode
//     DeclListNode        linked list of DeclNode
//     DeclNode:
//       VarDeclNode       TypeNode, IdNode, int
//       FnDeclNode        TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode    TypeNode, IdNode
//       StructDeclNode    IdNode, DeclListNode
//
//     FormalsListNode     linked list of FormalDeclNode
//     FnBodyNode          DeclListNode, StmtListNode
//     StmtListNode        linked list of StmtNode
//     ExpListNode         linked list of ExpNode
//
//     TypeNode:
//       IntNode           -- none --
//       BoolNode          -- none --
//       VoidNode          -- none --
//       StructNode        IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       DotAccessNode       ExpNode, IdNode
//       AssignNode          ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of kids, or
// internal nodes with a fixed number of kids:
//
// (1) Leaf nodes:
//        IntNode,   BoolNode,  VoidNode,  IntLitNode,  StrLitNode,
//        TrueNode,  FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of kids:
//        ProgramNode,     VarDeclNode,     FnDeclNode,     FormalDeclNode,
//        StructDeclNode,  FnBodyNode,      StructNode,     AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, ReadStmtNode,   WriteStmtNode   
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  CallStmtNode
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode,  CallExpNode,
//        UnaryExpNode,    BinaryExpNode,   UnaryMinusNode, NotNode,
//        PlusNode,        MinusNode,       TimesNode,      DivideNode,
//        AndNode,         OrNode,          EqualsNode,     NotEqualsNode,
//        LessNode,        GreaterNode,     LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode { 
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void doIndent(PrintWriter p, int indent) {
        for (int k=0; k<indent; k++) p.print(" ");
    }
}

// **********************************************************************
// ProgramNode,  DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    /**
     * nameAnalysis
     * Creates an empty symbol table for the outermost scope, then processes
     * all of the globals, struct defintions, and functions in the program.
     */
    public void nameAnalysis() {
        SymTable symTab = new SymTable();
        myDeclList.nameAnalysis(symTab);
    }
    
    public boolean typeCheck(){
    	return myDeclList.typeCheck();
    }
    
    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    // 1 kid
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, process all of the decls in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        nameAnalysis(symTab, symTab);
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab and a global symbol table globalTab
     * (for processing struct names in variable decls), process all of the 
     * decls in the list.
     */    
    public void nameAnalysis(SymTable symTab, SymTable globalTab) {
        for (DeclNode node : myDecls) {
            if (node instanceof VarDeclNode) {
                ((VarDeclNode)node).nameAnalysis(symTab, globalTab);
            } else {
                node.nameAnalysis(symTab);
            }
        }
    }
    
    public boolean typeCheck() {
        boolean noError = true;
    	for (DeclNode decl : myDecls) {
            boolean b = decl.typeCheck();
            noError = noError && b;
        }
    	return noError;
    }
    
    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    // list of kids (DeclNodes)
    private List<DeclNode> myDecls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * for each formal decl in the list
     *     process the formal decl
     *     if there was no error, add type of formal decl to list
     */
    public List<Type> nameAnalysis(SymTable symTab) {
        List<Type> typeList = new LinkedList<Type>();
        for (FormalDeclNode node : myFormals) {
            SemSym sym = node.nameAnalysis(symTab);
            if (sym != null) {
                typeList.add(sym.getType());
            }
        }
        return typeList;
    }    
    
    /**
     * Return the number of formals in this list.
     */
    public int length() {
        return myFormals.size();
    }
    
    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    // list of kids (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the declaration list
     * - process the statement list
     */
    public void nameAnalysis(SymTable symTab) {
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
    }    
    
    public boolean typeCheck(Type t) {
		return myStmtList.typeCheck(t);
	}
    
    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    // 2 kids
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
	
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, process each statement in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        for (StmtNode node : myStmts) {
            node.nameAnalysis(symTab);
        }
    }    
    
    public boolean typeCheck(Type t) {
    	boolean noError = true;
    	for (StmtNode node : myStmts) {
            boolean err = node.typeCheck(t);
            noError = noError && err;
    	}
    	
    	
    	return noError;
    }
    
    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    // list of kids (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
        typeList = new LinkedList<Type>();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, process each exp in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        for (ExpNode node : myExps) {
            node.nameAnalysis(symTab);
        }
    }
    
    public boolean typeCheck() {
    	boolean noError = true;
    	
    	for (ExpNode e: myExps) {
    		e.typeCheck();
    		typeList.add(e.getType());
    		
    		if (e.getType().isErrorType()) {
    			noError = false;
    		}
    	}
    	
    	return noError;
    	  	
    }
    
    public int getSize() {
    	return myExps.size();
    }
    
    public List<Type> getTypes() {
    	return typeList;
    }
    
    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }
    
    int getLineNum(int i) {
    	return myExps.get(i).getLineNum();
    }
    
    int getCharNum(int i) {
    	return myExps.get(i).getCharNum();
    }

    // list of kids (ExpNodes)
    private List<ExpNode> myExps;
    private List<Type> typeList;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    /**
     * Note: a formal decl needs to return a sym
     */
    abstract public SemSym nameAnalysis(SymTable symTab);
    
    public boolean typeCheck() {return true;}
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    /**
     * nameAnalysis (overloaded)
     * Given a symbol table symTab, do:
     * if this name is declared void, then error
     * else if the declaration is of a struct type, 
     *     lookup type name (globally)
     *     if type name doesn't exist, then error
     * if no errors so far,
     *     if name has already been declared in this scope, then error
     *     else add name to local symbol table     
     *
     * symTab is local symbol table (say, for struct field decls)
     * globalTab is global symbol table (for struct type names)
     * symTab and globalTab can be the same
     */
    public SemSym nameAnalysis(SymTable symTab) {
        return nameAnalysis(symTab, symTab);
    }
    
    public SemSym nameAnalysis(SymTable symTab, SymTable globalTab) {
        boolean badDecl = false;
        String name = myId.name();
        SemSym sym = null;
        IdNode structId = null;

        if (myType instanceof VoidNode) {  // check for void type
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        else if (myType instanceof StructNode) {
            structId = ((StructNode)myType).idNode();
            sym = globalTab.lookupGlobal(structId.name());
            
            // if the name for the struct type is not found, 
            // or is not a struct type
            if (sym == null || !(sym instanceof StructDefSym)) {
                ErrMsg.fatal(structId.lineNum(), structId.charNum(), 
                             "Invalid name of struct type");
                badDecl = true;
            }
            else {
                structId.link(sym);
            }
        }
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Multiply declared identifier");
            badDecl = true;            
        }
        
        if (!badDecl) {  // insert into symbol table
            try {
                if (myType instanceof StructNode) {
                    sym = new StructSym(structId);
                }
                else {
                    sym = new SemSym(myType.type());
                }
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return sym;
    }    
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.println(";");
    }

    // 3 kids
    private TypeNode myType;
    private IdNode myId;
    private int mySize;  // use value NOT_STRUCT if this is not a struct type

    public static int NOT_STRUCT = -1;
}

class FnDeclNode extends DeclNode {
    public FnDeclNode(TypeNode type,
                      IdNode id,
                      FormalsListNode formalList,
                      FnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name has already been declared in this scope, then error
     * else add name to local symbol table
     * in any case, do the following:
     *     enter new scope
     *     process the formals
     *     if this function is not multiply declared,
     *         update symbol table entry with types of formals
     *     process the body of the function
     *     exit scope
     */
    public SemSym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        FnSym sym = null;
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                         "Multiply declared identifier");
        }
        
        else { // add function name to local symbol table
            try {
                sym = new FnSym(myType.type(), myFormalsList.length());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        symTab.addScope();  // add a new scope for locals and params
        
        // process the formals
        List<Type> typeList = myFormalsList.nameAnalysis(symTab);
        if (sym != null) {
            sym.addFormals(typeList);
        }
        
        myBody.nameAnalysis(symTab); // process the function body
        
        try {
            symTab.removeScope();  // exit scope
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in FnDeclNode.nameAnalysis");
            System.exit(-1);
        }
        
        return null;
    }
    
    public boolean typeCheck() {
    	
    	Type fnType = this.myType.type();
    	return myBody.typeCheck(fnType);
    	
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.print("(");
        myFormalsList.unparse(p, 0);
        p.println(") {");
        myBody.unparse(p, indent+4);
        p.println("}\n");
    }

    // 4 kids
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FnBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this formal is declared void, then error
     * else if this formal is already in the local symble table,
     *     then issue multiply declared error message and return null
     * else add a new entry to the symbol table and return that Sym
     */
    public SemSym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;
        SemSym sym = null;
        
        if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Multiply declared identifier");
            badDecl = true;
        }
        
        if (!badDecl) {  // insert into symbol table
            try {
                sym = new SemSym(myType.type());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return sym;
    }    
    
    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
    }

    // 2 kids
    private TypeNode myType;
    private IdNode myId;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name is already in the symbol table,
     *     then multiply declared error (don't add to symbol table)
     * create a new symbol table for this struct definition
     * process the decl list
     * if no errors
     *     add a new entry to symbol table for this struct
     */
    public SemSym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Multiply declared identifier");
            badDecl = true;            
        }

        SymTable structSymTab = new SymTable();
        
        // process the fields of the struct
        myDeclList.nameAnalysis(structSymTab, symTab);
        
        if (!badDecl) {
            try {   // add entry to symbol table
                StructDefSym sym = new StructDefSym(structSymTab);
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return null;
    }    
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("struct ");
        p.print(myId.name());
        p.println("{");
        myDeclList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("};\n");

    }

    // 2 kids
    private IdNode myId;
    private DeclListNode myDeclList;
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    /* all subclasses must provide a type method */
    abstract public Type type();
}

class IntNode extends TypeNode {
    public IntNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new IntType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }
}

class BoolNode extends TypeNode {
    public BoolNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new BoolType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }
    
    /**
     * type
     */
    public Type type() {
        return new VoidType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        myId = id;
    }

    public IdNode idNode() {
        return myId;
    }
    
    /**
     * type
     */
    public Type type() {
        return new StructType(myId);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        p.print(myId.name());
    }
    
    // 1 kid
    private IdNode myId;
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void nameAnalysis(SymTable symTab);
    
    abstract public boolean typeCheck(Type t);
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignNode assign) {
        myAssign = assign;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myAssign.nameAnalysis(symTab);
    }
    
    public boolean typeCheck(Type t) {

    	return myAssign.typeCheck();
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(";");
    }

    // 1 kid
    private AssignNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    public boolean typeCheck(Type t) {
    	//TODO: arithmetic and relational operators: Only integer expressions can be used as 
    	// operands of these operators. The result of applying an arithmetic operator to 
    	// int operand(s) is int.
    	
    	myExp.typeCheck();
    	if (myExp.getType().isErrorType()) {
    		//TODO: Do More? Save error?
    		return false;
    	}
    	else if (!myExp.getType().isIntType()) {
    		ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), 
    				"Arithmetic operator applied to non-numeric operand");
    		return false;
    	}
    	return true;
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++;");
    }

    // 1 kid
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--;");
    }
    
    public boolean typeCheck(Type t) {
    	//TODO: arithmetic and relational operators: Only integer expressions can be used as 
    	// operands of these operators. The result of applying an arithmetic operator to 
    	// int operand(s) is int.
    	
    	myExp.typeCheck();
    	if (myExp.getType().isErrorType()) {
    		//TODO: Do More? Save error?
    		return false;
    	}
    	else if (!myExp.getType().isIntType()) {
    		ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), 
    				"Arithmetic operator applied to non-numeric operand");
    		return false;
    	}
    	return true;
    }

    // 1 kid
    private ExpNode myExp;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }    
    
    public boolean typeCheck(Type t) {
    	//TODO: Only an int or bool expression or a string literal can be 
    	// printed by cout. Only an int or bool identifer can be read by 
    	// cin. Note that the identifier can be a field of a struct type
    	// (accessed using . ) as long as the field is an int or a bool.
    	
    	myExp.typeCheck();
    	if (myExp.getType().isErrorType()) {
    		//TODO: Do more? save error?
    		return false;
    	}
    	else if(myExp.getType().isFnType()) {
    		//setType(new ErrorType());
    		ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), 
    				"Attempt to read a function");
    		return false;
    	}
    	else if(myExp.getType().isStructDefType()) {
    		//setType(new ErrorType());
    		ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), 
    				"Attempt to read a struct name");
    		return false;
    	}
    	else if(myExp.getType().isStructType()) {
    		//setType(new ErrorType());
    		ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), 
    				"Attempt to read a struct variable");
    		return false;
    	}
	else if (myExp.getType().isStringType()) {
	    ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(),
			 "Attempt to read a string literal");
	    return false;
	}
    	return true; 
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("cin >> ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // 1 kid (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("cout << ");
        myExp.unparse(p, 0);
        p.println(";");
    }
    
    public boolean typeCheck(Type t) {
    	
    	myExp.typeCheck();
    	//TODO: Copy from READ above
    	if (myExp.getType().isErrorType()) {
    		//TODO: Do more? save error?
    		return false;
    	}
    	else if(myExp.getType().isFnType()) {
    		//setType(new ErrorType());
    		ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), 
    				"Attempt to write a function");
    		return false;
    	}
    	else if(myExp.getType().isStructDefType()) {
    		//setType(new ErrorType());
    		ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), 
    				"Attempt to write a struct name");
    		return false;
    	}
    	else if(myExp.getType().isStructType()) {
    		//setType(new ErrorType());
    		ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), 
    				"Attempt to write a struct variable");
    		return false;
    	}
    	else if(myExp.getType().isVoidType()) {
    		//setType(new ErrorType());
    		ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), 
    				"Attempt to write void");
    		return false;
    	}
    	
    	return true; 
    }

    // 1 kid
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    //TODO: Add type object?
    public boolean typeCheck(Type t) {
    	boolean noError = true;
    	myExp.typeCheck();
    		
    	if (myExp.getType().isErrorType()) {
    		//setType(new ErrorType())
    		noError = false;
    	}
    	else if (!myExp.getType().isBoolType()) {
    		//setType(new ErrorType());
    		ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), 
    				"Non-bool expression used as an if condition");
    		noError = false;
    	}
    	
    	if(!myStmtList.typeCheck(t)) {
    		noError = false;
    	}
    	return noError;
    	
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
    }

    // e kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
                          StmtListNode slist1, DeclListNode dlist2,
                          StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }
    
    public boolean typeCheck(Type t) {
    	boolean noError = true;
    	myExp.typeCheck();
    	
    	//TODO: Steal from if statement
    	if (myExp.getType().isErrorType()) {
    		//setType(new ErrorType())
    		noError = false;
    	}
    	else if (!myExp.getType().isBoolType()) {
    		//setType(new ErrorType());
    		ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), 
    				"Non-bool expression used as an if condition");
    		noError = false;
    	}
    	
    	if (!myThenStmtList.typeCheck(t)) {
    		noError = false;
    	}
    	
    	if (!myElseStmtList.typeCheck(t)){
    		noError = false;
    	}
    	
    	return noError;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts of then
     * - exit the scope
     * - enter a new scope
     * - process the decls and stmts of else
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myThenDeclList.nameAnalysis(symTab);
        myThenStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
        symTab.addScope();
        myElseDeclList.nameAnalysis(symTab);
        myElseStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent+4);
        myThenStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
        doIndent(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent+4);
        myElseStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");        
    }

    // 5 kids
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    public boolean typeCheck(Type t) {
    	boolean noError = true;
    	myExp.typeCheck();
    	
    	if (myExp.getType().isErrorType()) {
    		//setType(new ErrorType())
    		noError = false;
    	}
    	else if (!myExp.getType().isBoolType()) {
    		//setType(new ErrorType());
    		ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), 
    				"Non-bool expression used as a while condition");
    		noError = false;
    	}
    	
    	if (!myStmtList.typeCheck(t)) {
    		noError = false;
    	}
    	
    	return noError;
    	
    	
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
    }

    // 3 kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }
    
    public boolean typeCheck(Type t) {
    	return myCall.typeCheck();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myCall.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(";");
    }

    // 1 kid
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child,
     * if it has one
     */
    public void nameAnalysis(SymTable symTab) {
        if (myExp != null) {
            myExp.nameAnalysis(symTab);
        }
    }
    
    //TODO: add type chekcing on return
    public boolean typeCheck(Type returnType) {
    	
    	if(myExp == null) {
    		if (returnType.isVoidType()) {
    			return true;
    		}
    		else {
    			ErrMsg.fatal(0, 0, 
    				"Missing return value");
    			return false;
    		}
    	}
    	boolean noError = myExp.typeCheck();
    	// 	Missing return value
    	
    	//Return with a value in a void function
    	
    	if (!returnType.isVoidType() && myExp.getType().isVoidType()) {
    		ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), 
    				"Missing return value");
    		return false;
    	}
    	else if (returnType.isVoidType() && !myExp.getType().isVoidType()) {
    		ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), 
    				"Return with a value in a void function");
    		return false;
    	}
    	else if (!(myExp.getType().equals(returnType))) {
    		ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), 
    				"Bad return value");
    		return false;
    	}
    	return noError;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(";");
    }

    // 1 kid
    private ExpNode myExp; // possibly null
    private Type myType;
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    /**
     * Default version for nodes with no names
     */
    public void nameAnalysis(SymTable symTab) { }
    
    abstract public boolean typeCheck();
    
    private Type myType = null;
    public Type getType(){
    	return myType;
    }
    public void setType(Type t) {
    	myType = t;
    }
    
    private int myLineNum;
    private int myCharNum;
    
    /**
     * Return the line number for this exp's node. 
     * The line number is the one corresponding to the RHS.
     */
    abstract public int getLineNum();
    
    /**
     * Return the char number for this exp's node. 
     * The char number is the one corresponding to the RHS.
     */
    abstract public int getCharNum();
    
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
    
    public boolean typeCheck() {
    	setType(new IntType());
    	return true;
    }
    
    /**
     * Return the line number for this exp's node. 
     * The line number is the one corresponding to the RHS.
     */
    public int getLineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this exp's node. 
     * The char number is the one corresponding to the RHS.
     */
    public int getCharNum() {
        return myCharNum;
    }
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }
    
    public boolean typeCheck() {
    	setType(new StringType());
    	return true;
    }
    
    /**
     * Return the line number for this exp's node. 
     * The line number is the one corresponding to the RHS.
     */
    public int getLineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this exp's node. 
     * The char number is the one corresponding to the RHS.
     */
    public int getCharNum() {
        return myCharNum;
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("true");
    }
    
    public boolean typeCheck() {
    	setType(new BoolType());
    	return true;
    }
    
    /**
     * Return the line number for this exp's node. 
     * The line number is the one corresponding to the RHS.
     */
    public int getLineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this exp's node. 
     * The char number is the one corresponding to the RHS.
     */
    public int getCharNum() {
        return myCharNum;
    }

    private int myLineNum;
    private int myCharNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("false");
    }
    
    public boolean typeCheck() {
    	setType(new BoolType());
    	return true;
    }

    
    /**
     * Return the line number for this exp's node. 
     * The line number is the one corresponding to the RHS.
     */
    public int getLineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this exp's node. 
     * The char number is the one corresponding to the RHS.
     */
    public int getCharNum() {
        return myCharNum;
    }
    
    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    /**
     * Link the given symbol to this ID.
     */
    public void link(SemSym sym) {
        mySym = sym;
    }
    
    /**
     * Return the name of this ID.
     */
    public String name() {
        return myStrVal;
    }
    
    /**
     * Return the symbol associated with this ID.
     */
    public SemSym sym() {
        return mySym;
    }
    
    /**
     * Return the line number for this ID.
     */
    public int lineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this ID.
     */
    public int charNum() {
        return myCharNum;
    }
    
    /**
     * Return the line number for this exp's node. 
     * The line number is the one corresponding to the RHS.
     */
    public int getLineNum() {
        return lineNum();
    }
    
    /**
     * Return the char number for this exp's node. 
     * The char number is the one corresponding to the RHS.
     */
    public int getCharNum() {
        return charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - check for use of undeclared name
     * - if ok, link to symbol table entry
     */
    public void nameAnalysis(SymTable symTab) {
        SemSym sym = symTab.lookupGlobal(myStrVal);
        if (sym == null) {
            ErrMsg.fatal(myLineNum, myCharNum, "Undeclared identifier");
        } else {
            link(sym);
        }
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (mySym != null) {
            p.print("(" + mySym + ")");
        }
    }
    
    public boolean typeCheck() {
    	setType(mySym.getType());
    	return true;
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private SemSym mySym;
}

class DotAccessExpNode extends ExpNode {
    public DotAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;    
        myId = id;
        mySym = null;
    }

    /**
     * Return the symbol associated with this dot-access node.
     */
    public SemSym sym() {
        return mySym;
    }    
    
    public boolean typeCheck() {;
    	setType(myId.sym().getType());
    	return true;
    }
    
    /**
     * Return the line number for this dot-access node. 
     * The line number is the one corresponding to the RHS of the dot-access.
     */
    public int lineNum() {
        return myId.lineNum();
    }
    
    /**
     * Return the char number for this dot-access node.
     * The char number is the one corresponding to the RHS of the dot-access.
     */
    public int charNum() {
        return myId.charNum();
    }
    
    /**
     * Return the line number for this exp's node. 
     * The line number is the one corresponding to the RHS.
     */
    public int getLineNum() {
        return lineNum();
    }
    
    /**
     * Return the char number for this exp's node. 
     * The char number is the one corresponding to the RHS.
     */
    public int getCharNum() {
        return charNum();
    }
    
    
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the LHS of the dot-access
     * - process the RHS of the dot-access
     * - if the RHS is of a struct type, set the sym for this node so that
     *   a dot-access "higher up" in the AST can get access to the symbol
     *   table for the appropriate struct definition
     */
    public void nameAnalysis(SymTable symTab) {
        badAccess = false;
        SymTable structSymTab = null; // to lookup RHS of dot-access
        SemSym sym = null;
        
        myLoc.nameAnalysis(symTab);  // do name analysis on LHS
        
        // if myLoc is really an ID, then sym will be a link to the ID's symbol
        if (myLoc instanceof IdNode) {
            IdNode id = (IdNode)myLoc;
            sym = id.sym();
            
            // check ID has been declared to be of a struct type
            
            if (sym == null) { // ID was undeclared
                badAccess = true;
            }
            else if (sym instanceof StructSym) { 
                // get symbol table for struct type
                SemSym tempSym = ((StructSym)sym).getStructType().sym();
                structSymTab = ((StructDefSym)tempSym).getSymTable();
            } 
            else {  // LHS is not a struct type
                ErrMsg.fatal(id.lineNum(), id.charNum(), 
                             "Dot-access of non-struct type");
                badAccess = true;
            }
        }
        
        // if myLoc is really a dot-access (i.e., myLoc was of the form
        // LHSloc.RHSid), then sym will either be
        // null - indicating RHSid is not of a struct type, or
        // a link to the Sym for the struct type RHSid was declared to be
        else if (myLoc instanceof DotAccessExpNode) {
            DotAccessExpNode loc = (DotAccessExpNode)myLoc;
            
            if (loc.badAccess) {  // if errors in processing myLoc
                badAccess = true; // don't continue proccessing this dot-access
            }
            else { //  no errors in processing myLoc
                sym = loc.sym();

                if (sym == null) {  // no struct in which to look up RHS
                    ErrMsg.fatal(loc.lineNum(), loc.charNum(), 
                                 "Dot-access of non-struct type");
                    badAccess = true;
                }
                else {  // get the struct's symbol table in which to lookup RHS
                    if (sym instanceof StructDefSym) {
                        structSymTab = ((StructDefSym)sym).getSymTable();
                    }
                    else {
                        System.err.println("Unexpected Sym type in DotAccessExpNode");
                        System.exit(-1);
                    }
                }
            }

        }
        
        else { // don't know what kind of thing myLoc is
            System.err.println("Unexpected node type in LHS of dot-access");
            System.exit(-1);
        }
        
        // do name analysis on RHS of dot-access in the struct's symbol table
        if (!badAccess) {
        
            sym = structSymTab.lookupGlobal(myId.name()); // lookup
            if (sym == null) { // not found - RHS is not a valid field name
                ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                             "Invalid struct field name");
                badAccess = true;
            }
            
            else {
                myId.link(sym);  // link the symbol
                // if RHS is itself as struct type, link the symbol for its struct 
                // type to this dot-access node (to allow chained dot-access)
                if (sym instanceof StructSym) {
                    mySym = ((StructSym)sym).getStructType().sym();
                }
            }
        }
    }    
    
    public void unparse(PrintWriter p, int indent) {
        myLoc.unparse(p, 0);
        p.print(".");
        myId.unparse(p, 0);
    }

    // 2 kids
    private ExpNode myLoc;    
    private IdNode myId;
    private SemSym mySym;          // link to Sym for struct type
    private boolean badAccess;  // to prevent multiple, cascading errors
}

class AssignNode extends ExpNode {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myLhs.nameAnalysis(symTab);
        myExp.nameAnalysis(symTab);
    }
    
    public boolean typeCheck() {
    	
    	myLhs.typeCheck();
    	myExp.typeCheck();
    	//TODO: Line number  
    	
    	if (myLhs.getType().isErrorType() || myExp.getType().isErrorType()) {
    		setType(new ErrorType());
    		return false;
    	}
    	if (myLhs.getType().isBoolType() && myExp.getType().isBoolType()) {
    		setType(new BoolType());
    		return true;
    	}
    	
    	else if (myLhs.getType().isIntType() && myExp.getType().isIntType()) {
    		setType(new IntType());
    		return true;
    	}
    	//Assigning a function to a function; e.g., "f = g;", where f and g are function names.
    	else if (myLhs.getType().isFnType() && myExp.getType().isFnType()){
    		setType(new ErrorType());
    		ErrMsg.fatal(myLhs.getLineNum(), myLhs.getCharNum(), "Function assignment");
    		return false;
    	}
    	else if (myLhs.getType().isStructType() && myExp.getType().isStructType()){
    		setType(new ErrorType());
    		ErrMsg.fatal(myLhs.getLineNum(), myLhs.getCharNum(), "Struct name assignment");
    		return false;
    	}
    	else if (myLhs.getType().isStructDefType() && myExp.getType().isStructDefType()){
    		setType(new ErrorType());
    		ErrMsg.fatal(myLhs.getLineNum(), myLhs.getCharNum(), "Struct variable assignment");
    		return false;
    	}
    	else {
    		setType(new ErrorType());
    		ErrMsg.fatal(myLhs.getLineNum(), myLhs.getCharNum(), "Type mismatch");
    		return false;  	
    	}
    	
    }
    
    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)  p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)  p.print(")");
    }
    
    /**
     * Return the line number for this exp's node. 
     * The line number is the one corresponding to the RHS.
     */
    public int getLineNum() {
        return myLhs.getLineNum();
    }
    
    /**
     * Return the char number for this exp's node. 
     * The char number is the one corresponding to the RHS.
     */
    public int getCharNum() {
        return myLhs.getCharNum();
    }

    // 2 kids
    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }
    
    /* Type check for function calls
     * Catches error for 1 of following:
     * 1. Call on non-function name
     * 2. Wrong number of parameters
     * 3. Wrong types on parameters 
     * */
    public boolean typeCheck() {
    	boolean noError = true;
    	SemSym sym = myId.sym();
    	if (sym == null) {
    		System.out.println("Type Check Error: Null sym at CallExpNode");
    	}
    	
    	//Non function case
    	if (!(sym instanceof FnSym)) {
    		ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Attempt to call a non-function");
    		setType(new ErrorType());
    		return false;
    	}
    	//Otherwise, use return type as type
    	setType(((FnSym)(myId.sym())).getReturnType());
    	
    	if (myExpList.getSize() != ((FnSym)sym).getNumParams()) {
    		ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Function call with wrong number of args");
    		return false;
    	}
    	
    	//typecheck every entry
    	myExpList.typeCheck();
    	List<Type> actualTypes = myExpList.getTypes();
    	List<Type> paramTypes = ((FnSym)sym).getParamTypes();
    	for (int i = 0; i<paramTypes.size(); i++) {
    		if (actualTypes.get(i).isErrorType() || paramTypes.get(i).isErrorType()) {
    			//Do nothuing
    		}
    		else if (!(actualTypes.get(i).isBoolType() && paramTypes.get(i).isBoolType()) &&
    				 !(actualTypes.get(i).isIntType() && paramTypes.get(i).isIntType())) {
    			ErrMsg.fatal(myExpList.getLineNum(i), myExpList.getCharNum(i), "Type of actual does not match type of formal");
    			noError = false;
    		}
    	}
    	
    	return noError;
    	
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myId.nameAnalysis(symTab);
        myExpList.nameAnalysis(symTab);
    }    
    
    // ** unparse **
    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");
    }
    
    /**
     * Return the line number for this exp's node. 
     * The line number is the one corresponding to the RHS.
     */
    public int getLineNum() {
        return myId.getLineNum();
    }
    
    /**
     * Return the char number for this exp's node. 
     * The char number is the one corresponding to the RHS.
     */
    public int getCharNum() {
        return myId.getCharNum();
    }

    // 2 kids
    private IdNode myId;
    private ExpListNode myExpList;  // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    /**
     * Return the line number for this exp's node. 
     * The line number is the one corresponding to the RHS.
     */
    public int getLineNum() {
        return myExp.getLineNum();
    }
    
    /**
     * Return the char number for this exp's node. 
     * The char number is the one corresponding to the RHS.
     */
    public int getCharNum() {
        return myExp.getCharNum();
    }
    
    // one child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }
    
    /**
     * Return the line number for this exp's node. 
     * The line number is the one corresponding to the RHS.
     */
    public int getLineNum() {
        return myExp1.getLineNum();
    }
    
    /**
     * Return the char number for this exp's node. 
     * The char number is the one corresponding to the RHS.
     */
    public int getCharNum() {
        return myExp1.getCharNum();
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myExp1.nameAnalysis(symTab);
        myExp2.nameAnalysis(symTab);
    }
    
    // two kids
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }
    
    public boolean typeCheck() {
    	myExp.typeCheck();
    	
    	if (myExp.getType().isErrorType()) {
    		setType(new ErrorType());
    		return false;
    	}
    	else if (!myExp.getType().isIntType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), 
    				"Arithmetic operator applied to non-numeric operand");
    		return false;
    	}
    	
    	setType(new IntType());
    	return true;
    }
}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(!");
        myExp.unparse(p, 0);
        p.print(")");
    }
    
    public boolean typeCheck() {
    	myExp.typeCheck();
    	
    	if (myExp.getType().isErrorType()) {
    		setType(new ErrorType());
    		return false;
    	}
    	else if (!myExp.getType().isBoolType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), 
    				"Logical operator applied to non-bool operand");
    		return false;
    	}
    	
    	setType(new BoolType());
    	return true;
    }
}

// **********************************************************************
// Subclasses of BinaryExpNode
// **********************************************************************

class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
    
    public boolean typeCheck() {
    	myExp1.typeCheck();
    	myExp2.typeCheck();
    	boolean noError = true;
    	if (myExp1.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp1.getType().isIntType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Arithmetic operator applied to non-numeric operand");
    		noError = false;
    	}
    	
    	if (myExp2.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp2.getType().isIntType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), 
    				"Arithmetic operator applied to non-numeric operand");
    		noError = false;
    	}
    	
    	if (noError) {
    		setType(new IntType());
    	}
    	
    	return noError;
    	
    }
}

class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
    
    public boolean typeCheck() {
    	myExp1.typeCheck();
    	myExp2.typeCheck();
    	boolean noError = true;
    	if (myExp1.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp1.getType().isIntType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Arithmetic operator applied to non-numeric operand");
    		noError = false;
    	}
    	
    	if (myExp2.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp2.getType().isIntType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), 
    				"Arithmetic operator applied to non-numeric operand");
    		noError = false;
    	}
    	
    	if (noError) {
    		setType(new IntType());
    	}
    	
    	return noError;
    	
    }
}

class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
    
    public boolean typeCheck() {
    	myExp1.typeCheck();
    	myExp2.typeCheck();
    	boolean noError = true;
    	if (myExp1.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp1.getType().isIntType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Arithmetic operator applied to non-numeric operand");
    		noError = false;
    	}
    	
    	if (myExp2.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp2.getType().isIntType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), 
    				"Arithmetic operator applied to non-numeric operand");
    		noError = false;
    	}
    	
    	if (noError) {
    		setType(new IntType());
    	}
    	
    	return noError;
    	
    }
}

class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
    
    public boolean typeCheck() {
    	myExp1.typeCheck();
    	myExp2.typeCheck();
    	boolean noError = true;
    	if (myExp1.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp1.getType().isIntType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Arithmetic operator applied to non-numeric operand");
    		noError = false;
    	}
    	
    	if (myExp2.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp2.getType().isIntType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), 
    				"Arithmetic operator applied to non-numeric operand");
    		noError = false;
    	}
    	
    	if (noError) {
    		setType(new IntType());
    	}
    	
    	return noError;
    	
    }
}

class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" && ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
    
    public boolean typeCheck() {
    	myExp1.typeCheck();
    	myExp2.typeCheck();
    	boolean noError = true;
    	if (myExp1.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp1.getType().isBoolType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Logical operator applied to non-bool operand");
    		noError = false;
    	}
    	
    	if (myExp2.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp2.getType().isBoolType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), 
    				"Logical operator applied to non-bool operand");
    		noError = false;
    	}
    	
    	if (noError) {
    		setType(new BoolType());
    	}
    	
    	return noError;
    	
    }
}

class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" || ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
    
    public boolean typeCheck() {
    	myExp1.typeCheck();
    	myExp2.typeCheck();
    	boolean noError = true;
    	if (myExp1.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp1.getType().isBoolType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Logical operator applied to non-bool operand");
    		noError = false;
    	}
    	
    	if (myExp2.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp2.getType().isBoolType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), 
    				"Logical operator applied to non-bool operand");
    		noError = false;
    	}
    	
    	if (noError) {
    		setType(new BoolType());
    	}
    	
    	return noError;
    	
    }
}

class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
    
    public boolean typeCheck() {
    	myExp1.typeCheck();
    	myExp2.typeCheck();

    	if (myExp1.getType().isErrorType() || myExp2.getType().isErrorType()) {
    		setType(new ErrorType());
    		return false;
    	}
    	else if (myExp1.getType().isVoidType() && myExp2.getType().isVoidType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Equality operator applied to void functions");
    		return false;
    	}
    	else if (myExp1.getType().isFnType() && myExp2.getType().isFnType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Equality operator applied to functions");
    		return false;
    	}
    	else if (myExp1.getType().isStructDefType() && myExp2.getType().isStructDefType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Equality operator applied to struct names");
    		return false;
    	}
    	else if (myExp1.getType().isStructType() && myExp2.getType().isStructType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Equality operator applied to struct variables");
    		return false;
    	}
    	else if (!(myExp1.getType().isBoolType() && myExp2.getType().isBoolType()) &&
    				!(myExp1.getType().isIntType() && myExp2.getType().isIntType())) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Type mismatch");
    		return false;
    	}
    	
    	
    	setType(new BoolType());
    	return true;
    	
    }
}

class NotEqualsNode extends BinaryExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" != ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
    
    public boolean typeCheck() {
    	myExp1.typeCheck();
    	myExp2.typeCheck();

    	if (myExp1.getType().isErrorType() || myExp2.getType().isErrorType()) {
    		setType(new ErrorType());
    		return false;
    	}
    	else if (myExp1.getType().isVoidType() && myExp2.getType().isVoidType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Equality operator applied to void functions");
    		return false;
    	}
    	else if (myExp1.getType().isFnType() && myExp2.getType().isFnType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Equality operator applied to functions");
    		return false;
    	}
    	else if (myExp1.getType().isStructDefType() && myExp2.getType().isStructDefType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Equality operator applied to struct names");
    		return false;
    	}
    	else if (myExp1.getType().isStructType() && myExp2.getType().isStructType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Equality operator applied to struct variables");
    		return false;
    	}
    	else if (!(myExp1.getType().isBoolType() && myExp2.getType().isBoolType()) &&
    				!(myExp1.getType().isIntType() && myExp2.getType().isIntType())) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Type mismatch");
    		return false;
    	}
    	
    	setType(new BoolType());
    	return true;
    	
    }
}

class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
    
    public boolean typeCheck() {
    	myExp1.typeCheck();
    	myExp2.typeCheck();
    	boolean noError = true;
    	if (myExp1.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp1.getType().isIntType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Relational operator applied to non-numeric operand");
    		noError = false;
    	}
    	
    	if (myExp2.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp2.getType().isIntType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), 
    				"Relational operator applied to non-numeric operand");
    		noError = false;
    	}
    	
    	if (noError) {
    		setType(new BoolType());
    	}
    	
    	return noError;
    	
    }
}

class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
    
    public boolean typeCheck() {
    	myExp1.typeCheck();
    	myExp2.typeCheck();
    	boolean noError = true;
    	if (myExp1.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp1.getType().isIntType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Relational operator applied to non-numeric operand");
    		noError = false;
    	}
    	
    	if (myExp2.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp2.getType().isIntType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), 
    				"Relational operator applied to non-numeric operand");
    		noError = false;
    	}
    	
    	if (noError) {
    		setType(new BoolType());
    	}
    	
    	return noError;
    	
    }
}

class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
    
    public boolean typeCheck() {
    	myExp1.typeCheck();
    	myExp2.typeCheck();
    	boolean noError = true;
    	if (myExp1.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp1.getType().isIntType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Relational operator applied to non-numeric operand");
    		noError = false;
    	}
    	
    	if (myExp2.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp2.getType().isIntType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), 
    				"Relational operator applied to non-numeric operand");
    		noError = false;
    	}
    	
    	if (noError) {
    		setType(new BoolType());
    	}
    	
    	return noError;
    	
    }
}

class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
    
    public boolean typeCheck() {
    	myExp1.typeCheck();
    	myExp2.typeCheck();
    	boolean noError = true;
    	if (myExp1.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp1.getType().isIntType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), 
    				"Relational operator applied to non-numeric operand");
    		noError = false;
    	}
    	
    	if (myExp2.getType().isErrorType()) {
    		setType(new ErrorType());
    		noError = false;
    	}
    	else if (!myExp2.getType().isIntType()) {
    		setType(new ErrorType());
    		ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), 
    				"Relational operator applied to non-numeric operand");
    		noError = false;
    	}
    	
    	if (noError) {
    		setType(new BoolType());
    	}
    	
    	return noError;
    	
    }
}
