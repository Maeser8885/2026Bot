package frc.robot.subsystems;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.IntakeConstants;

/**
 * IntakeSubsystem controls the front-mounted over-the-bumper intake. Physically, it consists of:
 * <ul>
 *   <li><b>Two parallel arms</b> that pivot at their base, with soft rubber roller wheels on an
 *       axle at the top. When deployed, the arms fold down ~115 degrees to place the rollers at
 *       ground level outside the robot perimeter.</li>
 *   <li><b>Arm motor</b> — a NEO (brushless) on a Spark Max, geared down for torque. Controls
 *       the arm angle using PID position control with the NEO's built-in encoder.</li>
 *   <li><b>Roller motor</b> — spins the rubber wheels to grab fuel off the ground, pull it up
 *       over the bumper, and fling it into the robot's storage hopper.</li>
 * </ul>
 *
 * <p><b>How the arm PID works:</b> The Spark Max has a built-in PID controller that runs on the
 * motor controller itself (not on the roboRIO). You give it a target angle (e.g., 115 degrees)
 * and it continuously adjusts motor power to hold that position. The output is capped at +/-40%
 * to provide "compliance" — the arm is firm enough to hold position but soft enough that fuel
 * can push it up slightly as it enters the robot.
 *
 * <p><b>Software soft limits</b> are configured on the Spark Max to prevent the arm from rotating
 * past its physical range (0 to ~120 degrees). These act as a hardware-level safety net — even
 * if the code has a bug that commands an impossible position, the Spark Max itself will refuse
 * to drive the motor past the soft limit.
 *
 * <p><b>Important:</b> The arm encoder is zeroed on startup, assuming the arm is in the stowed
 * (vertical) position. If the robot powers on with the arm not stowed, the encoder zero will be
 * wrong and all positions will be offset.
 */
public class IntakeSubsystem extends SubsystemBase {

  private final SparkMax armMotor;
  private final SparkMax rollerMotor;
  private final RelativeEncoder armEncoder;
  private final SparkClosedLoopController armController;

  public IntakeSubsystem() {
    // Arm motor — NEO (brushless), geared for torque
    armMotor = new SparkMax(IntakeConstants.kArmMotorId, MotorType.kBrushless);

    // Roller motor — NEO Vortex (brushless)
    rollerMotor = new SparkMax(IntakeConstants.kRollerMotorId, MotorType.kBrushless);
    rollerMotor.configure(new SparkMaxConfig().inverted(true), com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    // Configure arm motor
    SparkMaxConfig armConfig = new SparkMaxConfig();
    armConfig
        .smartCurrentLimit(IntakeConstants.kArmCurrentLimit)
        .idleMode(IdleMode.kBrake)
        .inverted(false); // TODO: Test on robot — invert if arm deploys wrong direction


    // PID for position control with output capped for compliance
    armConfig.closedLoop
        .p(IntakeConstants.kArmP)
        .i(IntakeConstants.kArmI)
        .d(IntakeConstants.kArmD)
        .outputRange(IntakeConstants.kArmMinOutput, IntakeConstants.kArmMaxOutput);

    // Software soft limits — hardware-level protection
    armConfig.softLimit
        .forwardSoftLimitEnabled(true)
        .forwardSoftLimit(IntakeConstants.kForwardSoftLimit)
        .reverseSoftLimitEnabled(true)
        .reverseSoftLimit(IntakeConstants.kReverseSoftLimit);

    armMotor.configure(armConfig,
        com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    // Configure roller motor
    SparkMaxConfig rollerConfig = new SparkMaxConfig();
    rollerConfig
        .smartCurrentLimit(IntakeConstants.kRollerCurrentLimit)
        .idleMode(IdleMode.kCoast)
        .inverted(true); // TODO: Test on robot — invert if rollers push fuel outward

    rollerMotor.configure(rollerConfig,
        com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    armEncoder = armMotor.getEncoder();
    armController = armMotor.getClosedLoopController();

    // Zero the encoder on startup — arm must be in stowed position at power-on
    armEncoder.setPosition(IntakeConstants.kStowedSetpoint);
  }

  public Command deploy() {
    return run(() -> armController.setSetpoint(IntakeConstants.kDeployedSetpoint, ControlType.kPosition));
  }

  public Command stow() {
    return run(() -> armController.setSetpoint(IntakeConstants.kStowedSetpoint, ControlType.kPosition));
  }

  public Command runRollers() {
    return run(() -> rollerMotor.set(IntakeConstants.kRollerSpeed));
  }

  public Command runRollersReverse() {
    return run(() -> rollerMotor.set(-IntakeConstants.kRollerSpeed));
  }

  public Command stopRollers() {
    return run(() -> rollerMotor.set(0));
  }

  public Command stop() {
    return run(() -> {
      armMotor.set(0);
      rollerMotor.set(0);
    });
  }

  public double getArmAngle() {
    return armEncoder.getPosition();
  }

  public boolean isAtPosition(double targetDegrees) {
    return Math.abs(getArmAngle() - targetDegrees) < IntakeConstants.kPositionToleranceDegrees;
  }

  public void setRollerPercent(double percent) {
    rollerMotor.set(percent);
  }

  public Command nudgeArmTarget(double deltaDegrees) {
    return run(() -> {
    double target = getArmAngle() + deltaDegrees;
    target = MathUtil.clamp(target,
        IntakeConstants.kReverseSoftLimit, IntakeConstants.kForwardSoftLimit);
    armController.setSetpoint(target, ControlType.kPosition);
    }
      );
  }

  @Override
  public void periodic() {
    SmartDashboard.putNumber("Intake/Arm Angle", getArmAngle());
    SmartDashboard.putNumber("Intake/Roller Output", rollerMotor.getAppliedOutput());
    SmartDashboard.putBoolean("Intake/Arm Deployed",
        isAtPosition(IntakeConstants.kDeployedSetpoint));
    SmartDashboard.putBoolean("Intake/Arm Stowed",
        isAtPosition(IntakeConstants.kStowedSetpoint));
  }

  public double getSetpoint() {
    return armController.getSetpoint();
  }
}
