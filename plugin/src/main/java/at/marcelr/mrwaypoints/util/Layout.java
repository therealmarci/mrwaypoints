package at.marcelr.mrwaypoints.util;

/** Slot layouts of the menus - symmetric, without Bukkit so they can be tested. */
public final class Layout {

    /** The 21 content slots of the main menu: three rows of seven. */
    public static final int[] CONTENT = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

    private Layout() {
    }

    /**
     * The first {@code count} content slots, each row symmetric around its middle: an odd number uses the middle
     * slots, an even number leaves the middle one free (four = two left, two right of it).
     */
    public static int[] centered(int count) {
        int shown = Math.min(count, CONTENT.length);
        int[] slots = new int[shown];
        int i = 0;
        for (int row = 0; row < 3 && i < shown; row++) {
            int inRow = Math.min(7, shown - i);
            int middle = row * 9 + 13;
            int half = inRow / 2;
            for (int k = half; k >= 1; k--) {
                slots[i++] = middle - k;
            }
            if (inRow % 2 == 1) {
                slots[i++] = middle;
            }
            for (int k = 1; k <= half; k++) {
                slots[i++] = middle + k;
            }
        }
        return slots;
    }

    /** Up to five buttons in the middle row of a three-row menu, symmetric around its centre (13). */
    public static int[] row(int count) {
        return switch (count) {
            case 1 -> new int[]{13};
            case 2 -> new int[]{11, 15};
            case 3 -> new int[]{11, 13, 15};
            case 4 -> new int[]{10, 12, 14, 16};
            default -> new int[]{11, 12, 13, 14, 15};
        };
    }

    /** Arrow from where the player looks to the target. */
    private static final String[] ARROWS = {"⬆", "⬈", "➡", "⬊", "⬇", "⬋", "⬅", "⬉"};

    /**
     * Which of eight directions the target lies in, relative to the look direction: 0 = ahead, 2 = right, 4 = behind,
     * 6 = left (Minecraft yaw: 0 = south, 90 = west; turning right raises the yaw).
     */
    public static int arrowIndex(float yaw, double dx, double dz) {
        double target = Math.toDegrees(Math.atan2(-dx, dz));
        double relative = ((target - yaw) % 360 + 540) % 360 - 180;
        return Math.floorMod((int) Math.round(relative / 45), 8);
    }

    public static String arrow(float yaw, double dx, double dz) {
        return ARROWS[arrowIndex(yaw, dx, dz)];
    }
}
