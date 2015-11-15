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
    abstract public void nameAnalysis(SymTable sTable);
    
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

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    public void nameAnalysis(SymTable sTable) {
        this.myDeclList.nameAnalysis(sTable);
    }

    // 1 kid
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
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

    /* Applies name analysis methodology to the DeclListNode class
     * Uses sTable provided to add new declarations to
     * */
    public void nameAnalysis(SymTable sTable) {
	Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).nameAnalysis(sTable);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    /* Applies name analysis methodology to the DeclListNode class
     * Used for declaration list within a struct
     * Uses sTable provided to check for other structs,
     * adds field declarations to structTable
     * */    
    public void nameAnalysis(SymTable structTable, SymTable sTable) {
	Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                DeclNode next = ((DeclNode)it.next());
                if (next instanceof VarDeclNode) {
                    ((VarDeclNode)next).nameAnalysis(structTable, sTable);
                }
                else {
                    next.nameAnalysis(sTable);
                }
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
        formalsTypes = new LinkedList<String>();
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

    public void nameAnalysis(SymTable sTable) {
        Iterator it = myFormals.iterator();
        try {
            while (it.hasNext()) {
                ((FormalDeclNode)it.next()).nameAnalysis(sTable);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }

        List<String> lst = formalsTypes;
        it = myFormals.iterator();
        try {
            while (it.hasNext()) {
                lst.add(((FormalDeclNode)it.next()).getType());
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    public List<String> getFormalsTypes() {

        return formalsTypes;
    }

    

    // list of kids (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
    private List<String> formalsTypes;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    public void nameAnalysis(SymTable sTable) {
	myDeclList.nameAnalysis(sTable);
	myStmtList.nameAnalysis(sTable);
    }

    // 2 kids
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    public void nameAnalysis(SymTable sTable) {
	Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().nameAnalysis(sTable);
        }
    }

    // list of kids (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
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

    public void nameAnalysis(SymTable sTable) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element                      
            it.next().nameAnalysis(sTable);
        }
    }
    // list of kids (ExpNodes)
    private List<ExpNode> myExps;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************


abstract class DeclNode extends ASTnode {
    public void nameAnalysis(SymTable sTable) {
    }
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0, false);
        p.println(";");
    }

    /* Applies name analysis method on VarDeclNode
     * Adds variable declaration to symbol table, based on primitive
     * or struct. Checks for multiply declared vars, improper struct decl
     * */
    public void nameAnalysis(SymTable sTable) {
        nameAnalysis(sTable,sTable); //Treat sTable like a "struct" scope
    }
    
    /* Applies name analysis method on VarDeclNode for struct fields
     * Adds variable declaration to symbol table, based on primitive
     * or struct. Checks for multiply declared vars, improper struct decl
     * */
    public void nameAnalysis(SymTable structTable, SymTable sTable) {
        SemSym sym = null;
        if (this.myType.getType().equals("void")) {
            ErrMsg.fatal(this.myId.getLineNum(), this.myId.getCharNum(),
            "Non-function declared void");
        }
        else if (!(this.myType instanceof StructNode)){
                sym = new SemSym(this.myType.getType());
            }
        else {
            IdNode id  = ((StructNode)(this.myType)).getId();
            SemSym structDeclSym = sTable.lookupGlobal(id.getId());
            if (structDeclSym != null && structDeclSym instanceof StructDeclSym) {
                id.setSym(structDeclSym);
                sym = new StructVarSym((StructDeclSym)structDeclSym, id.getId());
            }
            else {
                ErrMsg.fatal(id.getLineNum(),id.getCharNum(),
                    "Invalid name of struct type");
            }
        }
        
        if (structTable.lookupLocal(myId.getId()) != null) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), 
                         "Multiply declared identifier");
        }
        else if (sym != null){
        
            try {
                structTable.addDecl(this.myId.getId(),sym);
                
            } 
            catch (DuplicateSymException e) {
                System.out.println(
                    "Compiler Error: Duplicate Sym, Line 366");
            } 
            catch (EmptySymTableException e) {
                //e.printStackTrace();
            }
        }
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

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0, false);
        p.print("(");
        myFormalsList.unparse(p, 0);
        p.println(") {");
        myBody.unparse(p, indent+4);
        p.println("}\n");
    }

    public void nameAnalysis(SymTable sTable) {

//TODO: Analyze idnode uniquely when associated with function (add new func)
        SemSym sym = new FnDeclSym(this.myFormalsList.getFormalsTypes(),this.myType.getType());
        try {
            sTable.addDecl(this.myId.getId(), sym);
            this.myId.setSym(sym);
        } 
        catch (DuplicateSymException e) {
            ErrMsg.fatal(this.myId.getLineNum(), this.myId.getCharNum(),
                "Multiply declared identifier");
        } 
        catch (EmptySymTableException e) {
            //e.printStackTrace();
        }
        sTable.addScope();
        myFormalsList.nameAnalysis(sTable);
        myBody.nameAnalysis(sTable);
        try {
            sTable.removeScope();
        }
        catch (EmptySymTableException e) {
            System.out.println("SYSTEM ERROR: EMPTY SCOPE LIST");
        }
        
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

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
    }

    public void nameAnalysis(SymTable sTable) {
        if (this.myType.getType().equals("void")) {
            ErrMsg.fatal(this.myId.getLineNum(), this.myId.getCharNum(),
            "Non-function declared void");
            return;
        }
 
        SemSym sym = new SemSym(this.myType.getType());
        try {
            sTable.addDecl(this.myId.getId(), sym);
            this.myId.setSym(sym);
        } 
        catch (DuplicateSymException e) {
            ErrMsg.fatal(this.myId.getLineNum(), this.myId.getCharNum(),
                "Multiply declared identifier");
        } 
        catch (EmptySymTableException e) {
            //e.printStackTrace();
        }
        
    }

    public String getType() {
        return this.myId.getSym().getType();
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

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("struct ");
		myId.unparse(p, 0, false);
		p.println("{");
        myDeclList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("};\n");

    }
    
    public void nameAnalysis(SymTable sTable) {
        
        //Construct symTable sym for usage
        SymTable structTable = new SymTable();
        myDeclList.nameAnalysis(structTable, sTable);
        StructDeclSym sds = new StructDeclSym(structTable, myId.getId());

        
        //Check if name defined in SymbolTable already, add
        try {
            sTable.addDecl(myId.getId(),sds);
            myId.setSym(sds);
        }
        catch (DuplicateSymException ex) {
            ErrMsg.fatal(this.myId.getLineNum(), this.myId.getCharNum(),
                "Multiply declared identifier");
        }
        catch (EmptySymTableException ex) {
        }
        
    }

    // 2 kids
    private IdNode myId;
	private DeclListNode myDeclList;
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    public void unparse(PrintWriter p, int indent) {}
    public String getType() {return null;}
    public void nameAnalysis(SymTable sTable){}
}

