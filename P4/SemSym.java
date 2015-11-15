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
    
    public StructDeclSym(SymTable fields, String type) {
        super(type);
        this.fields = fields;
    }
    
    public SymTable getFields() {
        return fields;
    }
    
    public String toString() {
        fields.print();
        return null;
    }
}

class StructVarSym extends SemSym {
    private IdNode id;
    
    public StructVarSym(IdNode id,String type) {
        super(type);
        this.id = id;
    }
    
    public IdNode getId() {
        return id;
    }
    
    
    public String toString() {
        return null;
    }
}
