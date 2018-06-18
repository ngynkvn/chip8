package chip8;

//lwjgl imports

import org.lwjgl.*;
import org.lwjgl.glfw.*;

import java.io.*;
import java.nio.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.system.MemoryUtil.*;

public class Chip8
{

    private static CPU cpu;
    private static Memory mem;
    private static Graphics disp;
    private static KeyboardInput kb;
    private static long window;
    private static long lastTime;
    private static long accrual;

    public static void main(String[] args) throws Exception {
//        Configuration.DEBUG.set(true);
        if (args.length < 1) {
            System.out.println("Usage: Chip8 [file_name]");
            return;
        }

        init(args[0]);
        glClearColor(0, 1, 1, 0);
        while (!glfwWindowShouldClose(window)) {

            Graphics.render();
            glfwSwapBuffers(window);

            cpu.cycle();
            timeClocks();

            Thread.sleep(2);
            glfwPollEvents();
        }
    }

    private static void timeClocks() {
        accrual += deltaT();
        if (accrual >= 17) {
            cpu.dT -= 1;
            cpu.sT -= 1;
            accrual -= 17;
            if (cpu.dT < 0)
                cpu.dT = 0;
            if (cpu.sT < 0)
                cpu.sT = 0;
        }
    }

    private static long deltaT() {
        long time = getTime();
        long delta = time - lastTime;
        lastTime = time;
        return delta;
    }

    private static long getTime() {
        return System.nanoTime() / 1_000_000;
    }

    private static void init(String fileName) throws IOException {
        mem = new Memory(); //Move later.
        mem.load(new File(fileName));
        kb = new KeyboardInput();
        disp = new Graphics(); //Move later.
        cpu = new CPU(mem, disp);
        createWindowGLFW();
        Graphics.createGL();
        lastTime = getTime();
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
