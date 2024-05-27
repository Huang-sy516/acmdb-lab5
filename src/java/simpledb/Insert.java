package simpledb;

import java.io.IOException;

/**
 * Inserts tuples read from the child operator into the tableId specified in the
 * constructor
 */
public class Insert extends Operator {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param t
     *            The transaction running the insert.
     * @param child
     *            The child operator from which to read tuples to be inserted.
     * @param tableId
     *            The table in which to insert tuples.
     * @throws DbException
     *             if TupleDesc of child differs from table into which we are to
     *             insert.
     */
    private TransactionId t;
    private DbIterator[] children;
    private int tableId;
    private TupleDesc desc;
    private int count = 0;

    public Insert(TransactionId t,DbIterator child, int tableId)
            throws DbException {
        // some code goes here
        this.t = t;
        this.children = new DbIterator[1];
        children[0] = child;
        this.tableId = tableId;
        if (!Database.getCatalog().getTupleDesc(tableId).equals(child.getTupleDesc())){
            throw new DbException("invalid insert of different table data");
        }
        Type[] types = new Type[1];
        types[0] = Type.INT_TYPE;
        desc = new TupleDesc(types);
    }

    public TupleDesc getTupleDesc() {
        // some code goes here
        return desc;
    }

    public void open() throws DbException, TransactionAbortedException {
        // some code goes here
        children[0].open();
        super.open();
    }

    public void close() {
        // some code goes here
        super.close();
        children[0].close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
        children[0].rewind();
        count = 0;
        isFetchInit = false;
    }

    /**
     * Inserts tuples read from child into the tableId specified by the
     * constructor. It returns a one field tuple containing the number of
     * inserted records. Inserts should be passed through BufferPool. An
     * instances of BufferPool is available via Database.getBufferPool(). Note
     * that insert DOES NOT need check to see if a particular tuple is a
     * duplicate before inserting it.
     *
     * @return A 1-field tuple containing the number of inserted records, or
     *         null if called more than once.
     * @see Database#getBufferPool
     * @see BufferPool#insertTuple
     */
    private boolean isFetchInit = false;
    private void fetchInit()throws TransactionAbortedException, DbException{
        isFetchInit = true;
        while (children[0].hasNext()){
            try {
                Database.getBufferPool().insertTuple(t, tableId, children[0].next());
            }
            catch (IOException e){
                throw new DbException("IO Exception!");
            }
            count++;
        }
    }

    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
        if (!isFetchInit){
            fetchInit();
            Tuple tmp = new Tuple(desc);
            tmp.setField(0, new IntField(count));
            return tmp;
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
