# Team 8885 — 2026 Robot Plan

This document describes the physical robot, its subsystems, controls, autonomous strategy, and the coding tasks needed to make it all work. Students should use this as a blueprint to write the robot code.

---

## 1. Robot Overview

- **Chassis**: Square frame (~21.25" front-to-back x ~20.75" side-to-side, based on module locations)
- **Drivetrain**: Swerve drive (4 independent modules)
- **Weight**: ~110 lbs
- **Game**: REBUILT (2026 FRC game)
- **Game pieces**: FUEL — 5.91-inch diameter yellow high-density foam balls
- **Scoring**: Score fuel into the Hub (central field structure). Climb the Tower in endgame for bonus points.
- **Strategy**: Intake fuel from the field (front intake), store in onboard hopper (gravity-fed), shoot out the rear into the Hub
- **Controls**: 2-driver setup — Driver 1 (drivetrain), Driver 2/Operator (intake + shooter)
- **Controllers**: 2x Xbox controllers (Port 0 = driver, Port 1 = operator)

---

## 1b. Information Needed from Electrical / Mechanical

**Hand this list to the electrical and mechanical teams.** The software team cannot finish the code without this information. Items marked "MUST HAVE" are blocking — code won't compile or run without them. Items marked "TUNE ON ROBOT" need to be discovered through testing.

### MUST HAVE — From Electrical (CAN IDs and wiring)

Use the REV Hardware Client (plug each Spark Max in via USB) to read or assign CAN IDs. Every Spark Max on the robot needs a **unique** CAN ID.

**Total Spark Maxes on the robot: 12** (8 swerve + 2 shooter + 2 intake). If you count a different number of Spark Maxes physically wired, something is wrong — investigate before powering on.

- [x] Front-left swerve module — drive motor Spark Max CAN ID: **57**
- [x] Front-left swerve module — steer motor Spark Max CAN ID: **49**
- [x] Front-left swerve module — Thrifty encoder roboRIO analog input channel: **1**
- [x] Front-right swerve module — drive motor Spark Max CAN ID: **55**
- [x] Front-right swerve module — steer motor Spark Max CAN ID: **56**
- [x] Front-right swerve module — Thrifty encoder roboRIO analog input channel: **2**
- [x] Back-left swerve module — drive motor Spark Max CAN ID: **62**
- [x] Back-left swerve module — steer motor Spark Max CAN ID: **59**
- [x] Back-left swerve module — Thrifty encoder roboRIO analog input channel: **0**
- [x] Back-right swerve module — drive motor Spark Max CAN ID: **53**
- [x] Back-right swerve module — steer motor Spark Max CAN ID: **60**
- [x] Back-right swerve module — Thrifty encoder roboRIO analog input channel: **3**
- [x] Shooter — launcher motor Spark Max CAN ID: **58**
- [x] Shooter — feeder motor Spark Max CAN ID: **54**
- [x] Intake — arm motor Spark Max CAN ID: **50**
- [x] Intake — roller motor Spark Max CAN ID: **48**f
- [x] Confirm all swerve Spark Maxes are set to **brushless mode** (NEO motors) — configured in YAGSL JSON as `sparkmax_neo`
- [x] Confirm shooter Spark Maxes are set to **brushed mode** (CIM motors)
- [x] Confirm intake arm Spark Max is set to **brushless mode** (NEO motor)
- [x] Confirm intake roller Spark Max is set to **brushless mode** (NEO Vortex kmotor)

### MUST HAVE — From Electrical / Mechanical (hardware details)

- [x] **Gyro / IMU type and connection** — **NavX via SPI** (configured as `navx_spi` in YAGSL)
- [x] **Intake roller motor type** — **NEO Vortex** (brushless). Spark Max must be in brushless mode.
- [x] **Intake arm gear ratio** — **20:1** (1:20 reduction between NEO and arm pivot)
- [x] **Swerve module gear ratio** — Drive gear ratio: **5.9:1**, Angle gear ratio: **18.75:1** (configured in YAGSL physicalproperties.json)
- [x] **Robot dimensions** — Module locations from center: front/back **10.625"**, left/right **10.375"** (track width ~20.75", wheelbase ~21.25")
- [x] **Robot weight** — **110.23 lbs** (configured in YAGSL physicalproperties.json)
- [x] **Swerve wheel diameter** — **4 inches** (configured in YAGSL physicalproperties.json)
- [ ] **Driver camera** — Not yet installed. *(still needed)*
- [ ] **Limelight** — **Limelight 4** acquired, not yet mounted. Mounting position (height from ground, angle, offset from robot center) still TBD.

### TUNE ON ROBOT — Discovered through testing

