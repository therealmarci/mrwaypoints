package at.marcelr.mrwaypoints.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Menus are symmetric; the guide arrow follows the look direction (yaw 0 = south, 90 = west). */
class LayoutTest {

    @Test
    void actionsSymmetric() {
        assertArrayEquals(new int[]{13}, Layout.row(1));
        assertArrayEquals(new int[]{11, 15}, Layout.row(2));
        assertArrayEquals(new int[]{11, 12, 13, 14, 15}, Layout.row(5));
    }

    @Test
    void arrowAheadBehindAndSides() {
        // Looking south: south is ahead, north behind, west on the right, east on the left.
        assertEquals(0, Layout.arrowIndex(0, 0, 10));
        assertEquals(4, Layout.arrowIndex(0, 0, -10));
        assertEquals(2, Layout.arrowIndex(0, -10, 0));
        assertEquals(6, Layout.arrowIndex(0, 10, 0));
    }

    @Test
    void arrowFollowsTheLookDirection() {
        // Looking west (yaw 90): west is ahead, south on the left.
        assertEquals(0, Layout.arrowIndex(90, -10, 0));
        assertEquals(6, Layout.arrowIndex(90, 0, 10));
        // Looking north with a yaw outside -180..180.
        assertEquals(0, Layout.arrowIndex(540, 0, -10));
        // Looking south, south-west is ahead right.
        assertEquals(1, Layout.arrowIndex(0, -10, 10));
        assertEquals(7, Layout.arrowIndex(0, 10, 10));
    }
}
