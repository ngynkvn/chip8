# chip8
A Chip-8 emulator written in Java and OpenGL via the LWJGL library.

<img src="https://media.giphy.com/media/2voGd1tK2mymKilrqQ/giphy.gif" width="320" height="160"/>
<img src ="https://media.giphy.com/media/69nd3UccF2nFeyh8bb/giphy.gif"/>


[Chip-8](https://en.wikipedia.org/wiki/CHIP-8) is an interpreted programming language intended to run on Chip-8 virtual machines. 
Chip-8 systems only have 35 opcodes, making them fairly easy to emulate and a good introduction into emulators. 

The following commands should open up a window and allow you to play pong. (1 and Q are up and down respectively.)
```
./gradlew build
./gradlew run
```

Not implemented: Sound, Super Chip8 instruction set

Resources used:
+ /r/EmuDev
- [Cowgod's Chip-8 Technical Reference](http://devernay.free.fr/hacks/chip8/C8TECH10.HTM)
+ [open.gl](open.gl) and [docs.gl](docs.gl)
- [LWJGL](https://www.lwjgl.org/)
