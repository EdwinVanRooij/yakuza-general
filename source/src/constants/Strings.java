package constants;

/**
 * @author Edwin
 *         Created on 4/11/2016
 */
public class Strings {

    public static String get(String message, String... strings) {
        return String.format(message, (Object) strings);
    }

//    Client

    //    Commands
    public static final String SUB_PLAYER = "@";
    public static final String SUB_DONOR = "$";
    public static final String SUB_GM = "!";
    public static final String SUB_ADMIN = "#";


    public static final String EX_INCORRECT_SUB_HEADING = "You may not use %s.";
}
