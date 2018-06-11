package chip8;

public class Chip8 {

    static byte[] mem;
    static short pc;
    static byte sp;
    static byte[] registers; //Vx
    static byte delayRegister;
    static byte timeRegister;
    static short I;
    static short[] stack;
    static boolean disp;

    public static void main(String[] args) {
        init();
    }

    private static void init() {
        mem = new byte[4096];
        pc = 512;
        registers = new byte[16];
        delayRegister = 0;
        timeRegister = 0;
        stack = new short[16];
        sp = 0;
        I = 0;
        disp = new boolean[64 * 32];
    }
}
