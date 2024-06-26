package simpledb;

import java.util.*;

/**
 * The Join operator implements the relational join operation.
 */
public class HashEquiJoin extends Operator {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor. Accepts to children to join and the predicate to join them
     * on
     * 
     * @param p
     *            The predicate to use to join the children
     * @param child1
     *            Iterator for the left(outer) relation to join
     * @param child2
     *            Iterator for the right(inner) relation to join
     */
    private JoinPredicate p;
    private DbIterator[] children;

    public HashEquiJoin(JoinPredicate p, DbIterator child1, DbIterator child2) {
        // some code goes here
        // todo: temporary not consider the dbfile size!! and donot handle the operator is not equal
        this.p = p;
        children = new DbIterator[2];
        children[0] = child1;
        children[1] = child2;

    }

    public JoinPredicate getJoinPredicate() {
        // some code goes here
        return p;
    }

    public TupleDesc getTupleDesc() {
        // some code goes here
        return TupleDesc.merge(children[0].getTupleDesc(), children[1].getTupleDesc());
    }
    
    public String getJoinField1Name()
    {
        // some code goes here
	return children[0].getTupleDesc().getFieldName(p.getField1());
    }

    public String getJoinField2Name()
    {
        // some code goes here
        return children[1].getTupleDesc().getFieldName(p.getField2());
    }
    
    public void open() throws DbException, NoSuchElementException,
            TransactionAbortedException {
        // some code goes here
        children[0].open(); children[1].open();
        super.open();
    }

    public void close() {
        // some code goes here
        super.close();
        children[0].close(); children[1].close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
        children[0].rewind(); children[1].rewind();
        // hasInitialFetch = false;
        // Memory = new HashMap<>();
        joinReturnBuffer = new LinkedList<>();
    }

    transient Iterator<Tuple> listIt = null;

    /**
     * Returns the next tuple generated by the join, or null if there are no
     * more tuples. Logically, this is the next tuple in r1 cross r2 that
     * satisfies the join predicate. There are many possible implementations;
     * the simplest is a nested loops join.
     * <p>
     * Note that the tuples returned from this particular implementation of Join
     * are simply the concatenation of joining tuples from the left and right
     * relation. Therefore, there will be two copies of the join attribute in
     * the results. (Removing such duplicate columns can be done with an
     * additional projection operator if needed.)
     * <p>
     * For example, if one tuple is {1,2,3} and the other tuple is {1,5,6},
     * joined on equality of the first column, then this returns {1,2,3,1,5,6}.
     * 
     * @return The next matching tuple.
     * @see JoinPredicate#filter
     */
    private boolean hasInitialFetch = false;
    //private Map<Integer, List<Tuple>> Memory = new HashMap<>();
    private Map<Field, List<Tuple>> Memory = new HashMap<>();
    private Queue<Tuple> joinReturnBuffer = new LinkedList<>();
    private final int MaxHashNum = 1048576;

    /*private Integer hashProduce(Tuple t, int field){
        //int tmpNum = t.getField(field).hashCode() % MaxHashNum;
        //return new Integer(tmpNum * tmpNum % MaxHashNum);
        int tmpNum = t.getField(field).hashCode();
        return new Integer(tmpNum);

    }*/

    private void initialFetch() throws TransactionAbortedException, DbException{
        hasInitialFetch = true;
        while (children[0].hasNext()){
            Tuple tmp = children[0].next();
            //Integer key = hashProduce(tmp, p.getField1());
            Field key = tmp.getField(p.getField1());
            if (Memory.containsKey(key)){
                Memory.get(key).add(tmp);
            }
            else{
                List<Tuple> tuples = new ArrayList<>();
                tuples.add(tmp);
                Memory.put(key, tuples);
            }
        }
    }

    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
        if (!hasInitialFetch){
            initialFetch();
        }
        if (!joinReturnBuffer.isEmpty()){
            return joinReturnBuffer.poll();
        }
        while (children[1].hasNext()){
            Tuple tmp = children[1].next();
            //Integer key = hashProduce(tmp, p.getField2());
            Field key = tmp.getField(p.getField1());
            List<Tuple> potentialTuples = Memory.get(key);
            if (potentialTuples != null){
                for (Tuple potentialTuple : potentialTuples) {
                    if (p.filter(tmp, potentialTuple)){
                        joinReturnBuffer.offer(Tuple.mergeJoinTuples(potentialTuple, tmp));
                    }
                }
                if (!joinReturnBuffer.isEmpty()){
                    return joinReturnBuffer.poll();
                }
            }
        }
        return null;
    }

    @Override
    public DbIterator[] getChildren() {
        // some code goes here
        return children;
    }

    @Override
    public void setChildren(DbIterator[] children) {
        // some code goes here
        if (this.children != children){
            this.children = children;
        }
    }
    
}
