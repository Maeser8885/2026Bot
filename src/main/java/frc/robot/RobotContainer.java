// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.robot.Constants.OperatorConstants;
import frc.robot.commands.Autos;
import frc.robot.commands.ExampleCommand;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.ExampleSubsystem;
import frc.robot.subsystems.IntakeSubsystem;
import frc.robot.subsystems.ShooterSubsystem;
import frc.robot.subsystems.VisionSubsystem;
import swervelib.SwerveDrive;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import com.revrobotics.AbsoluteEncoder;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.studica.frc.AHRS;
import com.studica.frc.AHRS.NavXComType;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.AnalogEncoder;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.ScheduleCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...



  AHRS navX = new AHRS(NavXComType.kMXP_SPI);

  SendableChooser<Command> driveChooser = new SendableChooser<Command>();
  SendableChooser<Command> autoChooser = new SendableChooser<Command>();

  private final ExampleSubsystem m_exampleSubsystem = new ExampleSubsystem();
  private final DriveSubsystem m_driveSubsystem;
  private final VisionSubsystem m_visionSubsystem = new VisionSubsystem();
  private final ShooterSubsystem m_ShooterSubsystem = new ShooterSubsystem();
  private final IntakeSubsystem m_IntakeSubsystem = new IntakeSubsystem();

  private final CommandXboxController m_driverController = new CommandXboxController(OperatorConstants.kDriverControllerPort);
  private final CommandXboxController m_operatorController = new CommandXboxController(OperatorConstants.kOperatorControllerPort);

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    // Configure the trigger bindings
    m_driveSubsystem = new DriveSubsystem();

    m_visionSubsystem.setDefaultCommand(m_visionSubsystem.visionUpkeep(m_driveSubsystem));

    

    NamedCommands.registerCommand("shoot", m_ShooterSubsystem.shootAndFeed());
    NamedCommands.registerCommand("extendIntake", m_IntakeSubsystem.deploy());
    NamedCommands.registerCommand("intake", m_IntakeSubsystem.runRollers());
    NamedCommands.registerCommand("stopIntake", m_IntakeSubsystem.stop());
    NamedCommands.registerCommand("stopShooting", m_ShooterSubsystem.stop());
    NamedCommands.registerCommand("setAngleRightClockwise", m_driveSubsystem.setAngleOffset(90));
    
    autoChooser = AutoBuilder.buildAutoChooser();
    SmartDashboard.putData(autoChooser);

    configureBindings();

    configureDriveChooser();
  }

  public void configureDriveChooser(){
    
    driveChooser.addOption("Robot Oriented", m_driveSubsystem.driveAngularVelocity(
      () -> MathUtil.applyDeadband(m_driverController.getLeftX(), 0.1),
       () -> -MathUtil.applyDeadband(m_driverController.getLeftY(), 0.1),
        () -> MathUtil.applyDeadband(m_driverController.getRightX(), 0.1)));

    driveChooser.addOption("Angular Field Oriented", m_driveSubsystem.driveAngularVelocityFO(
      () -> MathUtil.applyDeadband(m_driverController.getLeftX(), 0.1),
       () -> -MathUtil.applyDeadband(m_driverController.getLeftY(), 0.1),
        () -> MathUtil.applyDeadband(m_driverController.getRightX(), 0.1)));

    driveChooser.setDefaultOption("Field Oriented Direct Angle", m_driveSubsystem.driveDirectAngleFO(
      () -> MathUtil.applyDeadband(m_driverController.getLeftX(), 0.1),
       () -> -MathUtil.applyDeadband(m_driverController.getLeftY(), 0.1),
        () -> MathUtil.applyDeadband(m_driverController.getRightX(), 0.1),
        () -> -MathUtil.applyDeadband(m_driverController.getRightY(), 0.1)));

        SmartDashboard.putData(driveChooser);


  }


  public void teleopInit(){
    CommandScheduler.getInstance().schedule(m_ShooterSubsystem.stop());

    m_driveSubsystem.setDefaultCommand(driveChooser.getSelected());
  }

  /**
   * Use this method to define your trigger->command mappings. Triggers can be created via the
   * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with an arbitrary
   * predicate, or via the named factories in {@link
   * edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for {@link
   * CommandXboxController Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller
   * PS4} controllers or {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight
   * joysticks}.
   */
  private void configureBindings() {
         m_driverController.a().toggleOnTrue(m_driveSubsystem.zeroYaw());
        
        
         m_operatorController.povUp().toggleOnTrue(m_IntakeSubsystem.deploy()); // intake extend
         m_operatorController.povDown().toggleOnTrue(m_IntakeSubsystem.stow());
         m_operatorController.b().toggleOnTrue(m_IntakeSubsystem.runRollers()); //run intake
         m_operatorController.x().toggleOnTrue(m_IntakeSubsystem.runRollersReverse()); //run intake reverse
        
         m_operatorController.b().or(m_operatorController.x()).toggleOnFalse(m_IntakeSubsystem.stopRollers()); //stop intake

        m_operatorController.a().toggleOnTrue(m_ShooterSubsystem.unfeed());
        m_operatorController.leftTrigger().toggleOnTrue(m_ShooterSubsystem.shootAndFeed()); // Shoot both full speed
        m_operatorController.rightTrigger().toggleOnTrue(m_ShooterSubsystem.spinUpAndShoot()); // Spin up and shoot
        m_operatorController.rightTrigger().or(m_operatorController.leftTrigger()).or(m_operatorController.a()).toggleOnFalse(m_ShooterSubsystem.stop());

    // Schedule `exampleMethodCommand` when the Xbox controller's B button is pressed,
    // cancelling on release.
    //m_driverController.b().whileTrue(m_exampleSubsystem.exampleMethodCommand());
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An example command will be run in autonomous
    // return autoChooser.getSelected();
    return new SequentialCommandGroup(
      m_ShooterSubsystem.spinUpAndShoot()
      ,new WaitCommand(10),
      m_ShooterSubsystem.stop()
    );
  }

  public void debugPeriodic(){
    SmartDashboard.putNumber("NAVX YAW", navX.getYaw());

    // SmartDashboard.putNumber("ARM SETPOINT", m_IntakeSubsystem.getSetpoint());
    // SmartDashboard.putNumber("ARM POSITION", m_IntakeSubsystem.getArmAngle());
  }
}
