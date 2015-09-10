/*Sahit Mandala, CS536*/

public class P1 {
    
    public static void main(String args[]) {
        String returnStr;
        String sampleStr, sampleStrB;
        Sym sampleSym, sampleSymB, returnSym;
        SymTable sampleST;
        //Sym Class tests
        
        // Constructor: Null input test
        Sym sampleSym = new Sym();
        
        //Constructor: NonEmpty String test
        sampleStr = "Integer";
        sampleSym = new Sym(sampleStr);
        
        //getType(): Expected value test
        sampleStr = "Integer";
        sampleSym = new Sym(sampleStr);
        returnStr = sampleSym.getType();
        if (!returnStr.equals(sampleStr)) {
            System.out.println(
"getType: return value ("+returnStr+") does not match expected value("
            +hw+")");
        }
        
        //toString(): Expected value test
        sampleStr = "Integer";
        sampleSym = new Sym(sampleStr);
        returnStr = sampleSym.toString();
        if (!returnStr.equals(sampleStr)) {
            System.out.println(
"toString: return value ("+returnStr+") does not match expected value("
            +sampleStr+")");
        }
        
        //SymTable Class tests
        
        //Constructor: no input test
        sampleST = new SymTable();
        
        //addScope: add 1 scope test
        sampleSt = new SymTable();
        sampleST.addScope();
        
         //addDecl: Empty SymTable Exception test
        sampleSt = new SymTable();
        sampleStr = "Hello World!";
        sampleSym = new Sym("Integer");
        try {
            sampleST.addDecl(sampleStr,sampleSym);
            System.out.println(
            "addDecl: Failed to throw EmptySymTableException");
        }
        catch (EmptySymTableException ex) {
        }
       
        //addDecl: Null name test
        sampleSt = new SymTable();
        sampleST.addScope();
        sampleStr = null;
        sampleSym = new Sym("Integer");
        try {
            sampleST.addDecl(sampleStr, sampleSym);
            System.out.println(
            "addDecl: Failed to throw NullPointerException on null name");
        }
        catch (NullPointerException ex) {
        }
        
        //addDecl: Null sym test
        sampleSt = new SymTable();
        sampleST.addScope();
        sampleStr = "Hello World!";
        sampleSym = null;
        try {
            sampleST.addDecl(sampleStr, sampleSym);
            System.out.println(
            "addDecl: Failed to throw NullPointerException on null Sym");
        }
        catch (NullPointerException ex) {
        }
        
        //addDecl: Null name and sym test
        sampleSt = new SymTable();
        sampleST.addScope();
        sampleStr = null;
        sampleSym = null;
        try {
            sampleST.addDecl(sampleStr, sampleSym);
            System.out.println(
            "addDecl: Failed to throw NullPointerException on null name, Sym");
        }
        catch (NullPointerException ex) {
        }
        
        //addDecl: Duplicate Sym Exception test
        sampleSt = new SymTable();
        sampleST.addScope();
        sampleStr = "Hello World!";
        sampleStrB = "Hello World!";
        sampleSym = new Sym("Integer");
        sampleSymB = new Sym("String");
        try {
            sampleST.addDecl(sampleStr, sampleSym);
            sampleST.addDecl(sampleStrB, sampleSymB);
            System.out.println(
            "addDecl: Failed to throw DuplicateSymException on same name");
        }
        catch (DuplicateSymException ex) {
        }
        
        //addDecl: Duplicate Sym across diff scope
        sampleSt = new SymTable();
        sampleST.addScope();
        sampleStr = "Hello World!";
        sampleStrB = "Hello World!";
        sampleSym = new Sym("Integer");
        sampleSymB = new Sym("String");
        try {
            sampleST.addDecl(sampleStr, sampleSym);
            sampleST.addScope();
            sampleST.addDecl(sampleStrB, sampleSymB);
        }
        catch (DuplicateSymException ex) {
            System.out.println(
            "addDecl: Threw DuplicateSymException on dupl across diff scopes");
        }
        
        //lookupLocal: Empty SymTable Exception test
         sampleST = new SymTable();
         sampleStr = "Hello World!";
         try {
            sampleST.lookupLocal(sampleStr);
            System.out.println(
            "addDecl: Failed to throw EmptySymTableException on lookup");
        }
        catch (DuplicateSymException ex) {
        }
        
        //lookupLocal: addDecl comparison (curr scope)
        sampleSt = new SymTable();
        sampleST.addScope();
        sampleStr = "Hello World!";
        sampleSym = new Sym("Integer");
        sampleST.addDecl(sampleStr,sampleSym);
        returnSym = sampleST.lookupLocal(sampleStr);
        if (!(returnSym.toString().equals(sampleSym.toString()))) {
            System.out.println(
            "lookupLocal: Failed to return correct Sym value in curr scope");
        }
        sampleStr = "Not Hello World!";
        sampleSym = new Sym("String");
        sampleST.addDecl(sampleStr,sampleSym);
        returnSym = sampleST.lookupLocal(sampleStr);
        if (!(returnSym.toString().equals(sampleSym.toString()))) {
            System.out.println(
            "lookupLocal: Failed to return correct Sym value in curr scope");
        }
        
        //lookupLocal: addDecl comparison (diff scope)
        sampleSt = new SymTable();
        sampleST.addScope();
        sampleStr = "Hello World!";
        sampleSym = new Sym("Integer");
        sampleST.addDecl(sampleStr,sampleSym);
        sampleST.addScope();
        sampleStr = "Not Hello World!";
        sampleSym = new Sym("String");
        sampleST.addDecl(sampleStr,sampleSym);
        returnSym = sampleST.lookupLocal("Not Hello World!");
        if (!(returnSym.toString().equals(sampleSym.toString()))) {
            System.out.println(
            "lookupLocal: Failed to return correct Sym value in curr scope");
        }
        returnSym = sampleST.lookupLocal("Hello World!");
        if (!(returnSym==null)) {
            System.out.println(
            "lookupLocal: Returned incorrect Sym value in curr scope");
        }
        
        //lookupLocal: addDecl comparison (duplicate ids, diff scope)
        sampleSt = new SymTable();
        sampleST.addScope();
        sampleStr = "Hello World!";
        sampleSym = new Sym("Integer");
        sampleST.addDecl(sampleStr,sampleSym);
        sampleST.addScope();
        sampleStr = "Hello World!";
        sampleSym = new Sym("String");
        sampleST.addDecl(sampleStr,sampleSym);
        returnSym = sampleST.lookupLocal("Hello World!");
        if (!(returnSym.toString().equals("String"))) {
            System.out.println(
            "lookupLocal: Failed to return correct Sym value in curr scope");
        }
        
        //lookupGlobal: Empty SymTable Exception test
        //lookupGlobal: addDecl comparison (curr scope)
        //lookupGlobal: addDecl comparison (diff scope)
        //lookupGlobal: addDecl comparison (duplicate ids, diff scope)
        
    }
    
}
