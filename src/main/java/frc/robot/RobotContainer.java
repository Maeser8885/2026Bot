package frc.robot;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Constants.DrivetrainConstants;
import frc.robot.Constants.OperatorConstants;
import frc.robot.commands.Autos;
import frc.robot.subsystems.DrivetrainSubsystem;
import frc.robot.subsystems.IntakeSubsystem;
import frc.robot.subsystems.ShooterSubsystem;

/**
 * RobotContainer is the central wiring hub of the robot. This is where all the pieces get
 * connected together — think of it as the "blueprint" that defines what the robot is made of
 * and how the driver controls it.
 *
 * <p>This class is responsible for:
 * <ul>
 *   <li><b>Creating subsystems</b> — each physical mechanism (drivetrain, shooter, intake) is
 *       instantiated here as a subsystem object</li>
 *   <li><b>Creating controllers</b> — the Xbox controllers that drivers use are set up here
 *       (driver on port 0, operator on port 1)</li>
 *   <li><b>Binding buttons to commands</b> — this is where you define "when the operator holds
 *       the right trigger, run the shooter." Each button/trigger is connected to a command.</li>
 *   <li><b>Setting up autonomous</b> — the auto chooser lets the drive team select which
 *       autonomous routine to run before a match starts</li>
 *   <li><b>Starting the driver camera</b> — streams a USB camera feed to the dashboard</li>
 * </ul>
 *
 * <p>If you want to understand how the robot works at a high level, start here. Follow the
 * subsystem creation and button bindings to see what each input does.
 */
public class RobotContainer {

  // --- Subsystems ---
  // Each subsystem represents one physical mechanism on the robot.
  // They are created here so that RobotContainer can wire them to controllers and commands.
  private final DrivetrainSubsystem drivetrain = new DrivetrainSubsystem();
  private final ShooterSubsystem shooter = new ShooterSubsystem();
  private final IntakeSubsystem intake = new IntakeSubsystem();

  // --- Controllers ---
  // We use two Xbox controllers: one for the driver (movement) and one for the operator (mechanisms).
  // CommandXboxController is the command-based wrapper — it gives you Trigger objects for each
  // button so you can attach commands with .onTrue(), .whileTrue(), etc.
  // Port numbers must match how the controllers are plugged into the Driver Station laptop.
  private final CommandXboxController driverController =
      new CommandXboxController(OperatorConstants.kDriverControllerPort);   // Port 0
  private final CommandXboxController operatorController =
      new CommandXboxController(OperatorConstants.kOperatorControllerPort); // Port 1

  // --- Autonomous chooser ---
  // A dropdown on the dashboard that lets the drive team pick an auto routine before the match.
  // SendableChooser sends its options to the dashboard automatically.
  private final SendableChooser<Command> autoChooser = new SendableChooser<>();

  // --- Test mode ---
  // Lets you spin individual motors one at a time to verify wiring and direction.
  // Only active when the Driver Station is in "Test" mode.
  private final TestMode testMode;

  /**
   * Constructor — called once from Robot.java when the robot starts up.
   * Sets up the camera, default commands, button bindings, and auto chooser.
   */
  public RobotContainer() {
    // Start streaming the USB driver camera to the dashboard so the driver can see
    // when the robot's view is blocked. No extra code needed — CameraServer handles it.
    CameraServer.startAutomaticCapture();

    configureDefaultCommands();
    configureBindings();
    configureAutoChooser();

    // Test mode uses the operator controller to spin individual motors
    testMode = new TestMode(shooter, intake, operatorController);
  }

  /**
   * Default commands run whenever their subsystem isn't being used by another command.
   * The drivetrain's default command reads the joysticks and drives the robot — so the
   * driver always has control unless an autonomous command or other command takes over.
   */
  private void configureDefaultCommands() {
    drivetrain.setDefaultCommand(
        drivetrain.run(() -> {
          // Read joystick axes and apply deadband.
          // Why the negative signs? Xbox controller axes are inverted from what WPILib expects:
          //   - Pushing the stick FORWARD gives a NEGATIVE Y value from the controller
          //   - WPILib expects FORWARD to be POSITIVE
          // So we negate each axis to fix the direction.
          // applyDeadband() makes small stick movements (drift) read as exactly 0.
          double xSpeed = -MathUtil.applyDeadband(
              driverController.getLeftY(), OperatorConstants.kJoystickDeadband);
          double ySpeed = -MathUtil.applyDeadband(
              driverController.getLeftX(), OperatorConstants.kJoystickDeadband);
          double rotSpeed = -MathUtil.applyDeadband(
              driverController.getRightX(), OperatorConstants.kJoystickDeadband);

          // Slow mode — holding the left bumper scales all speeds down for precise alignment.
          // Great for lining up shots or navigating tight spaces.
          if (driverController.getHID().getLeftBumperButton()) {
            xSpeed *= DrivetrainConstants.kSlowModeMultiplier;
            ySpeed *= DrivetrainConstants.kSlowModeMultiplier;
            rotSpeed *= DrivetrainConstants.kSlowModeMultiplier;
          }

          // Convert from 0-1 joystick range to actual speeds (m/s and rad/s)
          xSpeed *= DrivetrainConstants.kMaxSpeedMetersPerSecond;
          ySpeed *= DrivetrainConstants.kMaxSpeedMetersPerSecond;
          rotSpeed *= DrivetrainConstants.kMaxAngularSpeedRadiansPerSecond;

          // Drive the robot. Field-oriented means "forward" is always the same field direction,
          // regardless of which way the robot is facing. Robot-oriented means "forward" is
          // wherever the robot's front is pointing.
          drivetrain.drive(
              new Translation2d(xSpeed, ySpeed),
              rotSpeed,
              drivetrain.isFieldOriented());
        })
    );
  }

