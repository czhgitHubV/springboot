package com.czh.controller;

import com.czh.bean.BaseResult;
import com.czh.bean.RegInfo;
import com.czh.manager.EsManager;
import org.apache.commons.lang3.StringEscapeUtils;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author:chenzhihua
 * @Date: 2020/12/3 15:39
 * @Deacription:
 **/
@RestController
public class AutoRegController {

    private static final Logger logger = LoggerFactory.getLogger(AutoRegController.class);

    @Value("${pipeline.id}")
    private String pipelineId;

    @Autowired
    public EsManager manager;

    @RequestMapping("/index")
    public Map index(@RequestBody String contentText){
        Map<String,Object> map =new HashMap<String,Object>();
        String test="^(?:[^ \n]* ){1}(?<P0>[^ \n]+)(?:[^\n]*\n){1}(?<P1>[^ \\n]+)";
//        String testNew=test.replaceAll("\\\","");
        String testNew = StringEscapeUtils.unescapeJava(test);
        map.put("test", testNew);
        map.put("contentText", StringEscapeUtils.unescapeJava(contentText));
        logger.info("###index###操作成功!!!!");
        return map;
    }
    /**
     * @Author chenzhihua
     * @Date 15:39 2020/12/4
     * @Description 自动生成正则
     **/
    @RequestMapping("/autoreg")
    public String autoreg(@RequestBody RegInfo regInfo){
        if(regInfo!=null){
            String regexp = generateRegexp(regInfo.getContentText(), regInfo.getStartNum(),
                    regInfo.getEndNum(), regInfo.getNickName(), regInfo.getType());
            return regexp;
        }
        return "";
    }

