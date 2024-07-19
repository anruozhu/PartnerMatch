package com.anranruozhu.service;

import com.anranruozhu.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * @author anranruozhu
 * @ClassName AlgorithmUtilsTest
 * @description 算法工具类的测试
 * @create 2024/7/18 下午3:15
 **/
public class AlgorithmUtilsTest {
    @Test
    public void test() {
        String str1="安然若竹";
        String str2="安然箬竹";
        String str3="安然安然";
        int i = AlgorithmUtils.minDistance(str1, str2);
        int i1 = AlgorithmUtils.minDistance(str1, str3);
        System.out.println(i);
        System.out.println(i1);
    }
    @Test
    public void testTagList() {
        List<String> list1 = Arrays.asList("java", "python", "男");
        List<String> list2 =  Arrays.asList("java","大二","女");
        List<String> list3 =  Arrays.asList("python","大二","女");
        long i = AlgorithmUtils.minDistance(list1, list2);
        long i1 = AlgorithmUtils.minDistance(list1, list3);
        System.out.println(i);
        System.out.println(i1);
    }
}
