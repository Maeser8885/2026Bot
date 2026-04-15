package frc.robot.subsystems;

/**
 * Constants for the intake subsystem.
 *
 * <p>CAN IDs and gear ratio are confirmed from the electrical/mechanical teams.
 * PID gains, arm angles, and roller speed are starting defaults — tune on the robot.
 */
public final class IntakeConstants {
  // CAN IDs
  public static final int kArmMotorId = 50;
  public static final int kRollerMotorId = 48;

  // Arm gear ratio (20:1 reduction between NEO and arm pivot)
  public static final double kArmGearRatio = 20.0;

  // Arm positions in degrees (0 = stowed/vertical, positive = deployed)
  public static final double kStowedAngleDegrees = 0.0;
  public static final double kDeployedAngleDegrees = 90.0;

  // Soft limits — prevent arm from going past physical range
  public static final float kForwardSoftLimit = 100.0f;
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