These values need to be found experimentally. Start with the suggested defaults, then adjust.

**Drivetrain:**
- [ ] **Thrifty encoder absolute offsets** — With all modules pointing straight forward, read each encoder's raw value. These offsets go in the YAGSL module JSON files as `"absoluteEncoderOffset"`. You can read the raw values by publishing them to SmartDashboard in code, or by using YAGSL's built-in telemetry.
- [ ] **Drive max speed** — Start conservative (~3 m/s), increase as drivers get comfortable. Max for MK4n varies by gear ratio.
- [ ] **Rotation max speed** — Start conservative (~2π rad/s = 1 full rotation per second).
- [ ] **Slow mode multiplier** — Start at 0.25 (25% speed). Adjust based on driver preference.
- [ ] **Joystick deadband** — Start at 0.05-0.1. If the robot drifts when sticks are released, increase it.
- [ ] **Swerve drive/steer PID values** — Start with YAGSL defaults. Tune if modules oscillate or are sluggish.

**Shooter:**
- [ ] **Launcher motor direction** — Does it need to be inverted? Spin up the motor at low power and check which way the roller turns.
- [ ] **Feeder motor direction** — Same check. Feeder should push fuel into the launcher roller.
- [ ] **Launcher voltage** — Start at ~10.5V. Increase for more range, decrease if fuel overshoots.
- [ ] **Feeder voltage** — Start at ~9V. Too fast may jam; too slow won't feed reliably.
- [ ] **Rev-up time** — Start at 1.0 second. Listen to the launcher motor — when the pitch stops climbing, it's at speed. Adjust the timer to match.

**Intake:**
- [ ] **Arm deployed angle** — Start at ~115°. Place the rollers at ground level and read the encoder value.
- [ ] **Arm motor direction** — Does positive output deploy or stow the arm? Test at very low power first (~0.1).
- [ ] **Roller motor direction** — Should pull fuel inward toward the robot. Test and invert if wrong.
- [ ] **Arm PID P gain** — Start very low (e.g., 0.01-0.02). If the arm barely moves, increase. If it oscillates or slams, decrease.
- [ ] **Arm PID D gain** — Start at 0. Add a small amount (e.g., 0.001) only if the arm oscillates around its target.
- [ ] **Arm PID output cap** — Start at ±0.4 (40% max power). This limits arm force for compliance. Increase if the arm can't hold position against fuel; decrease if it's too stiff and jams fuel.
- [ ] **Roller speed** — Start at ~0.7 (70% output). Increase if fuel doesn't make it over the bumper; decrease if fuel flies past the storage area.
- [ ] **Arm current limit** — Start at 40A. Protects the gearbox and motor.
- [ ] **Roller current limit** — Start at 30-40A (depends on motor type).

**Autonomous:**
- [ ] **Auto drive speed** — Start at ~1.5 m/s (slow and controlled).
- [ ] **Auto drive duration** — Depends on starting position and field layout. Start at 2.0 seconds, measure on the actual field and adjust.

---

## 2. Subsystems

### 2.1 Drivetrain (Swerve)

**Physical description**: Four independently steerable wheel modules, one at each corner of the square chassis. Each module can spin its wheel (drive) and rotate its wheel direction (steer), allowing the robot to move in any direction while facing any direction.

**Swerve modules**: MK4n (SDS — Swerve Drive Specialties)

**Hardware per module**:

| Component | Type | Controller | FL | FR | BL | BR |
|-----------|------|------------|-----|-----|-----|-----|
| Drive motor | NEO | Spark Max | CAN 57 | CAN 55 | CAN 62 | CAN 53 |
| Steer motor | NEO | Spark Max | CAN 49 | CAN 56 | CAN 59 | CAN 60 |
| Absolute encoder | Thrifty (analog) | — | Analog 1 | Analog 2 | Analog 0 | Analog 3 |

**Gyro / IMU**: NavX via SPI (`navx_spi`)

**Gear ratio**: Drive **5.9:1**, Angle **18.75:1**

**Swerve library**: YAGSL — configured via JSON files in `src/main/deploy/swerve/`

**Driver camera**: USB camera mounted on the robot for driver visibility (especially when view is blocked by game elements). Streams video to the Driver Station dashboard via `CameraServer`. No vision processing — just a live feed.

**Vision (OPTIONAL — Phase 2)**: Limelight camera for AprilTag-based pose estimation. AprilTags are printed markers placed at known locations around the field. The Limelight detects them and calculates the robot's exact position and heading on the field. This data is fused with the gyro and wheel encoder data using WPILib's `SwerveDrivePoseEstimator` (supported natively by YAGSL) to continuously correct for gyro drift and wheel slip over the course of a match. Think of it as GPS for the robot. **Get basic driving working first before attempting this.**

