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
  
- 16 general purpose 8-bit registers from V0 to VF (hexadecimal digits), special 16-bit I-register, 
2 8-bit registers for delay and sound timers, 16-bit register for program counter and a 8-bit register
  for stack pointer.
  
- Keyboard with 16-key hexadecimal keyboard.

- 64x32 pixel monochrome display.

- 35 instructions, each 2-byte long. 

More information about the chip-8's design can be found at the references section.

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

## References

Materials used:

* [https://en.wikipedia.org/wiki/CHIP-8](https://en.wikipedia.org/wiki/CHIP-8)
* [http://devernay.free.fr/hacks/chip8/C8TECH10.HTM](http://devernay.free.fr/hacks/chip8/C8TECH10.HTM)
* [https://github.com/mattmikolay/chip-8/wiki/CHIP%E2%80%908-Technical-Reference](https://github.com/mattmikolay/chip-8/wiki/CHIP%E2%80%908-Technical-Reference)
