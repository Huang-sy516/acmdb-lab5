package simpledb;

// import javafx.util.Pair;

import java.util.*;

/**
 * Knows how to compute some aggregate over a set of IntFields.
 */
public class IntegerAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;

    /**
     * Aggregate constructor
     * 
     * @param gbfield
     *            the 0-based index of the group-by field in the tuple, or
     *            NO_GROUPING if there is no grouping
     * @param gbfieldtype
     *            the type of the group by field (e.g., Type.INT_TYPE), or null
     *            if there is no grouping
     * @param afield
     *            the 0-based index of the aggregate field in the tuple
     * @param what
     *            the aggregation operator
     */

    private int gbfield;
    private Type gbfidldtype;
    private int afield;
    private Op what;
    private TupleDesc desc;
    // private List<Tuple> singleTuple = new ArrayList<>();
    // private List<Pair<Tuple, Tuple>> doubleTuple = new ArrayList<>();
    private boolean NoGrouping;

    public IntegerAggregator(int gbfield, Type gbfidldtype, int afield, Op what) {
        this.gbfield = gbfield;
        this.gbfidldtype = gbfidldtype;
        this.afield = afield;
        this.what = what;
        NoGrouping = (gbfield == NO_GROUPING);

        // temporary do not consider sc_avg and s_count
        if (NoGrouping) {
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
     * Merge a new tuple into the aggregate, grouping as indicated in the
     * constructor
     * 
     * @param tup
     *            the Tuple containing an aggregate field and a group-by field
     */
    private Map<Field, Integer> targetMap = new HashMap<>();
    private Map<Field, Integer> helpMap = new HashMap<>();

    private Field fillerField = new IntField(-1);


    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
        Integer afieldValue = ((IntField) tup.getField(afield)).getValue();
        Field groupField;
        if (!NoGrouping) {
            groupField = tup.getField(gbfield);
        }
        else {
            groupField = fillerField;
        }
        switch (what) {
            case MIN:
                if (!targetMap.containsKey(groupField)){
                    targetMap.put(groupField, afieldValue);
                }
                else {
                    Integer minValue = targetMap.get(groupField);
                    minValue = minValue < afieldValue? minValue : afieldValue;
                    targetMap.put(groupField, minValue);
                }
                break;
            case MAX:
                if (!targetMap.containsKey(groupField)){
                    targetMap.put(groupField, afieldValue);
                }
                else {
                    Integer maxValue = targetMap.get(groupField);
                    maxValue = maxValue > afieldValue? maxValue : afieldValue;
                    targetMap.put(groupField, maxValue);
                }
                break;
            case SUM:
                if (!targetMap.containsKey(groupField)){
                    targetMap.put(groupField, afieldValue);
                }
                else{
                    Integer tmp = targetMap.get(groupField) + afieldValue;
                    targetMap.put(groupField, tmp);
                }
                break;
            case COUNT:
                if (!targetMap.containsKey(groupField)){
                    targetMap.put(groupField, 1);
                }
                else{
                    targetMap.put(groupField, targetMap.get(groupField)+1);
                }
                break;
            case AVG: case SUM_COUNT:
                // use target map to store sum
                if (!targetMap.containsKey(groupField)){
                    targetMap.put(groupField,afieldValue);
                    helpMap.put(groupField, 1);
                }
                else {
                    Integer tmp = targetMap.get(groupField) + afieldValue;
                    targetMap.put(groupField, tmp);
                    helpMap.put(groupField, helpMap.get(groupField)+1);
                }
                break;
            case SC_AVG:
                // do not need to implement it until lab 6
                break;
        }

    }


    /**
     * Create a DbIterator over group aggregate results.
     * 
     * @return a DbIterator whose tuples are the pair (groupVal, aggregateVal)
     *         if using group, or a single (aggregateVal) if no grouping. The
     *         aggregateVal is determined by the type of aggregate specified in
     *         the constructor.
     */

    // temporary do not consider sum_count and sc_avg!!
    private class SimpleIntAggregatorIterator implements DbIterator{
        Integer value;
        TupleDesc desc;
        boolean open = true;
        Map<Field, Integer> targetMap;
        Map<Field, Integer> helpMap;
        Iterator<Map.Entry<Field, Integer>> targetMapItr;
        boolean NoGrouping;

        public SimpleIntAggregatorIterator(TupleDesc desc, Map<Field, Integer> targetMap, Map<Field, Integer> helpMap, boolean NoGrouping) {
            this.desc = desc;
            this.NoGrouping= NoGrouping;
            this.targetMap = targetMap;
            this.helpMap = helpMap;
            targetMapItr = this.targetMap.entrySet().iterator();
        }

        @Override
        public void open() throws DbException, TransactionAbortedException {
            open = true;
        }

        @Override
        public boolean hasNext() throws DbException, TransactionAbortedException {
            return open && targetMapItr.hasNext();
        }

        @Override
        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
            if (hasNext()){
                Tuple tmp = new Tuple(desc);
                Field group;
                Integer aggValue;
                if (helpMap.isEmpty()){
                    Map.Entry<Field, Integer> curEntry = targetMapItr.next();
                    group = curEntry.getKey();
                    aggValue = curEntry.getValue();
                }
                else {
                    // avg situation
                    Map.Entry<Field, Integer> curEntry = targetMapItr.next();
                    group = curEntry.getKey();
                    aggValue = curEntry.getValue() / helpMap.get(group);
                }


                if (NoGrouping){
                    tmp.setField(0, new IntField(aggValue));
                }
                else{
                    tmp.setField(0, group);
                    tmp.setField(1, new IntField(aggValue));
                }
                return tmp;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void rewind() throws DbException, TransactionAbortedException {
            targetMapItr = targetMap.entrySet().iterator();
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

        switch (what){
            case MIN:case MAX:case SUM:case COUNT:case AVG:
                return new SimpleIntAggregatorIterator(desc, targetMap, helpMap, NoGrouping);
                default:
                    break;
        }
        return null;
    }

}
