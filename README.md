# Chip-8 interpreter

Chip-8 is an interpreted language developed in the 70's, intended to ease 
video game programming. Initially used on the *COSMAC VIP* and *Telmac 1800*, 
chip-8 interpreters have been implemented on various platforms.

## Specification

Chip-8's design determines:
- Memory in size of 4KB, with addresses from 0x000 to 0xFFF.
Addresses from 0x0 to 0x200 are reserved for the interpreter, usually it holds the default
font data.
  
- Stack in size of 16 16-bit values, allowing for up to 16 subroutines (chip-8 interpreter should allow
  for at least 12 subroutine calls).
  
- 16 general purpose 8-bit registers from `V0` to `VF` (hexadecimal digits), special 16-bit `I`-register, 
2 8-bit `DT` and `ST` registers for delay and sound timers, 16-bit `PC` register for a program counter and an 8-bit `SP` 
  register for a stack pointer.
  
- Keyboard with 16-key hexadecimal keyboard.

- 64x32 pixel monochrome display.

- 35 instructions, each 2-byte long. 

- A set of predefined sprites representing hexadecimal digits from 0x0 to 0xF. Each sprite is 5 bytes long. 
  In this interpreter, the sprites are stored in the reserved memory, beginning from the address 0x0.

More information about the chip-8's design can be found at the References section.

## How to build

You'll need maven to build the project, though you can also compile it with javac.

Clone the repository with:

```
git clone https://github.com/skni-kod/chip-8.git
```

Then enter the repository and build the jar package using:

```
mvn clean package
```

The output package can be found in the target folder.

## How to use

```
java -jar chip8.jar (ROM PATH) (PARAMETERS)
```

## Parameters

Parametrs allow you to configure the way the interpreter works. Changing the launch parameters is necessary 
for some games to run flawlessly, as described in Quirks/Compatibility section. If you run the interpreter
without any of these parameters explicitly set, the default value will be set.

|Parameter|Value|Purpose|Default value|Example|
|---|---|---|---|---|
|-freq|Integer|Sets chip-8's CPU frequency (in Hz).|500|-freq 300|
|-regGUI|None|Shows the window with real time view on registers and executed instructions. Use the parameter to turn the GUI on.|Turned off|-regGUI|
|-overlap|Boolean|Sets the screen overlapping mode - True to overlap sprites over the screen edges, False to turn the overlapping off.|True|-overlap false|
|-loadq|Boolean|Turns on the load-store quirk - True to turn on, False to turn off.|False|-loadq true|
|-shiftq|Boolean|Turns on the shift quirk - True to turn on, False to turn off.|True|-shiftq false|

More information on the quirks can be found in the Quirks/Compatibility section.

## Quirks/Compatibility

Chip-8's references aren't consistent on some specific behaviours, such as whether sprites should overlap to the other 
side of the screen, when the screen's border is met or which register should be shifted when using `8xyE` or `8xy7` shift
instructions. What's more, some games are written to make use of these quirks, so to run properly, each game may require 
different launch parameters. Example of such game is `BLITZ` by David Winter, which requires us to turn screen overlapping
off for the game to run flawlessly.

If the game you're trying to run behaves strange, try to experiment with the launch parameters and set the quirks to values
different from the default ones.

### Load - store quirk

The load-store quirk concerns instructions `Fx55` and `Fx65` - instructions that store and load register values to and from 
the memory, beginning from register V0 to register Vx. [Cowgod's reference](http://devernay.free.fr/hacks/chip8/C8TECH10.HTM) 
doesn't determine whether the `I` register should be modified when these instructions are executed. 
[Mattmikolay's reference](https://github.com/mattmikolay/chip-8/wiki/CHIP%E2%80%908-Instruction-Set) on the other hand states 
that the `I` register should be set to `I + X + 1` when the instructions are executed.

When the load-store quirk is turned on, the `I` register is left unmodified. 

When the load-store quirk is turned off, the `I` register is incremented with each stored/loaded registers (set to `I + X + 1`).

### Shift quirk

The shift quirk concerns instructions `8xy6` and `8xyE` - instructions that shift value stored in the registers one bit
right or left. [Cowgod's reference](http://devernay.free.fr/hacks/chip8/C8TECH10.HTM) describes shifting instructions as
shifting `Vx` rather than `Vy`, so the `Vy` register is not used at all. [Mattmikolay's reference](https://github.com/mattmikolay/chip-8/wiki/CHIP%E2%80%908-Instruction-Set)
states that the register `Vy`is shifted and then stored in the `Vx` register.

When the shift quirk is turned on, register `Vx` is the shifted one.

When the shift quirk is turned off, register `Vy` is the shifted one.

### Screen overlapping

[Cowgod's reference](http://devernay.free.fr/hacks/chip8/C8TECH10.HTM) states, that when a sprite is drawn on the screen and
 a part of the sprite is positioned partially outside the screen border, it should wrap around to the other side of the screen.
[Mattmikolay's reference](https://github.com/mattmikolay/chip-8/wiki/CHIP%E2%80%908-Instruction-Set) states, that in this 
situation sprite's sticking out part should just be clipped. 

When the overlapping mode is turned on, sprites positioned partially outside the screen border will be wrapped around and 
drawn on the other side.

When the overlapping mode is turned off, sprites positioned partially outside the screen border will be clipped.

## Compatible games

Here are some games I tested and found working properly with certain launch parameters.

### TODO

## Screenshots

![Tetris game](/screenshots/7.png?raw=true)

![UFO game](/screenshots/2.png?raw=true)

![Lander game](/screenshots/5.png?raw=true)

![Real time register view](/screenshots/3.png?raw=true)

## References

Materials used while creating the interpreter:

* [https://en.wikipedia.org/wiki/CHIP-8](https://en.wikipedia.org/wiki/CHIP-8)
* [http://devernay.free.fr/hacks/chip8/C8TECH10.HTM](http://devernay.free.fr/hacks/chip8/C8TECH10.HTM)
* [https://github.com/mattmikolay/chip-8/wiki/CHIP%E2%80%908-Technical-Reference](https://github.com/mattmikolay/chip-8/wiki/CHIP%E2%80%908-Technical-Reference)
