# 2026 FRC Robot Code

This document explains how FRC robot software works in general and how this specific robot's code is organized. It's written for someone who knows Java but is new to FRC programming.

---

## Table of Contents

- [Part 1: How FRC Robot Software Works](#part-1-how-frc-robot-software-works)
  - [The Big Picture](#the-big-picture)
  - [TimedRobot and the Robot Lifecycle](#timedrobot-and-the-robot-lifecycle)
  - [The Command-Based Framework](#the-command-based-framework)
  - [Subsystems](#subsystems)
  - [Commands](#commands)
  - [The Command Scheduler](#the-command-scheduler)
  - [Triggers and Button Bindings](#triggers-and-button-bindings)
  - [RobotContainer](#robotcontainer)
- [Part 2: How This Robot's Code Works](#part-2-how-this-robots-code-works)
  - [Project Structure](#project-structure)
  - [Robot.java - The Entry Point](#robotjava---the-entry-point)
  - [RobotContainer.java - The Wiring Hub](#robotcontainerjava---the-wiring-hub)
  - [Subsystems in This Robot](#subsystems-in-this-robot)
  - [Controller Mapping](#controller-mapping)
  - [Drive Modes](#drive-modes)
  - [Autonomous Mode](#autonomous-mode)
  - [Constants](#constants)
  - [Swerve Drive Configuration](#swerve-drive-configuration)
  - [Key Libraries Used](#key-libraries-used)
  - [Known TODOs and Notes](#known-todos-and-notes)

---

## Part 1: How FRC Robot Software Works

### The Big Picture

FRC robots run on a small computer called the **roboRIO**. When you deploy code, it runs a Java program on the roboRIO. The WPILib framework provides the structure for that program -- you don't write `main()` yourself. Instead, you fill in methods that the framework calls at the right times.

The standard architecture is called **Command-Based Programming**. It has three core ideas:

1. **Subsystems** represent physical parts of the robot (drivetrain, shooter, intake, etc.)
2. **Commands** represent actions the robot can take (drive forward, shoot, pick up a game piece)
3. A **Scheduler** runs in a loop, executing commands and making sure only one command uses each subsystem at a time

### TimedRobot and the Robot Lifecycle

Your main robot class extends `TimedRobot`. The framework calls methods on it in a loop, roughly every 20 milliseconds (50 times per second). The robot goes through **modes**, and for each mode there's an `init` method (called once when entering the mode) and a `periodic` method (called every 20ms while in that mode):

```
Robot powers on
  --> Robot() constructor runs
  --> robotPeriodic() called every 20ms, ALWAYS, in ALL modes

Driver Station sets mode:
  DISABLED    --> disabledInit()  ... disabledPeriodic() every 20ms
  AUTONOMOUS  --> autonomousInit() ... autonomousPeriodic() every 20ms
  TELEOP      --> teleopInit()     ... teleopPeriodic() every 20ms
  TEST        --> testInit()       ... testPeriodic() every 20ms
```

The most important thing in `robotPeriodic()` is this line:

```java
CommandScheduler.getInstance().run();
```

This single line drives the entire command-based framework. Every 20ms, the scheduler:
1. Checks all button/trigger bindings for new inputs
2. Runs the `execute()` method of every active command
3. Checks if any commands are finished (`isFinished()`)
4. Calls `periodic()` on every subsystem

### The Command-Based Framework

Think of it like a restaurant:
- **Subsystems** are the stations (grill, prep station, oven) -- only one cook can use each at a time
- **Commands** are the orders -- they describe what to do at a station
- **The Scheduler** is the head chef -- it decides what runs where and when

### Subsystems

A subsystem is a Java class that extends `SubsystemBase`. It represents a physical mechanism on the robot. Key rules:

- Each subsystem can only run **one command at a time**. If you schedule a new command that needs the same subsystem, the old one gets interrupted.
- Subsystems can have a **default command** that runs whenever no other command is using them (e.g., the drive subsystem's default command reads joystick input).
- The `periodic()` method on a subsystem runs every 20ms regardless of what command is running.

Example pattern:

```java
public class ShooterSubsystem extends SubsystemBase {
    private SparkMax motor = new SparkMax(/* CAN ID */);

    // Returns a Command that other code can schedule
    public Command shoot() {
        return run(() -> motor.set(1.0));  // run() creates a command from a lambda
    }

    public Command stop() {
        return runOnce(() -> motor.set(0)); // runOnce() runs once then finishes
    }
}
```

### Commands

A command has a lifecycle:

1. **`initialize()`** -- called once when the command starts
2. **`execute()`** -- called every 20ms while the command is active
3. **`isFinished()`** -- checked every cycle; if it returns `true`, the command ends
4. **`end(interrupted)`** -- called once when the command ends (either finished naturally or was interrupted)

You'll often see commands created inline using helper methods instead of separate classes:

```java
// run() -- calls the lambda every 20ms until interrupted
subsystem.run(() -> motor.set(1.0));

// runOnce() -- calls the lambda once, then the command finishes
subsystem.runOnce(() -> motor.set(0));

// startEnd() -- calls first lambda on start, second on end
subsystem.startEnd(() -> motor.set(1.0), () -> motor.set(0));
```

Commands can be composed:

```java
// Run in sequence: do A, then B, then C
new SequentialCommandGroup(commandA, commandB, commandC);

// Run in parallel: do A and B at the same time
new ParallelCommandGroup(commandA, commandB);

// Chain with .andThen(), .alongWith(), etc.
commandA.andThen(commandB);
```

### The Command Scheduler

The scheduler is a singleton (`CommandScheduler.getInstance()`). Every time `.run()` is called (in `robotPeriodic()`), it:

1. Polls all registered triggers (button bindings)
2. Initializes any newly-scheduled commands
3. Calls `execute()` on all running commands
4. Checks `isFinished()` and ends finished commands
5. Calls `periodic()` on all registered subsystems

You almost never interact with the scheduler directly. You schedule commands through button bindings or by calling `CommandScheduler.getInstance().schedule(command)`.

### Triggers and Button Bindings

Triggers connect inputs (buttons, sensors, conditions) to commands:

```java
// When button A is pressed, run the command
controller.a().onTrue(command);

// While button B is held, run the command; stop when released
controller.b().whileTrue(command);

// Toggle: first press starts the command, second press cancels it
controller.x().toggleOnTrue(command);
```

### RobotContainer

`RobotContainer` is the class where you wire everything together:
- Create subsystem instances
- Create controller instances
- Bind buttons to commands
- Set default commands
- Define which autonomous routine to use

It's constructed once in the `Robot()` constructor. It's not part of the framework -- it's a convention (a pattern the project template gives you).

---

## Part 2: How This Robot's Code Works

### Project Structure

```
src/main/java/frc/robot/
  Main.java                  -- JVM entry point (don't touch)
  Robot.java                 -- TimedRobot lifecycle methods
  RobotContainer.java        -- Wires subsystems, commands, and controls together
  Constants.java             -- All configuration values (CAN IDs, speeds, PID, etc.)
  LimelightHelpers.java      -- Utility library for Limelight vision camera

  subsystems/
    DriveSubsystem.java      -- Swerve drivetrain (4 wheel modules)
    VisionSubsystem.java     -- Limelight camera for AprilTag detection
    ShooterSubsystem.java    -- Shooter and feeder motors
    IntakeSubsystem.java     -- Intake arm (pivots up/down) and roller wheels
    ExampleSubsystem.java    -- Template (unused)

  commands/
    Autos.java               -- Autonomous routine templates
    ExampleCommand.java      -- Template (unused)

src/main/deploy/
  swerve/                    -- JSON config for swerve drive modules (motor IDs, offsets, PID)
  pathplanner/               -- PathPlanner autonomous paths and routines
```

### Robot.java - The Entry Point

This is the simplest file. It:

1. Creates `RobotContainer` in its constructor (which sets up the whole robot)
2. Calls `CommandScheduler.getInstance().run()` every 20ms in `robotPeriodic()`
3. Starts the autonomous command in `autonomousInit()`
4. Calls `m_robotContainer.teleopInit()` in `teleopInit()` (sets the drive mode) and cancels auto
5. Calls `debugPeriodic()` to log telemetry to SmartDashboard

You generally don't need to edit this file.

### RobotContainer.java - The Wiring Hub

This is where all the pieces connect. It:

- Instantiates all subsystems: `DriveSubsystem`, `VisionSubsystem`, `ShooterSubsystem`, `IntakeSubsystem`
- Creates two Xbox controllers (driver on port 0, operator on port 1)
- Sets up the vision subsystem as a default command (always running to update position estimates)
- Configures button bindings in `configureDrive()`
- Sets up three selectable drive modes via SmartDashboard
- Defines the autonomous routine in `getAutonomousCommand()`

### Subsystems in This Robot

#### DriveSubsystem (swerve drive)

The drivetrain uses **swerve drive** -- 4 independently steerable wheel modules that let the robot move in any direction while facing any direction. Each module has:
- A **drive motor** (SparkMax + NEO) that spins the wheel
- An **angle motor** (SparkMax + NEO) that rotates the wheel's direction
- An **absolute encoder** (Thrifty) that knows the wheel's exact angle

The subsystem uses the **YAGSL** library (Yet Another Generic Swerve Library) to manage all of this. Configuration is in JSON files under `src/main/deploy/swerve/`.

Key methods:
- `driveAngularVelocity()` -- Robot-relative driving (forward is always the robot's front)
- `driveAngularVelocityFO()` -- Field-relative driving (forward is always away from the driver)
- `driveDirectAngleFO()` -- Field-relative driving where the right stick points the robot's heading
- `zeroYaw()` -- Resets the gyro so the current heading becomes "forward"
- `visionUpdate()` -- Accepts pose corrections from the Limelight

#### VisionSubsystem (Limelight camera)

Uses a Limelight camera to detect AprilTags (vision targets placed around the field). It runs as a default command, continuously feeding position estimates to the drive subsystem so the robot knows where it is on the field.

The position estimate accounts for which alliance (red/blue) the robot is on, since the field is mirrored.

#### ShooterSubsystem

Two brushed motors on SparkMax controllers:
- **Shooter motor** (CAN ID 58) -- the main flywheel
- **Feeder motor** (CAN ID 51) -- feeds game pieces into the shooter

Key methods:
- `shootAndFeed()` -- Runs both motors at full speed immediately
- `spinUpAndShoot()` -- Spins up the shooter first, waits 0.5 seconds, then starts the feeder (gives the flywheel time to reach speed)
- `stop()` -- Stops everything

#### IntakeSubsystem

Two mechanisms:
- **Arm** (SparkMax + NEO, CAN ID 50) -- pivots up and down with a 20:1 gear ratio. Uses PID position control on the motor controller to hold position.
  - Stowed position: 0 degrees (vertical, tucked in)
  - Deployed position: -5.4 degrees (extended to the ground)
  - The arm **must be in the stowed position when the robot powers on**, because the encoder zeros at startup.
- **Rollers** (SparkMax + NEO Vortex, CAN ID 48) -- spin rubber wheels to grab game pieces at 40% speed

Key methods:
- `deploy()` / `stow()` -- Move the arm to deployed/stowed position
- `runRollers()` / `runRollersReverse()` / `stopRollers()` -- Control the intake wheels

### Controller Mapping

Two Xbox controllers are used:

**Driver Controller (port 0)** -- drives the robot:

| Input | Action |
|-------|--------|
| Left stick | Translate (move robot) |
| Right stick | Rotate (turn robot) |
| A button | Zero the gyro (reset "forward" direction) |

**Operator Controller (port 1)** -- controls mechanisms:

| Input | Action |
|-------|--------|
| D-pad Up | Deploy intake arm (extend to ground) |
| D-pad Down | Stow intake arm (retract) |
| B button | Run intake rollers forward |
| X button | Run intake rollers reverse |
| Release B or X | Stop intake rollers |
| Left trigger | Shoot (both motors instantly full speed) |
| Right trigger | Spin-up shoot (shooter first, feeder after 0.5s delay) |
| Release either trigger | Stop shooter and feeder |

### Drive Modes

Three drive modes are available, selectable via SmartDashboard before a match:

1. **Robot Oriented** -- "Forward" means the robot's front. Simple but disorienting when the robot is turned.
2. **Angular Field Oriented** -- "Forward" means away from the driver, regardless of robot heading. Right stick controls rotation speed.
3. **Field Oriented Direct Angle** (default) -- Same as above, but the right stick **points** the robot in a direction instead of spinning it.

The drive mode is applied when teleop starts (in `teleopInit()`).

### Autonomous Mode

Currently a simple hardcoded sequence:
1. Spin up the shooter and begin feeding after 0.5s
2. Wait 10 seconds
3. Stop the shooter

There are also PathPlanner auto routines defined in `src/main/deploy/pathplanner/autos/` (e.g., `SimpleRightShoot.auto`, `FarLeftShoot.auto`), but the named commands they depend on are **commented out** in `RobotContainer.java` (lines 71-77). To use PathPlanner autos, those lines would need to be uncommented and the auto chooser re-enabled.

### Constants

All hardware IDs, speeds, and tuning values are in `Constants.java`:

| Constant | Value | Purpose |
|----------|-------|---------|
| `maxSpeed` | 8 ft/s | Swerve drive max speed |
| `shooterCANId` | 58 | Shooter motor CAN bus ID |
| `feederCANId` | 51 | Feeder motor CAN bus ID |
| `shooterMaxSpeed` | 1.0 (100%) | Shooter motor power |
| `feederMaxSpeed` | 1.0 (100%) | Feeder motor power |
| `shootToFeedDelay` | 0.5s | Delay before feeder starts in spin-up mode |
| `kArmMotorId` | 50 | Intake arm motor CAN ID |
| `kRollerMotorId` | 48 | Intake roller motor CAN ID |
| `kArmGearRatio` | 20:1 | Arm gearbox reduction |
| `kStowedSetpoint` | 0 deg | Arm stowed angle |
| `kDeployedSetpoint` | -5.4 deg | Arm deployed angle |
| `kArmP` | 0.05 | Arm PID proportional gain |
| `kArmMaxOutput` | 0.4 (40%) | Max arm motor output (safety limit) |
| `kRollerSpeed` | 0.4 (40%) | Roller motor speed |

### Swerve Drive Configuration

Swerve module details are in JSON files under `src/main/deploy/swerve/`:

| Module | Drive Motor (CAN) | Angle Motor (CAN) | Encoder Offset |
|--------|-------------------|--------------------|----------------|
| Front Left | 57 | 49 | 174.33 deg |
| Front Right | 56 | 44 | 296.63 deg |
| Back Left | 46 | 47 | 209.00 deg |
| Back Right | 52 | 53 | 266.84 deg |

IMU: NavX-MXP via SPI

### Key Libraries Used

| Library | What It Does |
|---------|-------------|
| **WPILib** | Core FRC framework (TimedRobot, commands, subsystems, scheduler) |
| **YAGSL** | Swerve drive abstraction (reads JSON config, handles module math) |
| **REVLib** | Drivers for REV SparkMax motor controllers |
| **PathPlanner** | Create and follow autonomous paths on the field |
| **Studica (NavX)** | Driver for the NavX-MXP gyroscope |
| **LimelightHelpers** | Utility class for communicating with the Limelight vision camera |

### Known TODOs and Notes

From code comments and analysis:

- **Limelight name is empty** -- `VisionConstants.limelightName` needs to be set to the actual device name
- **Shooter CAN IDs marked TODO** -- verify CAN IDs 58 and 51 match the actual hardware
- **Test arm invert direction** -- may need to invert the arm motor depending on physical mounting
- **Test roller invert direction** -- same for roller motor
- **PathPlanner named commands are commented out** -- need to uncomment lines 71-77 in `RobotContainer.java` to use PathPlanner autos
- **Arm encoder zeroes at startup** -- the robot MUST be powered on with the intake arm in the stowed (vertical) position, or the encoder will have the wrong zero point
