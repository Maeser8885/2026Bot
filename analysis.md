# FRC Robot Code: A Guide for New Programmers

This document explains how FRC (FIRST Robotics Competition) robot code works in general, and then walks through how **Team 8885's 2026 robot project** is structured. It assumes you know Java but have never seen an FRC codebase before.

---

## Part 1: How FRC Robot Code Works

### What is WPILib?

WPILib is the official open-source library that FRC teams use to program their robots. It provides everything you need: motor control, sensor reading, driver input, autonomous routines, and more. Think of it as the "framework" your robot code lives inside — similar to how a web app lives inside Spring Boot or a game lives inside a game engine.

Your code doesn't have a traditional `main()` that you control. Instead, WPILib calls _your_ methods at the right time (like Android's `Activity` lifecycle or a game engine's update loop).

### The Robot Lifecycle

An FRC match has distinct phases, and WPILib gives you hooks for each one:

```
Power On → Robot Disabled → Autonomous (15 sec) → Teleoperated (2 min 15 sec) → Disabled
```

WPILib calls pairs of methods for each phase:

| Phase | Init Method (called once) | Periodic Method (called every 20ms) |
|-------|--------------------------|--------------------------------------|
| Always | `robotInit()` | `robotPeriodic()` |
| Disabled | `disabledInit()` | `disabledPeriodic()` |
| Autonomous | `autonomousInit()` | `autonomousPeriodic()` |
| Teleop | `teleopInit()` | `teleopPeriodic()` |
| Test | `testInit()` | `testPeriodic()` |

"Periodic" methods run in a loop roughly every 20 milliseconds (50 times per second). This is your robot's heartbeat — every 20ms, you read sensors, make decisions, and send commands to motors.

### The Command-Based Framework

Most FRC teams (including this one) use the **Command-Based** programming paradigm. It's a design pattern built on two core concepts:

#### Subsystems
A **Subsystem** represents a physical mechanism on the robot. Examples:
- Drivetrain (the wheels)
- Arm (a joint that moves up/down)
- Intake (rollers that grab game pieces)
- Shooter (wheels that launch game pieces)

Each subsystem "owns" its hardware (motors, sensors) and only one command can use a subsystem at a time. This prevents conflicts — you can't have two commands trying to move the arm in opposite directions simultaneously.

```java
public class Arm extends SubsystemBase {
    private final TalonFX motor = new TalonFX(5); // motor on CAN ID 5

    public Command moveUp() {
        return runOnce(() -> motor.set(0.5));
    }

    public Command stop() {
        return runOnce(() -> motor.set(0.0));
    }
}
```

#### Commands
A **Command** is an action the robot performs. Commands have a lifecycle:

1. `initialize()` — runs once when the command starts
2. `execute()` — runs every 20ms while the command is active
3. `isFinished()` — checked every cycle; if it returns `true`, the command ends
4. `end(boolean interrupted)` — runs once when the command stops (either it finished naturally, or it was interrupted by another command)

Commands **require** subsystems. The scheduler ensures only one command per subsystem runs at a time. If a new command needs a subsystem that's already in use, the old command gets interrupted.

```java
public class ScoreGamePiece extends Command {
    private final Arm arm;
    private final Shooter shooter;

    public ScoreGamePiece(Arm arm, Shooter shooter) {
        this.arm = arm;
        this.shooter = shooter;
        addRequirements(arm, shooter); // declares which subsystems this uses
    }

    @Override
    public void initialize() { arm.raiseToScoringPosition(); }

    @Override
    public void execute() { shooter.spinUp(); }

    @Override
    public boolean isFinished() { return shooter.isAtSpeed(); }

    @Override
    public void end(boolean interrupted) { shooter.stop(); }
}
```

#### The Command Scheduler
The **CommandScheduler** is the engine that runs everything. Every 20ms (inside `robotPeriodic()`), it:
1. Checks all trigger/button bindings for new commands to schedule
2. Runs `execute()` on all active commands
3. Checks `isFinished()` and ends completed commands
4. Calls `periodic()` on all registered subsystems

You never call the scheduler manually — it's invoked by a single line:
```java
CommandScheduler.getInstance().run();
```

#### Triggers and Button Bindings
**Triggers** connect controller inputs (buttons, joystick thresholds) to commands. Common patterns:

```java
// Run a command while the button is held; stop when released
driverController.b().whileTrue(intake.runIntake());

// Run a command once when the button is first pressed
driverController.a().onTrue(arm.moveToPosition(45));

// Run a command when a custom condition becomes true
new Trigger(sensor::isDetected).onTrue(ledSubsystem.flashGreen());
```

### How Code Gets to the Robot

FRC robots run on a **roboRIO** — a small Linux computer made by National Instruments. Your Java code is compiled, packaged into a JAR, and deployed to the roboRIO over WiFi or USB using Gradle:

