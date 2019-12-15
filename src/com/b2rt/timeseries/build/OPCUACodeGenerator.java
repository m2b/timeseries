package com.b2rt.timeseries.build;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

// Generatas com.itvizion.com.itvizion.timeseries.OPCCode java file containing enumeration which can be used in this
public class OPCUACodeGenerator {

    enum ParserState {BEFORE,CODES,AFTER};

    public static void main(String[] args) throws URISyntaxException {
        String javaTemplate = "src/main/resources/OPCCodeTemplate_DONOTUSE.txt";
        String javaFile="src/main/java/com/itvizion/com.itvizion.timeseries/OPCCode.java";
        String csvFile = "src/main/resources/OPCUACodes_DONOTUSE.csv";

        StringBuilder before=new StringBuilder();
        StringBuilder after=new StringBuilder();
        StringBuilder codes=new StringBuilder();
        ParserState state= ParserState.BEFORE;
        // Read template java file
        // using try with resource, Java 7 feature to close resourcesFile

        Path pathToFile = Paths.get(javaTemplate);
        try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.UTF_8)) {
            String line = null; //
            // loop until all lines are read

            while ((line=br.readLine())!=null) {
                switch(state)
                {
                    case BEFORE:
                        if(!line.trim().startsWith("// Codes list")) {
                            before.append(line);
                            before.append(System.lineSeparator());
                        }
                        else
                            state= ParserState.CODES;
                        break;
                    case CODES:
                        if(line.trim().startsWith("// End of codes"))
                            state= ParserState.AFTER;
                        break;
                    default: // After
                        after.append(line);
                        after.append(System.lineSeparator());
                }
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        }

        // Read the codes from csv file
        try {
            List<String> lines = readCodesFromCSV(csvFile);
            for(String line:lines)
            {
                codes.append(String.format("%s%s",line,System.lineSeparator()));
            }
        }
        catch(IOException ioe)
        {
            System.exit(1);
        }

        // Finally writeback to java file
        pathToFile = Paths.get(javaFile);
        try(BufferedWriter bw= Files.newBufferedWriter(pathToFile,StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE))
        {
            bw.write(before.toString());
            bw.write(codes.toString());
            bw.write(after.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> readCodesFromCSV(String fileName) throws IOException
    {
        List<String> codes = new ArrayList<>();
        Path pathToFile = Paths.get(fileName);
        // create an instance of BufferedReader
        // using try with resource, Java 7 feature to close resources
        try (BufferedReader br = Files.newBufferedReader(pathToFile,StandardCharsets.UTF_8))
        {
            // read the first line from the text file
            String line = null; //
            // loop until all lines are read
            while ((line=br.readLine())!= null) {
                // use string.split to load a string array with the values from
                // each line of the file, using a comma as the delimiter
                String[] attributes = line.split(",");
                OPCCodeValue code = OPCCodeValue.createCode(attributes);
                String codeline=String.format("%s(\"%s\",\"%s\"),",code.getCode(),code.getHex(),code.getDescription());
                codes.add(codeline);
            }
            // Replace last comma
            String lastline=codes.get(codes.size()-1);
            lastline=lastline.substring(0,lastline.length()-1)+";";
            codes.set(codes.size()-1,lastline);
            return codes;
        } catch (IOException ioe)
        {
            throw(ioe);
        }
    }


}