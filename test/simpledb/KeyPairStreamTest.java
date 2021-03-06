package simpledb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class KeyPairStreamTest {

    final static int BITS = 1024;
    final static int BITS_INTEGER = 15;

    private static Paillier_KeyPairBuilder keygen;
    private static Paillier_KeyPair keyPair;
    private static Paillier_PublicKey publicKey;

    @BeforeClass
    public static void init() {
        keygen = new Paillier_KeyPairBuilder();
        keygen.upperBound(BigInteger.valueOf(Integer.MAX_VALUE));
        keygen.bits(BITS_INTEGER);
        keyPair = keygen.generateKeyPair();
        publicKey = keyPair.getPublicKey();
    }

    @Test
    public void testInput() {
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("object.data"));
			objectOutputStream.writeObject(keyPair);
			objectOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail("Writing of public key failed.");
		}
		System.out.println("Public Key end of first test: " + keyPair.getPublicKey());
    } 

    @Test
    public void testOutput() {
		try {
	        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("object.data"));
	        Paillier_KeyPair keyPairFromFile = (Paillier_KeyPair) objectInputStream.readObject();
	        objectInputStream.close();
	        System.out.println("Expected: " + keyPair.getPublicKey().toString());
	        System.out.println("Actual: " + keyPairFromFile.getPublicKey().toString());
	        assertTrue(keyPairFromFile.getPublicKey().equals(publicKey));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Reading of public key failed.");
		}
    } 
}
