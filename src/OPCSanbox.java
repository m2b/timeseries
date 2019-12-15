import com.b2rt.timeseries.OPCCode;
import com.b2rt.timeseries.OPCQuality;
import com.b2rt.timeseries.build.OPCCodeValue;

import java.io.InvalidObjectException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class OPCSanbox {

    static List<OPCCodeValue> testValues=null;
    static {
        try {
            testValues = new ArrayList<>(Arrays.asList(
                    OPCCodeValue.createCode(new String[]{"Good", "0xC0","The value is good. There are no special conditions."}),
                    OPCCodeValue.createCode(new String[]{"GoodLocalOverride", "0xD8","Good--Local Override, Value Forced"}),
                    OPCCodeValue.createCode(new String[]{"Bad", "0x00","The value is bad but no specific reason is known"}),
                    OPCCodeValue.createCode(new String[]{"BadConfigError", "0x04","Bad - Configuration Error in Server"}),
                    OPCCodeValue.createCode(new String[]{"BadNotConnected","0x08","Bad - Not connected"}),
                    OPCCodeValue.createCode(new String[]{"BadDeviceFail","0x0C","Bad - Device Failure}"}),
                    OPCCodeValue.createCode(new String[]{"Uncertain", "0x40", "There is no specific reason why the value is uncertain."}),
                    OPCCodeValue.createCode(new String[]{"UncertainUnitsExceeded", "0x54","Uncertain - Engineering Units exceeded"})
            ));
        } catch (InvalidObjectException ive) {
            ive.printStackTrace();
        }
    }

    static OPCCodeValue BAD0x0=null;
    static OPCCodeValue BAD0x30=null;
    static OPCCodeValue BAD0x81=null;
    static OPCCodeValue BAD0x54ABCD=null;

    static {
        try
        {
            BAD0x0=OPCCodeValue.createCode(new String[]{"ShortHex", "0x0", null});
            BAD0x30=OPCCodeValue.createCode(new String[]{"ShortInvalidHex", "0x30", null});
            BAD0x81=OPCCodeValue.createCode(new String[]{"InvalidHex", "0x81", null});
            BAD0x54ABCD=OPCCodeValue.createCode(new String[]{"TooLongHex", "0x54ABCD", null});
        } catch (InvalidObjectException ive) {
            ive.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        // How does sorting work
        List<String> strings=Stream.of("Hello","World","Alexa","Google","Siri","Cortana","Amazon","Microsoft").collect(Collectors.toList());
        System.out.println("Before sorting:");
        System.out.println(strings);
        Collections.sort(strings);
        System.out.println("After sorting:");
        System.out.println(strings);

        // Comparator for OPCCodes
        Comparator<OPCCode> comparator=new Comparator<OPCCode>() {
            @Override
            public int compare(OPCCode o1, OPCCode o2) {
                return Long.compare(o1.getValue(),o2.getValue());
            }
        };

        // Check that opc codes can be sorted
        List<OPCCode> sortedCodes=Arrays.asList(OPCCode.values());
        System.out.println("Before sorting:");
        System.out.println(sortedCodes);
        Collections.sort(sortedCodes,comparator);
        System.out.println("After sorting:");
        System.out.println(sortedCodes);


        // For each testValue
        for(OPCCodeValue test:testValues)
        {
            String testcase=startTestCase(test);
            try
            {
                // Get from hex
                OPCCode code=OPCCode.getFromHex(test.getHex());
                assert code.getHex().equals(test.getHex()):"Test getHex failed";

                // Get from value
                int cl=Integer.decode(test.getHex());
                code=OPCCode.getFromInt(cl);
                assert code.getValue()==cl:"Test getFromInt failed";

                // Test quality
                OPCQuality q= code.getQuality();
                String msg=String.format("expected but found %s instead",q.name());
                if(test.getCode().toLowerCase().startsWith("good"))
                    assert q==OPCQuality.Good:"Quality of good "+msg;
                else if(test.getCode().toLowerCase().startsWith("bad"))
                    assert q==OPCQuality.Bad:"Quality of bad "+msg;
                else
                    assert q==OPCQuality.Uncertain:"Quality of uncertain "+msg;
            }
            catch(AssertionError err){
                System.out.println(testcase+" - failure: "+ err.getMessage());
            }

            endTestCase(testcase);
        }

        // Bad hex
        String testcase=String.format("code=%s, hex=%s, desc=%s","BadHex", "XXX", null);;
        System.out.println("Testing starting for test case "+testcase);
        try
        {
            OPCCodeValue.createCode(new String[]{"BadHex", "XXX", null});
            System.out.println(testcase+" failed to catch exception");
        }
        catch(InvalidObjectException ive)
        {
            System.out.println("Testing sucessful for case "+testcase);
        }

        // Other bad cases
        OPCCodeValue test=BAD0x0;
        testcase=startTestCase(test);
        OPCCode testcode=OPCCode.getFromHex(test.getHex());
        assert testcode.getHex().equals("0x00"):testcase+" short hex string was not extended";
        endTestCase(testcase);
        test=BAD0x30;
        testcase=startTestCase(test);
        testcode=OPCCode.getFromHex(test.getHex());
        assert testcode==null:testcase+" bad hex found in spite of not being valid";
        endTestCase(testcase);
        test=BAD0x81;
        testcase=startTestCase(test);
        testcode=OPCCode.getFromHex(test.getHex());
        assert testcode==null:testcase+" bad hex found in spite of not being valid";
        endTestCase(testcase);
        test=BAD0x54ABCD;
        testcase=startTestCase(test);
        testcode=OPCCode.getFromHex(test.getHex());
        assert testcode.getHex().length()==8 && testcode==OPCCode.UncertainUnitsExceeded:testcase+" invalid code created from hex that as too long";
        endTestCase(testcase);
    }

    static String startTestCase(OPCCodeValue test)
    {
        String testcase=String.format("code=%s, hex=%s, desc=%s",test.getCode(),test.getHex(),test.getDescription());
        System.out.println("Testing starting for test case "+testcase);
        return testcase;
    }

    static void endTestCase(String testcase)
    {
        System.out.println("Testing sucessful for case "+testcase);
    }

}