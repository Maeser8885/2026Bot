package frc.robot.subsystems;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
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

    // Configure arm motor
    SparkMaxConfig armConfig = new SparkMaxConfig();
    armConfig
        .smartCurrentLimit(IntakeConstants.kArmCurrentLimit)
        .idleMode(IdleMode.kBrake)
        .inverted(false); // TODO: Test on robot — invert if arm deploys wrong direction

    // Encoder conversion: NEO rotations -> arm degrees
    armConfig.encoder
        .positionConversionFactor(360.0 / IntakeConstants.kArmGearRatio);

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
        .inverted(false); // TODO: Test on robot — invert if rollers push fuel outward

    rollerMotor.configure(rollerConfig,
        com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    armEncoder = armMotor.getEncoder();
    armController = armMotor.getClosedLoopController();

    // Zero the encoder on startup — arm must be in stowed position at power-on
    armEncoder.setPosition(IntakeConstants.kStowedAngleDegrees);
  }

  /** Deploy the intake arm to ground level using PID position control. */
  public void deploy() {
    armController.setSetpoint(IntakeConstants.kDeployedAngleDegrees, ControlType.kPosition);
  }

  /** Stow the intake arm back to vertical using PID position control. */
  public void stow() {
    armController.setSetpoint(IntakeConstants.kStowedAngleDegrees, ControlType.kPosition);
  }

  /** Spin the roller wheels to intake fuel. */
  public void runRollers() {
    rollerMotor.set(IntakeConstants.kRollerSpeed);
  }

  /** Stop the roller wheels. */
  public void stopRollers() {
    rollerMotor.set(0);
  }

  /** Stop everything — arm PID off, rollers off. */
  public void stop() {
    armMotor.set(0);
    rollerMotor.set(0);
  }

  /** Get the current arm angle in degrees. */
  public double getArmAngle() {
    return armEncoder.getPosition();
  }

  /** Returns true if the arm is within tolerance of the target angle. */
  public boolean isAtPosition(double targetDegrees) {
    return Math.abs(getArmAngle() - targetDegrees) < IntakeConstants.kPositionToleranceDegrees;
  }

  // ===== Test mode helpers =====

  /** Set the roller motor to a raw percent output (-1.0 to 1.0). For test mode only. */
  public void setRollerPercent(double percent) {
    rollerMotor.set(percent);
  }

  /**
   * Nudge the arm target position by the given number of degrees. Positive = toward deployed,
   * negative = toward stowed. The target is clamped to the soft limits so you can't drive the
   * arm past its physical range. Uses PID position control, so the Spark Max will hold the
   * new position after the nudge.
   */
  public void nudgeArmTarget(double deltaDegrees) {
    double target = getArmAngle() + deltaDegrees;
    target = MathUtil.clamp(target,
        IntakeConstants.kReverseSoftLimit, IntakeConstants.kForwardSoftLimit);
    armController.setSetpoint(target, ControlType.kPosition);
  }

  @Override
  public void periodic() {
    SmartDashboard.putNumber("Intake/Arm Angle", getArmAngle());
    SmartDashboard.putNumber("Intake/Roller Output", rollerMotor.getAppliedOutput());
    SmartDashboard.putBoolean("Intake/Arm Deployed",
        isAtPosition(IntakeConstants.kDeployedAngleDegrees));
    SmartDashboard.putBoolean("Intake/Arm Stowed",
        isAtPosition(IntakeConstants.kStowedAngleDegrees));
  }
}
