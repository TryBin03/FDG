package trybin.fdg.util;

import java.util.List;

public class StringUtil {
    public static String replace(String str, List<String> values){
        for (int i = 0; i < values.size(); i++) {
            str = str.replace("${REP"+ i +"}",values.get(i));
        }
        return str;
    }
}
