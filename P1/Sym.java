/* Sahit Mandala, CS536  */

/*
The Sym class stores the 
*/
class Sym {

    /* Private variable storing the Sym's type*/
    private String type;

    /* Constructor, initializes Sym to have the given type
     Takes String t as type*/
    public Sym(String t) {
	type = t;
    }

    /* Returns this Sym's type*/
    public String getType() {
	return type;
    }

    /* Returns this Sym's type (To be changed)*/ 
    public String toString() {
	return type;
    }


}
