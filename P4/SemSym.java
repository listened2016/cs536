import java.util.*;

public class SemSym {
    private String type;

    public SemSym(String type) { 
        this.type = type;
    }
    
    public String getType() {
        return this.type;
    }
    
    public String toString() {
        return this.type;
    }
}

/* SemSym for variables declared of type struct. Carrys
 * SymTable associated with inner fields, type is name
 * of struct type
 */
class StructDeclSym extends SemSym {
    private SymTable fields;
    private String type;
    
    public StructDeclSym(SymTable fields, String type) {
        super(type);
        this.fields = fields;
    }
    
    public SymTable getFields() {
        return fields;
    }
    
    public String toString() {
        return super.getType();
    }
}

/* SemSym for variables declared of type struct. Carrys ptr to
 * StructDeclSym associated with StructDeclSym, type is name
 * of struct type
 */
class StructVarSym extends SemSym {
    private StructDeclSym structDeclId;
    private String type;
    
    public StructVarSym(StructDeclSym id,String type) {
        super(type);
        this.structDeclId = id;
    }
    
    //Returns sym for Struct'd declaration
    public StructDeclSym getDecl() {
        return structDeclId;
    }
    
    
    public String toString() {
        return super.getType();
    }
}

/* SemSym for variables declared of type struct. Carrys ptr to
 * StructDeclSym associated with StructDeclSym, type is name
 * of struct type
 */
class FnDeclSym extends SemSym {
    private String returnType;
    private List<String> formalsTypes;
    private String type;
    
    public FnDeclSym(List<String> formalsTypes,String returnType) {
        super(null);
        this.returnType = returnType;
        this.formalsTypes = formalsTypes;
    }
    
    public String getReturnType() {
        return returnType;
    }

    public List<String> getFormalsTypes() {
        return this.formalsTypes;
    }

    public String toString() {
        String sol = "";
        Iterator<String> itr = this.formalsTypes.iterator();
        if (itr.hasNext()) {
            sol+=itr.next();
        } 
        while (itr.hasNext()) {
            sol += ","+itr.next();
        }
        return sol+"->"+this.returnType;
    }
    
}

