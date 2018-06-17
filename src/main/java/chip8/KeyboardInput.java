package chip8;
import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

class KeyboardInput
{
    private static HashMap<Character, Integer> registerMap;
    private static int[] state;
    private static int action;
    KeyboardInput() {
        registerMap = new HashMap<>();
        state = new int[18];
        state[17] = GLFW_RELEASE;
        action = GLFW_RELEASE;
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
//        System.out.println(String.format("Got key: %c, state: %d, corresponds to register: %X", key, action, registerMap.getOrDefault((char)key,null)));
        key = registerMap.getOrDefault((char) key, -1);
        if (key != -1){
            state[key] = action;
            KeyboardInput.action = action;
        }
    }
    //hacky. fix this
    static boolean keyIsDown(byte x) {
        return state[x] == GLFW_PRESS;
    }
    static boolean keyIsReleased(byte x) {
        return state[x] == GLFW_RELEASE;
    }

    static boolean anyKeyDown() {
        return action == GLFW_PRESS;
    }
}
