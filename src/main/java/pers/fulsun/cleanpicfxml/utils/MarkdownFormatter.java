package pers.fulsun.cleanpicfxml.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MarkdownFormatter {
    // Regex to match image links with space after ( or before )
    private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[\\]\\(\\s*(.*?)\\s*\\)");

    /**
     * Fix image syntax spacing in a markdown string
     */
    public static String fixImageLinks(String markdown) {
        Matcher matcher = IMAGE_PATTERN.matcher(markdown);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String path = matcher.group(1).trim(); // remove spaces inside ()
            matcher.appendReplacement(sb, "![](" + path + ")");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


}