/*Sahit Mandala, CS536*/

public class P1 {
    
   public static void main(String[] args) {
      String returnStr;
      String sampleStr, sampleStrB;
      Sym sampleSym, sampleSymB, returnSym;
      SymTable sampleST;
      //Sym Class tests
                
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
            +sampleStr+")");
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
      sampleST= new SymTable();
      sampleST.addScope();
     
      //addDecl: Empty SymTable Exception test
      sampleST= new SymTable();
      sampleStr = "Hello World!";
      sampleSym = new Sym("Integer");
      try {
         sampleST.addDecl(sampleStr,sampleSym);
         System.out.println(
         "addDecl: Failed to throw EmptySymTableException");
      }
      catch (EmptySymTableException ex) {
      }
      catch (Exception ex) {
         System.out.println("addDecl:"+ex);
      }
    
      //addDecl: Null name test
      sampleST= new SymTable();
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
      catch (Exception ex) {
         System.out.println("addDecl:"+ex);
      }
     
      //addDecl: Null sym test
      sampleST= new SymTable();
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
      catch (Exception ex) {
         System.out.println("addDecl:"+ex);
      }
     
      //addDecl: Null name and sym test
      sampleST= new SymTable();
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
      catch (Exception ex) {
	  System.out.println("addDecl:"+ex);
      }
     
      //addDecl: Duplicate Sym Exception test
      sampleST= new SymTable();
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
      catch (Exception ex) {
         System.out.println("addDecl:"+ex);
      }
     
      //addDecl: Duplicate Sym across diff scope
      sampleST= new SymTable();
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
      catch (Exception ex) {
         System.out.println("addDecl:"+ex);
      }
      
      //lookupLocal: Empty SymTable Exception test
      sampleST = new SymTable();
      sampleStr = "Hello World!";
      try {
         sampleST.lookupLocal(sampleStr);
         System.out.println(
         "localLookup: Failed to throw EmptySymTableException on lookup");
      }
      catch (EmptySymTableException ex) {
      }
      
