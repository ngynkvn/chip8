package chip8;

class CPU
{
    private final OpcodeInterpreter opi;
    final Memory mem;
    final Graphics graphics;
    private long cycle;
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
        this.cycle = 0;
        this.I = 0;                  // Memory address store
        this.dT = 0;                 // -----------delay and sound timers
        this.sT = 0;                 // ----------^
        sp = 0;                      // stack pointer
        pc = 512;                    // program counter, starts at 512 for ROM
    }
    
    void cycle() {
        opi.dispatchCode(mem.readInstruction(pc));
        pc += 2;
        cycle++;
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
