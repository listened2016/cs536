public class SemSym {
    private String type;
    private String name;
    
    public SemSym(String type, String name) {
        this.type = type;
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public String toString() {
        return name;
    }
}
