package com.anranruozhu.utils;

import java.util.List;
import java.util.Objects;

/**
 * @author anranruozhu
 * @ClassName AlgorithmUtils
 * @description 算法工具类实现
 * @create 2024/7/18 下午3:25
 **/
public class AlgorithmUtils {
    /**
     * 编辑距离算法，（用来计算最相似的两个字符串）
     * @param word1
     * @param word2
     *@retrun
     **/
    public static int minDistance(String word1,String word2){
        int n=word1.length();
        int m=word2.length();

        if(n*m==0)
            return n+m;

        int[][] d=new int[n+1][m+10];
        for(int i=0;i<n+1;i++){
            d[i][0]=i;
        }
        for(int j=0;j<m+1;j++){
            d[0][j]=j;
        }
        for(int i=1;i<=n;i++){
            for(int j=1;j<=m;j++){
                int left=d[i-1][j]+1;
                int down=d[i][j-1]+1;
                int left_down=d[i-1][j-1];
                if(word1.charAt(i-1)!=word2.charAt(j-1))
                    left_down+=1;
                d[i][j]=Math.min(left,Math.min(left_down,down));
            }
        }

        return d[n][m];
    }
    /**
     * 编辑距离算法，（用来计算最相似的两个标签列表）
     * @param word1
     * @param word2
     *@retrun
     **/
    public static long minDistance(List<String> word1, List<String> word2){
        int n=word1.size();
        int m=word2.size();
        if(n*m==0)
            return n+m;
        int[][] d=new int[n+1][m+10];
        for(int i=0;i<n+1;i++){
            d[i][0]=i;
        }
        for(int j=0;j<m+1;j++){
            d[0][j]=j;
        }
        for(int i=1;i<=n;i++){
            for(int j=1;j<=m;j++){
                int left=d[i-1][j]+1;
                int down=d[i][j-1]+1;
                int left_down=d[i-1][j-1];
                if(!Objects.equals(word1.get(i - 1), word2.get(j - 1)))
                    left_down+=1;
                d[i][j]=Math.min(left,Math.min(left_down,down));
            }
        }
        return d[n][m];
    }
}
