package frc.robot;

/**
 * Robot-wide constants that are shared across multiple subsystems.
 *
 * <p>Subsystem-specific constants live next to their subsystem to minimize merge conflicts
 * when multiple students work in parallel:
 * <ul>
 *   <li>{@code subsystems/DrivetrainConstants.java} — speed limits, slow mode</li>
 *   <li>{@code subsystems/ShooterConstants.java} — CAN IDs, voltages, current limits</li>
 *   <li>{@code subsystems/IntakeConstants.java} — CAN IDs, arm angles, PID gains</li>
 * </ul>
 *
 * <p>This file only contains values used by multiple files (controller ports, auto settings).
 */
public final class Constants {

  // --- Intake PID mode ---
  // false = IntakeSparkMaxPid (simpler, onboard PID)
  // true  = IntakeRoboRioPid (gravity feedforward + asymmetric compliance)
  public static final boolean kUseRoboRioPid = false;

  public static class OperatorConstants {
    public static final int kDriverControllerPort = 0;
    public static final int kOperatorControllerPort = 1;
    public static final double kJoystickDeadband = 0.08;
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
