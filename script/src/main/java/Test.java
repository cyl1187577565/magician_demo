import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: yulong.cao
 * @since: 2020-12-11 20:22
 **/
public class Test extends ArrayList{
    public static void main(String[] args) throws Exception {
       List<Integer> list = Lists.newArrayList(1,2);
        List<Integer> collect = list.stream()
                .map(it -> {
                    if (it == 1) {
                        return new ArrayList<Integer>();
                    }

                    return Lists.newArrayList(it);
                })
                .flatMap(List::stream)
                .collect(Collectors.toList());
        System.out.println(collect);
    }
}
