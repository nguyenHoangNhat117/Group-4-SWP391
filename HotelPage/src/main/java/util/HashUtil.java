package util;

/**
 * One shared MD5 UTF-8 implementation for all password flows.
 */
public final class HashUtil {

    private HashUtil() {
    }

    public static String toMD5(String input) {
        return PasswordUtil.hash(input);
    }
}
