package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.Constants.AutoConstants;
import frc.robot.subsystems.DrivetrainSubsystem;
import frc.robot.subsystems.ShooterSubsystem;

/**
 * Autos is a utility class that builds autonomous command sequences. In FRC, "autonomous" is the
 * first 15 seconds of a match where the robot runs pre-programmed routines with no driver input.
 *
 * <p>Each method in this class returns a {@link Command} that represents a complete autonomous
 * routine. Commands are composed using {@link Commands#sequence(Command...)} to chain actions
 * together — "do this, then this, then this." Each step has a {@code .withTimeout()} as a safety
 * net so no step runs forever if something goes wrong.
 *
 * <p>The auto chooser in {@link frc.robot.RobotContainer} lets the drive team select which routine
 * to run from the dashboard before the match starts. The default is "Do Nothing" for safety.
 *
 * <p>This is a utility class (private constructor, static methods only) — you never instantiate it.
 */
public final class Autos {

  /**
   * Simple autonomous: drive forward, stop, rev up and shoot the preloaded fuel.
   * Total time: ~5-6 seconds. Leaves plenty of margin in the 15-second auto period.
   */
  public static Command driveForwardAndShoot(
      DrivetrainSubsystem drivetrain, ShooterSubsystem shooter) {
    return Commands.sequence(
        // Step 1: Drive forward at moderate speed for a set duration
        drivetrain.driveForward(AutoConstants.kDriveSpeed)
            .withTimeout(AutoConstants.kDriveDurationSeconds),

        // Step 2: Stop driving
        drivetrain.stopCommand(),

        // Step 3: Rev up launcher and shoot (auto-feeds after rev-up time)
        shooter.revAndShoot()
            .withTimeout(AutoConstants.kShootTimeoutSeconds),

        // Step 4: Stop shooter
        shooter.stopCommand()
    );
  }

  /** Does nothing. Safe default — 0 auto points beats crashing into the field. */
  public static Command doNothing() {
    return Commands.none();
  }

  private Autos() {
    throw new UnsupportedOperationException("This is a utility class!");
  }
}
