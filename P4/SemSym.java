public class SemSym {
    private String type;
    
    public SemSym(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public String toString() {
        return type;
    }
}

class StructDeclSym extends SemSym {
    private SymTable fields;
    
    public StructDeclSym(SymTable fields) {
        super(null);
        this.fields = fields;
    }
    
    public SymTable getFields() {
        return fields;
    }
    
    public String toString() {
        return null;
    }
}

class StructVarSym extends SemSym {
    private String id;
    
    public StructVarSym(String id) {
        super(null);
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    public String toString() {
        return null;
    }
}
