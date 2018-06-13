package chip8;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Chip8
{

    static CPU cpu;
    static Memory mem;

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.out.println("Usage: Chip8 [file_name]");
            return;
        }
        init(args[0]);
        for(int i = 0; i < 20; i++)
            cpu.cycle();
    }

    private static void init(String fileName) throws FileNotFoundException, IOException {
        mem = new Memory();
        mem.load(new File(fileName));
        cpu = new CPU(mem, new Graphics());
        // disp = new Display();
    }
}
