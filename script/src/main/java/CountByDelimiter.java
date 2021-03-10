import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @description: 统计文件中被特定分隔符的数据
 * @author: yulong.cao
 * @since: 2020-11-24 15:07
 **/
public class CountByDelimiter {
    private static final String DELIMITER = ",";

    public static void main(String[] args) throws Exception{
        InputStream inputStream= Thread.currentThread().getContextClassLoader().getResourceAsStream("count");
        BufferedReader bfd = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bfd.readLine()) != null){
            sb.append(line);
        }

        String text = sb.toString();
        String[] result = text.split(DELIMITER);
        System.out.println("=====================================");
        System.out.println("结果：" + result.length);
        System.out.println("=====================================");
    }
}
