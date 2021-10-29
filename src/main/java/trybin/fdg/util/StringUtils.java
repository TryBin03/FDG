package trybin.fdg.util;

import lombok.extern.slf4j.Slf4j;
import trybin.fdg.constant.Constant;

import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import static org.apache.commons.lang.StringUtils.leftPad;

@Slf4j
public class StringUtils {

    private static final String DEFAULT_CHARSET = "UTF-8";

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean numFlg = false;
        boolean upperFlg = false;
        boolean lowerFlg = false;
        boolean markFlg = false;
        for (int i = 0;i < password.length(); i++) {
            char ch = password.charAt(i);
            if (String.valueOf(ch).matches("[0-9]")) {
                numFlg = true;
            } else if (String.valueOf(ch).matches("[A-Z]")) {
                upperFlg = true;
            } else if (String.valueOf(ch).matches("[a-z]")) {
                lowerFlg = true;
            } else if (String.valueOf(ch).matches("[!-.:-@\\[-`{-~]")) {
                markFlg = true;
            }
        }
        return numFlg & upperFlg & lowerFlg & markFlg;
    }

    public static boolean isEmpty(String str) {
        return (str == null) || (str.length() == 0);
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static String escapeJSON(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        StringBuilder jsonEscape = new StringBuilder();
        for (int i = 0;i < json.length(); i++) {
            char character = json.charAt(i);
            switch (character){
                case '<':
                    jsonEscape.append("&lt;");
                    break;
                case '>':
                    jsonEscape.append("&gt;");
                    break;
                case '&':
                    jsonEscape.append("&amp;");
                    break;
                case '\'':
                    jsonEscape.append("&#39;");
                    break;
                default:
                    jsonEscape.append(character);
            }
        }
        return jsonEscape.toString();
    }

    public static String cutString(String str, int cutLen) {
        return cutString(str, cutLen, DEFAULT_CHARSET);
    }

    public static String cutString(String str, int cutLen, String charset) {
        StringBuffer buffer = new StringBuffer();
        try {
            int len = 0;
            for (int i = 0; i < str.length(); i++) {
                Character c = str.charAt(i);
                Charset cs=Charset.forName(charset);
                int byteLen = cs.encode(CharBuffer.wrap(new char[]{c})).limit();
                len += byteLen;
                if (len > cutLen) {
                    break;
                }
                buffer.append(c);
            }
        } catch (UnsupportedCharsetException e) {
            log.error(e.getMessage(), e);
            return str;
        }
        return buffer.toString();
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        String fileExtension = Constant.STRING_EMPTY;
        int fileDotIdx = fileName.lastIndexOf(".");
        if (fileDotIdx > -1) {
            fileExtension = fileName.substring(fileDotIdx + 1);
        }
        return fileExtension;
    }

    public static String formatString(String mark, String sample, String value) {
        if (value == null || value.isEmpty() || mark == null || sample == null) {
            return value;
        }
        int markLen = mark.length();
        int count = 0;
        int index = 0;
        while((index = sample.indexOf(mark,index))!=-1)
        {
            index = index + markLen;
            count++;
        }
        int sampleLen = sample.length();
        int valLen = value.length();
        if (markLen != 1 || count != valLen) {
            return value;
        }
        StringBuffer sb = new StringBuffer();
        int j = 0;
        for (int i =0;i < sampleLen;i++) {
            char ch = sample.charAt(i);
            if (mark.equals(String.valueOf(ch))) {
                sb.append(value.charAt(j));
                j++;
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String formatOrgCodeString(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        String fmtMark = "#";
        String fmtSample = Constant.STRING_EMPTY;
        if (value.length() == 3) {
            fmtSample = "0003###";
        } else if (value.length() == 4) {
            fmtSample = "000####";
        } else {
            return value;
        }
        return StringUtils.formatString(fmtMark, fmtSample, value);
    }

    public static boolean orgCodeEquals(String orgCode1, String orgCode2) {
        if (isEmpty(orgCode1) || isEmpty(orgCode2)) {
            return false;
        }
        int len1 = orgCode1.length();
        int len2 = orgCode2.length();
        if (len1 == len2) {
            return orgCode1.equals(orgCode2);
        } else {
            return paddingOrgCode(orgCode1).equals(paddingOrgCode(orgCode2));
        }
    }

    public static String paddingOrgCode(String codeStr) {
        if (isNotEmpty(codeStr)) {
            int len = codeStr.length();
            if (len == 3) {
                codeStr = "0003" + codeStr;
            } else if (len == 4) {
                codeStr = leftPad(codeStr, 7, "0");
            } else if (len < 7) {
                codeStr = leftPad(codeStr, 7, "0");
            }
            return codeStr;
        }
        return codeStr;
    }

    public static String getCodeBySplitColon(String codeStr, boolean reverseFlg) {
        if (isNotEmpty(codeStr)) {
            if (reverseFlg) {
                int index = codeStr.lastIndexOf(Constant.SPLIT_COLON);
                if (index == -1) {
                    index = codeStr.lastIndexOf(Constant.SPLIT_FULL_COLON);
                    if (index == -1) {
                        return codeStr;
                    }
                }
                return codeStr.substring(index + 1);
            } else {
                int index = codeStr.indexOf(Constant.SPLIT_COLON);
                if (index == -1) {
                    index = codeStr.indexOf(Constant.SPLIT_FULL_COLON);
                    if (index == -1) {
                        return codeStr;
                    }
                }
                return codeStr.substring(0, index);
            }
        }
        return codeStr;
    }

    public static String getCodeBySplitColon(String codeStr) {
        return getCodeBySplitColon(codeStr, false);
    }

    public static boolean isNumber(String str) {
        if (isEmpty(str)) {
            return false;
        }
        boolean flg = isNumberWithComma(str);
        if (flg) {
            return true;
        } else {
            return str.matches("^(-)?\\d+(\\.\\d+)?$");
        }
    }

    public static boolean isNumberWithComma(String str) {
        if (isEmpty(str)) {
            return false;
        }
        if (str.indexOf(Constant.SPLIT_COMMA) == -1) {
            return false;
        } else {
            return str.matches("^(-)?\\d{1,3}(,\\d{3})*(\\.\\d+)?$");
        }
    }

    public static String trimNumberComma(String str) {
        if (isNumberWithComma(str)) {
            return str.replaceAll(Constant.SPLIT_COMMA, Constant.STRING_EMPTY);
        } else {
            return str;
        }
    }

    public static int getDBLength(String str) throws UnsupportedEncodingException {
        if (isEmpty(str)) {
            return 0;
        }
        return str.getBytes(DEFAULT_CHARSET).length;
    }

    public static final String lowerCamelCase(String str) {
        str = str.toLowerCase();
        String result = "";
        String[] names = str.split("_");
        for (int i = 0; i < names.length; i++) {
            if (i == 0) {
                result += names[i].substring(0, 1).toLowerCase() + names[i].substring(1);
            } else {
                result += names[i].substring(0, 1).toUpperCase() + names[i].substring(1);
            }
        }
        return result;
    }
}
