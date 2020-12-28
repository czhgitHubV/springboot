package com.czh.sportplay;


import java.util.*;

/**
 * @Auhtor：陈志华
 * @createTime：2020-09-29 09:03
 * @Description：算出每个人的平均分，并按平均分升序输出
 * new String[]{"8,奥迪,12","7,节点,45","1,王五,70","2,李四,88","3,王五,60","4,张三,90","5,李四,40","6,王五,80"};
 * 奥迪 12
 * 节点 45
 * 李四 64
 * 王五 70
 * 张三 90
 */
public class TestArray {
    public static void main(String[] args) {
        List<String> list = Arrays.asList(new String[]{"8,奥迪,12","7,节点,45","1,王五,70","2,李四,88","3,王五,60","4,张三,90","5,李四,40","6,王五,80"});
        Map<String,Object> map=new HashMap<String,Object>();
        for (int i = 0; i < list.size(); i++) {
            String[] split = list.get(i).split(",");
            int count= 1;
            int sum=Integer.valueOf(split[2]);
            if(map.size()>1 && map.keySet().contains(split[1])){
                continue;
            }
            for (int j = i+1; j < list.size(); j++) {
                String[] split1 = list.get(j).split(",");
                if(split[1].equals(split1[1])){
                    count++;
                    sum+=Integer.valueOf(split1[2]);
                }
            }

            map.put(split[1],sum/count);
        }

        System.out.println(map);

        List<Object> nameList=Arrays.asList(map.keySet().toArray());
        List<Integer> scoreList=new ArrayList<Integer>();

        for (int i = 0; i <map.size() ; i++) {
            scoreList.add(Integer.valueOf(map.get(map.keySet().toArray()[i].toString()).toString()));
        }

        for (int i = 0; i < map.size(); i++) {
            int score=scoreList.get(i);
            Object name=nameList.get(i);
            for (int j = i+1; j <map.size() ; j++) {
                int scorej=scoreList.get(j);
                Object nameJ=nameList.get(j);
                if(score>scorej){
                    scoreList.set(i, scorej);
                    scoreList.set(j,score);

                    nameList.set(i,nameJ);
                    nameList.set(j,name);

                    score=scoreList.get(i);
                    name=nameList.get(i);
                }
            }
        }

        for (int i = 0; i < map.size(); i++) {
            System.out.println(nameList.get(i)+" "+scoreList.get(i));
        }


    }
}
