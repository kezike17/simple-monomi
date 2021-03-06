package simpledb;

import java.util.*;

/**
 * Filter is an operator that implements a relational select.
 */
public class Filter extends Operator {

    private static final long serialVersionUID = 1L;

    private Predicate pred;
    private OpIterator child;
    private OpIterator[] children;
    private boolean isOpen;

    /**
     * Constructor accepts a predicate to apply and a child operator to read
     * tuples to filter from.
     * 
     * @param p
     *            The predicate to filter tuples with
     * @param child
     *            The child operator
     */
    public Filter(Predicate p, OpIterator child) {
        // some code goes here
        this.pred = p;
        this.child = child;
        this.children = null;
        this.isOpen = false;
    }

    public Predicate getPredicate() {
        // some code goes here
        return this.pred;
    }

    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.child.getTupleDesc();
    }

    /**
     * Checks if iterator is open
     * @return open status
     */
    private void checkOpen() {
        if (!this.isOpen) {
          throw new IllegalStateException("Iterator is not open");
        }
    }
    
    public void open() throws DbException, NoSuchElementException,
            TransactionAbortedException {
        // some code goes here
        super.open();
        this.child.open();
        this.isOpen = true;
    }

    public void close() {
        // some code goes here
        super.close();
        this.child.close();
        this.isOpen = false;
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
        this.checkOpen();
        this.child.rewind();
    }

    /**
     * AbstractDbIterator.readNext implementation. Iterates over tuples from the
     * child operator, applying the predicate to them and returning those that
     * pass the predicate (i.e. for which the Predicate.filter() returns true.)
     * 
     * @return The next tuple that passes the filter, or null if there are no
     *         more tuples
     * @see Predicate#filter
     */
    protected Tuple fetchNext() throws NoSuchElementException,
            TransactionAbortedException, DbException {
        // some code goes here
    	Tuple next;
    	do {
            try {
            	next = this.child.next();
            } catch (NoSuchElementException nseExn) {
            	return null;
            }
    	} while (!this.pred.filter(next));
        return next;
    }

    @Override
    public OpIterator[] getChildren() {
        // some code goes here
        return this.children;
    }

    @Override
    public void setChildren(OpIterator[] children) {
        // some code goes here
        this.children = children;
    }

}
