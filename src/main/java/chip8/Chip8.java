package chip8;

//lwjgl imports
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Chip8
{

    private static CPU cpu;
    private static Memory mem;
    private static Graphics disp;
    private static KeyboardInput kb;
    private static long window;

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.out.println("Usage: Chip8 [file_name]");
            return;
        }

        init(args[0]);

        while(!glfwWindowShouldClose(window)) {
            GL.createCapabilities();
            glClearColor(0,0,0,0);

            glfwSwapBuffers(window);
            cpu.cycle();
            glfwPollEvents();
        }
    }

    private static void init(String fileName) throws IOException {
        mem = new Memory();
        mem.load(new File(fileName));
        kb = new KeyboardInput();
        disp = new Graphics();
        cpu = new CPU(mem, disp);
        createWindowGLFW();
    }

    private static void createWindowGLFW() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Couldn't initialize GLFW");

        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(500,500, "Chip-8 Emulator", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Couldn't create GLFW window.");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
            else
                kb.receive(key, action);
        });
    }
}
