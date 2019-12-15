import com.b2rt.data.InvalidTypeException;
import com.b2rt.data.SupportedType;
import com.b2rt.timeseries.OPCCode;
import com.b2rt.timeseries.OPCQuality;
import com.b2rt.timeseries.ValueQuality;
import com.b2rt.timeseries.ValueQualityObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SupportedTypeSandbox {
    public static void main(String[] args) {

        List<ValueQuality> vqs=new ArrayList<>();

        try {
            SupportedType<Integer> iv = new SupportedType(20);
            SupportedType<Long> lv= new SupportedType(10L);
            ValueQualityObject<Integer> ivq=new ValueQualityObject(iv,OPCCode.Good);
            ValueQualityObject<Long> lvq=new ValueQualityObject(lv,OPCCode.Bad);
            SupportedType<OPCQuality> ev=new SupportedType(OPCQuality.Good);
            ValueQualityObject<OPCCode> evq=new ValueQualityObject(ev,OPCCode.Good);
            SupportedType<Double> dv = new SupportedType(0.0);
            ValueQualityObject<Double> dvq=new ValueQualityObject(dv,OPCCode.Uncertain);
            SupportedType<Float> fv= new SupportedType(-123.455f);
            ValueQualityObject<Float> fvq=new ValueQualityObject(fv,OPCCode.BadConfigError);
            SupportedType<String> sv= new SupportedType("Hello OPC World");
            ValueQualityObject<String> svq=new ValueQualityObject(sv,OPCCode.GoodLocalOverride);
            ValueQualityObject<Instant> tvq=testVQAndInstantParsing("2017-01-20T23:59:00+00:00");
            vqs.add(tvq);
            tvq=testVQAndInstantParsing("2017-01-20T23:59:00-05:00");
            vqs.add(tvq);
            tvq=testVQAndInstantParsing("2018-01-13 14:23:46.973");
            vqs.add(tvq);
            tvq=testVQAndInstantParsing("2018-01-13 14:23:46.973047");
            vqs.add(tvq);
            tvq=testVQAndInstantParsing("2017-01-20T23:59:00.973047-05:00");
            vqs.add(tvq);            SupportedType<Boolean> bv= new SupportedType(false);
            ValueQualityObject<Boolean> bvq=new ValueQualityObject(bv,OPCCode.GoodLocalOverride);
            SupportedType<Character> cv= new SupportedType('$');
            ValueQualityObject<Character> cvq=new ValueQualityObject(cv,OPCCode.BadInactive);
            SupportedType<Byte> yv= new SupportedType((byte)0b101010101);
            ValueQualityObject<Byte> yvq=new ValueQualityObject(yv,OPCCode.UncertainUnitsExceeded);
            SupportedType<Short> hv= new SupportedType((short)-32768);
            ValueQualityObject<Short> hvq=new ValueQualityObject(sv,OPCCode.UncertainSensor);

            vqs.add(ivq);
            vqs.add(lvq);
            vqs.add(evq);
            vqs.add(dvq);
            vqs.add(fvq);
            vqs.add(svq);
            vqs.add(bvq);
            vqs.add(yvq);
            vqs.add(hvq);
            vqs.add(cvq);


            for(ValueQuality vq:vqs)
                System.out.printf("Value=%s, Quality=%s%n",vq.getValue(),vq.getQuality());
            try {
                SupportedType<Date> bad = new SupportedType<>(new Date());
                System.out.printf("Test failed, exception of type %s was expected %n",InvalidTypeException.class.getName());
            }
            catch(InvalidTypeException te)
            {
                System.out.printf("Test succeeded - restricted type of Date not valid");
            }

            // TODO: Test conversions...
            // To String
            String s=SupportedType.convert(iv.getValue(),String.class);
            System.out.printf("Converted %s int to %s string%n",iv.getValue(),s);
            s=SupportedType.convert(lv.getValue(),String.class);
            System.out.printf("Converted %s long to %s string%n",lv.getValue(),s);
            s=SupportedType.convert(dv.getValue(),String.class);
            System.out.printf("Converted %s double to %s string%n",dv.getValue(),s);
            s=SupportedType.convert("TRUE",String.class);
            System.out.printf("Converted %s string to %s string%n","TRUE",s);
            s=SupportedType.convert(fv.getValue(),String.class);
            System.out.printf("Converted %s float to %s string%n",fv.getValue(),s);
            s=SupportedType.convert(yv.getValue(),String.class);
            System.out.printf("Converted %s byte to %s string%n",yv.getValue(),s);
            s=SupportedType.convert(hv.getValue(),String.class);
            System.out.printf("Converted %s short %s string%n",hv.getValue(),s);
            s=SupportedType.convert(cv.getValue(),String.class);
            System.out.printf("Converted %s char to %s string%n",cv.getValue(),s);
            s=SupportedType.convert(ev.getValue(),String.class);
            System.out.printf("Converted %s enum to %s string%n",ev.getValue(),s);
            // To Boolean
            boolean b=SupportedType.convert(iv.getValue(),Boolean.class);
            System.out.printf("Converted %s int to %s boolean%n",iv.getValue(),b);
            b=SupportedType.convert(lv.getValue(),Boolean.class);
            System.out.printf("Converted %s long to %s boolean%n",lv.getValue(),b);
            b=SupportedType.convert(dv.getValue(),Boolean.class);
            System.out.printf("Converted %s double to %s boolean%n",dv.getValue(),b);
            b=SupportedType.convert("TRUE",Boolean.class);
            System.out.printf("Converted %s string to %s boolean%n","TRUE",b);
            b=SupportedType.convert(fv.getValue(),Boolean.class);
            System.out.printf("Converted %s float to %s boolean%n",fv.getValue(),b);
            b=SupportedType.convert(yv.getValue(),Boolean.class);
            System.out.printf("Converted %s byte to %s boolean%n",yv.getValue(),b);
            b=SupportedType.convert(hv.getValue(),Boolean.class);
            System.out.printf("Converted %s short %s boolean%n",hv.getValue(),b);
            b=SupportedType.convert(cv.getValue(),Boolean.class);
            System.out.printf("Converted %s char to %s boolean%n",cv.getValue(),b);
            // To Char
            char c=SupportedType.convert(false,Character.class);
            System.out.printf("Converted %s boolean to %s char%n",bv.getValue(),c);
            try {
                c = SupportedType.convert("too long", Character.class);
                System.out.printf("Test failed, exception of type %s was expected %n",UnsupportedOperationException.class.getName());
            }catch(RuntimeException re)
            {
                System.out.println("Test succeeded in catching RuntimeException caused by UnsupporteOperationException converting 'too long' to character");
            }
            c = SupportedType.convert("p", Character.class);
            System.out.printf("Converted %s string to %s char%n","p",c);
            // To byte
            byte by=SupportedType.convert(bv.getValue(),Byte.class);
            System.out.printf("Converted %s boolean to %s byte%n",bv.getValue(),by);
            String in="-128";
            by=SupportedType.convert(in,Byte.class);
            System.out.printf("Converted %s String to %s byte%n",in,by);
            // To short
            short sh=SupportedType.convert(bv.getValue(),Short.class);
            System.out.printf("Converted %s boolean to %s short%n",bv.getValue(),sh);
            sh=SupportedType.convert(yv.getValue(),Short.class);
            System.out.printf("Converted %s byte to %s short%n",yv.getValue(),sh);
            in="255";
            sh=SupportedType.convert(in,Short.class);
            System.out.printf("Converted %s String to %s short%n",in,sh);
            // To int
            int i=SupportedType.convert(bv.getValue(),Integer.class);
            System.out.printf("Converted %s boolean to %s integer%n",bv.getValue(),i);
            i=SupportedType.convert(yv.getValue(),Integer.class);
            System.out.printf("Converted %s byte to %s integer%n",yv.getValue(),i);
            i=SupportedType.convert(hv.getValue(),Integer.class);
            System.out.printf("Converted %s short to %s integer%n",hv.getValue(),i);
            in="-2147483648";  // min integer
            i=SupportedType.convert(in,Integer.class);
            System.out.printf("Converted %s String to %s integer%n",in,i);
            // To long
            long l=SupportedType.convert(bv.getValue(),Long.class);
            System.out.printf("Converted %s boolean to %s long%n",bv.getValue(),l);
            l=SupportedType.convert(yv.getValue(),Long.class);
            System.out.printf("Converted %s byte to %s long%n",yv.getValue(),l);
            l=SupportedType.convert(hv.getValue(),Long.class);
            System.out.printf("Converted %s short to %s long%n",hv.getValue(),l);
            l=SupportedType.convert(iv.getValue(),Long.class);
            System.out.printf("Converted %s integer to %s long%n",iv.getValue(),l);
            l=SupportedType.convert(fv.getValue(),Long.class);
            System.out.printf("Converted %s float to %s long%n",fv.getValue(),l);
            l=SupportedType.convert(dv.getValue(),Long.class);
            System.out.printf("Converted %s double to %s long%n",dv.getValue(),l);
            in="9223372036854775807";  // max long
            l=SupportedType.convert(in,Long.class);
            System.out.printf("Converted %s String to %s long%n",in,l);
            // To float
            float f=SupportedType.convert(bv.getValue(),Float.class);
            System.out.printf("Converted %s boolean to %s float%n",bv.getValue(),f);
            f=SupportedType.convert(yv.getValue(),Float.class);
            System.out.printf("Converted %s byte to %s float%n",yv.getValue(),f);
            f=SupportedType.convert(hv.getValue(),Float.class);
            System.out.printf("Converted %s short to %s float%n",hv.getValue(),f);
            f=SupportedType.convert(iv.getValue(),Float.class);
            System.out.printf("Converted %s integer to %s float%n",iv.getValue(),f);
            f=SupportedType.convert(fv.getValue(),Float.class);
            System.out.printf("Converted %s integer to %s float%n",iv.getValue(),f);
            f=SupportedType.convert(lv.getValue(),Float.class);
            System.out.printf("Converted %s long to %s float%n",lv.getValue(),f);
            f=SupportedType.convert(dv.getValue(),Float.class);
            System.out.printf("Converted %s double to %s float%n",dv.getValue(),f);
            in="-2.01";
            f=SupportedType.convert(in,Float.class);
            System.out.printf("Converted %s String to %s float%n",in,f);
            // To double
            in="-2345.45501";
            double d=SupportedType.convert(bv.getValue(),Double.class);
            System.out.printf("Converted %s boolean to %s double%n",bv.getValue(),d);
            d=SupportedType.convert(yv.getValue(),Double.class);
            System.out.printf("Converted %s byte to %s double%n",yv.getValue(),d);
            d=SupportedType.convert(hv.getValue(),Double.class);
            System.out.printf("Converted %s short to %s double%n",hv.getValue(),d);
            d=SupportedType.convert(iv.getValue(),Double.class);
            System.out.printf("Converted %s integer to %s double%n",iv.getValue(),d);
            d=SupportedType.convert(fv.getValue(),Double.class);
            System.out.printf("Converted %s integer to %s double%n",iv.getValue(),d);
            d=SupportedType.convert(lv.getValue(),Double.class);
            System.out.printf("Converted %s long to %s double%n",lv.getValue(),d);
            d=SupportedType.convert(dv.getValue(),Double.class);
            System.out.printf("Converted %s double to %s double%n",dv.getValue(),d);
            in="-2345.45501";
            d=SupportedType.convert(in,Double.class);
            System.out.printf("Converted %s String to %s double%n",in,d);

            // Try this
            int maxChar=1111988;
            for(int j=0;j<10;j++)
            {
                long ch=Math.round(maxChar*Math.random());
                System.out.println("Character simulated is:" + (char)ch);
            }
        }
        catch(InvalidTypeException ive)
        {
            ive.printStackTrace();
        }
    }

    static ValueQualityObject<Instant> testVQAndInstantParsing(String dt) {
        ValueQualityObject<Instant> tvq=null;
        try {
            Instant t = SupportedType.stringToInstant(dt);
            SupportedType<Instant> tv = new SupportedType(t);
            tvq = new ValueQualityObject(tv, OPCCode.Bad);
            System.out.printf("Instant test succeeded parsing %s%n", dt);
            System.out.printf("Instant stored was %s%n",t.toString());
        } catch (Exception pe) {
            System.out.printf("Instant test failed for %s%n",dt);
            System.out.println(pe.getMessage());
            System.out.println(pe.getStackTrace());
        }
        return tvq;
    }
}