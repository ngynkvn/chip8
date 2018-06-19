package chip8;

import java.util.Random;
import java.util.function.Predicate;

class OpcodeInterpreter
{
    private CPU cpu;
    private Memory mem;
    private Graphics graphics;
    private Random rand;

    OpcodeInterpreter(CPU cpu) {
        this.cpu = cpu;
        this.mem = cpu.mem;
        this.graphics = cpu.graphics;
        this.rand = new Random();
    }

    void dispatchCode(short code) {
        final int firstNibble = code & 0xF000;
        final int lastNibble = code & 0x000F;
        final int address = code & 0x0FFF;
        final int x = (code & 0x0F00) >> 8;
        final int y = (code & 0x00F0) >> 4;
        final int kk = code & 0x0FF;
        switch (firstNibble) {
            case 0x0000:
                if (code == 0x0E0)
                    graphics.clear();
                else
                    cpu.returnSub();
                break;
            case 0x1000:
                cpu.jumpTo(address);
                break;
            case 0x2000:
                cpu.subroutine(address);
                break;
            case 0x3000:
                skipIf(cpu.register[x] == kk);
                break;
            case 0x4000:
                skipIf(cpu.register[x] != kk);
                break;
            case 0x5000:
                skipIf(cpu.register[x] == kk);
                break;
            case 0x6000:
                cpu.register[x] = kk;
                break;
            case 0x7000:
                cpu.register[x] += kk;
                correctOverUnderflow(x);
                break;
            case 0x8000:
                switch (lastNibble) {
                    case 0x000:
                        cpu.register[x] = cpu.register[y];
                        break;
                    case 0x001:
                        cpu.register[x] |= cpu.register[y];
                        break;
                    case 0x002:
                        cpu.register[x] &= cpu.register[y];
                        break;
                    case 0x003:
                        cpu.register[x] ^= cpu.register[y];
                        break;
                    case 0x004:
                        ifSetVF((cpu.register[x] + cpu.register[y]) > 255);
                        cpu.register[x] += cpu.register[y];
                        correctOverUnderflow(x);
                        break;
                    case 0x005:
                        ifSetVF(cpu.register[x] >= cpu.register[y]);
                        cpu.register[x] -= cpu.register[y];
                        correctOverUnderflow(x);
                        break;
                    case 0x006:
                        ifSetVF((cpu.register[x] & 1) == 1);
                        cpu.register[x] >>= 1;
                        correctOverUnderflow(x);
                        break;
                    case 0x007:
                        ifSetVF(cpu.register[y] >= cpu.register[x]);
                        cpu.register[x] = (cpu.register[y] - cpu.register[x]);
                        correctOverUnderflow(x);
                        break;
                    case 0x00E:
                        ifSetVF((cpu.register[x] >>> 7) == 1);
                        cpu.register[x] <<= 1;
                        correctOverUnderflow(x);
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("Encountered unknown opcode: %X", code));
                }
                break;
            case 0x9000:
                skipIf(cpu.register[x] != cpu.register[y]);
                break;
            case 0xA000:
                cpu.I = (short) address;
                break;
            case 0xB000:
                cpu.pc = (short) (address + cpu.register[0]);
                break;
            case 0xC000:
                cpu.register[x] = (rand.nextInt(256) & kk);
                break;
            case 0xD000:
                // DRAW x, y, n
                ifSetVF(graphics.draw(cpu.register[x], cpu.register[y], lastNibble, cpu.I, mem));
                break;
            case 0xE000:
                switch (lastNibble) {
                    case 0x000E:
                        skipIf(KeyboardInput::keyIsDown, cpu.register[x]);
                        break;
                    case 0x0001:
                        skipIf(KeyboardInput::keyIsReleased, cpu.register[x]);
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("Encountered unknown opcode: %X", code));
                }
                break;
            case 0xF000:
                final int lastByte = code & 0x00FF;
                switch (lastByte) {
                    case 0x0007:
                        cpu.register[x] = cpu.dT;
                        break;
                    case 0x000A:
                        if (!KeyboardInput.anyKeyDown()) {
                            cpu.pc -= 2;
                        }
                        break;
                    case 0x0015:
                        cpu.dT = (byte)cpu.register[x];
                        break;
                    case 0x0018:
                        cpu.sT = cpu.register[x];
                        break;
                    case 0x001E:
                        cpu.I += cpu.register[x];
                        break;
                    case 0x0029:
                        //set I to digit Vx
                        cpu.setIDigit(cpu.register[x]);
                        break;
                    case 0x0033:
                        //BCD
                        int num = cpu.register[x];
                        byte hund = (byte)(num / 100);
                        byte ten = (byte) (num % 100 / 10);
                        byte one = (byte) (num % 10);
                        mem.writeToMem(cpu.I, hund);
                        mem.writeToMem(cpu.I + 1, ten);
                        mem.writeToMem(cpu.I + 2, one);
                        break;
                    case 0x0055:
                        for (int i = 0; i <= x; i++) {
                            mem.writeToMem(cpu.I + i, (byte)cpu.register[i]);
                        }
                        break;
                    case 0x0065:
                        for (int i = 0; i <= x; i++) {
                            cpu.register[i] = Byte.toUnsignedInt(mem.readByte(cpu.I + i));
                        }
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("Encountered unknown opcode: %X", code));
                }
                break;
            default:
                throw new IllegalArgumentException(String.format("Encountered unknown opcode: %X", code));
        }
    }

    private void correctOverUnderflow(int x) {
        cpu.register[x] = Byte.toUnsignedInt((byte)cpu.register[x]);
    }

    private void skipIf(boolean predicate) {
        if (predicate) {
            cpu.pc += 2;
        }
    }

    private void skipIf(Predicate<Integer> f, int a) {
        if (f.test(a)) {
            cpu.pc += 2;
        }
    }
    private void ifSetVF(boolean predicate) {
        cpu.register[0x00F] = predicate ? (byte) 1 : (byte) 0;
    }
}