      //lookupLocal: addDecl comparison (curr scope)
      sampleST= new SymTable();
      sampleST.addScope();
      sampleStr = "Hello World!";
      sampleSym = new Sym("Integer");
      try {
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
         "   lookupLocal: Failed to return correct Sym value in curr scope");
         }
      }
      catch (Exception ex) {
         System.out.println("lookupLocal:"+ex);
      }
      
      
      //lookupLocal: addDecl comparison (diff scope, null return)
      sampleST= new SymTable();
      sampleST.addScope();
      sampleStr = "Hello World!";
      sampleSym = new Sym("Integer");
      try {
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
      }
      catch (Exception ex) {
         System.out.println("lookupLocal:"+ex);
      }
      
      
      //lookupLocal: addDecl comparison (duplicate ids, diff scope)
      sampleST= new SymTable();
      sampleST.addScope();
      sampleStr = "Hello World!";
      sampleSym = new Sym("Integer");
      try {
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
      }
      catch (Exception ex) {
         System.out.println("lookupLocal:"+ex);
      }
      
      //lookupGlobal: Empty SymTable Exception test
      sampleST = new SymTable();
      sampleStr = "Hello World!";
      try {
         sampleST.lookupGlobal(sampleStr);
         System.out.println(
         "lookupGlobal: Failed to throw EmptySymTableException on lookup");
      }
      catch (EmptySymTableException ex) {
      }
      
      //lookupGlobal: addDecl comparison (curr scope)
      sampleST= new SymTable();
      sampleST.addScope();
      sampleStr = "Hello World!";
      sampleSym = new Sym("Integer");
      try {
         sampleST.addDecl(sampleStr,sampleSym);
         returnSym = sampleST.lookupGlobal(sampleStr);
         if (!(returnSym.toString().equals(sampleSym.toString()))) {
            System.out.println(
         "lookupGlobal: Failed to return correct Sym value in first scope");
         }
         sampleStr = "Not Hello World!";
         sampleSym = new Sym("String");
         
         sampleST.addDecl(sampleStr,sampleSym);
         returnSym = sampleST.lookupGlobal(sampleStr);
         if (!(returnSym.toString().equals(sampleSym.toString()))) {
            System.out.println(
         "lookupGlobal: Failed to return correct Sym value in first scope");
         }
      }
      catch (Exception ex) {
	  System.out.println("lookupGlobal:"+ex);
      }
      
      //lookupGlobal: addDecl comparison (diff scope)
      try {
         sampleST= new SymTable();
         sampleST.addScope();
         sampleStr = "Hello World!";
         sampleSym = new Sym("Integer");
      
         sampleST.addDecl(sampleStr,sampleSym);
         sampleST.addScope();
         sampleStr = "Not Hello World!";
         sampleSym = new Sym("String");
         sampleST.addDecl(sampleStr,sampleSym);
         returnSym = sampleST.lookupGlobal("Not Hello World!");
         if (!(returnSym.toString().equals("String"))) {
            System.out.println(
            "lookupGlobal: Failed to return correct Sym obj in 1st scope");
         }
         returnSym = sampleST.lookupGlobal("Hello World!");
         if (!(returnSym.toString().equals("Integer"))) {
            System.out.println(
            "lookupGlobal: Failed to return correct Sym obj in 2nd scope");
         }
      }
      catch (Exception ex) {
	  System.out.println("lookupGlobal:"+ex);
      }
      
      //lookupGlobal: addDecl comparison (duplicate ids, diff scope)
      sampleST= new SymTable();
      sampleST.addScope();
      sampleStr = "Hello World!";
      sampleSym = new Sym("Integer");
      try {
         sampleST.addDecl(sampleStr,sampleSym);
         sampleST.addScope();
         sampleStr = "Hello World!";
         sampleSym = new Sym("String");
         sampleST.addDecl(sampleStr,sampleSym);
         returnSym = sampleST.lookupGlobal("Hello World!");
         if (!(returnSym.toString().equals("String"))) {
            System.out.println(
      "lookupGlobal: Failed to return 1st Sym value across diff scopes");
         }
      }
      catch (Exception ex) {
	  System.out.println("lookupGlobal:"+ex);
      }
      
      //removeScope: Empty SymTable Exception test
      sampleST = new SymTable();
      try {
         sampleST.removeScope();
         System.out.println(
         "removeScope: Failed to throw EmptySymTableException on scope removal");
      }
      catch (EmptySymTableException ex) {
      }

      //removeScope: Add 1 scope, delete it, check emptySymTable
      sampleST= new SymTable();
      sampleST.addScope();
      sampleStr = "Hello World!";
      sampleSym = new Sym("Integer");
      try {
         sampleST.removeScope();
      }
      catch (Exception ex) {
         System.out.println("removeScope:"+ex);
      }
      try {
         sampleST.removeScope();
         System.out.println(
            "removeScope: Failed to raise EmptySimTable exception");
      }
      catch (EmptySymTableException ex) {
      }


      //removeScope: Add 2 scope, then remove; confirm lookupLocal
      sampleST= new SymTable();
      sampleST.addScope();
      sampleStr = "Hello World!";
      sampleSym = new Sym("Integer");
      try {
         sampleST.addDecl(sampleStr,sampleSym);
         sampleST.addScope();
         sampleStr = "Hello World!";
         sampleSym = new Sym("String");
         sampleST.addDecl(sampleStr,sampleSym);
         sampleST.removeScope();
         returnSym = sampleST.lookupLocal("Hello World!");
         if (!(returnSym.toString().equals("Integer"))) {
            System.out.println(
      "lookupGlobal: Failed to return correct Sym value after removal");
         }
      }
      catch (Exception ex) {
         System.out.println("lookupGlobal:"+ex);
      }

      //print: empty table
      sampleST= new SymTable();
      try { 
         sampleST.print();
      }
      catch (Exception ex) {
         System.out.println("print:"+ex);
      }
      //print: 1 scope, empty
      //print: 2 scopes, 1 sym each 
      

   }
      
}
   
