package chip8;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

class Memory {
    private byte[] memory;

    Memory() {
        memory = new byte[4096];
    }

    void load(File f) throws FileNotFoundException, IOException {
        if (!f.exists())
            throw new FileNotFoundException("Couldn't find file specified.");
        FileInputStream fin = new FileInputStream(f);
        for (int i = 512; fin.available() > 0; i++) { //Start at pos 512 because 0-512 is reserved for intepreter.
            int b = fin.read();
            memory[i] = (byte) b;
        }
        fin.close();
    }

    short readInstruction(short pc) {
        return (short) (memory[pc] << 8 | memory[pc + 1]);
    }
}
