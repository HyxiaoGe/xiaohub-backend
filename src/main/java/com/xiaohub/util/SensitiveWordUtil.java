package com.xiaohub.util;

import com.github.houbb.sensitive.word.core.SensitiveWordHelper;

public class SensitiveWordUtil {

    /**
     * 是否包含敏感词
     * @param word
     * @return
     */
    public static boolean isSensitiveWord(String word) {
        return SensitiveWordHelper.contains(word);
    }

}
