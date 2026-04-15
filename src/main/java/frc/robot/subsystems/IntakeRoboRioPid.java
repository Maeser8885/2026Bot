package frc.robot.subsystems;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Intake implementation using a <b>roboRIO-side PID loop</b> with gravity feedforward and
 * asymmetric output limits.
 *
 * <p>Unlike {@link IntakeSparkMaxPid}, this class runs the PID math on the roboRIO every 20ms
 * in {@link #periodic()}. This gives us full control over the output calculation, enabling two
 * features the Spark Max onboard PID can't do:
 *
 * <p><b>1. Gravity feedforward</b> — A constant term ({@code kG * sin(armAngle)}) counteracts the
 * arm's weight so the PID only has to handle disturbances (like ball impacts), not hold the arm
 * against gravity. This prevents the arm from slamming down during deployment. Set {@code kG = 0.0}
 * to disable gravity compensation.
 *
 * <p><b>2. Asymmetric output limits</b> — Different output caps depending on which direction the
 * motor is pushing:
 * <ul>
 *   <li><b>Deploy direction</b> (positive output, pushing arm toward ground): capped at
 *       {@code kArmDeployOutput} (~25%). This is the "compliant" direction — when a ball pushes the
 *       arm up during intake, the motor gently returns it to position instead of slamming it back.</li>
 *   <li><b>Stow direction</b> (negative output, pushing arm back up): capped at
 *       {@code kArmStowOutput} (~60%). This is the "firm" direction — prevents gravity from pulling
 *       the arm past its deployed angle.</li>
 * </ul>
 *
 * <p>The result: the arm deploys smoothly (no slam), holds firm at the deployed position, and gives
 * a little when balls push against it during intake (no bounce).
 *
 * @see IntakeSubsystem
 * @see IntakeSparkMaxPid
 */
public class IntakeRoboRioPid extends SubsystemBase implements IntakeSubsystem {

  // Gravity feedforward — counteracts the arm's weight so the PID only handles disturbances
  // (like ball hits). Tune by slowly increasing from 0 until the arm holds its position
  // without the PID doing much work. Set to 0.0 to disable gravity compensation.
  private static final double kG = 0.0;

  // Asymmetric output limits — different caps for each direction of arm movement.
  // "Deploy direction" (positive output) = motor pushing arm toward ground. When a ball pushes
  //   the arm up, this is how hard the motor pushes it back. LOWER = more give during intake.
  // "Stow direction" (negative output) = motor pushing arm back up. This resists the arm going
  //   past deployed. HIGHER = firmer, arm won't slam past its target.
  // NOTE: Which direction is positive depends on motor wiring. If the arm moves the wrong way,
  // flip inverted(true) on the SparkMaxConfig — do NOT swap these values.
  private static final double kArmDeployOutput = 0.25;   // compliant (gentle return)
  private static final double kArmStowOutput = 0.6;      // firm (resist over-deployment)

  private final SparkMax armMotor;
  private final SparkMax rollerMotor;
  private final RelativeEncoder armEncoder;
  private final PIDController pid;

  private double targetAngle = IntakeConstants.kStowedAngleDegrees;
  private boolean pidEnabled = false;

  public IntakeRoboRioPid() {
    armMotor = new SparkMax(IntakeConstants.kArmMotorId, MotorType.kBrushless);
    rollerMotor = new SparkMax(IntakeConstants.kRollerMotorId, MotorType.kBrushless);

    // Configure arm motor — PID gains are NOT set here because we run PID on the roboRIO.
    // We still configure soft limits on the Spark Max as a hardware safety net.
    SparkMaxConfig armConfig = new SparkMaxConfig();
    armConfig
        .smartCurrentLimit(IntakeConstants.kArmCurrentLimit)
        .idleMode(IdleMode.kBrake)
        .inverted(false); // TODO: Test on robot — invert if arm deploys wrong direction

    // Encoder conversion: NEO rotations -> arm degrees
    armConfig.encoder
        .positionConversionFactor(360.0 / IntakeConstants.kArmGearRatio);

    // Software soft limits — still active even though PID runs on the roboRIO.
    // The Spark Max will refuse to drive the motor past these limits regardless of
    // what output value we send from the roboRIO.
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

    // roboRIO PID controller — runs every 20ms in periodic()
    pid = new PIDController(IntakeConstants.kArmP, IntakeConstants.kArmI, IntakeConstants.kArmD);
    pid.setTolerance(IntakeConstants.kPositionToleranceDegrees);

    // Zero the encoder on startup — arm must be in stowed position at power-on
    armEncoder.setPosition(IntakeConstants.kStowedAngleDegrees);
  }

  @Override
  public void deploy() {
    targetAngle = IntakeConstants.kDeployedAngleDegrees;
    pidEnabled = true;
  }

  @Override
  public void stow() {
    targetAngle = IntakeConstants.kStowedAngleDegrees;
    pidEnabled = true;
  }

  @Override
  public void runRollers() {
    rollerMotor.set(IntakeConstants.kRollerSpeed);
  }

  @Override
  public void stopRollers() {
    rollerMotor.set(0);
  }

  @Override
  public void stop() {
    pidEnabled = false;
    armMotor.set(0);
    rollerMotor.set(0);
  }

  @Override
  public double getArmAngle() {
    return armEncoder.getPosition();
  }

  @Override
  public boolean isAtPosition(double targetDegrees) {
    return Math.abs(getArmAngle() - targetDegrees) < IntakeConstants.kPositionToleranceDegrees;
  }

  @Override
  public void setRollerPercent(double percent) {
    rollerMotor.set(percent);
  }

  @Override
  public void nudgeArmTarget(double deltaDegrees) {
    targetAngle = getArmAngle() + deltaDegrees;
    targetAngle = MathUtil.clamp(targetAngle,
        IntakeConstants.kReverseSoftLimit, IntakeConstants.kForwardSoftLimit);
    pidEnabled = true;
  }

  @Override
  public void periodic() {
    // --- roboRIO PID loop (runs every 20ms when enabled) ---
    if (pidEnabled) {
      double currentAngle = getArmAngle();

      // PID output — how hard the motor should push to reach the target angle
      double pidOutput = pid.calculate(currentAngle, targetAngle);

      // Gravity feedforward — counteracts the arm's weight.
      // sin(0°)=0 at stowed (weight is directly above pivot, no torque)
      // sin(90°)=1.0 at horizontal (maximum lever arm, peak gravity torque)
      // sin(115°)≈0.91 when deployed (still high torque pulling arm down)
      // The sign of kG should be set so the feedforward opposes gravity's pull.
      double gravityFF = kG * Math.sin(Math.toRadians(currentAngle));

      // Asymmetric output limits:
      // Positive output = pushing arm toward deployed (gentle — allows give for balls)
      // Negative output = pushing arm toward stowed (firm — resists over-deployment)
      double output = pidOutput + gravityFF;
      if (output > 0) {
        output = Math.min(output, kArmDeployOutput);
      } else {
        output = Math.max(output, -kArmStowOutput);
      }

      armMotor.set(output);

      // Telemetry for tuning
      SmartDashboard.putNumber("Intake/PID Output", pidOutput);
      SmartDashboard.putNumber("Intake/Gravity FF", gravityFF);
      SmartDashboard.putNumber("Intake/Total Output", output);
      SmartDashboard.putNumber("Intake/Target Angle", targetAngle);
    }

    // --- Dashboard (always runs) ---
    SmartDashboard.putNumber("Intake/Arm Angle", getArmAngle());
    SmartDashboard.putNumber("Intake/Roller Output", rollerMotor.getAppliedOutput());
    SmartDashboard.putBoolean("Intake/Arm Deployed",
        isAtPosition(IntakeConstants.kDeployedAngleDegrees));
    SmartDashboard.putBoolean("Intake/Arm Stowed",
        isAtPosition(IntakeConstants.kStowedAngleDegrees));
    SmartDashboard.putString("Intake/PID Mode", "RoboRIO (gravity FF + asymmetric)");
  }
}
