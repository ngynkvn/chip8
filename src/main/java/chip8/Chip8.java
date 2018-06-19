package chip8;

//lwjgl imports

import org.lwjgl.glfw.*;

import java.io.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Chip8
{

    private static CPU cpu;
    private static Memory memory;
    private static Graphics display;
    private static KeyboardInput kb;
    private static long window;

    public static void main(String[] args) throws Exception {
//        Configuration.DEBUG.set(true);
        if (args.length < 1) {
            System.out.println("Usage: Chip8 [file_name]");
            return;
        }

        init(args[0]);
        glClearColor(0, 1, 1, 0);
        while (!glfwWindowShouldClose(window)) {

            display.render();

            cpu.cycle();
            cpu.timeClocks();

            Thread.sleep(2);
            glfwPollEvents();
        }
    }

    private static void init(String fileName) throws IOException {
        kb = new KeyboardInput();
        createWindowGLFW();
        display = new Graphics(window);
        memory = new Memory();
        memory.load(new File(fileName));
        cpu = new CPU(memory, display);
    }

    private static void createWindowGLFW() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Couldn't initialize GLFW");

        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        window = glfwCreateWindow(640, 320, "Chip-8 Emulator", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Couldn't create GLFW window.");
        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) ->
        {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
            else
                kb.receive(key, action);
        });

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> glViewport(0, 0, width, height));
    }

}
