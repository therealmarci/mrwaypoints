package at.marcelr.mrwaypoints.util;

/** Slot layouts of the menus - symmetric, without Bukkit so they can be tested. */
public final class Layout {

    private Layout() {
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
