// package simpledb;

// import java.io.*;
// import java.nio.MappedByteBuffer;
// import java.nio.channels.FileChannel;
// import java.util.*;

// /**
//  * HeapFile is an implementation of a DbFile that stores a collection of tuples
//  * in no particular order. Tuples are stored on pages, each of which is a fixed
//  * size, and the file is simply a collection of those pages. HeapFile works
//  * closely with HeapPage. The format of HeapPages is described in the HeapPage
//  * constructor.
//  * 
//  * @see simpledb.HeapPage#HeapPage
//  * @author Sam Madden
//  */
// public class HeapFile implements DbFile {

//     /**
//      * Constructs a heap file backed by the specified file.
//      * 
//      * @param f
//      *            the file that stores the on-disk backing store for this heap
//      *            file.
//      */
//     private File f;
//     private TupleDesc td;
//     private int tableid;
//     //private HeapPage[] pages;
//     //private int count = 0;
//     private HeapPage privateGetPage(TransactionId tid, int pgNo) throws DbException, TransactionAbortedException{
//         Page page = Database.getBufferPool().getPage(tid, new HeapPageId(tableid, pgNo), Permissions.READ_WRITE);
//         return (HeapPage) page;
//     }


//     public HeapFile(File f, TupleDesc td) {
//         // some code goes here
//         this.f = f;
//         this.td = td;
//         this.tableid = f.getAbsoluteFile().hashCode();
//     }

//     /**
//      * Returns the File backing this HeapFile on disk.
//      * 
//      * @return the File backing this HeapFile on disk.
//      */
//     public File getFile() {
//         return f;
//     }

//     /**
//      * Returns an ID uniquely identifying this HeapFile. Implementation note:
//      * you will need to generate this tableid somewhere ensure that each
//      * HeapFile has a "unique id," and that you always return the same value for
//      * a particular HeapFile. We suggest hashing the absolute file name of the
//      * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
//      * 
//      * @return an ID uniquely identifying this HeapFile.
//      */
//     public int getId() {
//         // some code goes here
//         return tableid;
//     }

//     /**
//      * Returns the TupleDesc of the table stored in this DbFile.
//      * 
//      * @return TupleDesc of this DbFile.
//      */
//     public TupleDesc getTupleDesc() {
//         // some code goes here
//         return td;
//     }

//     // see DbFile.java for javadocs
//     public Page readPage(PageId pid){
//         // some code goes here
//         try {

//             // memory map
//             RandomAccessFile raf = new RandomAccessFile(f.getAbsoluteFile(), "r");
//             FileChannel fc = raf.getChannel();
//             MappedByteBuffer mbuff = fc.map(FileChannel.MapMode.READ_ONLY, pid.pageNumber() * BufferPool.getPageSize(),
//                     BufferPool.getPageSize());

//             byte[] data = new byte[BufferPool.getPageSize()];

//             mbuff.get(data, 0, BufferPool.getPageSize());
//             raf.close();

//             //be careful about this "id" use!
//             HeapPageId pageId = new HeapPageId(tableid,pid.pageNumber());
//             return new HeapPage(pageId, data);

//         }
//         catch (Exception E){
//             return null;
//         }

//     }

//     // see DbFile.java for javadocs
//     public void writePage(Page page) throws IOException {
//         // some code goes here
//         // not necessary for lab1
//         try {
//             // memory map
//             RandomAccessFile raf = new RandomAccessFile(f.getAbsoluteFile(), "rw");
//             FileChannel fc = raf.getChannel();
//             MappedByteBuffer mbuff = fc.map(FileChannel.MapMode.READ_WRITE, page.getId().pageNumber() * BufferPool.getPageSize(),
//                     BufferPool.getPageSize());

//             mbuff.put(page.getPageData(), 0, BufferPool.getPageSize());
//             raf.close();
//             page.markDirty(false, null);

//         }
//         catch (Exception E){
//             throw new IOException("something wrong with writing heap page");
//         }
//     }

//     /**
//      * Returns the number of pages in this HeapFile.
//      */
//     public int numPages() {
//         // some code goes here
//         return (int) Math.ceil ((double)f.length() / (double) BufferPool.getPageSize());
//     }

