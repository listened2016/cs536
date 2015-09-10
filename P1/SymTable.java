/* Sahit Mandala, CS536  */

import java.util.*;

/*
SymTable stores the identifiers declared in the program being
compiled and information about each identifier. Implemented
using a LinkedList of Hashmaps, with strings as keys and Sym objects
as identifiers
*/
public class SymTable {

    /* Private variable storing the list of hashmaps*/
    private List<HashMap<String,Sym>> symMapList;

    /* Constructor; initializes the SymTable's List field to 
     * contain a single, empty HashMap.
     */
    public SymTable() {
        symMapList = new LinkedList<HashMap<String,Sym>>();
    }
    
    /* Takes name of identifier and associated sym object
     * Adds the given name and sym to the first HashMap in the list.
     * 
     * Exceptions: 
     * If this SymTable's list is empty, throws an EmptySymTableException. 
     * If either name or sym (or both) is null, 
     * throw a NullPointerException. 
     * If the first HashMap in the list already contains the given name 
     * as a key, throw a DuplicateSymException. 
     */
    void addDecl(String name, Sym sym) throws DuplicateSymException, 
    EmptySymTableException {
        if ((name==null) || (sym==null)) {
            throw new NullPointerException();
        }
        
        if (symMapList.isEmpty()) {
            throw new EmptySymTableException();
        }
        
        if (symMapList.get(0).containsKey(name)) {
            throw new DuplicateSymException();
        }
        
        symMapList.get(0).put(name,sym);
        
        return;
    }
    
    /*Add a new, empty HashMap to the front of the list.*/
    void addScope() {
        symMapList.add(0,new HashMap<String,Sym>());
    }
    
    /* If the first HashMap in the list contains name as a key,
     * return the associated Sym; otherwise, return null.
     * 
     * Exceptions:
     * If this SymTable's list is empty, throw an EmptySymTableException.
     */
    Sym lookupLocal(String name) throws EmptySymTableException{
        if (symMapList.isEmpty()) {
            throw new EmptySymTableException();
        }
        
        if (symMapList.get(0).containsKey(name)) {
            return symMapList.get(0).get(name);
        }
        else {
            return null;
        }
    }
    
    /* If any HashMap in the list contains name as a key, return the 
     * first associated Sym; otherwise, return null.
     * 
     * Exceptions:
     * If this SymTable's list is empty, throw an EmptySymTableException.
     */
    Sym lookupGlobal(String name) throws EmptySymTableException{
        if (symMapList.isEmpty()) {
            throw new EmptySymTableException();
        }
        
        for (int i = 0; i < symMapList.size(); i++) {
            if (symMapList.get(0).containsKey(name)) {
                return symMapList.get(0).get(name);
            }
        }
        
        return null;
    }
    
    /* Removes the HashMap from the front of the list.
     * 
     * Exceptions:
     * If this SymTable's list is empty, throw an EmptySymTableException.
     */
    void removeScope() throws EmptySymTableException {
        if (symMapList.isEmpty()) {
            throw new EmptySymTableException();
        }
        
        symMapList.remove(0);
        return;
    }
    
    /*
     * This method is for debugging. First, prints “\nSym Table\n”.
     * Then, for each HashMap M in the list, prints M.toString() followed
     * by a newline. Finally, print one more newline. All output should
     * go to System.out. 
     */
    void print() {
        System.out.print("\nSym Table\n");
        for (int i = 0; i < symMapList.size(); i++) {
            System.out.print(symMapList.get(i).toString());
            System.out.print("\n");
        }
        return;
    }
    
}
