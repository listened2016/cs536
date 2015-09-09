/* Sahit Mandala, CS536  */

import java.util.*;

/*
SymTable stores the identifiers declared in the program being
compiled and information about each identifier. Implemented
using a List of Hashmaps, with strings as keys and Sym objects
as identifiers
*/
class SymTable {

    /* Private variable storing the list of hashmaps*/
    private List<HashMap<String,Sym>> symMapList;

    /* Constructor; initializes the SymTable's List field to 
	contain a single, empty HashMap.*/
    public SymTable() {
        symMapList = new List<HashMap<String,Sym>>();
    }
}
