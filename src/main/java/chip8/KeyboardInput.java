package chip8;
import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

class KeyboardInput
{
    private static int key;
    private static int action;
    private static HashMap<Character, Integer> registerMap;
    KeyboardInput() {
        registerMap = new HashMap<>();
        registerMap.put('1', 0x001);
        registerMap.put('2', 0x002);
        registerMap.put('3', 0x003);
        registerMap.put('4', 0x00C);
        registerMap.put('Q', 0x004);
        registerMap.put('W', 0x005);
        registerMap.put('E', 0x006);
        registerMap.put('R', 0x00D);
        registerMap.put('A', 0x007);
        registerMap.put('S', 0x008);
        registerMap.put('D', 0x009);
        registerMap.put('F', 0x00E);
        registerMap.put('Z', 0x00A);
        registerMap.put('X', 0x000);
        registerMap.put('C', 0x00B);
        registerMap.put('V', 0x00F);
    }

    void receive(int key, int action) {
        KeyboardInput.key = key;
        KeyboardInput.action = action;
//        System.out.println(String.format("Got key: %c, state: %d, corresponds to register: %X", key, action, registerMap.getOrDefault((char)key,-1)));
    }
    //hacky. fix this
    static boolean keyIsDown(int x) {
        return registerMap.getOrDefault((char)key, -1) == x && action == GLFW_PRESS;
    }
    static boolean keyIsReleased(int x) {
        return registerMap.getOrDefault((char)key, -1) == x && action == GLFW_RELEASE;
    }

    public static boolean anyKeyDown() {
        return action == GLFW_PRESS;
    }
}
