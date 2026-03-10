package mindurka;

import arc.struct.ByteSeq;
import arc.util.Log;

import java.nio.ByteBuffer;

public class Util {
    private Util() {}

    public static Object[] noargs = new Object[0];

    public static boolean enabled(byte bitfield, int field) {
        if (field < 0 || field > 7) return false;
        return (((int) bitfield & 0xff) >> field & 1) == 1;
    }
    public static boolean disabled(byte bitfield, int field) {
        if (field < 0 || field > 7) return false;
        return (((int) bitfield & 0xff) >> field & 1) == 0;
    }
    public static byte enable(byte bitfield, int field) {
        if (field < 0 || field > 7) return bitfield;
        int modded = bitfield & 0xff;
        modded |= 1 << field;
        return (byte) modded;
    }
    public static byte disable(byte bitfield, int field) {
        if (field < 0 || field > 7) return bitfield;
        int modded = bitfield & 0xff;
        modded &= ~(1 << field);
        return (byte) modded;
    }
    public static byte toggle(byte bitfield, int field) {
        if (field < 0 || field > 7) return bitfield;
        int modded = bitfield & 0xff;
        modded ^= (1 << field);
        return (byte) modded;
    }

    public static long packxy(int x, int y) {
        long x$1 = x & 0xffffffffL;
        long y$1 = y & 0xffffffffL;
        return x$1 << 32 | y$1;
    }
    public static int unpackx(long p) {
        return (int) (p >> 32);
    }
    public static int unpacky(long p) {
        return (int) (p & 0xffffffffL);
    }

    public static int mod(int x, int a) {
        x = x % a;
        if (x < 0) x = x + a;
        return x;
    }

    public interface Yeet<T> {
        T run() throws Exception;
    }
    public static <T> T yeet(Yeet<T> fn) {
        try {
            return fn.run();
        } catch (Exception e) {
            throw new RuntimeException("Yeet!", e);
        }
    }

    public interface YeetV {
        void run() throws Exception;
    }
    public static void yeet(YeetV fn) {
        try {
            fn.run();
        } catch (Exception e) {
            throw new RuntimeException("Yeet!", e);
        }
    }

    public static void writeShort(ByteSeq a, short value) {
        a.add((byte) (value / 256));
        a.add((byte) (value % 256));
    }
    public static short readShort(ByteSeq a, int pos) {
        if (pos + 1 >= a.size) throw new ArrayIndexOutOfBoundsException("Cannot read short number! (" + (pos + 1) + " >= " + a.size + ")");
        int most = a.get(pos) * 256;
        int least = a.get(pos + 1) & 0xff;

        return (short) (most >= 0 ? most + least : most - least);
    }

    private static final ByteBuffer intBuffer = ByteBuffer.allocate(4);
    public static void writeInt(ByteSeq a, int value) {
        intBuffer.position(0);
        intBuffer.putInt(value);
        a.addAll(intBuffer.array());
    }
    public static int readInt(ByteSeq a, int pos) {
        intBuffer.position(0);
        intBuffer.put(a.items, pos, 2);
        return intBuffer.getInt(0);
    }
}
