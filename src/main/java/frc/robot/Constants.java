package frc.robot;

/**
 * Constants is a central place for all the "magic numbers" in the robot code — things like
 * motor CAN IDs, speed limits, PID tuning values, and controller ports. By putting them all
 * in one file, you can easily find and change them without hunting through the rest of the code.
 *
 * <p>This class is organized into inner classes, one per subsystem:
 * <ul>
 *   <li>{@link OperatorConstants} — controller ports and joystick settings</li>
 *   <li>{@link DrivetrainConstants} — speed limits, slow mode, field-oriented default</li>
 *   <li>{@link ShooterConstants} — motor IDs, voltages, current limits, rev-up time</li>
 *   <li>{@link IntakeConstants} — motor IDs, arm angles, PID gains, roller speed</li>
 *   <li>{@link AutoConstants} — autonomous drive speed and duration</li>
 * </ul>
 *
 * <p><b>Important:</b> Many values in this file are placeholders (especially CAN IDs and tuning
 * values). They MUST be replaced with real values from the electrical and mechanical teams before
 * deploying to the robot. See {@code plan.md} section 1b for the full list of values needed.
 */
public final class Constants {

  public static class OperatorConstants {
    public static final int kDriverControllerPort = 0;
    public static final int kOperatorControllerPort = 1;
    public static final double kJoystickDeadband = 0.08;
  }

  public static class DrivetrainConstants {
    // Max speeds — start conservative, increase as drivers get comfortable
    public static final double kMaxSpeedMetersPerSecond = 3.0;
    public static final double kMaxAngularSpeedRadiansPerSecond = 2 * Math.PI; // 1 rotation/sec

    // Slow mode multiplier (hold bumper)
    public static final double kSlowModeMultiplier = 0.25;

    // Field-oriented drive enabled by default
    public static final boolean kFieldOrientedDefault = true;
  }

  public static class ShooterConstants {
    // CAN IDs — placeholder values, replace with real IDs from electrical team
    public static final int kLauncherMotorId = 20;
    public static final int kFeederMotorId = 21;

    // Voltages — starting points, tune on robot
    public static final double kLauncherVoltage = 10.5;
    public static final double kFeederVoltage = 9.0;

    // Current limit per motor (CIMs draw a lot at stall)
    public static final int kCurrentLimit = 60;

    // Time for launcher to spin up to speed
    public static final double kRevUpTimeSeconds = 1.0;
  }

  public static class IntakeConstants {
    // CAN IDs — placeholder values, replace with real IDs from electrical team
    public static final int kArmMotorId = 30;
    public static final int kRollerMotorId = 31;

    // Arm gear ratio — replace with actual value from mechanical team
    // This is the reduction between the NEO and the arm pivot (e.g., 100.0 means 100:1)
    public static final double kArmGearRatio = 100.0;

    // Arm positions in degrees (0 = stowed/vertical, positive = deployed)
    public static final double kStowedAngleDegrees = 0.0;
    public static final double kDeployedAngleDegrees = 115.0;

    // Soft limits — prevent arm from going past physical range
    public static final float kForwardSoftLimit = 120.0f;
    public static final float kReverseSoftLimit = -5.0f;

    // Arm PID gains — start with P only, add D if it oscillates
    public static final double kArmP = 0.02;
    public static final double kArmI = 0.0;
    public static final double kArmD = 0.0;

    // Arm PID output cap — limits max force for compliance
    public static final double kArmMaxOutput = 0.4;
    public static final double kArmMinOutput = -0.4;

    // Roller speed (percent output, 0.0 to 1.0)
    public static final double kRollerSpeed = 0.7;

    // Current limits
    public static final int kArmCurrentLimit = 40;
    public static final int kRollerCurrentLimit = 35;

    // Position tolerance for isAtPosition check
    public static final double kPositionToleranceDegrees = 3.0;
  }

  public static class AutoConstants {
    // Drive forward speed in m/s — keep it slow and controlled
    public static final double kDriveSpeed = 1.5;

    // Drive forward duration in seconds — tune on the actual field
    public static final double kDriveDurationSeconds = 2.0;

    // Timeout for the shoot sequence in auto
    public static final double kShootTimeoutSeconds = 3.0;
  }
}
