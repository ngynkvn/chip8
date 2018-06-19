package chip8;

class CPU
{
    private long lastTime;
    private long accrual;
    private final OpcodeInterpreter opi;
    final Memory mem;
    final Graphics graphics;
    short pc;
    int I;
    private short[] stack;
    private byte sp;
    int[] register;
    byte dT;
    int sT;

    CPU(Memory mem, Graphics disp) {
        this.graphics = disp;
        this.mem = mem;
        this.opi = new OpcodeInterpreter(this);
        this.register = new int[16];
        this.stack = new short[16];
        this.I = 0;                  // Memory address store
        this.dT = 0;                 // -----------delay and sound timers
        this.sT = 0;                 // ----------^
        this.accrual = 0;
        this.lastTime = getTime();
        sp = 0;                      // stack pointer
        pc = 512;                    // program counter, starts at 512 for ROM
    }

    void timeClocks() {
        this.accrual += deltaT();
        if (this.accrual >= 17) {
            this.dT -= 1;
            this.sT -= 1;
            this.accrual -= 17;
            if (this.dT < 0)
                this.dT = 0;
            if (this.sT < 0)
                this.sT = 0;
        }
    }

    private long deltaT() {
        long time = getTime();
        long delta = time - lastTime;
        lastTime = time;
        return delta;
    }

    private long getTime() {
        return System.nanoTime() / 1_000_000;
    }

    void cycle() {
        opi.dispatchCode(mem.readInstruction(pc));
        pc += 2;
    }

    void jumpTo(int addr) {
        pc = (short) addr;
        pc -= 2;
    }

    void subroutine(int addr) {
        stack[sp++] = pc;
        pc = (short) addr;
        pc -= 2;
    }

    void returnSub() {
        pc = stack[--sp];
    }

    void setIDigit(int IDigit) {
        I = mem.getAddressFor(IDigit);
    }
}
