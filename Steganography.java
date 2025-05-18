import java.awt.*;
import java.util.ArrayList;

public class Steganography {

    public static void clearLow(Pixel p) {
        p.setRed((p.getRed() / 4) * 4);
        p.setGreen((p.getGreen() / 4) * 4);
        p.setBlue((p.getBlue() / 4) * 4);
    }

    public static Picture testClearLow(Picture pic) {
        Picture copy = new Picture(pic);
        Pixel[][] pixels = copy.getPixels2D();
        for (Pixel[] row : pixels) {
            for (Pixel p : row) {
                clearLow(p);
            }
        }
        return copy;
    }

    public static void setLow(Pixel p, Color c) {
        clearLow(p);
        int red = p.getRed() + (c.getRed() / 64);
        int green = p.getGreen() + (c.getGreen() / 64);
        int blue = p.getBlue() + (c.getBlue() / 64);
        p.setColor(new Color(red, green, blue));
    }

    public static Picture testSetLow(Picture pic, Color c) {
        Picture copy = new Picture(pic);
        Pixel[][] pixels = copy.getPixels2D();
        for (Pixel[] row : pixels) {
            for (Pixel p : row) {
                setLow(p, c);
            }
        }
        return copy;
    }

    public static Picture revealPicture(Picture hidden) {
        Picture copy = new Picture(hidden);
        Pixel[][] pixels = copy.getPixels2D();
        Pixel[][] source = hidden.getPixels2D();

        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                Color col = source[r][c].getColor();

                int red = (col.getRed() % 4) * 64;
                int green = (col.getGreen() % 4) * 64;
                int blue = (col.getBlue() % 4) * 64;

                pixels[r][c].setColor(new Color(red, green, blue));
            }
        }
        return copy;
    }

    public static boolean canHide(Picture source, Picture secret) {
        return source.getWidth() >= secret.getWidth() && source.getHeight() >= secret.getHeight();
    }

    public static Picture hidePicture(Picture source, Picture secret) {
        Picture copy = new Picture(source);
        Pixel[][] sourcePixels = copy.getPixels2D();
        Pixel[][] secretPixels = secret.getPixels2D();

        for (int r = 0; r < secretPixels.length; r++) {
            for (int c = 0; c < secretPixels[0].length; c++) {
                setLow(sourcePixels[r][c], secretPixels[r][c].getColor());
            }
        }

        return copy;
    }

    public static Picture hidePicture(Picture source, Picture secret, int startRow, int startCol) {
        Picture copy = new Picture(source);
        Pixel[][] sourcePixels = copy.getPixels2D();
        Pixel[][] secretPixels = secret.getPixels2D();

        for (int r = 0; r < secretPixels.length; r++) {
            for (int c = 0; c < secretPixels[0].length; c++) {
                setLow(sourcePixels[startRow + r][startCol + c], secretPixels[r][c].getColor());
            }
        }

        return copy;
    }

    public static boolean isSame(Picture p1, Picture p2) {
        if (p1.getWidth() != p2.getWidth() || p1.getHeight() != p2.getHeight()) return false;

        Pixel[][] p1Pixels = p1.getPixels2D();
        Pixel[][] p2Pixels = p2.getPixels2D();

        for (int r = 0; r < p1Pixels.length; r++) {
            for (int c = 0; c < p1Pixels[0].length; c++) {
                if (!p1Pixels[r][c].getColor().equals(p2Pixels[r][c].getColor())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static ArrayList<Point> findDifferences(Picture p1, Picture p2) {
        ArrayList<Point> differences = new ArrayList<>();
        Pixel[][] a = p1.getPixels2D();
        Pixel[][] b = p2.getPixels2D();

        for (int r = 0; r < a.length; r++) {
            for (int c = 0; c < a[0].length; c++) {
                if (!a[r][c].getColor().equals(b[r][c].getColor())) {
                    differences.add(new Point(c, r));
                }
            }
        }
        return differences;
    }

    public static Picture showDifferentArea(Picture pic, ArrayList<Point> list) {
        if (list.isEmpty()) return pic;

        Picture copy = new Picture(pic);
        Pixel[][] pixels = copy.getPixels2D();

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

        for (Point p : list) {
            minX = Math.min(minX, p.x);
            maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
        }

        for (int x = minX; x <= maxX; x++) {
            pixels[minY][x].setColor(Color.RED);
            pixels[maxY][x].setColor(Color.RED);
        }
        for (int y = minY; y <= maxY; y++) {
            pixels[y][minX].setColor(Color.RED);
            pixels[y][maxX].setColor(Color.RED);
        }

        return copy;
    }

    public static ArrayList<Integer> encodeString(String s) {
        s = s.toUpperCase();
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        ArrayList<Integer> result = new ArrayList<>();

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == ' ') result.add(27);
            else result.add(alpha.indexOf(ch) + 1);
        }

        result.add(0); // end of message
        return result;
    }

    public static String decodeString(ArrayList<Integer> codes) {
        String result = "";
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        for (int val : codes) {
            if (val == 0) break;
            if (val == 27) result += " ";
            else result += alpha.charAt(val - 1);
        }
        return result;
    }

    private static int[] getBitPairs(int num) {
        int[] bits = new int[3];
        for (int i = 0; i < 3; i++) {
            bits[i] = num % 4;
            num /= 4;
        }
        return bits;
    }

    public static Picture hideText(Picture source, String s) {
        Picture copy = new Picture(source);
        Pixel[][] pixels = copy.getPixels2D();
        ArrayList<Integer> codes = encodeString(s);

        int index = 0;
        outer: for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                if (index >= codes.size()) break outer;
                int[] bits = getBitPairs(codes.get(index++));
                Pixel p = pixels[r][c];

                int red = (p.getRed() / 4) * 4 + bits[2];
                int green = (p.getGreen() / 4) * 4 + bits[1];
                int blue = (p.getBlue() / 4) * 4 + bits[0];
                p.setColor(new Color(red, green, blue));
            }
        }

        return copy;
    }

    public static String revealText(Picture source) {
        Pixel[][] pixels = source.getPixels2D();
        ArrayList<Integer> codes = new ArrayList<>();

        for (Pixel[] row : pixels) {
            for (Pixel p : row) {
                int red = p.getRed() % 4;
                int green = p.getGreen() % 4;
                int blue = p.getBlue() % 4;
                int value = red * 16 + green * 4 + blue;
                if (value == 0) return decodeString(codes);
                codes.add(value);
            }
        }

        return decodeString(codes);
    }

    public static void main(String[] args) {
        Picture beach = new Picture("beach.jpg");
        beach.explore();

        Picture cleared = testClearLow(beach);
        cleared.explore();

        Picture pinked = testSetLow(beach, Color.PINK);
        pinked.explore();

        Picture revealedPink = revealPicture(pinked);
        revealedPink.explore();

        Picture arch = new Picture("arch.jpg");
        Picture hidden = hidePicture(beach, arch);
        hidden.explore();

        Picture revealed = revealPicture(hidden);
        revealed.explore();

        Picture textPic = hideText(beach, "HELLO WORLD");
        textPic.explore();

        String message = revealText(textPic);
        System.out.println("Hidden message: " + message);
    }
}
