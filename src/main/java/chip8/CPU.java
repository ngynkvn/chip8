package chip8;

class CPU
{
    private final OpcodeInterpreter opi;
    private final Memory mem;
    short pc;
    private short I;
    private short[] stack;
    private byte sp;
    byte[] register;
    private byte dT;
    private byte pT;

     CPU(Memory mem) {
        this.opi = new OpcodeInterpreter(this);
        this.mem = mem;
        this.register = new byte[16];
        this.stack = new short[16];
        this.I = 0;
        this.dT = 0;
        this.pT = 0;
        sp = 0;
        pc = 512; //start at 512 for ROM
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
}
