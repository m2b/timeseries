import com.b2rt.data.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;

public class BigIntegerSandbox {

    static BigInteger[] testInputs ={new BigInteger("-106")
            ,new BigInteger("66")
            ,new BigInteger("-1")
            ,new BigInteger("1")
            ,new BigInteger("-106")
            ,BigInteger.ZERO
            ,new BigInteger("106")
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


    public static void main(String[] args) throws IOException {
        BufferedWriter bw=new BufferedWriter(new FileWriter("/Users/blackie/Documents/bigints.csv"));
        for(BigInteger a:testInputs)
            for(BigInteger b:testInputs) {
                bw.write(getCsv(a));
                bw.newLine();
                bw.write(getCsv(b));
                bw.newLine();
                bw.write(getCsv(a.add(b)));
                bw.newLine();
                bw.newLine();
            }
        bw.close();
    }

    static String getCsv(BigInteger i)
    {
        StringBuilder bl=new StringBuilder(Long.toString(i.longValue()));
        bl.append(System.lineSeparator());
        byte[] ba= Utils.reverse(i.toByteArray());
        for(int j=0;j<ba.length;j++)
        {
            bl.append((int)ba[j]);
            if(j<(ba.length-1))
                bl.append(",");
        }
        return bl.toString();
    }
}
