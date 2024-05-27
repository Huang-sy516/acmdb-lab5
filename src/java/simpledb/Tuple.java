package simpledb;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

/**
 * Tuple maintains information about the contents of a tuple. Tuples have a
 * specified schema specified by a TupleDesc object and contain Field objects
 * with the data for each field.
 */
public class Tuple implements Serializable {

    private static final long serialVersionUID = 1L;
    private TupleDesc td;
    private Field[] values;
    private RecordId rid;

    /**
     * Create a new tuple with the specified schema (type).
     *
     * @param td
     *            the schema of this tuple. It must be a valid TupleDesc
     *            instance with at least one field.
     */
    public Tuple(TupleDesc td) {
        // some code goes here
        this.td = td;
        values = new Field[td.numFields()];
    }

    /**
     * @return The TupleDesc representing the schema of this tuple.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return td;
    }

    /**
     * @return The RecordId representing the location of this tuple on disk. May
     *         be null.
     */
    public RecordId getRecordId() {
        // some code goes here
        return rid;
    }

    /**
     * Set the RecordId information for this tuple.
     *
     * @param rid
     *            the new RecordId for this tuple.
     */
    public void setRecordId(RecordId rid) {
        // some code goes here
        this.rid = rid;
    }

    /**
     * Change the values of the ith field of this tuple.
     *
     * @param i
     *            index of the field to change. It must be a valid index.
     * @param f
     *            new values for the field.
     */
    public void setField(int i, Field f) {
        // some code goes here
        values[i] = f;
    }

    /**
     * @return the values of the ith field, or null if it has not been set.
     *
     * @param i
     *            field index to return. Must be a valid index.
     */
    public Field getField(int i) {
        // some code goes here
        return values[i];
    }

    /**
     * Returns the contents of this Tuple as a string. Note that to pass the
     * system tests, the format needs to be as follows:
     *
     * column1\tcolumn2\tcolumn3\t...\tcolumnN
     *
     * where \t is any whitespace (except a newline)
     */
    public String toString() {
        // some code goes here
        String ans = "";
        for (Field value:values){
            ans += value.toString() + '\t';
        }
        // remove last '\t'
        ans = ans.substring(0, ans.length() - 1);
        return ans;
    }

    /**
     * @return
     *        An iterator which iterates over all the fields of this tuple
     * */
    private class TupleIterator<Field> implements Iterator<Field> {
        private int index = 0;
        private Field [] iterValue;

        public TupleIterator(Field[] iterValue) {
            this.iterValue = iterValue;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public void forEachRemaining(Consumer<? super Field> action) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public boolean hasNext() {
            return index < values.length;
        }

        @Override
        public Field next() {
            if (hasNext()) {
                return iterValue[index++];
            }
            else throw new NoSuchElementException("out of boundary");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple tuple = (Tuple) o;
        return td.equals(tuple.td) &&
                Arrays.equals(values, tuple.values) &&
                rid.equals(tuple.rid);
    }

    public Iterator<Field> fields()
    {
        // some code goes here
        return new TupleIterator<>(values);
    }

    public static Tuple mergeJoinTuples(Tuple t1, Tuple t2){
        Tuple returnTuple = new Tuple(TupleDesc.merge(t1.getTupleDesc(),t2.getTupleDesc()));
        Iterator itr1 = t1.fields();
        Iterator itr2 = t2.fields();
        int i = 0;
        while (itr1.hasNext()){
            returnTuple.setField(i++, (Field) itr1.next());
        }
        while (itr2.hasNext()){
            returnTuple.setField(i++, (Field) itr2.next());
        }
        return returnTuple;
    }

    /**
     * reset the TupleDesc of thi tuple
     * */
    public void resetTupleDesc(TupleDesc td)
    {
        // some code goes here
        this.td = td;
        values = new Field[td.numFields()];
    }
}
