# Example Branch — Remaining TODOs

<<<<<<< HEAD
This code compiles and builds, but it will **not work on the robot** until the placeholder values are replaced with real hardware data. This checklist covers everything that needs to be done.
=======
This code compiles and builds. Hardware CAN IDs, motor types, and gear ratios have been filled in from the electrical and mechanical teams. What remains is on-robot tuning and optional enhancements.
>>>>>>> 1e8b5f0cbe7ff02a3f73d7db1f3ca5c62a72c5ff

See `plan.md` section 1b for detailed descriptions of each item.

---

<<<<<<< HEAD
## 1. Get Information from Electrical Team

These values must come from the people who wired the robot. Use the REV Hardware Client to read CAN IDs from each Spark Max.

### Spark Max CAN IDs

Update these in `Constants.java` and the YAGSL JSON module files.

**Drivetrain** (update in `src/main/deploy/swerve/modules/*.json`):
- [ ] Front-left drive motor CAN ID (currently `1` in `frontleft.json`)
- [ ] Front-left steer motor CAN ID (currently `2` in `frontleft.json`)
- [ ] Front-right drive motor CAN ID (currently `3` in `frontright.json`)
- [ ] Front-right steer motor CAN ID (currently `4` in `frontright.json`)
- [ ] Back-left drive motor CAN ID (currently `5` in `backleft.json`)
- [ ] Back-left steer motor CAN ID (currently `6` in `backleft.json`)
- [ ] Back-right drive motor CAN ID (currently `7` in `backright.json`)
- [ ] Back-right steer motor CAN ID (currently `8` in `backright.json`)

**Shooter** (update in `Constants.java` → `ShooterConstants`):
- [ ] Launcher motor CAN ID (currently `20`)
- [ ] Feeder motor CAN ID (currently `21`)

**Intake** (update in `Constants.java` → `IntakeConstants`):
- [ ] Arm motor CAN ID (currently `30`)
- [ ] Roller motor CAN ID (currently `31`)

### Thrifty Encoder Analog Input Channels

Update in `src/main/deploy/swerve/modules/*.json` → `"encoder"` → `"id"` field.

- [ ] Front-left encoder analog input (currently `0` in `frontleft.json`)
- [ ] Front-right encoder analog input (currently `1` in `frontright.json`)
- [ ] Back-left encoder analog input (currently `2` in `backleft.json`)
- [ ] Back-right encoder analog input (currently `3` in `backright.json`)

### Spark Max Mode Confirmation

Verify each Spark Max is set to the correct mode (LED color). Wrong mode = motor won't spin or will behave erratically.

- [ ] All 8 swerve Spark Maxes → **brushless mode** (NEO motors)
- [ ] Both shooter Spark Maxes → **brushed mode** (CIM motors)
- [ ] Intake arm Spark Max → **brushless mode** (NEO motor)
- [ ] Intake roller Spark Max → **brushed or brushless** depending on motor type (see below)

---

## 2. Get Information from Mechanical Team

### Must-Know Hardware Details

- [ ] **Gyro / IMU type and connection** — Update `src/main/deploy/swerve/swervedrive.json` → `"imu"` → `"type"`. Currently set to `"navx_usb"`. Change if using NavX MXP (`"navx_mxp"`), Pigeon2 (`"pigeon2"`), etc.
- [ ] **Intake roller motor type** — Is it a NEO, NEO 550, CIM, mini-CIM, or something else? If brushless, change `MotorType.kBrushed` to `MotorType.kBrushless` in `IntakeSubsystem.java` line 28.
- [ ] **Intake arm gear ratio** — Update `Constants.java` → `IntakeConstants.kArmGearRatio` (currently `100.0`). This is the total reduction between the NEO output shaft and the arm pivot. Get this from the mechanical team or count the gear teeth.
- [ ] **Swerve module gear ratio (MK4n variant)** — L1, L2, or L3? Update `src/main/deploy/swerve/modules/physicalproperties.json` → `"drive"` → `"gearRatio"` (currently `6.75`, which is typical for L2). Also update `"angle"` → `"gearRatio"` if different from default `28.125`.
- [ ] **Robot dimensions** — Track width and wheelbase (center-to-center distance between swerve modules) in inches. Update the `"location"` → `"front"` and `"left"` values in each module JSON file (currently all set to `12` inches).
- [ ] **Swerve wheel diameter** — Update `physicalproperties.json` → `"drive"` → `"diameter"` (currently `4` inches). Confirm on the actual modules.
- [ ] **Robot weight** — Update `physicalproperties.json` → `"robotMass"` (currently `50` kg).

---

## 3. Tune on the Physical Robot
=======
## 1. Hardware Info — DONE

All CAN IDs, motor types, gear ratios, and physical properties have been entered into the code.

### Spark Max CAN IDs — All Set

