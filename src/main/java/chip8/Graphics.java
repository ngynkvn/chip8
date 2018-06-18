package chip8;


import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindFragDataLocation;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL45.glCreateBuffers;

class Graphics
{

    private static int[] display;


    Graphics() {
        display = new int[64 * 32];
    }

    static void createGL() {
        GL.createCapabilities();
        GLUtil.setupDebugMessageCallback();
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

    static void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        generateTexture();
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
    }

    private static void generateTexture() {
        int tex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, tex);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        IntBuffer buff = BufferUtils.createIntBuffer(64 * 32);
        buff.put(display).flip();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, 64, 32, 0, GL_RGBA, GL_UNSIGNED_BYTE, buff);
    }

    void clear() {
        display = new int[64 * 32];
    }

    boolean draw(int x, int y, int height, int I, Memory mem) {
        boolean collision = false;
        for(int i = 0; i < height; i++) {
            int b = mem.readByte(I+i);
            int mask = 1 << 7;
            for(int j = 0; j < 8; j++) {
                int pos = (x + j + ((y + i) << 6)) % 2048;
                int prev = display[pos];
                display[pos] ^= ((b & mask) != 0) ? 0xFFFFFFFF : 0;
                if (prev == 0xFFFFFFFF && display[pos] == 0) //caused pixel to be erased.
                    collision = true;
                mask >>>= 1;
            }
        }
        return collision;
    }
}
