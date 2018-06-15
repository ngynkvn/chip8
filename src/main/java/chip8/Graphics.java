package chip8;


class Graphics
{

    int[] display;


    Graphics() {
        display = new int[64 * 32];
    }

    void clear() {
        display = new int[64 * 32];
    }

    boolean draw(int x, int y, int height, int I, Memory mem) {
        int startLocation = x + (y << 5);
        boolean collision = false;
        for(int i = 0; i < height; i++) {
            int loc = startLocation + (i << 6);
            byte b = mem.readByte(I+i);
            System.out.println(String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(b))).replace(' ', '0'));
            int mask = 1 << 7;
            for(int j = loc; j < loc+8; j++) {
                int prev = display[i];
                display[j] ^= ((b & mask) != 0) ? 0xFFFFFFFF : 0;
                collision = (display[j] != prev) || collision;
                mask >>>= 1;
            }
        }
        return collision;
    }
    void dump() {
        int x = 0;
        for(int i = 0; i < 32; i++){
            for(int j = 0; j < 64; j++){
                System.out.printf("%s",display[x] == 0xFFFFFFFF ? "1" : "0");
                x++;
            }
            System.out.println();
        }
    }

    public int[] getArray() {
        return display;
    }

    public int[] getDisplay() {
        return display;
    }

    public void setDisplay(int[] display) {
        this.display = display;
    }
}
