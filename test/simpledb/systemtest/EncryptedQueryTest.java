package simpledb.systemtest;

import simpledb.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import junit.framework.JUnit4TestAdapter;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class EncryptedQueryTest {

    private HeapFile table;
    private EncryptedFile tableEnc;
    private DbFileIterator tableIter;
    private ConcurrentHashMap<String, KeyPair> keyPairs;
    private Paillier_KeyPair paillerKeyPair;
    private OPE_PrivateKey opePrivateKey;
    private OPE_PublicKey opePublicKey;
    private OPE_KeyPair opeKeyPair;

    @Before
    public void setupEncryptedQueryTest() throws IOException, DbException, TransactionAbortedException {
    	// Setup Paillier key pairs for encryption/decryption
    	Paillier_KeyPairBuilder paillierKeyGen = new Paillier_KeyPairBuilder();
        paillierKeyGen.upperBound(BigInteger.valueOf(Integer.MAX_VALUE));
        paillierKeyGen.bits(HeapFile.BITS_INTEGER);
        this.paillerKeyPair = paillierKeyGen.generateKeyPair();
        
        // Setup OPE key pairs for encryption/decryption
    	OPE_CipherPrivate opeCipherPrivate = new OPE_CipherPrivate.Mult(BigInteger.valueOf(5));
        OPE_CipherPublic opeCipherPublic = new OPE_CipherPublic.Mult(BigInteger.valueOf(5));
        this.opePrivateKey = new OPE_PrivateKey(opeCipherPrivate);
        this.opePublicKey = new OPE_PublicKey(opeCipherPublic);
        this.opeKeyPair = new OPE_KeyPair(this.opePrivateKey, this.opePublicKey);
        this.keyPairs = new ConcurrentHashMap<String, KeyPair>();
        
        this.keyPairs.put(HeapFile.PAILLIER_PREFIX, (KeyPair) this.paillerKeyPair);
        this.keyPairs.put(HeapFile.OPE_PREFIX, (KeyPair) this.opeKeyPair);
    	
    	// construct a 3-column table schema
        Type types[] = new Type[]{ Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE };
        String names[] = new String[]{ "a", "b", "c" };
        TupleDesc td = new TupleDesc(types, names);

        // create the table, associate it with some_data_file.dat
        // and tell the catalog about the schema of this table.
        this.table = new HeapFile(new File("test/simpledb/end_to_end_enc_test.dat"), td);
        Catalog catalog = Database.getCatalog();
        catalog.addTable(this.table, "end_to_end_enc_test");
        this.tableIter = this.table.iterator(new TransactionId());
        this.tableEnc = this.table.encrypt(this.keyPairs);
        catalog.addTable(this.tableEnc, "end_to_end_enc_test_enc");
    }

    @Test public void testEndToEndMax() throws NoSuchElementException, DbException, TransactionAbortedException, IOException {
        // construct the query
        TransactionId tid = new TransactionId();
        SeqScan seqScan = new SeqScan(tid, this.tableEnc.getId());
        int fieldIdx = this.tableEnc.getTupleDesc().fieldNameToIndex(HeapFile.OPE_PREFIX + "b");
        EncryptedAggregate max = new EncryptedAggregate(seqScan, fieldIdx, Aggregator.NO_GROUPING, EncryptedAggregator.EncOp.OPE_MAX);
        Tuple tupleMax;
        //try {
        max.open();
        tupleMax = max.next();
        System.out.println("OPE_MAX(b): " + tupleMax);
        max.close();
        Database.getBufferPool().transactionComplete(tid);
        System.out.println("MAX(b): " + this.opePrivateKey.decrypt(BigInteger.valueOf(((IntField)tupleMax.getField(0)).getValue())));
    }
}