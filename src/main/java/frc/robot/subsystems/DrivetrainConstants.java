package frc.robot.subsystems;

/**
 * Constants for the swerve drivetrain subsystem.
 *
 * <p>Swerve module CAN IDs, encoder channels, gear ratios, and physical properties
 * are NOT here — they live in the YAGSL JSON config files under
 * {@code src/main/deploy/swerve/}. YAGSL reads those directly.
 *
 * <p>This class only contains values used by {@link DrivetrainSubsystem} Java code:
 * speed limits, slow mode, and drive mode defaults.
 */
public final class DrivetrainConstants {
  // Max speeds — start conservative, increase as drivers get comfortable
  public static final double kMaxSpeedMetersPerSecond = 3.0;
  public static final double kMaxAngularSpeedRadiansPerSecond = 2 * Math.PI; // 1 rotation/sec

  // Slow mode multiplier (hold bumper)
  public static final double kSlowModeMultiplier = 0.25;

  // Field-oriented drive enabled by default
  public static final boolean kFieldOrientedDefault = true;
}
