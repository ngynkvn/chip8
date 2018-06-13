package chip8;

class Graphics
{
    
    private int[] display;

    Graphics() {
        display = new int[64 * 32];
    }

    void clear() {
        display = new int[64 * 32];
    }

    boolean draw(int x, int y, int lastNibble) {
        int startLocation = x + (y << 5);
        int mask = 1 << 8;
        boolean collision = false;
        for(int i = startLocation; i < startLocation+8; i++) {
            int prev = display[i];
            display[i] ^= ((lastNibble & mask) != 0) ? 1 : 0;
            collision = (display[i] != prev) || collision;
            mask >>>= 1;
        }
        return collision;
    }
}