```bash
./gradlew deploy
```

The roboRIO communicates with:
- **Motor controllers** over the CAN bus (a wiring network) — e.g., CTRE TalonFX, REV Spark Max
- **Sensors** over CAN, digital/analog IO, or USB — e.g., encoders, gyroscopes, cameras
- The **Driver Station** laptop over WiFi — this is how driver inputs get to the robot

### Vendor Libraries

Motor controllers and sensors are made by third-party companies who provide their own Java libraries:
- **CTRE Phoenix** — for TalonFX, TalonSRX, CANcoder, Pigeon2 gyro
- **REVLib** — for Spark Max motor controllers, NEO motors
- **YAGSL** — a community library for swerve drive (more on this below)

These are managed as JSON files in the `vendordeps/` folder. GradleRIO (the FRC Gradle plugin) reads them and downloads the correct JARs.

### Swerve Drive

Many modern FRC robots use a **swerve drive**, where each of the four wheels can independently rotate _and_ steer. This lets the robot move in any direction while facing any direction — like a hockey player skating sideways while looking at the puck.

Each swerve module has:
- A **drive motor** (spins the wheel for speed)
- A **steering/azimuth motor** (rotates the wheel to change direction)
- An **absolute encoder** (knows the exact angle of the wheel at all times)

A **gyroscope** (IMU) on the robot tracks the robot's heading so the software can do field-oriented driving — where "forward" on the joystick always means "toward the opponent's end of the field," regardless of which way the robot is facing.

---

## Part 2: How This Project (Team 8885, 2026) is Structured

### Project Status

This project is a **freshly scaffolded template** — the structure is in place and vendor libraries are installed, but the actual robot logic hasn't been written yet. All the subsystems and commands are placeholder examples. Think of it as the foundation of a house before the walls go up.

### File Structure

```
2026Bot/
├── build.gradle                    # Build configuration (team number, dependencies)
├── vendordeps/                     # Third-party library configs (9 libraries)
├── src/main/
│   ├── java/frc/robot/
│   │   ├── Main.java              # Entry point (don't touch this)
│   │   ├── Robot.java             # Lifecycle manager
│   │   ├── RobotContainer.java    # Wires everything together
│   │   ├── Constants.java         # Robot-wide constants
│   │   ├── subsystems/
│   │   │   ├── ExampleSubsystem.java
│   │   │   └── ExampleSubsystemSkeleton.java
│   │   └── commands/
│   │       ├── Autos.java         # Autonomous routine factory
│   │       └── ExampleCommand.java
│   └── deploy/
│       └── swerve/                # Swerve drive config (JSON files)
│           ├── swervedrive.json
│           └── modules/           # Per-wheel configs (currently empty)
```

### Walking Through the Code, File by File

#### `Main.java` — The Entry Point
```java
public final class Main {
    public static void main(String... args) {
        RobotBase.startRobot(Robot::new);
    }
}
```
This is the only `main()` in the project. It hands control to WPILib by telling it "here's my Robot class, start running it." You'll never need to change this file.

#### `Robot.java` — The Lifecycle Manager

This class extends `TimedRobot` and acts as the bridge between WPILib's lifecycle and your code. Key things it does:

- **Constructor**: Creates the `RobotContainer` (which builds everything else)
- **`robotPeriodic()`**: Calls `CommandScheduler.getInstance().run()` — this single line is what makes the entire command-based system work. Every 20ms, it ticks the scheduler.
- **`autonomousInit()`**: Gets the autonomous command from `RobotContainer` and schedules it
- **`teleopInit()`**: Cancels the autonomous command so teleop can take over

Most teams rarely modify this file beyond the template.

#### `RobotContainer.java` — The Wiring Hub

This is the most important file for understanding how the robot is assembled. It's where you:

1. **Create subsystems** — instantiate the physical mechanisms
2. **Create controllers** — set up driver/operator input devices
3. **Bind buttons to commands** — connect inputs to actions
4. **Define the autonomous command**

Currently, it creates one `ExampleSubsystem` and one Xbox controller (port 0), with two bindings:
- A custom trigger based on `exampleCondition()` (always returns false, so it never fires)
- The B button on the Xbox controller runs `exampleMethodCommand()` while held

#### `Constants.java` — Configuration Values

A central place for numbers that might change during tuning: motor IDs, PID gains, speeds, dimensions, etc. Right now it only has the driver controller port number (0). As the robot develops, this file will grow significantly.

#### `ExampleSubsystem.java` — A Template Subsystem

This shows the pattern every subsystem follows:
- Extend `SubsystemBase`
- Create methods that return `Command` objects (the modern "inline command" pattern)
- Override `periodic()` for code that should run every cycle regardless of what command is active (e.g., updating a dashboard)

Currently does nothing — it's a starting point for real subsystems like a drivetrain, arm, or intake.