  /**
   * Button bindings connect physical controller inputs to commands.
   *
   * <p>Key concepts:
   * <ul>
   *   <li><b>onTrue(command)</b> — runs the command ONCE when the button is first pressed</li>
   *   <li><b>whileTrue(command)</b> — runs the command as long as the button is held,
   *       then interrupts it (calls end()) when released</li>
   *   <li><b>startEnd(startAction, endAction)</b> — creates a command that runs startAction
   *       when it begins and endAction when it's interrupted/cancelled</li>
   *   <li><b>runOnce(action)</b> — creates a command that runs the action a single time
   *       and immediately finishes</li>
   *   <li><b>leftTrigger(0.5)</b> — the 0.5 is the threshold: the trigger counts as "pressed"
   *       when it's pulled more than 50% of the way down</li>
   * </ul>
   */
  private void configureBindings() {
    // =====================================================================
    //  DRIVER CONTROLLER (Port 0) — Movement only
    //  Left stick  = translate (move forward/back/left/right)
    //  Right stick = rotate (spin the robot)
    //  Left bumper = slow mode (hold)
    // =====================================================================

    // Start button → zero the gyro so "forward" matches the direction the robot is facing.
    // Use this at the start of a match or if field-oriented drive feels "off".
    driverController.start().onTrue(drivetrain.runOnce(drivetrain::zeroGyro));

    // Y button → toggle between field-oriented and robot-oriented drive.
    // Field-oriented is usually better, but robot-oriented can help when the driver
    // loses track of which way the robot is facing.
    driverController.y().onTrue(drivetrain.runOnce(drivetrain::toggleFieldOriented));

    // =====================================================================
    //  OPERATOR CONTROLLER (Port 1) — Intake + Shooter
    // =====================================================================

    // Left trigger (hold) → deploy the intake arm to the ground and spin the rollers.
    // When the operator releases the trigger, the arm stows back up and the rollers stop.
    // This uses startEnd(): the first lambda runs on press, the second runs on release.
    operatorController.leftTrigger(0.5).whileTrue(
        intake.startEnd(
            () -> { intake.deploy(); intake.runRollers(); },   // On press: arm down, rollers on
            () -> { intake.stow(); intake.stopRollers(); }     // On release: arm up, rollers off
        )
    );

    // Right trigger (hold) → rev the launcher motor, then automatically feed fuel once
    // the launcher is up to speed. Release to stop both motors.
    // revAndShoot() handles the full sequence internally (see ShooterSubsystem).
    operatorController.rightTrigger(0.5).whileTrue(shooter.revAndShoot());

    // A button → manually stow the intake arm. Safety override in case the arm gets
    // stuck in the deployed position (e.g., if the left trigger binding glitches).
    operatorController.a().onTrue(
        intake.runOnce(() -> { intake.stow(); intake.stopRollers(); })
    );

    // B button → emergency stop the shooter motors immediately.
    // Use if something goes wrong during a shot.
    operatorController.b().onTrue(shooter.stopCommand());
  }

  /**
   * Sets up the autonomous routine chooser that appears on the dashboard.
   * The drive team selects a routine before the match starts. "Do Nothing" is the default
   * because it's the safest option — you'd rather score 0 auto points than crash into
   * a field element because the wrong auto was selected.
   */
  private void configureAutoChooser() {
    autoChooser.setDefaultOption("Do Nothing", Autos.doNothing());
    autoChooser.addOption("Drive Forward and Shoot",
        Autos.driveForwardAndShoot(drivetrain, shooter));

    // putData sends the chooser to the dashboard so it shows up as a dropdown
    SmartDashboard.putData("Auto Chooser", autoChooser);
  }

  /** Returns the drivetrain subsystem. Used by Robot.java to zero the gyro in autonomousInit(). */
  public DrivetrainSubsystem getDrivetrain() {
    return drivetrain;
  }

  /** Returns the test mode handler. Used by Robot.java to run test mode logic. */
  public TestMode getTestMode() {
    return testMode;
  }

  /** Returns whichever auto routine the drive team selected on the dashboard. */
  public Command getAutonomousCommand() {
    return autoChooser.getSelected();
  }
}
