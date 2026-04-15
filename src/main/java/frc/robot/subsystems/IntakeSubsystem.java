package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.Subsystem;

/**
 * IntakeSubsystem is a <b>Java interface</b> — it defines what an intake can DO without specifying
 * HOW it does it. Think of it as a contract: any class that {@code implements IntakeSubsystem}
 * promises to provide all of these methods.
 *
 * <p><b>Why use an interface?</b> We have two different ways to control the intake arm:
 * <ul>
 *   <li>{@link IntakeSparkMaxPid} — uses the Spark Max's built-in PID controller.
 *       Simpler, but can't do gravity compensation or asymmetric compliance.</li>
 *   <li>{@link IntakeRoboRioPid} — runs the PID loop on the roboRIO.
 *       More advanced: adds gravity feedforward and different output limits for
 *       "firm resist downward" vs "soft give for ball intake."</li>
 * </ul>
 *
 * <p>Both classes implement this same interface, so the rest of the robot code
 * ({@code RobotContainer}, {@code TestMode}, etc.) doesn't care which one is being used.
 * It just calls {@code intake.deploy()} and the right thing happens. This is called
 * <b>polymorphism</b> — one interface, multiple implementations.
 *
 * <p>The choice of which implementation to use is made in {@code RobotContainer} based on
 * {@code Constants.kUseRoboRioPid}. Flip that boolean and the entire intake switches behavior
 * without changing any other file.
 *
 * <p>This interface extends WPILib's {@link Subsystem}, which gives us access to command-framework
 * methods like {@code startEnd()}, {@code runOnce()}, and {@code run()} — those are used in
 * {@code RobotContainer} to create inline commands from button bindings.
 */
public interface IntakeSubsystem extends Subsystem {

  /** Deploy the intake arm to ground level. */
  void deploy();

  /** Stow the intake arm back to vertical. */
  void stow();

  /** Spin the roller wheels to intake fuel. */
  void runRollers();

  /** Stop the roller wheels. */
  void stopRollers();

  /** Stop everything — arm and rollers. */
  void stop();

  /** Get the current arm angle in degrees (0 = stowed, ~115 = deployed). */
  double getArmAngle();

  /** Returns true if the arm is within tolerance of the target angle. */
  boolean isAtPosition(double targetDegrees);

  /** Set the roller motor to a raw percent output (-1.0 to 1.0). For test mode only. */
  void setRollerPercent(double percent);

  /**
   * Nudge the arm target position by the given number of degrees. Positive = toward deployed,
   * negative = toward stowed. Clamped to soft limits. For test mode.
   */
  void nudgeArmTarget(double deltaDegrees);
}