#### `ExampleCommand.java` — A Template Command

Shows the full command lifecycle pattern (initialize → execute → isFinished → end). This style of command (a standalone class) is used for complex commands. Simpler commands are often created inline using factory methods like `runOnce()`, `run()`, or `startEnd()` on the subsystem itself.

Note: `isFinished()` returns `false`, meaning this command runs forever until interrupted. That's a deliberate template choice — real commands would have a real end condition.

#### `Autos.java` — Autonomous Routines

A utility class (private constructor, static methods) that builds autonomous command sequences. The `exampleAuto()` method chains two commands in sequence:

```java
Commands.sequence(
    subsystem.exampleMethodCommand(),  // do something quick
    new ExampleCommand(subsystem)      // then do something ongoing
);
```

In a real robot, this would be things like "drive forward 3 meters, pick up a game piece, drive to the scoring position, score it."

### Vendor Libraries Installed

The team has installed libraries that strongly hint at the planned hardware:

| Library | Purpose |
|---------|---------|
| **YAGSL** | Swerve drive management (handles the math of coordinating 4 independently steerable wheels) |
| **Phoenix6 + Phoenix5** | CTRE motor controllers (TalonFX, etc.) and sensors (CANcoder, Pigeon2 gyro) |
| **REVLib** | REV Spark Max motor controllers and NEO brushless motors |
| **ReduxLib** | Required by YAGSL |
| **ThriftyLib** | Required by YAGSL |
| **Studica** | Studica robotics components |
| **Maple-Sim** | Physics simulation for testing without a real robot |
| **WPILibNewCommands** | The command-based framework itself |

### Swerve Drive Configuration

The `src/main/deploy/swerve/` folder contains JSON configuration files for YAGSL. The main file (`swervedrive.json`) defines:
- **IMU**: NavX gyroscope connected via USB
- **4 swerve modules**: front-left, front-right, back-left, back-right

The individual module JSON files (motor types, CAN IDs, encoder offsets, PID values, physical properties) are **currently empty** — these need to be filled in once the physical robot is built and wired. This is where you'd specify things like "front-left drive motor is a TalonFX on CAN ID 1, steering motor is a Spark Max on CAN ID 2, absolute encoder offset is 47.3 degrees."

### What Needs to Happen Next

To go from this template to a working robot, the team will need to:

1. **Configure the swerve module JSON files** with actual motor types, CAN IDs, encoder offsets, and PID tuning values
2. **Create real subsystems** (e.g., `DrivetrainSubsystem` using YAGSL, `ArmSubsystem`, `IntakeSubsystem`) with actual motor and sensor objects
3. **Create real commands** for robot actions (drive, intake, score, climb, etc.)
4. **Wire up controller bindings** in `RobotContainer` so driver inputs control the robot
5. **Build autonomous routines** in `Autos.java` for the 15-second autonomous period
6. **Fill in `Constants.java`** with hardware IDs, tuning values, and physical dimensions
7. **Test in simulation** using Maple-Sim before deploying to the real robot

### Quick Reference: Adding a New Mechanism

When you add a new mechanism (like an arm), the pattern is:

1. **Add constants** in `Constants.java` (motor CAN IDs, speeds, positions)
2. **Create a subsystem** in `subsystems/` that extends `SubsystemBase` and controls the hardware
3. **Create commands** (either inline in the subsystem or as separate classes in `commands/`)
4. **Instantiate the subsystem** in `RobotContainer`
5. **Bind controls** to commands in `RobotContainer.configureBindings()`
6. **Add autonomous steps** in `Autos.java` if the mechanism is used in auto

---

## Glossary

| Term | Meaning |
|------|---------|
| **roboRIO** | The small Linux computer on the robot that runs your code |
| **CAN bus** | A wiring network that connects the roboRIO to motor controllers and sensors |
| **Driver Station** | The laptop + software that drivers use to control the robot during a match |
| **Subsystem** | A Java class representing a physical mechanism (drivetrain, arm, etc.) |
| **Command** | A Java class representing an action the robot performs |
| **Trigger** | A condition (button press, sensor value) that starts/stops a command |
| **Scheduler** | The engine that manages which commands are running on which subsystems |
| **Swerve drive** | A drivetrain where each wheel can independently spin and steer |
| **IMU/Gyro** | A sensor that measures the robot's rotation (heading) |
| **Absolute encoder** | A sensor that knows its exact position even after power cycling |
| **PID** | Proportional-Integral-Derivative — a control algorithm for precise motor positioning |
| **Vendor dependency** | A third-party library for specific hardware (CTRE, REV, etc.) |
| **GradleRIO** | The FRC-specific Gradle plugin that handles building and deploying robot code |
| **YAGSL** | "Yet Another Generic Swerve Library" — handles swerve drive math and configuration |
