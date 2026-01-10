package mindurka;

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
}