**Drivetrain** (in `src/main/deploy/swerve/modules/*.json`):
- [x] Front-left: drive **57**, steer **49**, encoder analog **1**
- [x] Front-right: drive **55**, steer **56**, encoder analog **2**
- [x] Back-left: drive **62**, steer **59**, encoder analog **0**
- [x] Back-right: drive **53**, steer **60**, encoder analog **3**

**Shooter** (in `Constants.java` → `ShooterConstants`):
- [x] Launcher motor CAN ID: **58**
- [x] Feeder motor CAN ID: **54**

**Intake** (in `Constants.java` → `IntakeConstants`):
- [x] Arm motor CAN ID: **50**
- [x] Roller motor CAN ID: **48**

### Spark Max Mode Confirmation — All Confirmed

- [x] All 8 swerve Spark Maxes → **brushless mode** (NEO motors) — `sparkmax_neo` in YAGSL JSON
- [x] Both shooter Spark Maxes → **brushed mode** (CIM motors) — `MotorType.kBrushed` in code
- [x] Intake arm Spark Max → **brushless mode** (NEO motor) — `MotorType.kBrushless` in code
- [x] Intake roller Spark Max → **brushless mode** (NEO Vortex) — `MotorType.kBrushless` in code

### Hardware Details — All Set

- [x] **Gyro / IMU** — NavX via SPI (`navx_spi` in `swervedrive.json`)
- [x] **Intake roller motor type** — NEO Vortex (brushless), set in `IntakeSubsystem.java`
- [x] **Intake arm gear ratio** — 20:1 (`kArmGearRatio = 20.0` in `Constants.java`)
- [x] **Swerve drive gear ratio** — 5.9:1 (in `physicalproperties.json`)
- [x] **Swerve angle gear ratio** — 18.75:1 (in `physicalproperties.json`)
- [x] **Robot dimensions** — front/back 10.625", left/right 10.375" (in module JSON files)
- [x] **Swerve wheel diameter** — 4 inches (in `physicalproperties.json`)
- [x] **Robot weight** — 110.23 lbs (in `physicalproperties.json`)

### Still Waiting On

- [ ] **Driver camera** — Not yet installed on robot
- [ ] **Limelight 4** — Acquired but not yet mounted. Need mounting position (height, angle, offset from robot center) before vision code can be configured.

---

## 2. Tune on the Physical Robot
>>>>>>> 1e8b5f0cbe7ff02a3f73d7db1f3ca5c62a72c5ff

These values can only be determined by testing. Start with the defaults in the code, then adjust.

### Drivetrain (first priority)

- [ ] **Encoder offsets** — With all swerve modules pointing straight forward, read each Thrifty encoder's raw value. Enter these in each module JSON file → `"absoluteEncoderOffset"` (currently all `0.0`).
- [ ] **Motor inversions** — If any swerve module drives or steers the wrong direction, set `"inverted"` → `"drive": true` or `"angle": true` in the module JSON file.
- [ ] **Drive/steer PID** — Tune in `pidfproperties.json` if modules oscillate or are sluggish.
- [ ] **Max speed** — Increase `DrivetrainConstants.kMaxSpeedMetersPerSecond` (currently `3.0`) as drivers get comfortable.
- [ ] **Slow mode multiplier** — Adjust `DrivetrainConstants.kSlowModeMultiplier` (currently `0.25`) based on driver preference.
- [ ] **Joystick deadband** — Increase `OperatorConstants.kJoystickDeadband` (currently `0.08`) if the robot drifts when sticks are released.

### Shooter

<<<<<<< HEAD
- [ ] **Motor directions** — If the launcher or feeder spins the wrong way, change `.inverted(false)` to `.inverted(true)` in `ShooterSubsystem.java` (lines 29 and 35).
=======
- [ ] **Motor directions** — If the launcher or feeder spins the wrong way, change `.inverted(false)` to `.inverted(true)` in `ShooterSubsystem.java`.
>>>>>>> 1e8b5f0cbe7ff02a3f73d7db1f3ca5c62a72c5ff
- [ ] **Launcher voltage** — Adjust `ShooterConstants.kLauncherVoltage` (currently `10.5`).
- [ ] **Feeder voltage** — Adjust `ShooterConstants.kFeederVoltage` (currently `9.0`).
- [ ] **Rev-up time** — Adjust `ShooterConstants.kRevUpTimeSeconds` (currently `1.0`). Listen to the motor — when the pitch stops climbing, it's at speed.

### Intake

