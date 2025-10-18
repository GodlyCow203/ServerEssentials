package serveressentials.serveressentials.util;

import java.util.Random;

public class GradientUtil {

    public static String gradient(String text, int[] start, int[] end) {
        StringBuilder out = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (length - 1);

            int r = (int) (start[0] + ratio * (end[0] - start[0]));
            int g = (int) (start[1] + ratio * (end[1] - start[1]));
            int b = (int) (start[2] + ratio * (end[2] - start[2]));

            out.append("\u001B[38;2;")
                    .append(r).append(";")
                    .append(g).append(";")
                    .append(b).append("m")
                    .append(text.charAt(i));
        }

        out.append("\u001B[0m");
        return out.toString();
    }

    public static String gradient(String text, int[][] colors) {
        StringBuilder out = new StringBuilder();
        int segments = colors.length - 1;
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (length - 1);
            int seg = Math.min((int) (ratio * segments), segments - 1);
            float segRatio = (ratio * segments) - seg;

            int[] start = colors[seg];
            int[] end = colors[seg + 1];

            int r = (int) (start[0] + segRatio * (end[0] - start[0]));
            int g = (int) (start[1] + segRatio * (end[1] - start[1]));
            int b = (int) (start[2] + segRatio * (end[2] - start[2]));

            out.append("\u001B[38;2;")
                    .append(r).append(";")
                    .append(g).append(";")
                    .append(b).append("m")
                    .append(text.charAt(i));
        }

        out.append("\u001B[0m");
        return out.toString();
    }

    // 🔥 Random gradient generator
    public static int[][] randomGradient() {
        Random rand = new Random();
        int choice = rand.nextInt(5); // pick from 5 presets

        switch (choice) {
            case 0: // Galaxy
                return new int[][] {
                        {138, 43, 226}, {30, 144, 255}, {255, 105, 180}, {138, 43, 226}
                };
            case 1: // Lava
                return new int[][] {
                        {255, 0, 0}, {255, 140, 0}, {255, 215, 0}, {255, 140, 0}, {255, 0, 0}
                };
            case 2: // Ice
                return new int[][] {
                        {0, 255, 255}, {0, 191, 255}, {240, 248, 255}, {0, 255, 255}
                };
            case 3: // Neon
                return new int[][] {
                        {0, 255, 255}, {255, 0, 255}, {0, 0, 0}, {0, 255, 255}
                };
            case 4: // Rainbow
            default:
                return new int[][] {
                        {255, 0, 0}, {255, 127, 0}, {255, 255, 0},
                        {0, 255, 0}, {0, 0, 255}, {75, 0, 130}, {148, 0, 211}
                };
        }
    }
}
