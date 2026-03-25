# Team 8885 — 2026 FRC Robot Code

This repository contains the robot code for **FRC Team 8885's 2026 season**. It is built with [WPILib](https://docs.wpilib.org/), Java 17, and the [Command-Based](https://docs.wpilib.org/en/stable/docs/software/commandbased/index.html) programming framework. The drivetrain uses [YAGSL](https://yagsl.gitbook.io/yagsl) (Yet Another Generic Swerve Library) for swerve drive control.

---

## How FRC Robot Software Works

If you're new to FRC programming, this section explains the core concepts. It assumes you know Java but haven't worked with a robot codebase before.

### WPILib and the Robot Lifecycle

WPILib is the official open-source framework for FRC robots. Your code doesn't run from a traditional `main()` you control — instead, WPILib calls _your_ methods at the right time, similar to Android's `Activity` lifecycle or a game engine's update loop.

An FRC match has distinct phases:

```
Power On → Disabled → Autonomous (15 sec) → Teleoperated (2 min 15 sec) → Disabled
```

WPILib calls pairs of methods for each phase:

| Phase | Init Method (called once) | Periodic Method (called every 20ms) |
|-------|--------------------------|--------------------------------------|
| Always | `robotInit()` | `robotPeriodic()` |
| Disabled | `disabledInit()` | `disabledPeriodic()` |
| Autonomous | `autonomousInit()` | `autonomousPeriodic()` |
| Teleop | `teleopInit()` | `teleopPeriodic()` |
| Test | `testInit()` | `testPeriodic()` |

"Periodic" methods run roughly every **20 milliseconds** (50 times per second). This is the robot's heartbeat — every 20ms you read sensors, make decisions, and send commands to motors.

### The Command-Based Framework

This project uses the **Command-Based** paradigm, built on two core ideas:

#### Subsystems

A **Subsystem** represents a physical mechanism on the robot (drivetrain, arm, intake, shooter, etc.). Each subsystem "owns" its hardware — motors and sensors — and **only one command can use a subsystem at a time**. This prevents conflicts like two commands trying to move the arm in opposite directions.

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

1. **`initialize()`** — runs once when the command starts
2. **`execute()`** — runs every 20ms while the command is active
3. **`isFinished()`** — checked each cycle; when it returns `true`, the command ends
4. **`end(boolean interrupted)`** — runs once when the command stops

Commands declare which subsystems they **require**. The scheduler ensures only one command per subsystem runs at a time — if a new command needs a busy subsystem, the old command gets interrupted.

```java
public class ScoreGamePiece extends Command {
    private final Arm arm;
    private final Shooter shooter;

    public ScoreGamePiece(Arm arm, Shooter shooter) {
        this.arm = arm;
        this.shooter = shooter;
        addRequirements(arm, shooter);
    }

    @Override public void initialize() { arm.raiseToScoringPosition(); }
    @Override public void execute() { shooter.spinUp(); }
    @Override public boolean isFinished() { return shooter.isAtSpeed(); }
    @Override public void end(boolean interrupted) { shooter.stop(); }
}
```

#### The Command Scheduler

The **CommandScheduler** is the engine behind it all. Every 20ms (inside `robotPeriodic()`), it:

1. Checks all trigger/button bindings for new commands to schedule
2. Runs `execute()` on all active commands
3. Checks `isFinished()` and ends completed commands
4. Calls `periodic()` on all registered subsystems

It's driven by a single line: `CommandScheduler.getInstance().run();`

#### Triggers and Button Bindings

**Triggers** connect controller inputs to commands:

```java
// Run a command while the button is held
driverController.b().whileTrue(intake.runIntake());

// Run a command once when the button is first pressed
driverController.a().onTrue(arm.moveToPosition(45));

// Run a command when a custom condition becomes true
new Trigger(sensor::isDetected).onTrue(ledSubsystem.flashGreen());
```

### How Code Gets to the Robot

