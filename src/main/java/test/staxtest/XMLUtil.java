package test.staxtest;

public class XMLUtil {

    public static boolean isInValid(char c) {
        return ((c >= 0x00 && c <= 0x08) ||
                (c >= 0x0b && c <= 0x0c) ||
                (c >= 0x0e && c <= 0x1F));
    }
}
