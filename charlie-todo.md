# Charlie Branch — Code Review & TODO

Analysis of the `Charlie` branch compared to `plan.md`. Last reviewed: 2026-03-25.

---

## What's Done

### Drivetrain (5.1) — ~70%

**Completed:**
- YAGSL JSON config files fully set up with real CAN IDs from the electrical team:
  - Front-left: drive=57, steer=49, Thrifty encoder=analog 1
  - Front-right: drive=55, steer=56, Thrifty encoder=analog 2
  - Back-left: drive=62, steer=59, Thrifty encoder=analog 0
  - Back-right: drive=53, steer=60, Thrifty encoder=analog 3
- `swervedrive.json` — IMU configured as `navx_spi`
- `physicalproperties.json` — robot mass (110.23 lbs), wheel diameter (4"), drive gear ratio (5.9:1), angle gear ratio (18.75:1), current limits (drive 40A, angle 20A), ramp rates
- `pidfproperties.json` — initial PID values in place (P=0.0020645 for both drive and angle)
- `controllerproperties.json` — heading PID (P=0.4, D=0.01), joystick radius deadband (0.5)
- `DriveSubsystem.java` — YAGSL SwerveDrive created from JSON, field-oriented drive commands (angular velocity and direct angle variants), vision pose update method
- `Constants.DriveConstants.maxSpeed` set to 3 ft/s (conservative starting value)
- `RobotContainer.java` — all 8 swerve SparkMax motors declared, 4 analog encoders declared, `debugPeriodic()` publishes encoder values to SmartDashboard
- Module locations filled in (front/back: 10.625", left/right: 10.375")

**Still TODO (per plan):**
- [ ] Add `toggleFieldOriented()` and `isFieldOriented()` to DriveSubsystem
- [ ] Implement slow mode (left bumper hold → reduced speed)
- [ ] Add `zeroGyro()` command bound to Start button
- [ ] Bind Y button to toggle field-oriented/robot-oriented
- [ ] Set default drive command on drivetrain subsystem (joystick → drive with deadband)
- [ ] Add driver camera: `CameraServer.startAutomaticCapture()` in RobotContainer
- [ ] Publish module states, gyro heading, pose, and drive mode in `periodic()`
- [ ] Encoder absolute offsets are all 0 — need calibration on robot
- [ ] Call `zeroGyro()` in `autonomousInit()` in Robot.java (YAGSL 2026 no longer auto-zeros)

---

### Shooter (5.2) — ~30%

**Completed:**
- `ShooterSubsystem.java` — CAN SparkMax version with `shootAndFeed()`, `unfeed()`, `spinUpAndShoot()` (rev-up with delay from constants), `stop()`
- `NewShooterSubsystem.java` — alternate PWM Spark version with simple `start()`/`stop()`
- `Constants.ShooterConstants` — structure exists with `shootToFeedDelay = 1 second`

**Still TODO (per plan):**
- [ ] Pick ONE shooter subsystem approach (plan says CAN SparkMax in brushed mode — use `ShooterSubsystem.java`, delete `NewShooterSubsystem.java`)
- [ ] Fill in real CAN IDs for launcher and feeder motors (currently all 0)
- [ ] Set real motor speeds/voltages (currently 0 — nothing will spin)
  - Launcher starting point: ~10.5V
  - Feeder starting point: ~9V
- [ ] Add current limit config (60A per motor) using SparkMaxConfig pattern
- [ ] Configure motors in **brushed mode** (`MotorType.kBrushed`) — plan says CIM motors
- [ ] Bind right trigger (operator controller) → `revAndShoot()` with `whileTrue`
- [ ] Bind B button (operator controller) → emergency stop
- [ ] Add `periodic()` dashboard publishing (motor output, current draw, launcher ready state)
- [ ] Add `stopCommand()` for use in autonomous

---

### Vision / Limelight (5.1b) — ~15%

**Completed:**
- `LimelightHelpers.java` — full library imported (v1.14)
- `VisionSubsystem.java` — exists but minimal (just initializes camera pose)
- `DriveSubsystem.updateVisionPose()` — method exists to feed vision data into YAGSL pose estimator

**Still TODO (per plan):**
- [ ] This is Phase 2 — do NOT work on this until basic driving is solid
- [ ] Poll Limelight for AprilTag pose estimates in `periodic()`
- [ ] Feed valid poses into `swerveDrive.addVisionMeasurement()`
- [ ] Add standard deviations for vision trust weighting
- [ ] Display vision status on dashboard (targets seen, estimated pose)
- [ ] Configure Limelight hardware (static IP, pipeline, mounting position)

---

### Intake (5.3) — 0% Not Started

- [ ] Create `Constants.IntakeConstants` with all values (CAN IDs, gear ratio, positions, PID gains, speed, current limits)
- [ ] Create `subsystems/IntakeSubsystem.java`
  - Arm motor (NEO, brushless) with SparkMax
  - Roller motor (TBD type) with SparkMax
  - RelativeEncoder with position conversion factor from gear ratio
  - Software soft limits on arm (SparkMaxConfig pattern)
  - PID position control via SparkClosedLoopController
  - `deploy()`, `stow()`, `runRollers()`, `stopRollers()`, `stop()`
  - `getArmAngle()`, `isAtPosition(target)`
  - `periodic()` with dashboard publishing
- [ ] Bind left trigger (operator) → deploy + run rollers; release → stow + stop rollers
- [ ] Bind A button (operator) → manual stow override

---

### Autonomous (5.4) — 0% Not Started

- [ ] Create `Constants.AutoConstants` (drive speed, drive duration)
- [ ] Create auto routines in `Autos.java`:
  - "Drive Forward and Shoot" — `Commands.sequence()` with timeouts
  - "Do Nothing" — `Commands.none()`
- [ ] Add `SendableChooser<Command>` in RobotContainer with "Do Nothing" as default
- [ ] Publish auto chooser to SmartDashboard
- [ ] Update `getAutonomousCommand()` to return `autoChooser.getSelected()`
- [ ] Add `driveForward(speed)` helper command on DriveSubsystem (robot-oriented, straight forward)

---

### General / Infrastructure (5.5) — ~15%

- [ ] Add operator controller (`CommandXboxController` on port 1) in RobotContainer
- [ ] Add `Constants.OperatorConstants.kOperatorControllerPort = 1`
- [ ] Remove `ExampleSubsystem`, `ExampleSubsystemSkeleton`, `ExampleCommand`, and example auto from `Autos.java`
- [ ] Create `TestMode.java` for motor-by-motor verification
  - D-pad Up/Down to cycle motors
  - Right stick Y to spin selected motor (capped at 20%)
  - Bumpers to nudge intake arm +/-5 degrees
  - B for emergency stop
  - Wire into `Robot.java` testInit/testPeriodic
- [ ] Set up full dashboard publishing (Elastic-compatible via SmartDashboard calls):
  - Swerve module states, gyro heading, robot pose
  - Current drive mode (field-oriented vs robot-oriented)
  - Shooter state (idle / revving / shooting)
  - Intake arm angle, intake state
  - Auto chooser widget

---

## Other Notes

- Charlie added `PathplannerLib-2026.1.2` to vendordeps — could be useful for more advanced autonomous paths later, but the plan only calls for a simple timed drive-forward auto for now.
- The `RobotContainer` currently declares SparkMax and AnalogInput objects directly (outside of the subsystem) — these should ideally live inside the subsystems that own them.
- Two shooter subsystems exist (`ShooterSubsystem` and `NewShooterSubsystem`) — need to consolidate to one.
- The `build.gradle` has a change (sourceCompatibility bumped or similar) — verify this is intentional.
