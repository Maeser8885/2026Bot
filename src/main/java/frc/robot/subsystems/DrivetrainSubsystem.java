package frc.robot.subsystems;

import java.io.File;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.DrivetrainConstants;
import swervelib.SwerveDrive;
import swervelib.parser.SwerveParser;

/**
 * DrivetrainSubsystem controls the swerve drive — the four independently steerable wheel modules
 * that let the robot move in any direction while facing any direction.
 *
 * <p>In FRC, a "subsystem" represents a physical mechanism on the robot. This subsystem owns
 * all four swerve modules, the gyroscope, and the math that coordinates them. It uses YAGSL
 * (Yet Another Generic Swerve Library) to handle the complex swerve kinematics, so you don't
 * have to write the swerve math yourself — YAGSL reads JSON config files from the
 * {@code src/main/deploy/swerve/} directory and sets everything up automatically.
 *
 * <p>This subsystem supports two drive modes:
 * <ul>
 *   <li><b>Field-oriented</b> (default) — "forward" on the joystick always means the same
 *       direction on the field, regardless of which way the robot is facing. The gyro
 *       compensates for robot heading.</li>
 *   <li><b>Robot-oriented</b> — "forward" means wherever the robot's front is pointing.
 *       Toggle with the Y button on the driver controller.</li>
 * </ul>
 *
 * <p>The driver also has a "slow mode" (left bumper) for precise alignment and a gyro reset
 * button (Start) to re-zero field-oriented drive.
 */
public class DrivetrainSubsystem extends SubsystemBase {

  private final SwerveDrive swerveDrive;
  private boolean fieldOriented = DrivetrainConstants.kFieldOrientedDefault;

  public DrivetrainSubsystem() {
    try {
      File swerveJsonDirectory = new File(Filesystem.getDeployDirectory(), "swerve");
      swerveDrive = new SwerveParser(swerveJsonDirectory)
          .createSwerveDrive(DrivetrainConstants.kMaxSpeedMetersPerSecond);
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize swerve drive from JSON config", e);
    }
  }

  /**
   * Drive the robot with translation and rotation inputs.
   *
   * @param translation Translation2d (x = forward/backward, y = left/right) in m/s
   * @param rotation    Rotation speed in rad/s
   * @param fieldRelative Whether to drive field-oriented or robot-oriented
   */
  public void drive(Translation2d translation, double rotation, boolean fieldRelative) {
    swerveDrive.drive(translation, rotation, fieldRelative, false);
  }

  /** Zero the gyro heading. Call this to re-zero field-oriented drive. */
  public void zeroGyro() {
    swerveDrive.zeroGyro();
  }

  /** Toggle between field-oriented and robot-oriented drive modes. */
  public void toggleFieldOriented() {
    fieldOriented = !fieldOriented;
  }

  /** Returns true if currently in field-oriented mode. */
  public boolean isFieldOriented() {
    return fieldOriented;
  }

  /**
   * Returns a command that drives the robot straight forward (robot-oriented)
   * at the given speed. Pair with .withTimeout() to limit distance.
   */
  public Command driveForward(double speedMetersPerSecond) {
    return run(() -> drive(
        new Translation2d(speedMetersPerSecond, 0),
        0,
        false // robot-oriented for auto
    ));
  }

  /** Returns a command that immediately stops the drivetrain. */
  public Command stopCommand() {
    return runOnce(() -> drive(new Translation2d(0, 0), 0, false));
  }

  @Override
  public void periodic() {
    SmartDashboard.putBoolean("Drive/Field Oriented", fieldOriented);
    SmartDashboard.putNumber("Drive/Gyro Heading", swerveDrive.getYaw().getDegrees());
    SmartDashboard.putString("Drive/Pose",
        swerveDrive.getPose().getTranslation().toString());
  }
}
