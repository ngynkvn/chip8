package chip8;

//lwjgl imports
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.Callback;

import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Chip8
{

    private static CPU cpu;
    private static Memory mem;
    private static Graphics disp;
    private static KeyboardInput kb;
    private static long window;

    public static void main(String[] args) throws Exception {
//        Configuration.DEBUG.set(true);
        if (args.length < 1) {
            System.out.println("Usage: Chip8 [file_name]");
            return;
        }

        init(args[0]);
        glClearColor(0,1,1,0);
        while(!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            generateTexture();
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
            glfwSwapBuffers(window);

            cpu.cycle();
            if(cpu.dT != 0)
                cpu.dT--;
            if(cpu.sT != 0)
                cpu.sT--;
            glfwPollEvents();
            Thread.sleep(17);
        }
    }

    private static void generateTexture() {
        int tex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, tex);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
        IntBuffer buff = BufferUtils.createIntBuffer(64*32);
        buff.put(disp.display).flip();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, 64, 32, 0, GL_RGBA, GL_UNSIGNED_BYTE, buff);
    }

    private static void init(String fileName) throws IOException {
        mem = new Memory(); //Move later.
        mem.load(new File(fileName));
        kb = new KeyboardInput();
        disp = new Graphics(); //Move later.
        cpu = new CPU(mem, disp);
        createWindowGLFW();
        createGL();
    }

    private static void createGL() {
        GLCapabilities caps = GL.createCapabilities();
        Callback debugProc = GLUtil.setupDebugMessageCallback();
        float vertices[] = { //lifted from open.gl/textures
                //  Position    Texcoords
                -1.0f,  1.0f, 0.0f, 0.0f, // Top-left 0
                1.0f,  1.0f,  1.0f, 0.0f, // Top-right 1
                1.0f, -1.0f,  1.0f, 1.0f, // Bottom-right 2
                -1.0f, -1.0f, 0.0f, 1.0f  // Bottom-left 3
        };

        int indices[] = {
                0, 1, 2,
                2, 3, 0
        };

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(4*4);
        vertexBuffer.put(vertices).flip();
        int vbo = glCreateBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);


        IntBuffer indexBuffer = BufferUtils.createIntBuffer(6);
        indexBuffer.put(indices).flip();
        int ebo = glCreateBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);

        try {
            //Load shader files.
            glShaderSource(vertexShader, loadFile("./resources/vertex.vert"));
            glShaderSource(fragmentShader, loadFile("./resources/fragment.frag"));

            glCompileShader(vertexShader);
            glCompileShader(fragmentShader);

            int[] statusV = new int[1];
            int[] statusF = new int[1];
            glGetShaderiv(vertexShader, GL_COMPILE_STATUS, statusV);
            glGetShaderiv(fragmentShader, GL_COMPILE_STATUS, statusF);

            if(statusV[0] == GL_FALSE || statusF[0] == GL_FALSE) {
                throw new RuntimeException("Failed to compile the shaders.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.err.printf("Vertex errors:\n%s\nFragment errors:\n%s\n", glGetShaderInfoLog(vertexShader), glGetShaderInfoLog(fragmentShader));
        }

        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glBindFragDataLocation(shaderProgram, 0, "outColor");
        glLinkProgram(shaderProgram);
        glUseProgram(shaderProgram);

        int posAttrib = glGetAttribLocation(shaderProgram, "position");
        glVertexAttribPointer(posAttrib, 2, GL_FLOAT, false, 16, 0);
        glEnableVertexAttribArray(posAttrib);

        int texAttrib = glGetAttribLocation(shaderProgram, "texcoord");
        glVertexAttribPointer(texAttrib, 2, GL_FLOAT, false, 16, 8);
        glEnableVertexAttribArray(texAttrib);

    }

    private static String loadFile(String s) throws IOException {
        return new String(Files.readAllBytes(Paths.get(s)), StandardCharsets.UTF_8);
    }

    private static void createWindowGLFW() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Couldn't initialize GLFW");

//        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        window = glfwCreateWindow(640,320, "Chip-8 Emulator", NULL, NULL);
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
