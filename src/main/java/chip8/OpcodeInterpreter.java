package chip8;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

class OpcodeInterpreter {
    private CPU cpu;

    OpcodeInterpreter(CPU cpu) {
        this.cpu = cpu;
    }

    void dispatchCode(short code) {
        final int firstNibble = code & 0xF000;
        final int address = code & 0x0FFF;
        final int x = (code & 0x0F00) >> 8;
        final int y = (code & 0x00F0) >> 4;
        final int kk = code & 0x0FF;

        System.out.printf("%X\n", code);
        System.out.printf("firstByte: %X\n", firstNibble);
        switch (firstNibble) {
        case 0x0000:
            if (code == 0x0E0)
                System.out.println("Screen clear");
            // clearScreen();
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
            int lastNibble = code & 0x000F;
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
                ifSetVF((i, j) -> cpu.register[y] > cpu.register[x], i -> cpu.register[x] = (byte) (cpu.register[y] - cpu.register[x]));
                break;
            case 0x00E:
                ifSetVF((i, j) -> (cpu.register[x] >>> 7) == 1, i -> cpu.register[x] *= 2);
                break;
            }
            // conditionals
            break;
        case 0x9000:
            break;
        case 0xA000:
            break;
        case 0xB000:
            break;
        case 0xC000:
            break;
        case 0xD000:
            break;
        case 0xE000:
            break;
        case 0xF000:
            break;
        default:
            throw new IllegalArgumentException(String.format("Encountered unknown opcode: %X", code));
        }
    }

    private void skipIf(BiPredicate<Integer, Integer> f, Integer a, Integer b) {
        // System.out.println(f.test(a, b));
        if (f.test(a, b)) {
            cpu.pc += 2;
        }
    }

    private void ifSetVF(BiPredicate<Integer, Integer> f, Consumer<Object> sideEffect) {
        cpu.register[0x00F] = f.test(null, null) ? (byte) 1 : (byte) 0;
        sideEffect.accept(null);
    }
}