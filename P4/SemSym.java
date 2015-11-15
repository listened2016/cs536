public class SemSym {
    private String type;
    
    public SemSym(String type) { 
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public String toString() {
        return this.type;
    }
}

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
        return type;
    }
}

/* SemSym for variables declared of type struct. Carrys ptr to
 * StructDeclSym associated with StructDeclSym
 *
 */
class StructVarSym extends SemSym {
    private StructDeclSym structDeclId;
    private String type;
    
    public StructVarSym(IdNode id,String type) {
        super(type);
        this.StructDeclId = id;
    }
    
    public StructDeclSym getDecl() {
        return id;
    }
    
    
    public String toString() {
        return type;
    }
}
