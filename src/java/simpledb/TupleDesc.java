package simpledb;

import java.io.Serializable;
import java.util.*;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc implements Serializable {

    /**
     * A help class to facilitate organizing the information of each field
     * */
    public static class TDItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The type of the field
         * */
        public final Type fieldType;
        
        /**
         * The name of the field
         * */
        public final String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }

        public Type getFieldType() {
            return fieldType;
        }

        public String getFieldName() {
            return fieldName;
        }
    }

    /**
     * @return
     *        An iterator which iterates over all the field TDItems
     *        that are included in this TupleDesc
     * */
    public Iterator<TDItem> iterator() {
        // some code goes here
        return TDItems.iterator();
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     * @param fieldAr
     *            array specifying the names of the fields. Note that names may
     *            be null.
     */
    private List<TDItem> TDItems = new ArrayList<>();
    private int size = 0;


    public TupleDesc(Type[] typeAr, String[] fieldAr) {
        // some code goes here
        //Todo: check the length of both
        /*
        if (typeAr.length != fieldAr.length){
            try {
                throw new Exception("length is not equal");
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("length is not equal");
            }
        }*/
        for (int i = 0; i < typeAr.length; ++i){
            TDItems.add(new TDItem(typeAr[i], fieldAr[i]));
            size += TDItems.get(i).fieldType.getLen();
        }
    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
        // some code goes here
        for (int i = 0; i < typeAr.length; ++i){
            TDItems.add(new TDItem(typeAr[i], null));
            size += TDItems.get(i).fieldType.getLen();
        }
    }

    public TupleDesc(List<TDItem> TDItems) {
        this.TDItems = TDItems;
        for (TDItem tmp: TDItems){
            size += tmp.fieldType.getLen();
        }
    }

    public List<TDItem> getTDItems() {
        return TDItems;
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        // some code goes here
        return TDItems.size();
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     * 
     * @param i
     *            index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        // some code goes here
        if (i >= TDItems.size()){
            throw new NoSuchElementException();
        }
        return TDItems.get(i).fieldName;
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     * 
     * @param i
     *            The index of the field to get the type of. It must be a valid
     *            index.
     * @return the type of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        // some code goes here
        if (i >= TDItems.size()){
            throw new NoSuchElementException();
        }
        return TDItems.get(i).fieldType;
    }

    /**
     * Find the index of the field with a given name.
     * 
     * @param name
     *            name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException
     *             if no field with a matching name is found.
     */
    public int fieldNameToIndex(String name) throws NoSuchElementException {
        // some code goes here
        //Todo: find a more elegant one
        for (int i = 0; i < TDItems.size(); ++i){
            String myName = TDItems.get(i).fieldName;
            if (myName != null) {
                if (myName.equals(name)) {
                    return i;
                }
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     *         Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        // some code goes here
        return size;
    }
    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     * 
     * @param td1
     *            The TupleDesc with the first fields of the new TupleDesc
     * @param td2
     *            The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
        // some code goes here
        List<TDItem> tmp = new ArrayList<>();
        tmp.addAll(td1.TDItems);
        tmp.addAll(td2.TDItems);
        return new TupleDesc(tmp);
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they are the same size and if the n-th
     * type in this TupleDesc is equal to the n-th type in td.
     * 
     * @param o
     *            the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */
    public boolean equals(Object o) {
        // some code goes here
        if (o instanceof TupleDesc) {
            if (this.getSize() == ((TupleDesc)o).getSize()){
                Iterator<TDItem> iter1 = this.iterator();
                Iterator<TDItem> iter2 = ((TupleDesc)o).iterator();
                for (;iter1.hasNext() && iter2.hasNext(); ){
                    TDItem tmp1 = iter1.next();
                    TDItem tmp2 = iter2.next();
                    if (!tmp1.fieldType.equals(tmp2.fieldType)){
                        return false;
                    }
                }
            }
            else return false;
            return true;
        }
        return false;
    }

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        // throw new UnsupportedOperationException("unimplemented");
        return toString().hashCode();
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     * 
     * @return String describing this descriptor.
     */
    public String toString() {
        // some code goes here
        //Todo: more elegent one!
        String ans = "";
        for (TDItem tmp :TDItems){
            ans += tmp.fieldType.toString() + '(' + tmp.fieldName + ')';
        }
        return ans;
    }
}
