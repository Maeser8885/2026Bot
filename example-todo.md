# Example Branch — Remaining TODOs

This code compiles and builds. Hardware CAN IDs, motor types, and gear ratios have been filled in from the electrical and mechanical teams. What remains is on-robot tuning and optional enhancements.

See `plan.md` section 1b for detailed descriptions of each item.

---

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

These values can only be determined by testing. Start with the defaults in the code, then adjust.

### Drivetrain (first priority)

- [ ] **Encoder offsets** — With all swerve modules pointing straight forward, read each Thrifty encoder's raw value. Enter these in each module JSON file → `"absoluteEncoderOffset"` (currently all `0.0`).
- [ ] **Motor inversions** — If any swerve module drives or steers the wrong direction, set `"inverted"` → `"drive": true` or `"angle": true` in the module JSON file.
- [ ] **Drive/steer PID** — Tune in `pidfproperties.json` if modules oscillate or are sluggish.
- [ ] **Max speed** — Increase `DrivetrainConstants.kMaxSpeedMetersPerSecond` (currently `3.0`) as drivers get comfortable.
- [ ] **Slow mode multiplier** — Adjust `DrivetrainConstants.kSlowModeMultiplier` (currently `0.25`) based on driver preference.
- [ ] **Joystick deadband** — Increase `OperatorConstants.kJoystickDeadband` (currently `0.08`) if the robot drifts when sticks are released.

### Shooter

- [ ] **Motor directions** — If the launcher or feeder spins the wrong way, change `.inverted(false)` to `.inverted(true)` in `ShooterSubsystem.java`.
- [ ] **Launcher voltage** — Adjust `ShooterConstants.kLauncherVoltage` (currently `10.5`).
- [ ] **Feeder voltage** — Adjust `ShooterConstants.kFeederVoltage` (currently `9.0`).
- [ ] **Rev-up time** — Adjust `ShooterConstants.kRevUpTimeSeconds` (currently `1.0`). Listen to the motor — when the pitch stops climbing, it's at speed.

### Intake

- [ ] **Arm motor direction** — If the arm deploys the wrong way, change `.inverted(false)` to `.inverted(true)` in `IntakeSubsystem.java`. **Test at very low power first.**
- [ ] **Roller motor direction** — If rollers push fuel outward instead of inward, change `.inverted(false)` to `.inverted(true)` in `IntakeSubsystem.java`.
- [ ] **Deployed angle** — Adjust `IntakeConstants.kDeployedAngleDegrees` (currently `115.0`). Deploy the arm to ground level and read the encoder value from the dashboard.
- [ ] **Arm PID P gain** — Adjust `IntakeConstants.kArmP` (currently `0.02`). If the arm barely moves, increase. If it oscillates or slams, decrease.
- [ ] **Arm PID D gain** — Add `IntakeConstants.kArmD` (currently `0.0`) only if the arm oscillates around its target position.
- [ ] **Arm output cap** — Adjust `IntakeConstants.kArmMaxOutput` / `kArmMinOutput` (currently `0.4`). Increase if arm can't hold position; decrease if it's too stiff and jams fuel.
- [ ] **Roller speed** — Adjust `IntakeConstants.kRollerSpeed` (currently `0.7`). Increase if fuel doesn't make it over the bumper.

### Autonomous

- [ ] **Auto drive speed** — Adjust `AutoConstants.kDriveSpeed` (currently `1.5` m/s).
- [ ] **Auto drive duration** — Adjust `AutoConstants.kDriveDurationSeconds` (currently `2.0`). Measure on the actual field.

---

## 3. Optional Enhancements

Only attempt these after everything above is working:

- [ ] **Limelight AprilTag vision** — Limelight 4 is available. See `plan.md` section 5.1b for full instructions. Needs to be physically mounted first, then configure mounting position in code. Adds vision-corrected odometry for more accurate field-oriented drive.
- [ ] **USB driver camera** — Once installed, verify streaming works. The code already calls `CameraServer.startAutomaticCapture()` in `RobotContainer`.
- [ ] **Dashboard layout** — Set up an Elastic dashboard layout showing all the telemetry data (drive mode, shooter state, arm angle, auto chooser). SmartDashboard and Shuffleboard are deprecated in 2026 — use Elastic instead.

---

## Quick Reference: Where to Change Things

| What to change | File | Location |
|---------------|------|----------|
| Encoder offsets | `src/main/deploy/swerve/modules/*.json` | `"absoluteEncoderOffset"` |
| Swerve motor inversions | `src/main/deploy/swerve/modules/*.json` | `"inverted"` → `"drive"`/`"angle"` |
| Drive/steer PID | `src/main/deploy/swerve/modules/pidfproperties.json` | `"drive"`/`"angle"` PID values |
| All tuning values | `src/main/java/frc/robot/Constants.java` | Various inner classes |
| Shooter motor inversions | `ShooterSubsystem.java` | `.inverted(true/false)` in constructor |
| Intake motor inversions | `IntakeSubsystem.java` | `.inverted(true/false)` in constructor |
