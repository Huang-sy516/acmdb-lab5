package simpledb;

import java.util.*;

/**
 * Knows how to compute some aggregate over a set of StringFields.
 */
public class StringAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;

    /**
     * Aggregate constructor
     * @param gbfield the 0-based index of the group-by field in the tuple, or NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null if there is no grouping
     * @param afield the 0-based index of the aggregate field in the tuple
     * @param what aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */
    private int gbfield;
    private Type gbfidldtype;
    private int afield;
    private Op what;
    private TupleDesc desc;
    // private List<Tuple> singleTuple = new ArrayList<>();
    // private List<Pair<Tuple, Tuple>> doubleTuple = new ArrayList<>();

    public StringAggregator(int gbfield, Type gbfidldtype, int afield, Op what) {
        this.gbfield = gbfield;
        this.gbfidldtype = gbfidldtype;
        this.afield = afield;
        this.what = what;
        if (what != Op.COUNT) throw new IllegalArgumentException();
        if (gbfield == NO_GROUPING) {
            Type[] tmp = new Type[1];
            tmp[0] = Type.INT_TYPE;
            desc = new TupleDesc(tmp);
        }
        else{
            Type[] tmp = new Type[2];
            tmp[0] = gbfidldtype;
            tmp[1] = Type.INT_TYPE;
            desc = new TupleDesc(tmp);
        }
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the constructor
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    private int singleCount = 0;
    private Map<Field, Integer> countMap = new HashMap<>();

    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
        if (gbfield == NO_GROUPING) {
            ++singleCount;
        } else {
            Field groupValue = tup.getField(gbfield);
            if (countMap.containsKey(groupValue)) {
                countMap.put(groupValue, countMap.get(groupValue) + 1);
            } else {
                countMap.put(groupValue, 1);
            }
        }
    }

    /**
     * Create a DbIterator over group aggregate results.
     *
     * @return a DbIterator whose tuples are the pair (groupVal,
     *   aggregateVal) if using group, or a single (aggregateVal) if no
     *   grouping. The aggregateVal is determined by the type of
     *   aggregate specified in the constructor.
     */
    private class StringAggregatorSingleIterator implements DbIterator {
        Integer value;
        TupleDesc desc;
        boolean open = true;
        boolean use = false;
        public StringAggregatorSingleIterator(Integer value, TupleDesc desc) {
            this.value = value;
            this.desc = desc;
        }

        @Override
        public void open() throws DbException, TransactionAbortedException {
            open = true;
        }

        @Override
        public boolean hasNext() throws DbException, TransactionAbortedException {
            if (open && !use) {
                use = true;
                return true;
            }
            else return false;
        }

        @Override
        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
            if (hasNext()){
                Tuple tmp = new Tuple(desc);
                tmp.setField(0, new IntField(value));
                return tmp;
            }
            return null;
        }

        @Override
        public void rewind() throws DbException, TransactionAbortedException {
            use = false;
        }

        @Override
        public TupleDesc getTupleDesc() {
            return desc;
        }

        @Override
        public void close() {
            open = false;
        }
    }
    private class StringAggregatorGroupIterator implements DbIterator {
        Iterator<Map.Entry<Field, Integer>> countMapItr;
        Map<Field, Integer> countMap;
        private boolean open = true;

        TupleDesc desc;
        public StringAggregatorGroupIterator(Map<Field, Integer> countMap, TupleDesc desc) {
            this.countMap = countMap;
            this.desc = desc;
            countMapItr = this.countMap.entrySet().iterator();
        }

        @Override
        public void open() throws DbException, TransactionAbortedException {
            open = true;
        }

        @Override
        public boolean hasNext() throws DbException, TransactionAbortedException {
            return open && countMapItr.hasNext();
        }

        @Override
        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
            if (hasNext()){
                Map.Entry<Field, Integer> curEntry = countMapItr.next();

                Tuple tmp = new Tuple(desc);
                tmp.setField(0, curEntry.getKey());
                tmp.setField(1, new IntField(curEntry.getValue()));
                return tmp;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void rewind() throws DbException, TransactionAbortedException {
            countMapItr = this.countMap.entrySet().iterator();
        }

        @Override
        public TupleDesc getTupleDesc() {
            return desc;
        }

        @Override
        public void close() {
            open = false;
        }
    }

    public DbIterator iterator() {
        // some code goes here
        if (gbfield == NO_GROUPING){
            return new StringAggregatorSingleIterator(singleCount, desc);
        }
        else{
            return new StringAggregatorGroupIterator(countMap, desc);
        }
    }

}
