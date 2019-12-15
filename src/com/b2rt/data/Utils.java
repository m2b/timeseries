package com.b2rt.data;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static String md5Hash(String text, Integer take) {
        try {
            byte[] digest = null;
            java.security.MessageDigest md =  java.security.MessageDigest.getInstance("MD5");
            md.update(text.getBytes(), 0, text.length());
            digest = md.digest();
            String result = "";
            for (int i = 0; i < digest.length; i++) {
                result += String.format("%02x", digest[i]);
            }
            return result.substring(0, Math.min(result.length(), take));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] getBitArray(byte b)
    {
        byte[] bits=new byte[8];
        for(int i=0;i<8;i++)
        {
            bits[i] = (byte)(b & 0x1);
            b =(byte)(b >>> 1);
        }
        return bits;
    }

    public static byte[] getBitArray(byte[] bytes)
    {
        List<Byte> bitsList=new ArrayList<>();
        for(byte b:bytes) {
            for(byte bs:getBitArray(b))
                bitsList.add(bs);
        }
        byte[] bits=new byte[bitsList.size()];
        for(int i=0;i<bits.length;i++)
            bits[i]=bitsList.get(i);
        return bits;
    }

    public static byte getByteFromBits(byte[] bits)
    {
        if(bits==null && bits.length<1)
            return 0;
        int b=0;
        int limit=Math.min(bits.length,8);
        for(int i=0;i<limit;i++)
        {
            b=b | ((bits[i] & 0x1)<<i);
        }
        return (byte)(b & 0xff);
    }

    public static byte[] getByteArrayFromBits(byte[] bits)
    {
        int blocks=bits.length/8;
        int remainder=bits.length%8;
        byte[] bytes=new byte[blocks+(remainder>0?1:0)];
        // Full blocks of 8 bit(e)s
        int i;
        for(i=0;i<blocks;i++)
        {
            byte[] bitsInBlock=new byte[8];
            for(int j=0;j<8;j++) {
                bitsInBlock[j]=bits[i * 8 + j];
            }
            bytes[i]=getByteFromBits(bitsInBlock);
        }
        // Remainder
        if(remainder>0)
        {
            byte[] bitsInBlock=new byte[remainder];
            for(int j=0;j<remainder;j++) {
                bitsInBlock[j]=bits[i * 8 + j];
            }
            bytes[i]=getByteFromBits(bitsInBlock);
        }
        return bytes;
    }

    public static String showbits(long i)
    {
        BigInteger bi=BigInteger.valueOf(i);
        byte[] bytes=Utils.reverse(bi.toByteArray());
        StringBuilder ret=new StringBuilder();
        for(byte b:bytes) {
            int idx=0;
            do {
                byte bit =(byte)(b & 0x1);
                ret.append(bit);
                b =(byte)(b >>> 1);
            }
            while (++idx < 8);
        }
        return ret.reverse().toString();
    }

    public static byte[] reverse(byte[] a)
    {
        byte[] ret=new byte[a.length];
        for(int i=0;i<a.length;i++)
        {
            ret[a.length-1-i]=a[i];
        }
        return ret;
    }

    // byte[0] is sum, byte[1] is carry, byte[2] is padding needed due to overflow
    public static byte[] add(byte a,byte b)
    {
        byte carry=0;
        byte sum=0;
        if(a==0)
        {
            sum=b;
            carry=0;
        }
        else if(b==0)
        {
            sum=a;
            carry=0;
        }
        else {
            byte[] sumbits=new byte[8];
            byte[] bitsa = getBitArray(a);
            byte[] bitsb = getBitArray(b);
            for (int i = 0; i < 8; i++) {
                sumbits[i] = (byte) (bitsa[i] + bitsb[i] + carry);
                if (sumbits[i] == 2) {
                    sumbits[i] = 0;
                    carry = 1;
                } else if (sumbits[i] == 3) {
                    sumbits[i] = 1;
                    carry = 1;
                } else
                    carry = 0;
            }
            sum = getByteFromBits(sumbits);
        }

        // http://sandbox.mc.edu/~bennet/cs110/tc/orules.html
        // Negative overflow
        if(sum>0 && a<0 && b<0) {
            return new byte[]{sum,(byte)(carry-1),-1};
        }
        // Positive overflow
        else if(sum<0 && a>0 && b>0)
        {
            return new byte[]{sum,carry,0};
        }
        else
        {
            return new byte[]{sum,carry};
        }
    }

    public static byte[] add(byte[] a,byte[] b)
    {
        // TODO: Using integer math (i.e. pad missing bytes with -1 or 0)
        /*
        int size=Math.max(a.length,b.length);

        // Get sign bits of inputs, this is needed to handle overflow
        int signa=a[a.length-1]<0?-1:1;
        int signb=b[b.length-1]<0?-1:1;


        // Properly pad if sign is negative, this is needed to get correct bit additions
        boolean padded=false;
        if(signa==-1 && a.length<size)
        {
            byte[] newa=new byte[size];
            System.arraycopy(a,0,newa,0,a.length);
            for(int i=a.length;i<size;i++)
                newa[i]=-1;
            a=newa;
            padded=true;
        }
        if(signb==-1 && b.length<size)
        {
            byte[] newb=new byte[size];
            System.arraycopy(b,0,newb,0,b.length);
            for(int i=b.length;i<size;i++)
                newb[i]=-1;
            b=newb;
            padded=true;
        }

        // Add bytes
        byte[] result=new byte[size];
        int sum=0;
        int carry=0;
        for(int i=0;i<size;i++) {
            if (i < a.length && i < b.length) {
                sum = a[i] + b[i] + carry;
            }
            else if(i < a.length) {
                sum = a[i] + carry;
            }
            else {
                sum = b[i] + carry;
            }
            carry=sum>>>8;
            if(padded)
                carry=0;
            result[i]=(byte)(sum & 0xff);
        }
        */

        byte[] bitsa=getBitArray(a);
        byte[] bitsb=getBitArray(b);
        int size=Math.max(bitsa.length,bitsb.length);

        // Get sign bits of inputs, this is needed to handle overflow
        int signa=bitsa[bitsa.length-1]==1?-1:1;
        int signb=bitsb[bitsb.length-1]==1?-1:1;

        // Properly pad if sign is negative, this is needed to get correct bit additions
        if(signa==-1 && bitsa.length<size)
        {
            byte[] newbitsa=new byte[size];
            System.arraycopy(bitsa,0,newbitsa,0,bitsa.length);
            for(int i=bitsa.length;i<size;i++)
                newbitsa[i]=1;
            bitsa=newbitsa;
        }
        if(signb==-1 && bitsb.length<size)
        {
            byte[] newbitsb=new byte[size];
            System.arraycopy(bitsb,0,newbitsb,0,bitsb.length);
            for(int i=bitsb.length;i<size;i++)
                newbitsb[i]=1;
            bitsb=newbitsb;
        }

        // Add bits
        byte[] sumbits=new byte[size];
        byte carry=0;
        for(int i=0;i<size;i++) {
            if (i < bitsa.length && i < bitsb.length) {
                sumbits[i] = (byte) (bitsa[i] + bitsb[i] + carry);
            }
            else if(i < bitsa.length) {
                sumbits[i] = (byte) (bitsa[i] + carry);
            }
            else {
                sumbits[i] = (byte) (bitsb[i] + carry);
            }
            if (sumbits[i] == 2) {
                sumbits[i] = 0;
                carry = 1;
            } else if (sumbits[i] == 3) {
                sumbits[i] = 1;
                carry = 1;
            } else
                carry = 0;
        }
        byte[] result=getByteArrayFromBits(sumbits);

        // Padding byte if overflow
        // http://sandbox.mc.edu/~bennet/cs110/tc/orules.html
        boolean negoverflow=result[result.length-1]>0 && signa==-1 && signb==-1;
        boolean posoverflow=result[result.length-1]<0 && signa==1 && signb==1;
        if(posoverflow || negoverflow)
        {
            byte[] temp=new byte[result.length+1];
            System.arraycopy(result,0,temp,0,result.length);
            temp[result.length]=(byte)(posoverflow?0:-1);
            return temp;
        }

        return result;
    }

    // Add and bitshift twice (from MSB to LSB)
    public static byte[] avg(byte[] a,byte[] b)
    {
        // Add
        byte[] sum=add(a,b);
        byte[] bits=getBitArray(sum);
        byte[] avgbits=new byte[sum.length];

        // Get pad based on MSB bit of sum, this is needed to handle padding
        byte pad=bits[bits.length-1];

        for(int i=0;i<bits.length;i++)
        {
            // Bitshift twice
            if(i<(bits.length-2))
                avgbits[i]=sum[i+2];
            else
                avgbits[i]=pad;
        }
        return getByteArrayFromBits(avgbits);
    }
}