//     // see DbFile.java for javadocs
//     public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
//             throws DbException, IOException, TransactionAbortedException {
//         // some code goes here
//         HeapPage tmpPage;
//         for (int i = 0; i < numPages(); ++i){
//             tmpPage = privateGetPage(tid, i);
//             if (tmpPage.getNumEmptySlots() != 0){
//                 tmpPage.insertTuple(t);
//                 ArrayList<Page> returnArray = new ArrayList<>();
//                 returnArray.add(tmpPage);
//                 return returnArray;
//             }
//         }

//         //todo: you must be very carefull about these code which append data on the end of the file
//         //tmpPage = new HeapPage(new HeapPageId(tableid, numPages()), HeapPage.createEmptyPageData());
//         //writePage(tmpPage);
//         BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(f.getAbsoluteFile(), true));
//         byte[] emptyData = HeapPage.createEmptyPageData();
//         bw.write(emptyData);
//         bw.close();

//         tmpPage = privateGetPage(tid, numPages() - 1);
//         tmpPage.insertTuple(t);
//         ArrayList<Page> returnArray = new ArrayList<>();
//         returnArray.add(tmpPage);
//         return returnArray;
//         // not necessary for lab1
//         /*//append data
//         BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(f.getAbsoluteFile(), true));
//         byte[] emptyData = HeapPage.createEmptyPageData();
//         bw.write(emptyData);
//         bw.close();

//         // new page
//         tmpPage = new HeapPage(new HeapPageId(tableid, numPages),
//                 HeapPage.createEmptyPageData());
//         numPages++;
//         tmpPage.insertTuple(t);
//         ArrayList<Page> returnArray = new ArrayList<>();
//         returnArray.add(tmpPage);
//         return returnArray;
//         // not necessary for lab1
//         */
//     }

//     // see DbFile.java for javadocs
//     public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
//             TransactionAbortedException {
//         // some code goes here
//         Page tmpPage = privateGetPage(tid, t.getRecordId().getPageId().pageNumber());
//         ((HeapPage) tmpPage).deleteTuple(t);

//         // Todo: be carefull about this return !!
//         ArrayList<Page> returnArray = new ArrayList<>();
//         returnArray.add(tmpPage);
//         return returnArray;
//         // not necessary for lab1
//     }

//     // see DbFile.java for javadocs
//     private class HPFileIterator implements DbFileIterator{
//         TransactionId tid;
//         int pageIndex;
//         int pageBound;
//         boolean open;
//         HeapPage curPage;
//         Iterator<Tuple> curIter;

//         public HPFileIterator(TransactionId tid) {
//             this.tid = tid;
//             this.pageIndex = 0;
//             this.pageBound = numPages();
//             this.open = false;
//         }

//         @Override
//         public void open() throws DbException, TransactionAbortedException {
//             this.open = true;
//             curPage = privateGetPage(tid, pageIndex++);
//             curIter = curPage.iterator();
//         }

//         @Override
//         public boolean hasNext() throws DbException, TransactionAbortedException {
//             if (!open) return false;
//             if (curIter.hasNext()) return true;
//             else{
//                 while (pageIndex < pageBound) {
//                     curPage = privateGetPage(tid, pageIndex++);
//                     curIter = curPage.iterator();
//                     if (curIter.hasNext()){
//                         return true;
//                     }
//                 }
//                 return false;
//             }

//         }

//         @Override
//         public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
//             if (hasNext()){
//                 return curIter.next();
//             }
//             else throw new NoSuchElementException("out of boundary");
//         }

//         @Override
//         public void rewind() throws DbException, TransactionAbortedException {
//             pageIndex = 0;
//             curPage = privateGetPage(tid, pageIndex++);
//             curIter = curPage.iterator();

//         }

//         @Override
//         public void close() {
//             //temporarily don't do any things here;
//             open = false;
//         }
//     }

//     public DbFileIterator iterator(TransactionId tid) {
//         // some code goes here
//         return new HPFileIterator(tid);
//     }

// }


package simpledb;



import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 *
 * @author Sam Madden
 * @see HeapPage#HeapPage
 */
public class HeapFile implements DbFile {

    private File file;

    private TupleDesc td;

