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

class StructVarSym extends SemSym {
    private IdNode id;
    private String type;
    
    public StructVarSym(IdNode id,String type) {
        super(type);
        this.id = id;
    }
    
    public IdNode getId() {
        return id;
    }
    
    
    public String toString() {
        return type;
    }
}
