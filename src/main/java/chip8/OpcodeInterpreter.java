package chip8;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

class OpcodeInterpreter
{
    private CPU cpu;
    private Memory mem;
    private Display display;
    private Random rand;

    OpcodeInterpreter(CPU cpu) {
        this.cpu = cpu;
        this.mem = cpu.mem;
        this.display = cpu.display;
        this.rand = new Random();
    }

    void dispatchCode(short code) {
        final int firstNibble = code & 0xF000;
        final int lastNibble = code & 0x000F;
        final int address = code & 0x0FFF;
        final int x = (code & 0x0F00) >> 8;
        final int y = (code & 0x00F0) >> 4;
        final int kk = code & 0x0FF;
        short ptr;
        System.out.printf("%X\n", code);
        switch (firstNibble) {
            case 0x0000:
                if (code == 0x0E0)
                    display.clear();
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
                skipIf(Objects::equals, (int) cpu.register[x], kk);
                break;
            case 0x4000:
                skipIf((a, b) -> !Objects.equals(a, b), (int) cpu.register[x], kk);
                break;
            case 0x5000:
                skipIf(Objects::equals, (int) cpu.register[x], (int) cpu.register[y]);
                break;
            case 0x6000:
                cpu.register[x] = (byte) kk;
                break;
            case 0x7000:
                cpu.register[x] += (byte) kk;
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
                        ifSetVF((i, j) -> Byte.toUnsignedInt(cpu.register[x]) + Byte.toUnsignedInt(cpu.register[y]) > 255,
                                i -> cpu.register[x] += cpu.register[y]);
                        break;
                    case 0x005:
                        ifSetVF((i, j) -> cpu.register[x] > cpu.register[y], i -> cpu.register[x] -= cpu.register[y]);
                        break;
                    case 0x006:
                        ifSetVF((i, j) -> (cpu.register[x] & 1) == 1, i -> cpu.register[x] /= 2);
                        break;
                    case 0x007:
                        ifSetVF((i, j) -> cpu.register[y] > cpu.register[x],
                                i -> cpu.register[x] = (byte) (cpu.register[y] - cpu.register[x]));
                        break;
                    case 0x00E:
                        ifSetVF((i, j) -> (cpu.register[x] >>> 7) == 1, i -> cpu.register[x] *= 2);
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("Encountered unknown opcode: %X", code));
                }
                break;
            case 0x9000:
                skipIf((a, b) -> !Objects.equals(a, b), (int) cpu.register[x], (int) cpu.register[y]);
                break;
            case 0xA000:
                cpu.I = (short) address;
                break;
            case 0xB000:
                cpu.pc = (short) (address + cpu.register[0]);
                break;
            case 0xC000:
                cpu.register[x] = (byte) (rand.nextInt(256) & kk);
                break;
            case 0xD000:
                // DRAW x, y, n
                display.draw(x, y, lastNibble);
                break;
            case 0xE000:
                switch (lastNibble) {
                    case 0x000E:
                        break;
                    case 0x0001:
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
                    case 0x000A:
                        System.out.println(String.format("not implemented! -> %X", code));
                        // wait for keypress
                        break;
                    case 0x0015:
                        cpu.dT = cpu.register[x];
                        break;
                    case 0x0018:
                        cpu.sT = cpu.register[x];
                        break;
                    case 0x001E:
                        cpu.I += cpu.register[x];
                        break;
                    case 0x0029:
                        //set I to digit Vx
                        cpu.setIDigit(x);
                        break;
                    case 0x0033:
                        System.out.println(String.format("not implemented! -> %X", code));
                        //BCD
                        break;
                    case 0x0055:
                        ptr = cpu.I;
                        for(int i = 0; i < x; i++){
                            mem.writeToMem(ptr, cpu.register[i]);
                            ptr++;
                        }
                        break;
                    case 0x0065:
                        ptr = cpu.I;
                        for(int i = 0; i < x; i++) {
                            cpu.register[i] = mem.readByte(ptr+i);
                            ptr++;
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

    private void skipIf(BiPredicate<Integer, Integer> f, Integer a, Integer b) {
        if (f.test(a, b)) {
            cpu.pc += 2;
        }
    }

    private void ifSetVF(BiPredicate<Integer, Integer> f, Consumer<Object> sideEffect) {
        cpu.register[0x00F] = f.test(null, null) ? (byte) 1 : (byte) 0;
        sideEffect.accept(null);
    }
}