package com.quartz.cn.springbootquartzdemo.confg.util;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 *
 * @describe 中文转化成拼音工具类
 * @author zfc
 * @date 2017年12月19日 上午11:32:59
 */
public class ChineseToPinYinUtil2 {

    public static void main(String[] args) {
        // 中文字符串首字母大写
        System.out.println("张富昌（中文字符串首字母大写）:" + toFirstCharUpCase("张富昌"));
        // 中文字符串首字母小写
        System.out.println("张富昌（中文字符串首字母小写）:" + toFirstCharLowCase("张富昌"));
        // 中文字符串拼音大写
        System.out.println("张富昌（中文字符串拼音大写）:" + toPinyinUpCase("张富昌"));
        // 中文字符串拼音小写
        System.out.println("张富昌（中文字符串拼音小写）:" + toPinyinLowCase("张富昌"));
       
        
    }

    /**
     * 获取中文字符串相应的拼音首字母小写
     *
     * @param chinese
     * @return
     * @author zfc
     *
     */
    public static String toFirstCharLowCase(String chinese) {
        if (null == chinese) {
            return null;
        }
        String pinyinStr = "";
        // 将字符串转为字符数组
        char[] newChar = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        // 设置小写
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < newChar.length; i++) {
            if (newChar[i] > 128) {
                try {
                    pinyinStr += PinyinHelper.toHanyuPinyinStringArray(newChar[i], defaultFormat)[0].charAt(0);
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinyinStr += newChar[i];
            }
        }
        return pinyinStr;
    }

    /**
     * 获取中文字符串相应的拼音首字母大写
     *
     * @param chinese
     * @return
     * @author zfc
     *
     */
    public static String toFirstCharUpCase(String chinese) {
        if (null == chinese) {
            return null;
        }
        String pinyinStr = "";
        // 将字符串转为字符数组
        char[] newChar = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        // 设置大写
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < newChar.length; i++) {
            if (newChar[i] > 128) {
                try {
                    pinyinStr += PinyinHelper.toHanyuPinyinStringArray(newChar[i], defaultFormat)[0].charAt(0);
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinyinStr += newChar[i];
            }
        }
        return pinyinStr;
    }

    /**
     * 将中文字符串转换成拼音小写
     *
     * @param chinese
     * @return
     * @author zfc
     *
     */
    public static String toPinyinLowCase(String chinese) {
        if (null == chinese) {
            return null;
        }
        String pinyinStr = "";
        // 将字符串转为字符数组
        char[] newChar = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        // 设置小写
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < newChar.length; i++) {
            if (newChar[i] > 128) {
                try {
                    pinyinStr += PinyinHelper.toHanyuPinyinStringArray(newChar[i], defaultFormat)[0];
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinyinStr += newChar[i];
            }
        }
        return pinyinStr;
    }

    /**
     * 将中文字符串转换成拼音大写
     *
     * @param chinese
     * @return
     * @author zfc
     *
     */
    public static String toPinyinUpCase(String chinese) {
        if (null == chinese) {
            return null;
        }
        String pinyinStr = "";
        // 将字符串转为字符数组
        char[] newChar = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        // 设置大写
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < newChar.length; i++) {
            if (newChar[i] > 128) {
                try {
                    pinyinStr += PinyinHelper.toHanyuPinyinStringArray(newChar[i], defaultFormat)[0];
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinyinStr += newChar[i];
            }
        }
        return pinyinStr;
    }
    
    /**
    
     * 将str转换成拼音，如果不是汉字或者没有对应的拼音，则不作转换

     * 如： 明天 转换成 MINGTIAN

     * @param str：要转化的汉字

     * @param spera：转化结果的分割符

     * @return

     * @throws BadHanyuPinyinOutputFormatCombination

     */

   public static String toPinYin(String str, String spera, Type type){
       try {
           HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
           if(str == null || str.trim().length()==0)
               return "";
           if(type == Type.UPPERCASE)
               format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
           else
               format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
           String py = "";
           String temp = "";
           String[] t;
           for(int i=0;i<str.length();i++){
               char c = str.charAt(i);
               if((int)c <= 128)
                   py += c;
               else{
                   t = PinyinHelper.toHanyuPinyinStringArray(c, format);
                   if(t == null)
                       py += c;
                   else{
                       temp = t[0];
                       if(type == Type.FIRSTUPPER)
                           temp = t[0].toUpperCase().charAt(0)+temp.substring(1);
                       String pattern = "[\\d]{1}";//正则去掉音调即数字
                       temp = temp.replaceAll(pattern,"");
                       py += temp+(i==str.length()-1?"":spera);
                   }
               }
           }
           return py.trim();
       }catch (BadHanyuPinyinOutputFormatCombination e){
           e.printStackTrace();
           return null;
       }
   }
   public static enum Type {
       UPPERCASE,              //全部大写
       LOWERCASE,              //全部小写
       FIRSTUPPER              //首字母大写
   }

   public static String toPinYin(String str){
       return toPinYin(str, "", Type.UPPERCASE);
   }


   public static String toPinYin(String str,String spera){
       return toPinYin(str, spera, Type.UPPERCASE);
   }
}