package util;

import java.util.regex.Pattern;

public class inputParser {

    private static final int SMALLEST_PORT = 1025;
    private static final int LARGEST_PORT = 65535;
    private static final String DEFAULT_IP = "localhost";

    private static final Pattern IPv4 = Pattern.compile("^((25[0-5]|(2[0-4]|1[0-9]|[1-9]|)[0-9])(\\.(?!$)|$)){4}$");

    public static boolean isValidPort(String port) {
        try {
            int port_int = Integer.parseInt(port);

            if (port_int <= LARGEST_PORT && port_int >= SMALLEST_PORT)
                return true;
            else {
                return false;
            }
        } catch(Exception e){
            return false;
        }
    }

    public static boolean isValidIP(String ip) {
        if (ip.equals(DEFAULT_IP))
            return true;

        return IPv4.matcher(ip).matches();
    }
}