FRC robots run on a **roboRIO** — a small Linux computer made by National Instruments. Your Java code is compiled into a JAR and deployed over WiFi or USB:

```bash
./gradlew deploy
```

The roboRIO communicates with:
- **Motor controllers** over the CAN bus (a wiring network) — e.g., CTRE TalonFX, REV Spark Max
- **Sensors** over CAN, digital/analog IO, or USB — e.g., encoders, gyroscopes, cameras
- The **Driver Station** laptop over WiFi — this is how driver inputs reach the robot

### Vendor Libraries

Hardware manufacturers provide Java libraries for their motor controllers and sensors. These are managed as JSON files in the `vendordeps/` folder, and GradleRIO (the FRC Gradle plugin) downloads the correct JARs automatically.

### Swerve Drive

This robot uses a **swerve drive**, where each of the four wheels can independently spin _and_ steer. This lets the robot move in any direction while facing any direction — like a hockey player skating sideways while looking at the puck.

Each swerve module has:
- A **drive motor** (spins the wheel for speed)
- A **steering/azimuth motor** (rotates the wheel to change direction)
- An **absolute encoder** (knows the exact wheel angle even after power cycling)

A **gyroscope (IMU)** tracks the robot's heading, enabling **field-oriented driving** — where "forward" on the joystick always means "toward the opponent's end of the field," regardless of which way the robot is facing.

---

## Project Structure

```
2026Bot/
├── build.gradle                        # Build config (GradleRIO 2026.1.1, Java 17)
├── vendordeps/                         # Third-party library configs (9 libraries)
├── src/main/
│   ├── java/frc/robot/
│   │   ├── Main.java                  # Entry point — hands control to WPILib
│   │   ├── Robot.java                 # Lifecycle manager — ticks the scheduler
│   │   ├── RobotContainer.java        # Wires subsystems, commands, and controls together
│   │   ├── Constants.java             # Robot-wide configuration values
│   │   ├── subsystems/
│   │   │   ├── ExampleSubsystem.java  # Template subsystem
│   │   │   └── ExampleSubsystemSkeleton.java
│   │   └── commands/
│   │       ├── Autos.java             # Autonomous routine factory
│   │       └── ExampleCommand.java    # Template command
│   └── deploy/
│       └── swerve/                    # YAGSL swerve drive configuration
│           ├── swervedrive.json       # Root swerve config (IMU type, module references)
│           └── modules/               # Per-wheel motor, encoder, and PID configs
│               ├── frontleft.json
│               ├── frontright.json
│               ├── backleft.json
│               └── backright.json
```

### Key Files Explained

#### `Main.java`

The only `main()` in the project. It passes the `Robot` class to WPILib's `RobotBase.startRobot()`. You'll never need to change this.

#### `Robot.java`

Extends `TimedRobot` and bridges WPILib's lifecycle to our code:

- **Constructor** — creates `RobotContainer`, which builds everything else
- **`robotPeriodic()`** — calls `CommandScheduler.getInstance().run()`, the single line that drives the entire command-based system every 20ms
- **`autonomousInit()`** — retrieves and schedules the autonomous command from `RobotContainer`
- **`teleopInit()`** — cancels the autonomous command so teleop takes over

#### `RobotContainer.java`

The central wiring hub. This is where you:

1. **Instantiate subsystems** (the physical mechanisms)
2. **Create controllers** (driver/operator input devices)
3. **Bind buttons to commands** (connect inputs to actions)
4. **Select the autonomous command**

Currently configured with one `ExampleSubsystem` and one Xbox controller on port 0.

#### `Constants.java`

Central home for configuration values — motor CAN IDs, PID gains, speeds, physical dimensions, and anything else that might change during tuning. Currently minimal (just the controller port), this file grows as the robot develops.

#### `Autos.java`

A utility class with static factory methods that build autonomous command sequences. Autonomous routines chain commands together — for example, "drive forward, pick up a game piece, drive to the goal, score."

#### Subsystems and Commands

