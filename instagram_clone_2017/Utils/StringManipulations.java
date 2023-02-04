package com.example.instagram_clone_2017.Utils;

public class StringManipulations {
    private static final String TAG = "STRING_MANIPULATION";
    public static String getTags(String string)
    {
        if (string.indexOf("#") > 0)
        {
            StringBuilder sb = new StringBuilder();
            char[] charArray = string.toCharArray();
            boolean foundWords = false;
            for (char c : charArray)
            {
                if (c == '#')
                {
                    foundWords = true;
                    sb.append(c);
                }
                else
                {
                    if (foundWords)
                    {
                        sb.append(c);
                    }
                }
                if (c == ' ')
                {
                    foundWords = false;
                }
            }
            String s = sb.toString().replace(" ", "").replace("#", ",#");
            return s.substring(1, s.length());
        }
        return string;
    }
}
