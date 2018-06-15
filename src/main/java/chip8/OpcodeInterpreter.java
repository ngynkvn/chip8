package chip8;

import java.util.Objects;
import java.util.Random;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
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
        short ptr;
//        System.out.printf("received instruction: %X\n", code);
        switch (firstNibble) {
            case 0x0000:
                if (code == 0x0E0)
                    graphics.clear();
                else
                    cpu.returnSub();
                break;
            case 0x1000:
                System.out.printf("JUMP: %X, int value: %d\n", address, address);
                cpu.jumpTo(address);
                break;
            case 0x2000:
                cpu.subroutine(address);
                break;
            case 0x3000:
                System.out.printf("register%d is %d, kk is %d\n", x, cpu.register[x], kk);
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
                System.out.printf("%X\n", code);
//                System.out.printf("DRAWING %d %d %d\n", cpu.register[x], cpu.register[y], lastNibble);
                if (graphics.draw(cpu.register[x], cpu.register[y], lastNibble, cpu.I, mem))
                    cpu.register[0x000F] = 1;
                else
                    cpu.register[0x00f] = 0;
                break;
            case 0xE000:
                switch (lastNibble) {
                    case 0x000E:
                        skipIf(KeyboardInput::keyIsDown, x);
                        break;
                    case 0x0001:
                        skipIf(KeyboardInput::keyIsReleased, x);
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
                        //BCD
                        byte num = cpu.register[x];
                        byte hund = (byte) (Byte.toUnsignedInt(num) / 100);
                        byte ten = (byte) (Byte.toUnsignedInt(num) % 100 / 10);
                        byte one = (byte) (Byte.toUnsignedInt(num) % 10);
                        mem.writeToMem(cpu.I, hund);
                        mem.writeToMem(cpu.I + 1, ten);
                        mem.writeToMem(cpu.I + 2, one);
                        break;
                    case 0x0055:
                        ptr = cpu.I;
                        for (int i = 0; i < x; i++) {
                            mem.writeToMem(ptr + i, cpu.register[i]);
                        }
                        break;
                    case 0x0065:
                        ptr = cpu.I;
                        for (int i = 0; i < x; i++) {
                            cpu.register[i] = mem.readByte(ptr + i);
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

    private void skipIf(Predicate<Integer> f, Integer a) {
        if (f.test(a)) {
            cpu.pc += 2;
        }
    }

    private void ifSetVF(BiPredicate<Integer, Integer> f, Consumer<Object> sideEffect) {
        cpu.register[0x00F] = f.test(null, null) ? (byte) 1 : (byte) 0;
        sideEffect.accept(null);
    }
}