The `subsystems/` and `commands/` directories contain the template examples. Real subsystems will control actual hardware (motors, sensors), and real commands will define the robot's actions during both autonomous and teleoperated play.

---

## Vendor Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| **YAGSL** | 2026.3.12 | Swerve drive control — handles kinematics and module coordination |
| **Phoenix6** | 26.1.2 | CTRE hardware: TalonFX motors, CANcoder, Pigeon2 gyro, CANrange, CANdi, CANdle |
| **Phoenix5** | 5.36.0 | Legacy CTRE compatibility layer |
| **REVLib** | 2026.0.5 | REV Robotics: Spark Max motor controllers, NEO motors |
| **ReduxLib** | 2026.1.1 | Redux Robotics utilities (required by YAGSL) |
| **ThriftyLib** | 2026.1.1 | ThriftyBot utilities (required by YAGSL) |
| **Studica** | 2026.0.0 | Studica robotics components |
| **Maple-Sim** | 0.4.0-beta | Physics simulation for testing without hardware |
| **WPILibNewCommands** | 1.0.0 | The command-based framework |

## Swerve Drive Configuration

YAGSL is configured via JSON files in `src/main/deploy/swerve/`:

- **`swervedrive.json`** — root config defining the IMU (NavX via USB) and references to four module configs
- **Module files** (`frontleft.json`, `frontright.json`, `backleft.json`, `backright.json`) — define motor types, CAN IDs, encoder offsets, and PID values for each wheel
- **`physicalproperties.json`** and **`pidfproperties.json`** — shared physical dimensions and PID defaults

These JSON files are deployed to the roboRIO at `/home/lvuser/deploy/swerve/` and read by YAGSL at runtime.

## Building and Deploying

**Build the project:**
```bash
./gradlew build
```

**Deploy to the robot** (roboRIO must be connected via WiFi or USB):
```bash
./gradlew deploy
```

**Run in simulation:**
```bash
./gradlew simulateJava
```

## Architecture Diagram

```
┌─────────────────────────────────────────────────────┐
│                     Robot.java                       │
│               (WPILib lifecycle hooks)               │
│                        │                             │
│              robotPeriodic() every 20ms              │
│                        │                             │
│              CommandScheduler.run()                   │
│                        │                             │
├────────────────────────┼────────────────────────────┤
│                 RobotContainer.java                   │
│            (creates and wires everything)             │
│                        │                             │
│         ┌──────────────┼──────────────┐              │
│         │              │              │              │
│    Subsystems      Controllers    Triggers           │
│    (hardware)    (Xbox, etc.)   (bindings)           │
│         │              │              │              │
│         └──────────────┼──────────────┘              │
│                        │                             │
│                    Commands                          │
│              (actions the robot takes)               │
│                        │                             │
│                   Autos.java                         │
│            (autonomous command sequences)             │
└─────────────────────────────────────────────────────┘
```

## Glossary

| Term | Meaning |
|------|---------|
| **roboRIO** | The small Linux computer on the robot that runs your code |
| **CAN bus** | A wiring network connecting the roboRIO to motor controllers and sensors |
| **Driver Station** | The laptop + software drivers use to control the robot during a match |
| **Subsystem** | A Java class representing a physical mechanism (drivetrain, arm, etc.) |
| **Command** | A Java class representing an action the robot performs |
| **Trigger** | A condition (button press, sensor value) that starts/stops a command |
| **Scheduler** | The engine managing which commands run on which subsystems |
| **Swerve drive** | A drivetrain where each wheel can independently spin and steer |
| **IMU / Gyro** | A sensor that measures the robot's rotation (heading) |
| **Absolute encoder** | A sensor that knows its exact position even after power cycling |
| **PID** | Proportional-Integral-Derivative — a control algorithm for precise positioning |
| **Vendor dependency** | A third-party library for specific hardware (CTRE, REV, etc.) |
| **GradleRIO** | The FRC Gradle plugin that handles building and deploying robot code |
| **YAGSL** | "Yet Another Generic Swerve Library" — handles swerve drive math and config |
