import com.b2rt.data.Utils;

import java.math.BigInteger;

public class byteMathSandbox {
    public static byte[] testvals = {-1,1,0,43,66,-43,-66,106,-106,-127,126};


    public static void main(String[] args) {
        for(byte a:testvals) {
            for (byte b : testvals) {
                executeTest(a, b);
            }
        }
        for(byte b:testvals)
        {
            showBits(b);
        }
    }

    static void executeTest(byte a,byte b) {
        byte[] results = Utils.add(a, b);
        long expected = a + b;
        byte[] addresults={results[0]};
        if(results.length>2)
            addresults=new byte[]{addresults[0],results[2]};
        BigInteger result = new BigInteger(Utils.reverse(addresults));
        System.out.printf("Input A: %s, Input B: %s%n", a, b);
        System.out.printf("Expected: %s, Result: %s%n", expected, result);
        System.out.printf("Bits as string: %s%n", Utils.showbits(expected));
        byte[] expect = BigInteger.valueOf(expected).toByteArray();
        byte[] bits=Utils.getBitArray(Utils.reverse(expect));
        StringBuilder bl=new StringBuilder();
        for(byte bit:bits)
            bl.append(bit);
        System.out.printf("Bits from array of bits: %s%n",bl.reverse().toString());
        for (int i = expect.length; i > 0; i--) {
            System.out.printf("Byte %s - Expected: %s, Actual: %s%n", expect.length - i, expect[i-1],addresults[expect.length - i]);
        }
        // Show bytes from bits
        byte[] back=Utils.getByteArrayFromBits(bits);
        for (int i = 0; i < back.length; i++) {
            System.out.printf("Byte %s from bits - Actual: %s%n", i, back[i]);
        }
        System.out.printf("Carry byte: %s%n",results[1]);

        System.out.println();

    }

    static void showBits(byte b)
    {
        System.out.printf("Byte: %s%n",b);
        System.out.printf("Bits as string: %s%n", Utils.showbits((int) b));
        byte[] bits=Utils.getBitArray(b);
        StringBuilder bl=new StringBuilder();
        for(byte bit:bits)
            bl.append(bit);
        System.out.printf("Bits from array of bits: %s%n",bl.reverse().toString());
        // Get byte back and print it
        byte back=Utils.getByteFromBits(bits);
        System.out.printf("Byte back: %s%n%n",back);
    }
}