**Key behaviors**:
- **Field-oriented drive** (default) — "forward" on joystick always means the same direction on the field, regardless of which way the robot is facing. Uses the gyro to compensate for robot heading. This is the primary drive mode.
- **Robot-oriented drive** (toggle) — "forward" on joystick means forward relative to the robot's front. Useful for precise alignment or if the gyro drifts. Driver can toggle between modes with a button.
- Slow mode (reduced speed for precise alignment)
- Heading lock / auto-align (optional, if time permits)
- **(OPTIONAL)** Vision-corrected odometry — Limelight AprilTag readings correct the robot's pose estimate in real time

---

### 2.2 Shooter

**Physical description**: Based on the 2026 kitbot launcher mechanism, mounted on the **rear** of the robot. It is a dual-roller system with two independently controlled rollers stacked inline. Fuel (game pieces) stored in the robot are fed from the robot's internal storage into the feeder roller, which pushes them into the spinning launcher roller to shoot.

**Hardware**:

| Component | Type | Controller | CAN ID |
|-----------|------|------------|--------|
| Launcher motor | CIM (brushed) | Spark Max (brushed mode) | **58** |
| Feeder motor | CIM (brushed) | Spark Max (brushed mode) | **54** |

**No sensors** — shooting is entirely driver-controlled.

**Key states**:

| State | Launcher Motor | Feeder Motor | Description |
|-------|---------------|--------------|-------------|
| Idle | Off | Off | Default state, nothing spinning |
| Rev-up | Full speed | Off | Launcher spins up to launch speed (~1 sec) |
| Shoot | Full speed | Forward | Feeder pushes fuel into the spinning launcher |
| Stop | Off | Off | Return to idle after shooting |

**Key behaviors**:
- **Rev-up then shoot sequence**: Driver holds right trigger to rev the launcher motor. After ~1 second spin-up, the feeder automatically engages and launches the fuel. Release the trigger to stop everything.
- **Feeder only spins in one direction** (toward the launcher). The intake side of the kitbot feeder roller is not used — a separate front-mounted intake handles that job.
- **Motor directions**: The launcher and feeder motors may need to be inverted relative to each other depending on how they're mounted. **Test on the robot** — if a motor spins the wrong way, set `config.inverted(true)` on the `SparkMaxConfig` for that motor (see REVLib 2026 API table in section 5). Get this right before tuning voltages.
- **Open-loop control**: No encoder feedback. Motors run at set voltage levels. CIM motors on Spark Maxes in brushed mode don't have built-in encoders, so speed is controlled by voltage output, not closed-loop PID.
- **Current limits**: Should be set to ~60A per motor to prevent brownouts (CIMs draw a lot of current at stall).

**Voltage reference** (starting points, tune on the robot):
- Launcher: ~10.5V
- Feeder: ~9V

---

### 2.3 Intake

**Physical description**: Over-the-bumper intake mounted on the **front** of the robot. Two parallel arms are physically linked and pivot at their base (bottom of the robot frame). An axle at the top of the arms holds multiple soft rubber roller wheels spaced along it. When deployed, the arms fold down ~110-120 degrees from vertical (stowed) to place the rollers at ground level outside the robot perimeter. The spinning rollers grab fuel, pull it up and over the bumper, and fling it into the open storage area inside the robot.

**Storage**: Open hopper area in the center of the robot frame. Holds approximately 5-10 fuel balls. Gravity-fed from the storage area down into the rear-mounted shooter.

**Hardware**:

| Component | Type | Controller | CAN ID |
|-----------|------|------------|--------|
| Arm motor | NEO (brushless, geared 20:1 for torque) | Spark Max (brushless mode) | **50** |
| Roller motor | NEO Vortex (brushless) | Spark Max (brushless mode) | **48** |
| Arm encoder | NEO built-in encoder | (integrated) | — |

**No external sensors or limit switches** — arm travel limits are enforced in software using the NEO's built-in encoder.

**Key positions**:

| Position | Arm Angle (approx) | Description |
|----------|-------------------|-------------|
| Stowed | 0° (vertical) | Arms up, rollers inside frame. Starting config, within robot perimeter. |
| Deployed | ~110-120° from stowed | Arms down, rollers at ground level, outside robot perimeter. Ready to intake. |

