/*
 * Copyright 2018-2018 https://github.com/myoss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.myoss.phoenix.core.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.github.myoss.phoenix.core.lang.base.DateTimeFormatUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 身份证工具类
 *
 * @author Jerry.Chen
 * @since 2018年6月1日 上午2:32:26
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IdCardUtils {
    /**
     * 中国公民身份证号码最小长度。
     */
    private static final int                           CHINA_ID_MIN_LENGTH = 15;

    /**
     * 中国公民身份证号码最大长度。
     */
    private static final int                           CHINA_ID_MAX_LENGTH = 18;

    /**
     * 每位加权因子
     */
    private static final int[]                         POWER               = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10,
            5, 8, 4, 2                                                    };

    /**
     * 最低年限
     */
    private static final int                           MIN                 = 1930;

    /**
     * 内地省份首字母对应数字
     */
    private static final ImmutableMap<String, String>  CITY_CODE;

    /**
     * 台湾省份首字母对应数字
     */
    private static final ImmutableMap<String, Integer> TW_FIRST_CODE;

    /**
     * 香港省份首字母对应数字
     */
    private static final ImmutableMap<String, Integer> HK_FIRST_CODE;
    /**
     * 验证澳门身份证号码正则表达式，据说这个正则表达式更严谨
     */
    public static final Pattern                        MO_CARD_PATTERN     = Pattern
                                                                                   .compile("^[1|5|7][0-9]{6}(\\(?[0-9]\\)?)$");

    /**
     * 性别：男性
     */
    public static final String                         MALE                = "M";
    /**
     * 性别：女性
     */
    public static final String                         FEMALE              = "F";
    /**
     * 性别：未知
     */
    public static final String                         UN_KNOW             = "N";

    static {
        Builder<String, String> cityBuilder = ImmutableMap.builder();
        cityBuilder.put("11", "北京市").put("12", "天津市").put("13", "河北省").put("14", "山西省").put("15", "内蒙古自治区")
                .put("21", "辽宁省").put("22", "吉林省").put("23", "黑龙江省").put("31", "上海市").put("32", "江苏省").put("33", "浙江省")
                .put("34", "安徽省").put("35", "福建省").put("36", "江西省").put("37", "山东省").put("41", "河南省").put("42", "湖北省")
                .put("43", "湖南省").put("44", "广东省").put("45", "广西壮族自治区").put("46", "海南省").put("50", "重庆市")
                .put("51", "四川省").put("52", "贵州省").put("53", "云南省").put("54", "西藏自治区").put("61", "陕西省")
                .put("62", "甘肃省").put("63", "青海省").put("64", "宁夏回族自治区").put("65", "新疆维吾尔族自治区").put("71", "台湾")
                .put("81", "香港").put("82", "澳门").put("91", "国外");
        CITY_CODE = cityBuilder.build();

        Builder<String, Integer> twFirstCodeBuilder = ImmutableMap.builder();
        twFirstCodeBuilder.put("A", 10).put("B", 11).put("C", 12).put("D", 13).put("E", 14).put("F", 15).put("G", 16)
                .put("H", 17).put("J", 18).put("K", 19).put("L", 20).put("M", 21).put("N", 22).put("P", 23)
                .put("Q", 24).put("R", 25).put("S", 26).put("T", 27).put("U", 28).put("V", 29).put("X", 30)
                .put("Y", 31).put("W", 32).put("Z", 33).put("I", 34).put("O", 35);
        TW_FIRST_CODE = twFirstCodeBuilder.build();

        Builder<String, Integer> hkFirstCodeBuilder = ImmutableMap.builder();
        hkFirstCodeBuilder.put("A", 1).put("B", 2).put("C", 3).put("R", 18).put("U", 21).put("Z", 26).put("X", 24)
                .put("W", 23).put("O", 15).put("N", 14);
        HK_FIRST_CODE = hkFirstCodeBuilder.build();
    }

    /**
     * 内地省份首字母对应数字
     *
     * @return 省份首字母对应数字
     */
    public static ImmutableMap<String, String> getCityCode() {
        return CITY_CODE;
    }

    /**
     * 台湾身份首字母对应数字
     *
     * @return 省份首字母对应数字
     */
    public static ImmutableMap<String, Integer> getTwFirstCode() {
        return TW_FIRST_CODE;
    }

    /**
     * 香港身份首字母对应数字
     *
     * @return 省份首字母对应数字
     */
    public static ImmutableMap<String, Integer> getHkFirstCode() {
        return HK_FIRST_CODE;
    }

    /**
     * 将15位身份证号码转换为18位
     *
     * @param idCard 15位身份编码
     * @return 18位身份编码
     */
    public static String convertCard15To18(String idCard) {
        String idCard18 = "";
        if (idCard == null) {
            return idCard18;
        }
        if (idCard.length() != CHINA_ID_MIN_LENGTH) {
            return null;
        }
        if (isNum(idCard)) {
            // 获取出生年月日
            String birthday = idCard.substring(6, 12);
            LocalDateTime birthDate;
            try {
                // 18位身份证号码是从从1999年10月1日起实行，所以15位的号码肯定是2000年之前的
                birthDate = DateTimeFormatUtils.parseToDateEN("19" + birthday);
                Objects.requireNonNull(birthDate);
            } catch (Exception e) {
                log.warn(birthday + " parse to localDate failed", e);
                return null;
            }

            // 获取出生年(完全表现形式,如：2010)
            idCard18 = idCard.substring(0, 6) + birthDate.getYear() + idCard.substring(8);

            // 转换字符数组
            char[] cArr = idCard18.toCharArray();
            int[] iCard = convertCharToInt(cArr);
            int iSum17 = getPowerSum(iCard);

            // 获取校验位
            String sVal = getCheckCode18(iSum17);
            if (sVal.length() > 0) {
                idCard18 += sVal;
            } else {
                return null;
            }
        } else {
            return null;
        }
        return idCard18;
    }

    /**
     * 验证身份证是否合法
     *
     * @param idCard 身份证件号码
     * @return true：校验成功；false：校验失败
     */
    public static boolean validateCard(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return false;
        }
        String card = idCard.trim();
        if (validateIdCard18(card)) {
            return true;
        }
        if (validateIdCard15(card)) {
            return true;
        }
        String[] cardArray = validateIdCard10(card);
        return "true".equals(cardArray[2]);
    }

    /**
     * 验证18位身份编码是否合法
     *
     * @param idCard 身份编码
     * @return 是否合法
     */
    public static boolean validateIdCard18(String idCard) {
        boolean bTrue = false;
        if (idCard == null) {
            return false;
        }
        if (idCard.length() == CHINA_ID_MAX_LENGTH) {
            // 前17位
            String code17 = idCard.substring(0, 17);
            // 第18位
            String code18 = idCard.substring(17, CHINA_ID_MAX_LENGTH);
            if (isNum(code17)) {
                char[] cArr = code17.toCharArray();
                int[] iCard = convertCharToInt(cArr);
                int iSum17 = getPowerSum(iCard);
                // 获取校验位
                String val = getCheckCode18(iSum17);
                if (val.equalsIgnoreCase(code18)) {
                    bTrue = true;
                }
            }
        }
        return bTrue;
    }

    /**
     * 验证15位身份编码是否合法
     *
     * @param idCard 身份编码
     * @return 是否合法
     */
    public static boolean validateIdCard15(String idCard) {
        if (idCard == null) {
            return false;
        }
        if (idCard.length() != CHINA_ID_MIN_LENGTH) {
            return false;
        }
        if (!isNum(idCard)) {
            return false;
        }
        String proCode = idCard.substring(0, 2);
        if (CITY_CODE.get(proCode) == null) {
            return false;
        }
        String birthCode = idCard.substring(6, 12);
        try {
            // 18位身份证号码是从从1999年10月1日起实行，所以15位的号码肯定是2000年之前的
            String year = "19" + birthCode.substring(0, 2);
            //  验证生日小于当前日期，是否有效
            LocalDate birthDate = LocalDate.of(Integer.parseInt(year), Integer.parseInt(birthCode.substring(2, 4)),
                    Integer.parseInt(birthCode.substring(4, 6)));
            return birthDate.getYear() < MIN || !LocalDate.now().isBefore(birthDate);
        } catch (Exception e) {
            log.warn(birthCode + " parse to year failed", e);
            return false;
        }

    }

    /**
     * 验证10位身份编码是否合法
     *
     * @param idCard 身份编码
     * @return 身份证信息数组
     *         <p>
     *         [0] - 台湾、澳门、香港 [1] - 性别(男M,女F,未知N) [2] - 是否合法(合法true,不合法false)
     *         若不是身份证件号码则返回null
     *         </p>
     */
    public static String[] validateIdCard10(String idCard) {
        String[] info = new String[3];
        if (idCard == null) {
            return info;
        }
        String card = idCard.replaceAll("[(|)]", "");
        if (card.length() != 8 && card.length() != 9 && idCard.length() != 10) {
            return info;
        }
        if (idCard.matches("^[a-zA-Z][0-9]{9}$")) {
            // 台湾
            info[0] = "台湾";
            String char2 = idCard.substring(1, 2);
            switch (char2) {
                case "1":
                    info[1] = MALE;
                    break;
                case "2":
                    info[1] = FEMALE;
                    break;
                default:
                    info[1] = UN_KNOW;
                    info[2] = "false";
                    return info;
            }
            info[2] = validateTWCard(idCard) ? "true" : "false";
        } else if (idCard.matches("^[1|5|7][0-9]{6}\\(?[0-9A-Z]\\)?$")) {
            // 澳门
            info[0] = "澳门";
            info[1] = UN_KNOW;
            info[2] = validateMOCard(idCard) ? "true" : "false";
        } else if (idCard.matches("^[A-Z]{1,2}[0-9]{6}\\(?[0-9A]\\)?$")) {
            // 香港
            info[0] = "香港";
            info[1] = UN_KNOW;
            info[2] = validateHKCard(idCard) ? "true" : "false";
        } else {
            return info;
        }

        return info;
    }

    /**
     * 验证台湾身份证号码
     *
     * @param idCard 身份证号码
     * @return 验证码是否符合
     */
    public static boolean validateTWCard(String idCard) {
        if (idCard == null) {
            return false;
        }
        String start = idCard.substring(0, 1);
        String mid = idCard.substring(1, 9);
        String end = idCard.substring(9, 10);
        Integer iStart = TW_FIRST_CODE.get(start);
        Integer sum = iStart / 10 + (iStart % 10) * 9;
        char[] chars = mid.toCharArray();
        Integer flag = 8;
        for (char c : chars) {
            sum = sum + Integer.parseInt(String.valueOf(c)) * flag;
            flag--;
        }
        return (sum % 10 == 0 ? 0 : (10 - sum % 10)) == Integer.parseInt(end);
    }

    /**
     * 验证澳门身份证号码 TODO 目前只验证规则，未找到校验码的算法
     *
     * @param idCard 身份证号码
     * @return 验证码是否符合
     */
    public static boolean validateMOCard(String idCard) {
        if (idCard == null) {
            return false;
        }
        Matcher m = MO_CARD_PATTERN.matcher(idCard);
        if (m.find()) {
            return m.group(1).length() == 1 || m.group(1).length() == 3;
        }
        return false;
    }

    /**
     * 验证香港身份证号码(存在Bug，部份特殊身份证无法检查)
     * <p>
     * 身份证前2位为英文字符，如果只出现一个英文字符则表示第一位是空格，对应数字58 前2位英文字符A-Z分别对应数字10-35
     * 最后一位校验码为0-9的数字加上字符"A"，"A"代表10
     * </p>
     * <p>
     * 将身份证号码全部转换为数字，分别对应乘9-1相加的总和，整除11则证件号码有效
     * </p>
     *
     * @param idCard 身份证号码
     * @return 验证码是否符合
     */
    public static boolean validateHKCard(String idCard) {
        if (idCard == null) {
            return false;
        }
        String card = idCard.replaceAll("[(|)]", "");
        Integer sum;
        if (card.length() == 9) {
            sum = ((int) card.substring(0, 1).toUpperCase().toCharArray()[0] - 55) * 9
                    + ((int) card.substring(1, 2).toUpperCase().toCharArray()[0] - 55) * 8;
            card = card.substring(1, 9);
        } else {
            sum = 522 + ((int) card.substring(0, 1).toUpperCase().toCharArray()[0] - 55) * 8;
        }
        String mid = card.substring(1, 7);
        String end = card.substring(7, 8);
        char[] chars = mid.toCharArray();
        Integer flag = 7;
        for (char c : chars) {
            sum = sum + Integer.parseInt(String.valueOf(c)) * flag;
            flag--;
        }
        if ("A".equalsIgnoreCase(end)) {
            sum = sum + 10;
        } else {
            sum = sum + Integer.parseInt(end);
        }
        return sum % 11 == 0;
    }

    /**
     * 将字符数组转换成数字数组
     *
     * @param ca 字符数组
     * @return 数字数组
     */
    public static int[] convertCharToInt(char[] ca) {
        if (ca == null) {
            return null;
        }
        int len = ca.length;
        int[] iArr = new int[len];
        try {
            for (int i = 0; i < len; i++) {
                iArr[i] = Integer.parseInt(String.valueOf(ca[i]));
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return iArr;
    }

    /**
     * 将身份证的每位和对应位的加权因子相乘之后，再得到和值
     *
     * @param iArr 数组
     * @return 身份证编码。
     */
    public static int getPowerSum(int[] iArr) {
        int iSum = 0;
        if (iArr == null) {
            return iSum;
        }
        if (POWER.length == iArr.length) {
            for (int i = 0; i < iArr.length; i++) {
                for (int j = 0; j < POWER.length; j++) {
                    if (i == j) {
                        iSum = iSum + iArr[i] * POWER[j];
                    }
                }
            }
        }
        return iSum;
    }

    /**
     * 将power和值与11取模获得余数进行校验码判断
     *
     * @param iSum POWER
     * @return 校验位
     */
    public static String getCheckCode18(int iSum) {
        String sCode = "";
        switch (iSum % 11) {
            case 10:
                sCode = "2";
                break;
            case 9:
                sCode = "3";
                break;
            case 8:
                sCode = "4";
                break;
            case 7:
                sCode = "5";
                break;
            case 6:
                sCode = "6";
                break;
            case 5:
                sCode = "7";
                break;
            case 4:
                sCode = "8";
                break;
            case 3:
                sCode = "9";
                break;
            case 2:
                sCode = "X";
                break;
            case 1:
                sCode = "0";
                break;
            case 0:
                sCode = "1";
                break;
            default:
                break;
        }
        return sCode;
    }

    /**
     * 根据身份编号获取生日
     *
     * @param idCard 身份编号
     * @return 生日(yyyyMMdd)
     */
    public static Date getBirthDateByIdCard(String idCard) {
        String birthVal = getBirthByIdCard(idCard);
        if (birthVal == null) {
            return null;
        }
        return DateTimeFormatUtils.parse2DateEN(birthVal);
    }

    /**
     * 根据身份编号获取生日
     *
     * @param idCard 身份编号
     * @return 生日(yyyyMMdd)
     */
    public static String getBirthByIdCard(String idCard) {
        String idCardTemp = idCard;
        if (idCardTemp == null) {
            return null;
        }
        Integer len = idCardTemp.length();
        if (len < CHINA_ID_MIN_LENGTH) {
            return null;
        } else if (len == CHINA_ID_MIN_LENGTH) {
            idCardTemp = convertCard15To18(idCardTemp);
        }
        if (idCardTemp == null) {
            return null;
        }
        return idCardTemp.substring(6, 14);
    }

    /**
     * 根据身份编号获取生日年
     *
     * @param idCard 身份编号
     * @return 生日(yyyy)
     */
    public static Short getYearByIdCard(String idCard) {
        String idCardTemp = idCard;
        if (idCardTemp == null) {
            return null;
        }
        Integer len = idCardTemp.length();
        if (len < CHINA_ID_MIN_LENGTH) {
            return null;
        } else if (len == CHINA_ID_MIN_LENGTH) {
            idCardTemp = convertCard15To18(idCardTemp);
        }
        if (idCardTemp == null) {
            return null;
        }
        return Short.valueOf(idCardTemp.substring(6, 10));
    }

    /**
     * 根据身份编号获取生日月
     *
     * @param idCard 身份编号
     * @return 生日(MM)
     */
    public static Short getMonthByIdCard(String idCard) {
        String idCardTemp = idCard;
        if (idCardTemp == null) {
            return null;
        }
        Integer len = idCardTemp.length();
        if (len < CHINA_ID_MIN_LENGTH) {
            return null;
        } else if (len == CHINA_ID_MIN_LENGTH) {
            idCardTemp = convertCard15To18(idCardTemp);
        }
        if (idCardTemp == null) {
            return null;
        }
        return Short.valueOf(idCardTemp.substring(10, 12));
    }

    /**
     * 根据身份编号获取生日天
     *
     * @param idCard 身份编号
     * @return 生日(dd)
     */
    public static Short getDateByIdCard(String idCard) {
        String idCardTemp = idCard;
        if (idCardTemp == null) {
            return null;
        }
        Integer len = idCardTemp.length();
        if (len < CHINA_ID_MIN_LENGTH) {
            return null;
        } else if (len == CHINA_ID_MIN_LENGTH) {
            idCardTemp = convertCard15To18(idCardTemp);
        }
        if (idCardTemp == null) {
            return null;
        }
        return Short.valueOf(idCardTemp.substring(12, 14));
    }

    /**
     * 根据身份编号获取性别
     *
     * @param idCard 身份编号
     * @return 性别(M - 男, F - 女, U - 不详)
     */
    public static String getGenderByIdCard(String idCard) {
        String idCardTemp = idCard;
        String sGender = "U";
        if (idCardTemp == null) {
            return sGender;
        }
        // 场景一：台湾
        if (Arrays.binarySearch(new long[] { 8, 9, 10 }, idCardTemp.length()) >= 0) {
            if (idCardTemp.matches("^[a-zA-Z][0-9]{9}$")) {
                String char2 = idCardTemp.substring(1, 2);
                if ("1".equals(char2)) {
                    sGender = MALE;
                } else if ("2".equals(char2)) {
                    sGender = FEMALE;
                }
            }
            return sGender;
        }

        // 场景二：内陆
        if (idCardTemp.length() == CHINA_ID_MIN_LENGTH) {
            idCardTemp = convertCard15To18(idCardTemp);
        }
        if (idCardTemp != null && idCardTemp.length() >= 18) {
            String sCardNum = idCardTemp.substring(16, 17);
            if (Integer.parseInt(sCardNum) % 2 != 0) {
                sGender = MALE;
            } else {
                sGender = FEMALE;
            }
        }
        return sGender;
    }

    /**
     * 根据身份编号获取户籍省份
     *
     * @param idCard 身份编码
     * @return 省级编码。
     */
    public static String getProvinceByIdCard(String idCard) {
        if (idCard == null) {
            return null;
        }
        int len = idCard.length();
        String provinceNum = "";
        if (len == CHINA_ID_MIN_LENGTH || len == CHINA_ID_MAX_LENGTH) {
            provinceNum = idCard.substring(0, 2);
        }
        return CITY_CODE.get(provinceNum);
    }

    /**
     * 验证字符串是否为数字
     *
     * @param val 字符
     * @return true：是数字；false：不是数字
     */
    public static boolean isNum(String val) {
        return !(val == null || "".equals(val)) && val.matches("^[0-9]*$");
    }

}