<<<<<<< HEAD
- [ ] **Arm motor direction** — If the arm deploys the wrong way, change `.inverted(false)` to `.inverted(true)` in `IntakeSubsystem.java` (line 35). **Test at very low power first.**
- [ ] **Roller motor direction** — If rollers push fuel outward instead of inward, change `.inverted(false)` to `.inverted(true)` in `IntakeSubsystem.java` (line 64).
=======
- [ ] **Arm motor direction** — If the arm deploys the wrong way, change `.inverted(false)` to `.inverted(true)` in `IntakeSubsystem.java`. **Test at very low power first.**
- [ ] **Roller motor direction** — If rollers push fuel outward instead of inward, change `.inverted(false)` to `.inverted(true)` in `IntakeSubsystem.java`.
>>>>>>> 1e8b5f0cbe7ff02a3f73d7db1f3ca5c62a72c5ff
- [ ] **Deployed angle** — Adjust `IntakeConstants.kDeployedAngleDegrees` (currently `115.0`). Deploy the arm to ground level and read the encoder value from the dashboard.
- [ ] **Arm PID P gain** — Adjust `IntakeConstants.kArmP` (currently `0.02`). If the arm barely moves, increase. If it oscillates or slams, decrease.
- [ ] **Arm PID D gain** — Add `IntakeConstants.kArmD` (currently `0.0`) only if the arm oscillates around its target position.
- [ ] **Arm output cap** — Adjust `IntakeConstants.kArmMaxOutput` / `kArmMinOutput` (currently `0.4`). Increase if arm can't hold position; decrease if it's too stiff and jams fuel.
- [ ] **Roller speed** — Adjust `IntakeConstants.kRollerSpeed` (currently `0.7`). Increase if fuel doesn't make it over the bumper.

### Autonomous

- [ ] **Auto drive speed** — Adjust `AutoConstants.kDriveSpeed` (currently `1.5` m/s).
- [ ] **Auto drive duration** — Adjust `AutoConstants.kDriveDurationSeconds` (currently `2.0`). Measure on the actual field.

---

<<<<<<< HEAD
## 4. Optional Enhancements

Only attempt these after everything above is working:

- [ ] **Limelight AprilTag vision** — See `plan.md` section 5.1b for full instructions. Adds vision-corrected odometry for more accurate field-oriented drive.
- [ ] **USB driver camera** — Verify the USB camera is physically installed and streaming. The code already calls `CameraServer.startAutomaticCapture()` in `RobotContainer`.
=======
## 3. Optional Enhancements

Only attempt these after everything above is working:

- [ ] **Limelight AprilTag vision** — Limelight 4 is available. See `plan.md` section 5.1b for full instructions. Needs to be physically mounted first, then configure mounting position in code. Adds vision-corrected odometry for more accurate field-oriented drive.
- [ ] **USB driver camera** — Once installed, verify streaming works. The code already calls `CameraServer.startAutomaticCapture()` in `RobotContainer`.
>>>>>>> 1e8b5f0cbe7ff02a3f73d7db1f3ca5c62a72c5ff
- [ ] **Dashboard layout** — Set up an Elastic dashboard layout showing all the telemetry data (drive mode, shooter state, arm angle, auto chooser). SmartDashboard and Shuffleboard are deprecated in 2026 — use Elastic instead.

---

## Quick Reference: Where to Change Things

| What to change | File | Location |
|---------------|------|----------|
<<<<<<< HEAD
| Swerve CAN IDs | `src/main/deploy/swerve/modules/*.json` | `"drive"/"angle"` → `"id"` |
| Encoder channels | `src/main/deploy/swerve/modules/*.json` | `"encoder"` → `"id"` |
| Encoder offsets | `src/main/deploy/swerve/modules/*.json` | `"absoluteEncoderOffset"` |
| Module locations | `src/main/deploy/swerve/modules/*.json` | `"location"` → `"front"`/`"left"` |
| Swerve gear ratios | `src/main/deploy/swerve/modules/physicalproperties.json` | `"conversionFactors"` |
| Gyro type | `src/main/deploy/swerve/swervedrive.json` | `"imu"` → `"type"` |
| Shooter CAN IDs | `src/main/java/frc/robot/Constants.java` | `ShooterConstants` |
| Intake CAN IDs | `src/main/java/frc/robot/Constants.java` | `IntakeConstants` |
| All tuning values | `src/main/java/frc/robot/Constants.java` | Various inner classes |
| Motor inversions | `*Subsystem.java` files | `.inverted(true/false)` in constructor |
| Roller motor type | `IntakeSubsystem.java` | Line 28: `MotorType.kBrushed` |
=======
| Encoder offsets | `src/main/deploy/swerve/modules/*.json` | `"absoluteEncoderOffset"` |
| Swerve motor inversions | `src/main/deploy/swerve/modules/*.json` | `"inverted"` → `"drive"`/`"angle"` |
| Drive/steer PID | `src/main/deploy/swerve/modules/pidfproperties.json` | `"drive"`/`"angle"` PID values |
| All tuning values | `src/main/java/frc/robot/Constants.java` | Various inner classes |
| Shooter motor inversions | `ShooterSubsystem.java` | `.inverted(true/false)` in constructor |
| Intake motor inversions | `IntakeSubsystem.java` | `.inverted(true/false)` in constructor |
>>>>>>> 1e8b5f0cbe7ff02a3f73d7db1f3ca5c62a72c5ff