**Key behaviors**:
- **Deploy**: Arm motor rotates arms from stowed (0°) to deployed (~115°) using PID position control.
- **Stow**: Arm motor rotates arms back from deployed to stowed.
- **Intake**: With arms deployed, roller motor spins to grab fuel off the ground. Rollers pull fuel up over the bumper and into the storage area.
- **Position hold with compliance**: While intaking, the arm holds its deployed position via PID, but needs to allow slight upward "give" when fuel pushes against the rollers as it enters. This can be achieved by capping the PID output (limiting the maximum downward force the motor applies), so the arm is firm enough to stay near the ground but soft enough that a fuel ball can push it up slightly as it passes through. Alternatively, a lower P gain or a current-limit approach can achieve this.
- **Software limits**: The arm motor must be software-limited to prevent rotating past stowed (0°) or past deployed (~120°). Use the `SparkMaxConfig` soft limit settings (e.g., `config.softLimit.forwardSoftLimit(120.0)`) to enforce this — protects the mechanism even if the code has bugs. See the REVLib 2026 config-object pattern in section 5.
- **Arm gearing**: The arm motor is geared **20:1** for torque. The encoder conversion factor is `360.0 / 20.0 = 18.0` degrees per motor rotation. This is needed to set accurate PID position targets.
- **Motor directions**: The arm motor direction determines which way is "deploy" vs "stow" — **test on the robot with low power first.** If the arm goes the wrong way, set `config.inverted(true)` on that motor's `SparkMaxConfig` (see REVLib 2026 API table in section 5). Same for the roller motor — it should spin to pull fuel inward toward the robot. Get directions right before tuning anything else.

---

## 3. Controls

### Driver 1 — Drivetrain (Xbox Controller — Port 0)

Drives the robot. Nothing else — focus on positioning.

| Input | Action |
|-------|--------|
| Left stick | Strafe (X/Y translation) |
| Right stick X | Rotation |
| Left bumper (hold) | Slow mode (reduced drive speed for precise alignment) |
| Y (press) | Toggle field-oriented / robot-oriented drive |
| Start | Reset gyro heading (re-zero field-oriented drive) |

All other buttons on this controller are unbound. Y button toggles between drive modes — the current mode should be displayed on the dashboard so the driver always knows which mode they're in.

### Driver 2 — Operator (Xbox Controller — Port 1)

Controls intake and shooter. Two triggers — that's it.

