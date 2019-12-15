package com.b2rt.data;

import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.nio.ByteBuffer;

class UtilsTest {

    static BigInteger[] testInputs ={
            new BigInteger("-106")
            ,new BigInteger("66")
            ,new BigInteger("-1")
            ,new BigInteger("1")
            ,BigInteger.ZERO
            ,new BigInteger("512")
            ,new BigInteger("-256")
            ,new BigInteger("1234567890")
            ,new BigInteger("14")
            ,new BigInteger("23")
            ,new BigInteger("38")
            ,new BigInteger("2345678901")
            ,new BigInteger("3456789012")
            ,new BigInteger("-14")
            ,new BigInteger("-23")
            ,new BigInteger("-38")
            ,new BigInteger("-512")
            ,new BigInteger("256")
            ,new BigInteger("-1234567890")
            ,new BigInteger("-2345678901")
            ,new BigInteger("-3456789012")
    };

    static Integer[] testInputs2={
            new Integer(-106)
            ,new Integer(512)
            ,new Integer(1234567890)
    };

    @org.junit.jupiter.api.Test
    void add() {
        // Print numbers in test as Number and ByteArray
//        for(BigInteger bi:testInputs)
//            System.out.printf("Integer=%s, ByteArray=%s%n",bi,byteArrayToString(Utils.reverse(bi.toByteArray())));

        // Testing with big integer
        for(BigInteger i: testInputs) {
           for (BigInteger j : testInputs) {
               System.out.println(String.format("Adding %s and %s",i,j));
               BigInteger expected=i.add(j);
               byte[] added= Utils.add(Utils.reverse(i.toByteArray()),Utils.reverse(j.toByteArray()));
               BigInteger results=new BigInteger(Utils.reverse(added));
               try {
                   assertEquals(expected, results);
                   System.out.println("Passed test!");
               }catch(AssertionFailedError afe)
               {
                   System.out.println("TEST FAILED");
                   System.out.printf("InputA=%s, ByteArray=%s%n",i,byteArrayToString(Utils.reverse(i.toByteArray())));
                   System.out.printf("InputB=%s, ByteArray=%s%n",j,byteArrayToString(Utils.reverse(j.toByteArray())));
                   System.out.printf("Expected=%s, ByteArray=%s%n",expected,byteArrayToString(Utils.reverse(expected.toByteArray())));
                   System.out.printf("Result=%s, ByteArray=%s%n",results,byteArrayToString(Utils.reverse(results.toByteArray())));
                   throw(afe);
               }
           }
        }

        // Testing with integers
        System.out.println();
        System.out.println("TESTING INTEGERS");
        for(Integer i:testInputs2)
        {
            for(Integer j:testInputs2)
            {
                Integer expected = i + j;
                ByteBuffer buffi=ByteBuffer.allocate(4);
                byte[] ibytes = buffi.putInt(i.intValue()).array();
                ByteBuffer buffj=ByteBuffer.allocate(4);
                byte[] jbytes = buffj.putInt(j.intValue()).array();
                //String hexexpected=Integer.toHexString(expected.intValue());
                ByteBuffer buffexp=ByteBuffer.allocate(4);
                byte[] expectedbytes=buffexp.putInt(expected.intValue()).array();

                byte[] added=Utils.add(Utils.reverse(ibytes),Utils.reverse(jbytes));
                System.out.printf("InputA=%s, ByteArray=%s%n",i,byteArrayToString(Utils.reverse(ibytes)));
                System.out.printf("InputB=%s, ByteArray=%s%n",j,byteArrayToString(Utils.reverse(jbytes)));
                System.out.printf("Expected=%s, ByteArray=%s%n",expected,byteArrayToString(Utils.reverse(expectedbytes)));
                System.out.printf("Result ByteArray=%s%n",byteArrayToString(added));
            }
        }
    }

    @org.junit.jupiter.api.Test
    void avg() {
    }


    String byteArrayToString(byte[] ba)
    {
        StringBuilder b=new StringBuilder("{");
        for(int i=0;i<ba.length;i++)
        {
            b.append((int)ba[i]);
            if(i<(ba.length-1))
                b.append(",");
        }
        b.append("}");
        return b.toString();
    }
}