class IntNode extends TypeNode {
    public IntNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }
    
    public String getType() {
        return "int";
    }
}

class BoolNode extends TypeNode {
    public BoolNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }
    public String getType() {
        return "bool";
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
    
    public String getType() {
        return "void";
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
		myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
		myId.unparse(p, 0, false);
    }
    
    public String getType() {
        return myId.getId();
    }
    
    public IdNode getId() {
        return myId;
    }
//TODO: add return type function
	
	// 1 kid
    private IdNode myId;
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    public void nameAnalysis(SymTable sTable){
        
    }
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignNode assign) {
        myAssign = assign;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(";");
    }
    
    public void nameAnalysis(SymTable sTable){
       myAssign.nameAnalysis(sTable); 
    }

    // 1 kid
    private AssignNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++;");
    }
    
    public void nameAnalysis(SymTable sTable){
       myExp.nameAnalysis(sTable); 
    }

    // 1 kid
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--;");
    }
    
    public void nameAnalysis(SymTable sTable){
       myExp.nameAnalysis(sTable); 
    }

    // 1 kid
    private ExpNode myExp;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("cin >> ");
        myExp.unparse(p, 0);
        p.println(";");
    }
    
    public void nameAnalysis(SymTable sTable){
       myExp.nameAnalysis(sTable); 
    }

    // 1 kid (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("cout << ");
        myExp.unparse(p, 0);
        p.println(";");
    }
    
    public void nameAnalysis(SymTable sTable){
       myExp.nameAnalysis(sTable); 
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
    
    public void nameAnalysis(SymTable sTable){
        
       myExp.nameAnalysis(sTable);
       sTable.addScope();
       myDeclList.nameAnalysis(sTable);
       myStmtList.nameAnalysis(sTable);
       try {
           sTable.removeScope();
        }
        catch (EmptySymTableException e){
        }
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
    
    public void nameAnalysis(SymTable sTable){
        
       myExp.nameAnalysis(sTable);
       sTable.addScope();
       myThenDeclList.nameAnalysis(sTable);
       myThenStmtList.nameAnalysis(sTable);
       try {
           sTable.removeScope();
       }
        catch (EmptySymTableException e){
        }
       sTable.addScope();
       myElseDeclList.nameAnalysis(sTable);
       myElseStmtList.nameAnalysis(sTable);
       try {
           sTable.removeScope();
        }
        catch (EmptySymTableException e){
        }
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
    
    public void nameAnalysis(SymTable sTable){
        
       myExp.nameAnalysis(sTable);
       sTable.addScope();
       myDeclList.nameAnalysis(sTable);
       myStmtList.nameAnalysis(sTable);
       try {
           sTable.removeScope();
        }
        catch (EmptySymTableException e){
        }
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

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(";");
    }
    
    public void nameAnalysis(SymTable sTable) {
        myCall.nameAnalysis(sTable);
    }

    // 1 kid
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
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
    
    public void nameAnalysis(SymTable sTable) {
        myExp.nameAnalysis(sTable);
    }

    // 1 kid
    private ExpNode myExp; // possibly null
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    abstract public void unparse(PrintWriter p, int indent);
    public void nameAnalysis(SymTable sTable){

    }
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

    public void nameAnalysis(SymTable sTable){
	return;
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

    public void nameAnalysis(SymTable sTable){
	return;
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

    public void nameAnalysis(SymTable sTable){
        return;
    }

    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
        mySym = null;
        //TODO HERE
    }

    public void unparse(PrintWriter p, int indent) {
        unparse(p,indent,true);
    }
    
    public void unparse(PrintWriter p, int indent, boolean printType) {
        p.print(myStrVal);
        if(mySym!=null && printType) {
            p.print("("+mySym.toString()+")");
        }
    }

    public void nameAnalysis(SymTable sTable) {
        SemSym sym = sTable.lookupGlobal(myStrVal);
        //Undeclared identifier used
        if (sym == null) {
            ErrMsg.fatal(myLineNum, myCharNum, "Undeclared identifier");
        }
        else {
            this.setSym(sym);
        }
    }

    public SemSym getSym() {
        return mySym;
    }

    public void setSym(SemSym sym) {
        this.mySym = sym;
    }
    
    public int getLineNum() {
        return this.myLineNum;
    }
    
    public int getCharNum() {
        return this.myCharNum;
    }
    
    public String getId() {
        return this.myStrVal;
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

    public void unparse(PrintWriter p, int indent) {
		myLoc.unparse(p, 0);
		p.print(".");
		myId.unparse(p, 0);
    }
    
    public void nameAnalysis(SymTable sTable){

        SymTable subTable = null;
        
        myLoc.nameAnalysis(sTable);
        
        //Get subtable to check for var
        if (myLoc instanceof IdNode) {
            SemSym locSym = ((IdNode)myLoc).getSym();
            
            if (locSym !=null && (locSym instanceof StructVarSym)) {
                StructDeclSym structDecl = ((StructVarSym)locSym).getDecl();
                subTable = structDecl.getFields();
            }
            else if (locSym == null) {
                this.hasFailed = true; 
                return;
            }
            else {  // LHS is not a struct type
                ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), 
                             "Dot-access of non-struct type");
                             this.hasFailed = true;
                return;
            }    
            
        }
        else if (myLoc instanceof DotAccessExpNode) {
            if (((DotAccessExpNode)myLoc).hasFailed()) {
                this.hasFailed = true;
                return;
            }
            else {
                SemSym locSym = ((DotAccessExpNode)myLoc).getSym();
                //No struct in myLoc
                if (locSym == null) {
                    ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), 
                             "Dot-access of non-struct type");
                    this.hasFailed = true;
                    return;
                }
                //Get symbol table for struct
                else {
                    //SemSym structDecl = ((StructDeclSym)locSym).getId().getSym();
                    if (locSym !=null && (locSym instanceof StructVarSym)) {
                        StructDeclSym structDecl = ((StructVarSym)locSym).getDecl();
                        subTable = ((StructDeclSym)structDecl).getFields();
                    }
                }
            }
        }
    
        
        if (subTable!=null) {
            SemSym sym = subTable.lookupGlobal(this.myId.getId());
            if (sym==null) {
                ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), 
                             "Invalid struct field name");
                             this.hasFailed = true;
            }
            else {
                myId.setSym(sym);
                if (sym instanceof StructVarSym) {
                    this.mySym = ((StructVarSym)sym);
                }
            }
            
        }
        else {
            System.out.println("Compiler Error, line 1071");
        }
    }
    
    public SemSym getSym() {
        return this.mySym;
    }
    
    public boolean hasFailed() {
        return this.hasFailed;
    }

    // 2 kids
    private ExpNode myLoc;	
    private IdNode myId;
    private SemSym mySym;
    private boolean hasFailed;
}

class AssignNode extends ExpNode {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
		if (indent != -1)  p.print("(");
	    myLhs.unparse(p, 0);
		p.print(" = ");
		myExp.unparse(p, 0);
		if (indent != -1)  p.print(")");
    }

    public void nameAnalysis(SymTable sTable){
        myLhs.nameAnalysis(sTable);
        myExp.nameAnalysis(sTable);
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

    // ** unparse **
    public void unparse(PrintWriter p, int indent) {
	    myId.unparse(p, 0);
		p.print("(");
		if (myExpList != null) {
			myExpList.unparse(p, 0);
		}
		p.print(")");
    }

    public void nameAnalysis(SymTable sTable){
        myId.nameAnalysis(sTable);
	    myExpList.nameAnalysis(sTable);
    }

    // 2 kids
    private IdNode myId;
    private ExpListNode myExpList;  // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }
    
    public void nameAnalysis(SymTable sTable){
        myExp.nameAnalysis(sTable);
    }

    // one child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }

    public void nameAnalysis(SymTable sTable){
        myExp1.nameAnalysis(sTable);
        myExp2.nameAnalysis(sTable);
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

}