| Input | Action |
|-------|--------|
| Left trigger (hold) | Deploy intake + spin rollers. Release → stow arm + stop rollers. |
| Right trigger (hold) | Shoot — revs launcher, auto-feeds after ~1 sec spin-up. Release → stop shooter. |
| A | Stow intake (manual override — use if intake doesn't auto-stow properly) |
| B | Emergency stop shooter (kills both motors immediately) |

All other buttons on this controller are unbound. Keep it simple — the operator only needs to learn two triggers.

---

## 4. Autonomous

**Strategy**: Keep it simple and reliable. Drive forward to a scoring position and shoot the preloaded fuel. If time allows, add a "do nothing" option as a safe fallback.

**Routine: "Drive Forward and Shoot"**

| Step | Action | Duration (approx) |
|------|--------|--------------------|
| 1 | Drive forward at moderate speed | ~2-3 sec (tune distance on field) |
| 2 | Stop driving | instant |
| 3 | Rev up launcher | ~1 sec |
| 4 | Run feeder to shoot | ~1-2 sec |
| 5 | Stop shooter | instant |

Total: ~5-6 seconds. Leaves plenty of margin in the 15-second auto period.

**Implementation**: This is a sequential command composition:
```
Commands.sequence(
    // Drive forward for a set time
    drivetrain.driveForward(speed).withTimeout(3.0),
    // Stop the drivetrain
    drivetrain.stopCommand(),
    // Rev the launcher, wait for spin-up, then feed
    shooter.revAndShoot().withTimeout(3.0),
    // Stop everything
    shooter.stopCommand()
)
```

**Routine: "Do Nothing"**

Does nothing. Use this if auto is broken or untested — earning 0 auto points is better than crashing into the field or other robots.

**Auto Chooser**: A `SendableChooser<Command>` in `RobotContainer` lets the drive team pick which auto routine to run from the Driver Station before the match starts. Default should be "Do Nothing" for safety.

---

## 5. Coding Tasks

### Priority Order

Code these in this order. Each builds on the previous:

1. **General / Infrastructure (5.5)** — clean up template code, set up operator controller, dashboard
2. **Drivetrain (5.1)** — must be working before anything else can be tested on the field
3. **Shooter (5.2)** — simpler mechanism, can be coded in parallel with drivetrain by a second student
4. **Intake (5.3)** — most complex (PID position control), tackle after shooter works
5. **Autonomous (5.4)** — requires drivetrain + shooter to be working
6. **Limelight (5.1b)** — only if everything else is solid

### What You Can Code NOW vs. What Needs the Robot

**You do NOT need the physical robot or any hardware info to start coding.** Use placeholder values (e.g., CAN ID `999`, angle `115.0`) and swap in real values once the electrical/mechanical team provides them. The structure, logic, methods, commands, and bindings are all independent of specific hardware values.

**Code RIGHT NOW (no robot needed):**
- All subsystem classes (`DrivetrainSubsystem`, `ShooterSubsystem`, `IntakeSubsystem`) — full method implementations, periodic(), dashboard publishing
- All commands and command sequences
- `RobotContainer` — both controllers, all bindings, auto chooser
- `Autos.java` — all autonomous routines
- `Constants.java` — full structure with all inner classes and placeholder values
- Driver camera setup (`CameraServer.startAutomaticCapture()`)

**Needs real values BEFORE deploying to the robot (but code compiles without them):**
- Shooter and intake CAN IDs in `Constants.java` — swap placeholders for real IDs from electrical team
- Swerve CAN IDs, Thrifty encoder analog input channels, encoder offsets, module locations, and gear ratio — all in the YAGSL JSON files under `src/main/deploy/swerve/modules/`
- Gyro/IMU type — in `swervedrive.json`
- Starting tuning values (voltages, PID gains, speeds) — use the suggested defaults from section 1b

**Can ONLY be determined on the physical robot:**
- Motor inversions — spin each motor at low power and check direction, then set `config.inverted(true/false)`
- Encoder offsets — point all swerve modules forward, read the raw encoder values
- All tuning values — PID gains, voltages, speeds, angles, deadbands

**Intake roller motor type is now known:** NEO Vortex (brushless). Use `MotorType.kBrushless`:
```java
private final SparkMax rollerMotor = new SparkMax(
    IntakeConstants.kRollerMotorId, MotorType.kBrushless);
```

**Bottom line: Students can write ALL the code today. The robot is only needed for configuration values and tuning.**

### Important: REVLib 2026 API Changes

REVLib for 2026 renamed several classes. If you find old examples online, they will use the wrong names. The correct 2026 class names are:

| Old Name (pre-2026) | New Name (2026) |
|---------------------|-----------------|
| `CANSparkMax` | `SparkMax` |
| `CANSparkMaxLowLevel.MotorType` | `SparkLowLevel.MotorType` |
| `SparkPIDController` | `SparkClosedLoopController` |
| `SparkMaxRelativeEncoder` | `RelativeEncoder` (same) |
| `sparkMax.setSoftLimit(...)` | `SparkMaxConfig` → `config.softLimit.forwardSoftLimit(val)` |
| `sparkMax.getPIDController()` | `sparkMax.getClosedLoopController()` |
| `controller.setReference(val, type)` | `controller.setSetpoint(val, ControlType.kPosition)` |
| `sparkMax.setSmartCurrentLimit(amps)` | `SparkMaxConfig` → `config.smartCurrentLimit(amps)` |
| `sparkMax.setInverted(true)` | `SparkMaxConfig` → `config.inverted(true)` |
| `sparkMax.setIdleMode(mode)` | `SparkMaxConfig` → `config.idleMode(IdleMode.kBrake)` |

**Imports** — use `com.revrobotics.spark.*` and `com.revrobotics.spark.config.*`:
```java
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.ClosedLoopConfig;
import com.revrobotics.RelativeEncoder;       // note: directly under com.revrobotics
import com.revrobotics.ResetMode;             // note: directly under com.revrobotics
import com.revrobotics.PersistMode;           // note: directly under com.revrobotics
```

**Important — Config-Object Pattern**: REVLib 2026 does NOT let you call setter methods directly on the Spark Max. Instead, build a `SparkMaxConfig` object, set everything on it, then apply it once:
```java
SparkMaxConfig config = new SparkMaxConfig();
config
    .inverted(false)
    .idleMode(IdleMode.kBrake)
    .smartCurrentLimit(40);
config.closedLoop
    .p(0.01).i(0).d(0)
    .outputRange(-1, 1);
config.softLimit
    .forwardSoftLimitEnabled(true)
    .forwardSoftLimit(120.0)
    .reverseSoftLimitEnabled(true)
    .reverseSoftLimit(0.0);

sparkMax.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
```
Old examples calling methods directly on the Spark Max **will not compile**. If you find code online using `sparkMax.set...()` for configuration, it's outdated.

### 5.1 Drivetrain

- [ ] **Constants**: Define in `Constants.java` (inner class `DrivetrainConstants`)
  - Drive speed limits (max translational m/s, max rotational rad/s)
  - Slow mode multiplier
  - Deadband values for joystick input
  - Field-oriented default (should be `true` — field-oriented on startup)
  - **Note**: Swerve CAN IDs, encoder channels, and gyro config do NOT go in `Constants.java` — they go in the YAGSL JSON config files (see below). YAGSL reads its configuration entirely from JSON.

- [ ] **YAGSL JSON config**: Fill in `src/main/deploy/swerve/` files
  - `swervedrive.json` — confirm IMU type
  - `modules/frontleft.json` — drive motor CAN ID, steer motor CAN ID, Thrifty encoder analog input channel, encoder offset, and module location (front/left distance from robot center in inches)
  - `modules/frontright.json` — same
  - `modules/backleft.json` — same
  - `modules/backright.json` — same
  - `controllerproperties.json` — joystick deadband and heading PID values
  - **Note**: YAGSL encoder config uses `"type": "thrifty"` and `"id"` is the roboRIO **analog input channel** (0-3), not a CAN ID or DIO port
  - **Note**: Each module JSON needs a `"location"` field with `"front"` and `"left"` distances (in inches) from the robot center to that module. For a square chassis, all four values will have the same magnitude — just flip the signs (e.g., front-left is +12/+12, front-right is +12/-12, etc.)
  - `modules/physicalproperties.json` — wheel diameter, robot dimensions, weight
  - `modules/pidfproperties.json` — drive and steer PID values (start with YAGSL defaults, tune on robot)

- [ ] **Driver camera**: In `RobotContainer` constructor, add `CameraServer.startAutomaticCapture()` — this starts streaming a USB camera to the Driver Station dashboard. One line of code, no subsystem needed.

- [ ] **Subsystem class**: Create `subsystems/DrivetrainSubsystem.java`
  - Initialize YAGSL `SwerveDrive` from JSON config using `new SwerveParser(directory).createSwerveDrive(maxSpeed)`
  - **Important (YAGSL 2026 change)**: YAGSL no longer auto-zeros the gyro on startup. You must manually zero the gyro — call `zeroGyro()` in `autonomousInit()` in `Robot.java`, or use `RobotModeTriggers`
  - `drive(Translation2d translation, double rotation, boolean fieldOriented)` method
  - `zeroGyro()` method to reset heading
  - `toggleFieldOriented()` — flips a boolean flag between field-oriented and robot-oriented
  - `isFieldOriented()` — returns current mode (used by the drive command and for dashboard display)
  - Expose odometry (robot pose) for auto and dashboard
  - `periodic()` — update dashboard with module states, gyro heading, pose, and current drive mode (field-oriented vs robot-oriented)

- [ ] **Commands**:
  - Default drive command: reads joystick axes, applies deadband, calls `drive()` passing the current `isFieldOriented()` state
  - Zero gyro command: bound to Start button
  - Toggle drive mode command: `runOnce(() -> drivetrain.toggleFieldOriented())` bound to Y button
  - Slow mode: modifier on the default drive command (hold bumper to reduce speed)

- [ ] **Bindings in `RobotContainer.java`**:
  - Set default command on drivetrain to joystick drive
  - Start button → zero gyro
  - Y button → toggle field-oriented / robot-oriented
  - Left bumper → slow mode toggle/hold

### 5.1b Limelight Vision (OPTIONAL — Phase 2)

**Do not attempt until basic swerve driving is fully working.** This enhances field-oriented drive by correcting gyro drift and positional error using AprilTag detections.

- [ ] **Install Limelight vendor library**: Download the LimelightLib JSON from the Limelight docs and add to `vendordeps/`

- [ ] **Configure Limelight hardware**:
  - Connect Limelight to the robot network (static IP: 10.88.85.11 by default for team 8885)
  - Access the Limelight web UI and set the pipeline to AprilTag detection
  - Set the camera's physical mounting position (height, angle, offset from robot center) in the Limelight UI

- [ ] **Add vision to `DrivetrainSubsystem.java`**:
  - In `periodic()`, poll the Limelight for AprilTag-based pose estimates using `LimelightHelpers.getBotPoseEstimate_wpiBlue()`
  - If a valid pose is returned (targets detected), feed it into YAGSL's pose estimator via `swerveDrive.addVisionMeasurement(pose, timestamp)`
  - Add standard deviations to weight how much to trust vision vs. odometry (vision is less precise at long range, more precise up close)
  - Display vision status on dashboard (targets seen, estimated pose)

- [ ] **Testing**:
  - Verify Limelight sees AprilTags on the field (check the web UI)
  - Drive around and watch the dashboard pose — it should stay accurate even after spinning in circles (which normally causes gyro drift)
  - If the pose jumps erratically, increase the standard deviations (trust vision less) or check the camera mounting position config

### 5.2 Shooter

- [ ] **Constants**: Define in `Constants.java` (inner class `ShooterConstants`)
  - Launcher motor CAN ID
  - Feeder motor CAN ID
  - Launcher voltage (starting point: ~10.5V)
  - Feeder voltage (starting point: ~9V)
  - Current limit per motor (60A)
  - Rev-up time threshold (~1 second)

- [ ] **Subsystem class**: Create `subsystems/ShooterSubsystem.java`
  - Initialize 2 Spark Max controllers in **brushed mode** (`new SparkMax(canId, SparkLowLevel.MotorType.kBrushed)`)
  - Set current limits on both motors (60A)
  - `runLauncher()` — set launcher motor to launch voltage
  - `runFeeder()` — set feeder motor to feed voltage
  - `stop()` — stop both motors
  - `isLauncherReady()` — returns true if launcher has been running for at least the rev-up time (use a `Timer`)
  - `revAndShoot()` — returns a `Command` (with `addRequirements(this)`) that runs the full rev-up + feed sequence. Used by both the operator trigger binding and autonomous. Internally: `initialize()` starts launcher + resets timer → `execute()` checks timer and starts feeder once rev-up time has elapsed → `end()` stops both motors. `isFinished()` returns false (runs until interrupted by trigger release or timeout).
  - `stopCommand()` — returns a `runOnce(() -> stop())` command. Used by auto to explicitly stop after shooting.
  - `periodic()` — update dashboard with motor output, current draw, launcher ready state

- [ ] **Commands**:
  - No separate command classes needed — `revAndShoot()` and `stopCommand()` on the subsystem handle everything.
  - The operator trigger binding uses `revAndShoot()` with `whileTrue` — hold to rev+shoot, release to stop.
  - Auto uses `revAndShoot().withTimeout(3.0)` followed by `stopCommand()`.

- [ ] **Bindings in `RobotContainer.java`**:
  - Right trigger (hold) → rev-and-shoot sequence: launcher spins up, feeder engages after ~1 sec, release stops everything
  - B button → emergency stop shooter (kills both motors immediately via `runOnce(() -> shooter.stop())`)

### 5.3 Intake

- [ ] **Constants**: Define in `Constants.java` (inner class `IntakeConstants`)
  - Arm motor CAN ID
  - Roller motor CAN ID
  - Arm gear ratio (needed to convert encoder rotations → arm degrees)
  - Stowed position (0° — or whatever encoder value corresponds to vertical)
  - Deployed position (~115° — tune on robot)
  - Arm soft limits (min and max encoder values)
  - Arm PID gains (P, I, D) — start with P only, add D if it oscillates
  - Arm max output / output cap (for compliance — e.g., limit to ±0.5 so the arm doesn't slam)
  - Roller motor speed (percent output or voltage)
  - Arm current limit
  - Roller current limit

- [ ] **Subsystem class**: Create `subsystems/IntakeSubsystem.java`
  - Initialize Spark Max for arm motor in **brushless mode** (NEO)
  - Initialize Spark Max for roller motor (brushed or brushless — confirm on robot)
  - Get the built-in `RelativeEncoder` from the arm Spark Max
  - Configure the encoder conversion factor: `setPositionConversionFactor(360.0 / gearRatio)` so positions are in degrees
  - Set **software soft limits** on the arm Spark Max. **Note**: REVLib 2026 uses a `SparkMaxConfig` object for configuration — soft limits are set via `config.softLimit.forwardSoftLimit(deployedLimit)` and `config.softLimit.reverseSoftLimit(stowedLimit)`, then applied with `sparkMax.configure(config)`. Do not use the old `setSoftLimit()` method — it no longer exists. This is hardware-level protection that works even if command logic has bugs.
  - Configure the built-in **PID controller** on the Spark Max (`getClosedLoopController()` — renamed in REVLib 2026, see API table above) with P, I, D gains and output range (cap the output for compliance)
  - `deploy()` — set PID target to deployed position via `closedLoopController.setSetpoint(deployedAngle, ControlType.kPosition)`
  - `stow()` — set PID target to stowed position via `closedLoopController.setSetpoint(stowedAngle, ControlType.kPosition)`
  - `runRollers()` — spin roller motor at intake speed
  - `stopRollers()` — stop roller motor
  - `stop()` — stop everything (arm PID off, rollers off)
  - `getArmAngle()` — return current arm angle in degrees from encoder
  - `isAtPosition(double target)` — returns true if arm is within a small tolerance of the target
  - `periodic()` — update dashboard with arm angle, roller state, PID error, current draw
  - **Important**: On robot startup, the arm should be stowed and the encoder should be zeroed (or the stowed position should be defined as the encoder's zero). If the arm isn't always in the same position at startup, you may need a calibration routine.

- [ ] **Commands**:
  - Use a `startEnd()` or `runEnd()` inline command bound with `whileTrue` — this is the simplest approach and matches the control scheme:
    - **On start**: call `deploy()` and `runRollers()`
    - **On end** (when trigger is released): call `stow()` and `stopRollers()`
  - This can be written as a single inline command in `RobotContainer`, no separate command class needed. Calling `startEnd()` on the subsystem automatically adds it as a requirement:
    ```java
    // "intake" is the IntakeSubsystem instance
    intake.startEnd(
        () -> { intake.deploy(); intake.runRollers(); },   // on start
        () -> { intake.stow(); intake.stopRollers(); }     // on end (trigger release)
    )
    ```
  - **A button (manual stow override)**: `runOnce(() -> { intake.stow(); intake.stopRollers(); })` — safety fallback if the auto-stow doesn't trigger

- [ ] **Bindings in `RobotContainer.java`** (on operator controller, port 1):
  - Left trigger (hold) → deploy arm + run rollers; release → stow arm + stop rollers
  - A button → manual stow override

### 5.4 Autonomous

- [ ] **Constants**: Define in `Constants.java` (inner class `AutoConstants`)
  - Auto drive speed (moderate — don't go full speed in auto)
  - Auto drive duration (seconds to drive forward — tune on the actual field)

- [ ] **Auto routines in `Autos.java`**:
  - **"Drive Forward and Shoot"**: `Commands.sequence()` that drives forward for a set time, stops, revs the launcher, feeds, then stops the shooter. Use `.withTimeout()` on each step as a safety net so no step runs forever if something goes wrong.
  - **"Do Nothing"**: `Commands.none()` — literally does nothing. Safe default.

- [ ] **Auto chooser in `RobotContainer.java`**:
  - Create a `SendableChooser<Command>`
  - Add "Do Nothing" as the **default** option
  - Add "Drive Forward and Shoot"
  - Publish in the constructor: `SmartDashboard.putData("Auto Chooser", autoChooser)` (this writes to NetworkTables — Elastic dashboard will pick it up)
  - Update `getAutonomousCommand()` to return `autoChooser.getSelected()`

- [ ] **DrivetrainSubsystem helper**: Add a `driveForward(double speed)` command method that drives the robot straight forward (no rotation) in **robot-oriented mode** (not field-oriented — in auto you want the robot to drive wherever it's currently facing, not relative to the field). Pair with `.withTimeout(seconds)` when called.

### 5.5 General / Infrastructure

- [ ] **Clean up template code**: Remove `ExampleSubsystem`, `ExampleSubsystemSkeleton`, `ExampleCommand`, and the example auto from `Autos.java` once real subsystems are in place

- [ ] **Add operator controller in `RobotContainer.java`**:
  - Create a second `CommandXboxController` for the operator on **port 1**
  - Add `kOperatorControllerPort = 1` to `Constants.OperatorConstants`
  - All mechanism bindings (intake, shooter) go on this controller
  - The existing driver controller on port 0 is only for drivetrain

- [ ] **Test mode**: Create a `TestMode.java` class that lets you spin individual motors one at a time at low speed to verify wiring, CAN IDs, and motor directions. Runs when the Driver Station is set to "Test" mode. Controls (on the operator controller):
  - D-pad Up/Down — cycle through motors (Launcher, Feeder, Intake Roller)
  - Right stick Y — spin the selected motor (capped at 20% output for safety)
  - Right bumper — nudge the intake arm +5° toward deployed
  - Left bumper — nudge the intake arm −5° toward stowed
  - B button — emergency stop all motors
  - The intake arm is NOT in the free-spin list because it has physical travel limits — the bumper nudge uses PID position control in small increments so you can't slam it into the frame
  - Wire it up in `Robot.java`: call `testMode.init()` in `testInit()` and `testMode.periodic()` in `testPeriodic()`
  - **Tip**: Before using test mode in code, test each Spark Max individually with the **REV Hardware Client** over USB — that lets you verify CAN IDs and brushed/brushless mode without deploying any code

- [ ] **Dashboard**: Publish key data so the drive team can see robot state. **Note**: SmartDashboard and Shuffleboard are deprecated in 2026 (removed in 2027). Use **Elastic** (by Team 353) as the dashboard instead — it reads from NetworkTables just like SmartDashboard did. You can still use `SmartDashboard.putNumber()`/`putString()`/`putBoolean()` calls in code to publish data (the calls write to NetworkTables, which Elastic reads), but the SmartDashboard _application_ itself should not be used.
  - Swerve module states, gyro heading, robot pose
  - Current drive mode (field-oriented vs robot-oriented)
  - Shooter state (idle / revving / shooting)
  - Intake arm angle, intake state (stowed / deployed / intaking)
  - Auto chooser widget
