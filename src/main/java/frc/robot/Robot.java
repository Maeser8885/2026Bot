package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

/**
 * Robot is the main class that WPILib calls during a match. Think of it as the "lifecycle manager"
 * for your robot code. You don't call these methods yourself — WPILib calls them automatically
 * at the right time during each phase of a match (disabled, autonomous, teleop, test).
 *
 * <p>The most important thing this class does is call {@code CommandScheduler.getInstance().run()}
 * every 20ms in {@link #robotPeriodic()}. That single line is what makes the entire command-based
 * framework work — it polls buttons, runs active commands, and calls subsystem periodic methods.
 *
 * <p>This class also handles transitions between match phases: starting the autonomous command
 * when auto begins, cancelling it when teleop starts, and zeroing the gyro for field-oriented
 * drive at the start of autonomous.
 */
public class Robot extends TimedRobot {
  private Command m_autonomousCommand;
  private final RobotContainer m_robotContainer;

  /**
   * Robot constructor — called once when the robot code first starts.
   * Creates the RobotContainer, which sets up all subsystems, controllers, and bindings.
   */
  public Robot() {
    m_robotContainer = new RobotContainer();
  }

  /**
   * Called every 20ms (~50 times per second), regardless of which mode the robot is in.
   * This is the heartbeat of the command-based framework — it:
   *   1. Checks all button/trigger bindings for state changes
   *   2. Runs the execute() method of every active command
   *   3. Calls periodic() on every registered subsystem
   * If you remove this line, nothing in the command framework will work.
   */
  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
  }

  /**
   * Called once when the robot enters disabled mode (at startup, between match phases, or
   * when the driver station is disconnected). Stops any test mode motors that might still
   * be running.
   */
  @Override
  public void disabledInit() {
    m_robotContainer.getTestMode().stopAll();
  }

  /** Called every 20ms while disabled. Usually empty — the robot shouldn't do anything. */
  @Override
  public void disabledPeriodic() {}

  /**
   * Called once at the start of the autonomous period (first 15 seconds of a match).
   * This is where we:
   *   1. Zero the gyro so field-oriented drive starts with the correct heading
   *   2. Retrieve whichever auto routine the drive team selected on the dashboard
   *   3. Schedule that command so the CommandScheduler starts running it
   */
  @Override
  public void autonomousInit() {
    // YAGSL 2026 breaking change: the gyro is no longer auto-zeroed on startup.
    // We must do it manually here so field-oriented drive works correctly.
    m_robotContainer.getDrivetrain().zeroGyro();

    // Get the selected auto routine from the dashboard chooser and start it
    m_autonomousCommand = m_robotContainer.getAutonomousCommand();
    if (m_autonomousCommand != null) {
      CommandScheduler.getInstance().schedule(m_autonomousCommand);
    }
  }

  /** Called every 20ms during autonomous. Empty because commands handle everything. */
  @Override
  public void autonomousPeriodic() {}

  /**
   * Called once at the start of the teleop (driver-controlled) period.
   * If an autonomous command is still running, cancel it so it doesn't interfere
   * with driver control. The drivetrain's default command (joystick driving) will
   * automatically take over once the auto command is cancelled.
   */
  @Override
  public void teleopInit() {
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
  }

  /** Called every 20ms during teleop. Empty because commands handle everything. */
  @Override
  public void teleopPeriodic() {}

  /**
   * Called once when entering test mode (selectable in the Driver Station).
   * Cancels all running commands and initializes the motor test interface.
   * See {@link TestMode} for controls and usage.
   */
  @Override
  public void testInit() {
    CommandScheduler.getInstance().cancelAll();
    m_robotContainer.getTestMode().init();
  }

  /**
   * Called every 20ms during test mode. Runs the motor test interface — reads the operator
   * controller and drives the selected motor at low speed. See {@link TestMode} for details.
   */
  @Override
  public void testPeriodic() {
    m_robotContainer.getTestMode().periodic();
  }

  /** Called once when simulation starts. Only relevant when running in the WPILib simulator. */
  @Override
  public void simulationInit() {}

  /** Called every 20ms during simulation. Only relevant when running in the WPILib simulator. */
  @Override
  public void simulationPeriodic() {}
}
