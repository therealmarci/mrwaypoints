package at.marcelr.mrwaypoints.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Reading and writing plugin messages in Minecraft's buffer format (VarInt, VarInt-prefixed UTF-8 strings,
 * big-endian numbers) - the mod reads and writes them with its FriendlyByteBuf.
 */
public final class Wire {

    private static final int MAX_STRING_BYTES = 32_767;

    private Wire() {
    }

    public static final class Writer {
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private final DataOutputStream out = new DataOutputStream(bytes);

        public Writer varInt(int value) {
            try {
                while ((value & ~0x7F) != 0) {
                    out.writeByte((value & 0x7F) | 0x80);
                    value >>>= 7;
                }
                out.writeByte(value);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            return this;
        }

        public Writer string(String value) {
            byte[] utf8 = value.getBytes(StandardCharsets.UTF_8);
            varInt(utf8.length);
            bytes.writeBytes(utf8);
            return this;
        }

        public Writer bool(boolean value) {
            bytes.write(value ? 1 : 0);
            return this;
        }

        public Writer shortValue(int value) {
            try {
                out.writeShort(value);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            return this;
        }

        public Writer intValue(int value) {
            try {
                out.writeInt(value);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            return this;
        }

        public byte[] bytes() {
            return bytes.toByteArray();
        }
    }

    public static final class Reader {
        private final DataInputStream in;

        public Reader(byte[] data) {
            this.in = new DataInputStream(new ByteArrayInputStream(data));
        }

        public int varInt() throws IOException {
            int value = 0;
            for (int shift = 0; shift < 35; shift += 7) {
                int b = in.readUnsignedByte();
                value |= (b & 0x7F) << shift;
                if ((b & 0x80) == 0) {
                    return value;
                }
            }
            throw new IOException("VarInt too long");
        }

        public String string() throws IOException {
            int length = varInt();
            if (length < 0 || length > MAX_STRING_BYTES) {
                throw new IOException("String too long: " + length);
            }
            byte[] utf8 = new byte[length];
            in.readFully(utf8);
            return new String(utf8, StandardCharsets.UTF_8);
        }

        public int unsignedByte() throws IOException {
            return in.readUnsignedByte();
        }

        public boolean bool() throws IOException {
            return in.readUnsignedByte() != 0;
        }

        /** The rest is optional (fields added in later versions of a message) - false if nothing is left. */
        public boolean hasMore() throws IOException {
            return in.available() > 0;
        }
    }
}
