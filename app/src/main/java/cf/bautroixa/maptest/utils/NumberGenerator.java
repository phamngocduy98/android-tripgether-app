package cf.bautroixa.maptest.utils;

import java.util.Random;

public class NumberGenerator {
    public static String generateNumberString(int length) {
        StringBuilder result = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            result.append(random.nextInt(10));
        }
        return result.toString();
    }
}
