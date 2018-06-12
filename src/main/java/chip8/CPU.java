package chip8;

class CPU
{
    private final OpcodeInterpreter opi;
    final Memory mem;
    final Display display;
    short pc;
    short I;
    private short[] stack;
    private byte sp;
    byte[] register;
    byte dT;
    byte sT;

     CPU(Memory mem) {
        this.display = new Display();
        this.mem = mem;
        this.opi = new OpcodeInterpreter(this);
        this.register = new byte[16];
        this.stack = new short[16];
        this.I = 0;                  // Memory address store
        this.dT = 0;                 // -----------delay and sound timers
        this.sT = 0;                 // ----------^
        sp = 0;                      // stack pointer
        pc = 512;                    // program counter, starts at 512 for ROM
    }
    
    void cycle() {
        opi.dispatchCode(mem.readInstruction(pc));
        pc += 2;
    }

    void jumpTo(int addr) {
        pc = (short) addr;
	}

    void subroutine(int addr) {
        stack[sp++] = pc;
        pc = (short) addr;
	}

    void returnSub() {
        pc = stack[--sp];
	}

    void setIDigit(int IDigit) {
        I = mem.getAddressFor(IDigit);
    }
}