    /**
     * Constructs a heap file backed by the specified file.
     *
     * @param f the file that stores the on-disk backing store for this heap
     *          file.
     */
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        this.file = f;
        this.td = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     *
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return this.file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     *
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
        return this.file.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     *
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here
        int pageNo = pid.pageNumber();
        int pageSize = BufferPool.getPageSize();
        try {
            RandomAccessFile curFile = new RandomAccessFile(this.file, "r");
            byte[] data = new byte[pageSize];
            curFile.seek((long) pageNo * pageSize);
            curFile.read(data);
            HeapPage curPage = new HeapPage(new HeapPageId(pid.getTableId(), pid.pageNumber()), data);
            curFile.close();
            return curPage;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException();
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
        RandomAccessFile file = new RandomAccessFile(this.file, "rw");  // 通过RandomAccessFile写
        file.seek((long) BufferPool.getPageSize() * page.getId().pageNumber());
        file.write(page.getPageData());
        file.close();
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        long len = file.length();  // 获取HeapFile在磁盘的大小
        return (int) Math.floor(len * 1.0 / BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        // not necessary for lab1
        ArrayList<Page> res = new ArrayList<>();
        // 遍历这个DbFile的pages，看有没有空的slot
        for (int i = 0; i < this.numPages(); i++) {
            HeapPage curPage = (HeapPage) Database.getBufferPool().getPage(
                    tid,
                    new HeapPageId(this.getId(), i),
                    Permissions.READ_WRITE
            );
            if (curPage.getNumEmptySlots() > 0) {
                curPage.insertTuple(t);
                res.add(curPage);
                return res;
            } else {
                Database.getBufferPool().releasePage(tid, curPage.pid);
            }
        }
        HeapPage curPage = new HeapPage(
                new HeapPageId(this.getId(), this.numPages()),
                HeapPage.createEmptyPageData()
        );
        curPage.insertTuple(t);
        writePage(curPage);
        res.add(curPage);
        return res;
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        // not necessary for lab1
        PageId pageId = t.getRecordId().getPageId();
        HeapPage curPage = (HeapPage) Database.getBufferPool().getPage(tid, pageId, Permissions.READ_WRITE);
        curPage.deleteTuple(t);

        ArrayList<Page> res = new ArrayList<>();
        res.add(curPage);
        return res;
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
        return new HeapFileIterator(this, tid);
    }

    public class HeapFileIterator implements DbFileIterator {

        private int curPageNo;

        private int pageNum;

        private Iterator<Tuple> iterator;

        private HeapFile heapFile;

        private TransactionId tid;

        public HeapFileIterator(HeapFile heapFile, TransactionId tid) {
            this.heapFile = heapFile;
            this.tid = tid;
            this.curPageNo = 0;
            this.pageNum = heapFile.numPages();
        }

        public Iterator<Tuple> getNextPage(HeapPageId id) throws TransactionAbortedException, DbException {
            BufferPool pool = Database.getBufferPool();  // 从bufferpool中读取page，如果没有再从磁盘读（getPage中有判断）
            HeapPage page = (HeapPage) pool.getPage(this.tid, id, Permissions.READ_ONLY);
            return page.iterator();
        }

        @Override
        public void open() throws DbException, TransactionAbortedException {
            this.curPageNo = 0;
            HeapPageId pageId = new HeapPageId(this.heapFile.getId(), 0);  // table和HeapFile一一对应
            this.iterator = this.getNextPage(pageId);
        }

        @Override
        public boolean hasNext() throws DbException, TransactionAbortedException {
            for (int i = this.curPageNo; i < this.pageNum; i++) {
                if (this.iterator == null) {
                    return false;
                }
                if (this.iterator.hasNext()) {
                    return true;
                }
                this.curPageNo += 1;
                if (this.curPageNo >= pageNum) {
                    return false;
                }
                this.iterator = this.getNextPage(new HeapPageId(this.heapFile.getId(), this.curPageNo));
            }
            return false;
        }

        @Override
        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
            if (this.iterator == null || !this.iterator.hasNext()) {
                throw new NoSuchElementException();
            }
            return this.iterator.next();
        }

        @Override
        public void rewind() throws DbException, TransactionAbortedException {
            this.close();
            this.open();
        }

        @Override
        public void close() {
            this.iterator = null;
        }
    }

}