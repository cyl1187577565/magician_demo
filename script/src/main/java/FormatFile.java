import io.fileIo.FileUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @description:
 * @author: yulong.cao
 * @since: 2020-11-24 15:20
 **/
public class FormatFile {
    private static final String DELIMITER = ",";
    private static final String BASE_PATH = "/Users/bianlifeng/Documents/work/data/";
    private static final String TARGET_FILE_NAME = "result";


    private static final int INT_MODEL = 1;
    private static final int STR_MODEL = 0;

    private static final boolean needAppendDelimiter = true;
    private static final int formatMode = STR_MODEL;

    public static void main(String[] args) throws Exception{
        InputStream inputStream= Thread.currentThread().getContextClassLoader().getResourceAsStream("format");
        BufferedReader bfd = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bfd.readLine()) != null){
            sb.append(line);
            if(needAppendDelimiter){
                sb.append(",");
            }
        }

        String text = sb.toString();
        text.trim();
        String[] split = text.split(DELIMITER);
        StringBuilder result = new StringBuilder();
        for(String str: split){
            result.append(format(str));
        }

        System.out.println(result.substring(0, result.lastIndexOf(",")));
        String resultFile = BASE_PATH + TARGET_FILE_NAME;
        FileUtil.writeToFile(result.substring(0, result.lastIndexOf(",")),resultFile);

    }

    private static  String format(String str){
        switch (formatMode){
            case 0: return formatStr(str);
            case 1: return formatInt(str);
        }
        return "";
    }

    private static String formatStr(String str){
        return "\""+str+"\",";
    }

    private static String formatInt(String str){
        return str+",";
    }
}
