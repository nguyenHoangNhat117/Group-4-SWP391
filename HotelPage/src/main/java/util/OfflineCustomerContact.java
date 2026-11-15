package util;

public final class OfflineCustomerContact {
    private OfflineCustomerContact() { }

    public static String phone(String value) {
        return AuthSupport.text(value).replaceAll("[\\s\\p{Z}()\\-]", "");
    }

    public static boolean validPhone(String value) {
        return phone(value).matches("[+]?[0-9]{7,15}");
    }

    public static String phoneSql(String column) {
        if (!"c.Phone".equals(column) && !"Phone".equals(column)) {
            throw new IllegalArgumentException("Unexpected phone column");
        }
        String expression="COALESCE("+column+",N'')";
        for(String part:new String[]{"N' '","CHAR(9)","CHAR(10)","CHAR(13)","NCHAR(160)","N'('","N')'","N'-'"}){
            expression="REPLACE("+expression+","+part+",N'')";
        }
        return expression;
    }
}