    @RequestMapping("/autoreg2")
    public BaseResult<Map> autoreg2(@RequestBody List<RegInfo> listInfo){
        BaseResult result = new BaseResult();
        Map<String,Object> map =new HashMap<String,Object>();
        String regexp="";
        String message="操作成功!!!";
        try{
//            listInfo.sort((x,y)->{
//                if (x.getStartNum() > y.getStartNum()) return 1;
//                else if (x.getStartNum() == y.getStartNum()) return 0;
//                else return -1;
//            });
            regexp = getRegexp(listInfo);
            map.put("regexp",StringEscapeUtils.unescapeJava(regexp));
            map.put("listInfo",listInfo);
            result.setResult(map);
        }catch (Exception e){
            message="生成正则表达式出现错误，请联系管理员!!!";
            logger.error(message+e.getMessage());
            result.setResult(null);
            result.setMessage(message);
        }

        if(result.getResult()!=null){
            //下发配置到es中
            boolean pipeline = addPipeline(pipelineId, regexp);
            if(!pipeline){
                message="下发配置到es失败!!!";
                result.setResult(null);
                result.setMessage(message);
            }
            String index="sportplay-index";
            Map<String,Object> dataMap=new HashMap<String,Object>();
            dataMap.put("message", listInfo.get(0).getContentText());
            try {
                IndexResponse indexResponse = manager.execIndex(index,null,dataMap,pipelineId);
                if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                    logger.info("索引文档创建成功!!!");
                } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                    logger.info("索引文档更新成功!!!");
                }
                List<Map> maps = manager.queryAll(indexResponse.getIndex());
                System.out.println(maps);
            } catch (IOException e) {
                logger.error("索引插入数据失败!!!"+e.getMessage());
            }


        }
        return result;
    }

    //新增一个管道,下发配置到es
    private boolean addPipeline(String pipelineId,String patterns){
        boolean ack=false;
        try{
            String pipelineSetting="{\"description\":\"this is the Grok Processor\"," +
                    "\"processors\":[{\"grok\":{\"field\":\"message\",\"patterns\":[\""+patterns+"\"]}}]}";
            System.out.println(pipelineSetting);
            AcknowledgedResponse acknowledgedResponse = manager.putPipeline(pipelineId, pipelineSetting);
            if(acknowledgedResponse.isAcknowledged()){
                ack=true;
            }
        }catch (Exception e){
            logger.error("下发配置到es失败，请心理管理员!!!"+e.getMessage());
        }
        return ack;
    }


    /**
     * @Author chenzhihua
     * @Date 15:19 2020/12/7
     * @Description 自动生成正则
     **/
    private int pos = 0;//已匹配到的位置
    public String getRegexp(List<RegInfo> listsi){
        listsi.sort((x,y)->{
            if (x.getStartNum() > y.getStartNum()) return 1;
            else if (x.getStartNum() == y.getStartNum()) return 0;
            else return -1;
        });
        String regstr = "^";//^表示行头
        String contentText=listsi.get(0).getContentText();//原字符串
        byte[] strtobytes = contentText.getBytes();//原字符串转为字符数组
        for (int i = 0; i < listsi.size(); i++)
        {
            int startnum=listsi.get(i).getStartNum();
            int endnum=listsi.get(i).getEndNum();
            if (startnum - pos > 0)//如果选区开始位置和匹配到的位置有差别，就是选区前面还有字符的意思
            {
                int cr = countlf1(strtobytes, pos, startnum - 1, (byte) 10);//计算选区前换行符个数，还要更新pos位置
                if (cr > 0)
                {
                    regstr += "(?:[^\\n]*\\n){" + cr + "}";//替换选区前换行符
                }

                if (strtobytes[startnum - 1] != 10)  //如果前分隔符不是换行符。如果是换行符，前一语句已经完成了替换。
                {
                    int cc = countchar(strtobytes, pos, startnum - 1, strtobytes[startnum - 1]);//统计两者之间前分隔符的个数
                    regstr += "(?:[^" + escapemethod(contentText.substring(startnum - 1, startnum)) + "\\n]*" + escapemethod(contentText.substring(startnum - 1, startnum)) + "){" + cc + "}";   //使用(?:[^字符\n]*字符){次数}替换
                }
            }
            pos = startnum;
            switch (listsi.get(i).getType())
            {
                case "STRING":
                    String sourcestr = contentText.substring(startnum, endnum+1);//选定区
                    char[] backchar = contentText.substring(endnum+1, endnum+2).toCharArray();//后分隔符
                    if (sourcestr.contains(String.valueOf(backchar[0])))       //如果选定区内包含有后分隔符,则按后分隔符分隔替换（如果不这样做匹配容易出错）
                    {
                        regstr += "(?<" + listsi.get(i).getNickName() + ">";
                        String[] str = sourcestr.split(String.valueOf(backchar[0]));//按后分隔符分组
                        for(int j=0;j<str.length;j++)   //按后分隔符替换成正则
                        {
                            if (str[j].equals(""))//如果为空
                            {
                                regstr += escapemethod(String.valueOf(backchar[0]));
                            }
                            else
                            {
                                if (j == str.length - 1)//如果是最后一个,就不用加escapemethod(backchar[0].ToString()
                                {
//                                    int cr = Regex.Matches(str[j], "\n").Count;
                                    int cr=countchar(str[j].getBytes(), 0, str[j].getBytes().length-1, (byte) 10);//计算str[j]字符串中\n个数
                                    if (cr>0)
                                    {
                                        regstr += "(?:[^\\n]*\\n){" + cr + "}";//替换选区内换行符
                                    }
                                    if (backchar[0] != 10)  //如果后分隔符是换行符
                                    {
                                        //MessageBox.Show(((int)backchar[0]).ToString());
                                        regstr += "[^" + escapemethod(String.valueOf(backchar[0])) + "\\n]+";
                                    }
                                    else
                                    {
                                        regstr += "[^\\n]+";
                                    }
                                }
                                else //否则要加escapemethod(backchar[0].ToString()
                                {
                                    int cr=countchar(str[j].getBytes(), 0, str[j].getBytes().length-1, (byte) 10);
                                    if (cr > 0)
                                    {
                                        regstr += "(?:[^\\n]*\\n){" + cr + "}";//替换选区内换行符
                                    }
                                    if (((int)backchar[0]) != 10)   //如果后分隔符是换行符
                                    {
                                        regstr += "[^" + escapemethod(String.valueOf(backchar[0])) + "\\n]+" + escapemethod(String.valueOf(backchar[0]));
                                    }
                                    else
                                    {
                                        regstr += "[^\\n]+" + "\\n";
                                    }
                                }
                            }
                        }
                        regstr += ")";
                    }
                    else
                    {
                        if (((int)backchar[0]) != 10)  //如果后分隔符是换行符
                        {
                            regstr += "(?<" + listsi.get(i).getNickName() + ">" + "[^" + escapemethod(String.valueOf(backchar[0])) + "\\n]+)"; //如果是字符串，使用(?<别名>[^间隔符号\n]+)替换
                        }
                        else
                        {
                            regstr += "(?<" + listsi.get(i).getNickName() + ">" + "[^\\n]+)";
                        }
                    }
                    break;
                case "NUMBER":
                    regstr += "(?<" + listsi.get(i).getNickName() + ">" + "\\d+)";
                    break;
                case "FLOAT":
                    regstr += "(?<" + listsi.get(i).getNickName() + ">" + "-?\\d+(?:\\.\\d+)?)";
                    break;
                case "Datetime:MMM dd HH:mm:ss":
                    regstr += "(?<" + listsi.get(i).getNickName() + ":Datetime>[A-Za-z]+\\s+\\d+\\s+\\d+:\\d+:\\d+)";
                    break;
                case "Datetime:yyyy-MM-dd HH:mm:ss":
                    regstr += "(?<" + listsi.get(i).getNickName() + ":Datetime>\\d+-\\d+-\\d+\\s+\\d+:\\d+:\\d+)";
                    break;
                case "IPV4":
                    regstr += "(?<" + listsi.get(i).getNickName() + ">" + "\\d+\\.\\d+\\.\\d+\\.\\d+)";
                    break;
                default:
                    break;
            }
            pos += endnum-startnum+1;
        }

        return regstr;
    }

    /**
     * @Author chenzhihua
     * @Date 15:14 2020/12/4
     * @Description
     * contentText:原字符串
     * startNum:选中内容的起始位置
     * endNum:选中内容的结束位置
     * nickName:别名
     * type:类型
     **/
    private String generateRegexp(String contentText,Integer startNum,Integer endNum,String nickName,String type){
        String regstr = "^";//^表示行头
        byte[] strtobytes = contentText.getBytes();

        if(startNum>0){
            int cr = countlf(strtobytes, 0, startNum, (byte) 10);//计算选区前换行符个数
            if (cr > 0)
            {
                regstr += "(?:[^\\n]*\\n){" + cr + "}";//替换选区前换行符
            }
            if(strtobytes[startNum-1] != 10){
                int charIndex = getCharIndex(strtobytes, 0, startNum - 1, (byte) 10);
                int cc = countchar(strtobytes, charIndex+1, startNum - 1, strtobytes[startNum - 1]);//统计两者之间前分隔符的个数
                regstr += "(?:[^" + escapemethod(contentText.substring(startNum-1, startNum)) + "\\n]*" + escapemethod(contentText.substring(startNum - 1, startNum)) + "){" + cc + "}";   //使用(?:[^字符\n]*字符){次数}替换
            }

        }

        switch (type)
        {
            case "STRING":
                String sourcestr = contentText.substring(startNum, endNum+1);//选定区
                char[] backchar = contentText.substring(endNum+1, endNum+2).toCharArray();//后分隔符
                if (sourcestr.contains(String.valueOf(backchar[0])))       //如果选定区内包含有后分隔符,则按后分隔符分隔替换（如果不这样做匹配容易出错）
                {
                    regstr += "(?<" + nickName + ">";
                    String[] str = sourcestr.split(String.valueOf(backchar[0]));//按后分隔符分组
                    for(int j=0;j<str.length;j++)   //按后分隔符替换成正则
                    {
                        if (str[j].equals(""))//如果为空
                        {
                            regstr += escapemethod(String.valueOf(backchar[0]));
                        }
                        else
                        {
                            if (j == str.length - 1)//如果是最后一个,就不用加escapemethod(backchar[0].ToString()
                            {
                                /*int cr = Regex.Matches(str[j], "\n").Count;*/
                                int crs=countchar(str[j].getBytes(), 0, str[j].getBytes().length-1, (byte) 10);//计算str[j]字符串中\n个数
                                if (crs>0)
                                {
                                    regstr += "(?:[^\\n]*\\n){" + crs + "}";//替换选区内换行符
                                }
                                if(backchar[0] != 10){ //如果后分隔符是换行符
                                    regstr += "[^" + escapemethod(String.valueOf(backchar[0])) + "\\n]+";
                                }else{
                                    regstr += "[^\\n]+";
                                }
                            }
                            else //否则要加escapemethod(backchar[0].ToString()
                            {
                                int crs=countchar(str[j].getBytes(), 0, str[j].getBytes().length-1, (byte) 10);
                                if (crs > 0)
                                {
                                    regstr += "(?:[^\\n]*\\n){" + crs + "}";//替换选区内换行符
                                }

                                if (backchar[0] != 10)   //如果后分隔符是换行符
                                {
                                    regstr += "[^" + escapemethod(String.valueOf(backchar[0])) + "\\n]+" + escapemethod(String.valueOf(backchar[0]));
                                }
                                else
                                {
                                    regstr += "[^\\n]+" + "\\n";
                                }
                            }
                        }
                    }
                    regstr += ")";
                }
                else
                {
                    if (backchar[0] != 10)  //如果后分隔符是换行符
                    {
                        regstr += "(?<" + nickName + ">" + "[^" + escapemethod(String.valueOf(backchar[0])) + "\\n]+)"; //如果是字符串，使用(?<别名>[^间隔符号\n]+)替换
                    }
                    else
                    {
                        regstr += "(?<" + nickName + ">" + "[^\\n]+)";
                    }
                }
                break;
            case "NUMBER":
                regstr += "(?<" + nickName + ">" + "\\d+)";
                break;
            case "FLOAT":
                regstr += "(?<" + nickName + ">" + "-?\\d+(?:\\.\\d+)?)";
                break;
            case "Datetime:MMM dd HH:mm:ss":
                regstr += "(?<" + nickName + ":Datetime>[A-Za-z]+\\s+\\d+\\s+\\d+:\\d+:\\d+)";
                break;
            case "Datetime:yyyy-MM-dd HH:mm:ss":
                regstr += "(?<" + nickName + ":Datetime>\\d+-\\d+-\\d+\\s+\\d+:\\d+:\\d+)";
                break;
            case "IPV4":
                regstr += "(?<" + nickName + ">" + "\\d+\\.\\d+\\.\\d+\\.\\d+)";
                break;
            default:
                break;
        }

        return regstr.replaceAll("\\\\","");
    }

    private int countlf(byte[] bytestr, int startnum, int endnum, byte specbyte)//计算byte数组中指定符的个数
    {
        int c = 0;
        for (int i = startnum; i <= endnum; i++)
        {
            if (bytestr[i] == specbyte)//如果等于换行符
            {
                c += 1;
//                pos = i;//更新pos位置
            }
        }
        return c;
    }

    private int countlf1(byte[] bytestr, int startnum, int endnum, byte specbyte)//计算byte数组中指定符的个数
    {
        int c = 0;
        for (int i = startnum; i <= endnum; i++)
        {
            if (bytestr[i] == specbyte)//如果等于换行符
            {
                c += 1;
                pos = i;//更新pos位置
            }
        }
        return c;
    }

    private int countchar(byte[] bytestr, int startnum, int endnum, byte targetbyte)//计算byte数组中含某个值的个数
    {
        int c = 0;
        for (int i = startnum; i <= endnum; i++)
        {
            if (bytestr[i] == targetbyte)//如果等于空格
            {
                c += 1;
            }
        }
        return c;
    }

    //获取选区前最后的那个换行符的下标
    private int getCharIndex(byte[] bytestr, int startnum, int endnum, byte targetbyte)
    {
        int c = 0;
        for (int i = startnum; i < endnum; i++)
        {
            if (bytestr[i] == targetbyte)//如果等于\n
            {
                c = i;
            }
        }
        return c;
    }

    private String escapemethod(String instr)//判断是否要转义，转义就加\，否则不加
    {
        String outstr="";
        switch (instr)
        {
            case "$":
                outstr = "\\$" ;
                break;
            case "(":
                outstr = "\\(" ;
                break;
            case ")":
                outstr = "\\)" ;
                break;
            case "*":
                outstr = "\\*" ;
                break;
            case "+":
                outstr = "\\+" ;
                break;
            case ".":
                outstr = "\\." ;
                break;
            case "[":
                outstr = "\\[" ;
                break;
            case "]":
                outstr = "\\]" ;
                break;
            case "?":
                outstr = "\\?" ;
                break;
            case "\\":
                outstr = "\\\\" ;
                break;
            case "^":
                outstr = "\\^" ;
                break;
            case "{":
                outstr = "\\{" ;
                break;
            case "}":
                outstr = "\\}" ;
                break;
            case "|":
                outstr = "\\|" ;
                break;
            default://其他不用转义
                outstr = instr;
                break;
        }
        return outstr;
    }

    public static void main(String[] args) {
        String sourcestr="=====";
        String ch="=";
        char[] backchar=ch.toCharArray();
        String[] split = sourcestr.split(String.valueOf(backchar[0]));
        System.out.println(split.length);


    }


}
