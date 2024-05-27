package simpledb;

import java.io.IOException;

/**
 * The delete operator. Delete reads tuples from its child operator and removes
 * them from the table they belong to.
 */
public class Delete extends Operator {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor specifying the transaction that this delete belongs to as
     * well as the child to read from.
     * 
     * @param t
     *            The transaction this delete runs in
     * @param child
     *            The child operator from which to read tuples for deletion
     */
    private TransactionId tid;
    private DbIterator[] children;
    private TupleDesc desc;
    private int count = 0;

    public Delete(TransactionId t, DbIterator child) {
        // some code goes here
        this.tid = t;
        children = new DbIterator[1];
        children[0] = child;
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
     * Deletes tuples as they are read from the child operator. Deletes are
     * processed via the buffer pool (which can be accessed via the
     * Database.getBufferPool() method.
     * 
     * @return A 1-field tuple containing the number of deleted records.
     * @see Database#getBufferPool
     * @see BufferPool#deleteTuple
     */
    private boolean isFetchInit = false;

    private void initFetch() throws TransactionAbortedException, DbException {
        isFetchInit = true;
        while (children[0].hasNext()) {
            try {
                Database.getBufferPool().deleteTuple(tid, children[0].next());
            }
            catch (IOException e){
                throw new DbException("IO Exception");
            }
            count++;
        }
    }

    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
        if (!isFetchInit){
            initFetch();
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
