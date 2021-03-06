/* Sahit Mandala, CS536  */

/*
The Sym class stores the associated information for each identifier
Currently stores a "type" string for identifier
*/
public class Sym {

   /* Private variable storing the Sym's type*/
   private String type;
   
   /* Constructor, initializes Sym to have the given type
   * @param t  String representing type*/
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
