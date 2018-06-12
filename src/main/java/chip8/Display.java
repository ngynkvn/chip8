package chip8;

class Display {
    
    private int[] display;

    Display() {
        display = new int[64 * 32];
    }

    void clear() {
        display = new int[64 * 32];
    }

    void draw(int x, int y, int lastNibble) {
        int startLocation = x + (y << 5);
        int mask = 1 << 8;
        for(int i = startLocation; i < startLocation+8; i++) {
            display[i] ^= (lastNibble & mask) != 0 ? 1 : 0;
            mask >>>= 1;
        }
    }
}
