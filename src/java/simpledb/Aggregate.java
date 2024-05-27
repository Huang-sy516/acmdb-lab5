package simpledb;

import java.util.*;

/**
 * The Aggregation operator that computes an aggregate (e.g., sum, avg, max,
 * min). Note that we only support aggregates over a single column, grouped by a
 * single column.
 */
public class Aggregate extends Operator {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * 
     * Implementation hint: depending on the type of afield, you will want to
     * construct an {@link IntAggregator} or {@link StringAggregator} to help
     * you with your implementation of readNext().
     * 
     * 
     * @param child
     *            The DbIterator that is feeding us tuples.
     * @param afield
     *            The column over which we are computing an aggregate.
     * @param gfield
     *            The column over which we are grouping the result, or -1 if
     *            there is no grouping
     * @param aop
     *            The aggregation operator to use
     */
    private DbIterator[] child = new DbIterator[1];
    private int afield;
    private int gfield;
    private Aggregator.Op aop;
    private TupleDesc desc;

    public Aggregate(DbIterator child, int afield, int gfield, Aggregator.Op aop) {
        this.child[0] = child;
        this.afield = afield;
        this.gfield = gfield;
        this.aop = aop;
        desc = child.getTupleDesc();
    }

    /**
     * @return If this aggregate is accompanied by a groupby, return the groupby
     *         field index in the <b>INPUT</b> tuples. If not, return
     *         {@link simpledb.Aggregator#NO_GROUPING}
     * */
    public int groupField() {
	// some code goes here
        if (gfield == -1){
            return Aggregator.NO_GROUPING;
        }
        else{
            return gfield;
        }
    }

    /**
     * @return If this aggregate is accompanied by a group by, return the name
     *         of the groupby field in the <b>OUTPUT</b> tuples If not, return
     *         null;
     * */
    public String groupFieldName() {
	// some code goes here
        if (gfield == -1){
            return null;
        }
        else{
            return desc.getFieldName(gfield);
        }
    }

    /**
     * @return the aggregate field
     * */
    public int aggregateField() {
	// some code goes here
	    return afield;
    }

    /**
     * @return return the name of the aggregate field in the <b>OUTPUT</b>
     *         tuples
     * */
    public String aggregateFieldName() {
	// some code goes here
	    return desc.getFieldName(afield);
    }

    /**
     * @return return the aggregate operator
     * */
    public Aggregator.Op aggregateOp() {
	// some code goes here
	    return aop;
    }

    public static String nameOfAggregatorOp(Aggregator.Op aop) {
	    return aop.toString();
    }

    public void open() throws NoSuchElementException, DbException,
	    TransactionAbortedException {
	// some code goes here
        child[0].open();
        super.open();
    }

    /**
     * Returns the next tuple. If there is a group by field, then the first
     * field is the field by which we are grouping, and the second field is the
     * result of computing the aggregate, If there is no group by field, then
     * the result tuple should contain one field representing the result of the
     * aggregate. Should return null if there are no more tuples.
     */
    private boolean fetchInit = false;
    private Aggregator agg;
    private DbIterator aggTupleIterator;

    private void fetchInitProcess() throws TransactionAbortedException, DbException{
        fetchInit = true;
        if (desc.getFieldType(afield).equals(Type.INT_TYPE)){
            if (gfield == -1){
                agg = new IntegerAggregator(gfield, null, afield, aop);
            }
            else agg = new IntegerAggregator(gfield, desc.getFieldType(gfield), afield, aop);
        }
        else {
            if (gfield == -1) {
                agg = new StringAggregator(gfield, null, afield, aop);
            }
            else agg = new StringAggregator(gfield, desc.getFieldType(gfield), afield, aop);
        }
        while (child[0].hasNext()){
            agg.mergeTupleIntoGroup(child[0].next());
        }
        aggTupleIterator = agg.iterator();
    }

    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
	// some code goes here
        if (!fetchInit){
            fetchInitProcess();
        }
        if (aggTupleIterator.hasNext()){
            return aggTupleIterator.next();
        }
        return null;
    }

    public void rewind() throws DbException, TransactionAbortedException {
	// some code goes here
        child[0].rewind();
        fetchInit = false;
    }

    /**
     * Returns the TupleDesc of this Aggregate. If there is no group by field,
     * this will have one field - the aggregate column. If there is a group by
     * field, the first field will be the group by field, and the second will be
     * the aggregate value column.
     * 
     * The name of an aggregate column should be informative. For example:
     * "aggName(aop) (child_td.getFieldName(afield))" where aop and afield are
     * given in the constructor, and child_td is the TupleDesc of the child
     * iterator.
     */
    public TupleDesc getTupleDesc() {
	// some code goes here
        List<TupleDesc.TDItem> tdItems = new ArrayList<>();
        TupleDesc.TDItem aTmp = child[0].getTupleDesc().getTDItems().get(afield);
        String newAName = aop.toString() + "(" + child[0].getTupleDesc().getFieldName(afield) + ")";
	    if (gfield == -1){
            tdItems.add(new TupleDesc.TDItem(aTmp.getFieldType(), newAName));
	        return new TupleDesc(tdItems);
        }
        else{
	        tdItems.add(child[0].getTupleDesc().getTDItems().get(gfield));
            tdItems.add(new TupleDesc.TDItem(aTmp.getFieldType(), newAName));
            return new TupleDesc(tdItems);
        }
    }

    public void close() {
	// some code goes here
        super.close();
        child[0].close();
    }

    @Override
    public DbIterator[] getChildren() {
	// some code goes here
	    return child;
    }

    @Override
    public void setChildren(DbIterator[] children) {
	// some code goes here
        if (this.child != children){
            this.child = children;
            desc = children[0].getTupleDesc();
            fetchInit = false;
        }
    }
    
}
