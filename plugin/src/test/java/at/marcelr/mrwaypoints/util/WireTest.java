package at.marcelr.mrwaypoints.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The bytes must be exactly what the mod's FriendlyByteBuf reads (VarInt, VarInt-prefixed UTF-8, big-endian int). */
class WireTest {

    @Test
    void sameBytesAsFriendlyByteBuf() {
        byte[] bytes = new Wire.Writer().varInt(300).string("Shop").intValue(-60).bool(true).bytes();
        assertArrayEquals(new byte[]{
                (byte) 0xAC, 0x02,                      // VarInt 300
                4, 'S', 'h', 'o', 'p',                  // String
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xC4, // int -60
                1}, bytes);                             // boolean
    }

    @Test
    void readsWhatItWrites() throws IOException {
        byte[] bytes = new Wire.Writer().string("Sehenswürdigkeiten").bool(false).bytes();
        Wire.Reader in = new Wire.Reader(bytes);
        assertEquals("Sehenswürdigkeiten", in.string());
        assertFalse(in.bool());
        assertFalse(in.hasMore());
        assertTrue(new Wire.Reader(new Wire.Writer().bool(true).bytes()).bool());
    }
}
