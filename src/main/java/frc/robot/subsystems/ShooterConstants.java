package frc.robot.subsystems;

/**
 * Constants for the shooter subsystem.
 *
 * <p>CAN IDs are confirmed from the electrical team. Voltages and rev-up time
 * are starting defaults — tune on the robot.
 */
public final class ShooterConstants {
  // CAN IDs
  public static final int kLauncherMotorId = 58;
  public static final int kFeederMotorId = 54;

  // Voltages — starting points, tune on robot
  public static final double kLauncherVoltage = 10.5;
  public static final double kFeederVoltage = 9.0;

  // Current limit per motor (CIMs draw a lot at stall)
  public static final int kCurrentLimit = 60;

  // Time for launcher to spin up to speed
  public static final double kRevUpTimeSeconds = 1.0;
}
