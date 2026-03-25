package frc.robot;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.subsystems.IntakeSubsystem;
import frc.robot.subsystems.ShooterSubsystem;

/**
 * TestMode lets you spin individual motors one at a time at low speed to verify wiring,
 * CAN IDs, and motor directions before running the robot for real. This runs when you
 * select "Test" mode in the Driver Station.
 *
 * <p><b>Controls (operator controller):</b>
 * <ul>
 *   <li><b>D-pad Up/Down</b> — cycle through motors (Launcher → Feeder → Intake Roller)</li>
 *   <li><b>Right stick Y</b> — spin the selected motor (capped at 20% output)</li>
 *   <li><b>Right bumper</b> — nudge the intake arm toward deployed (+5°)</li>
 *   <li><b>Left bumper</b> — nudge the intake arm toward stowed (−5°)</li>
 *   <li><b>B button</b> — emergency stop all motors</li>
 * </ul>
 *
 * <p>The intake arm motor is NOT in the free-spin list because the arm has physical travel
 * limits — spinning it freely could slam the arm into the frame. Instead, it uses dedicated
 * nudge buttons that move the arm in small increments using PID position control, and the
 * Spark Max's soft limits prevent it from going past its safe range.
 *
 * <p><b>Safety notes:</b>
 * <ul>
 *   <li>All free-spin motors are capped at 20% output — enough to see direction, not enough
 *       to launch fuel across the shop</li>
 *   <li>Only one motor spins at a time — releasing the stick stops it immediately</li>
 *   <li>The arm motor is protected by soft limits AND the small nudge increment</li>
 *   <li>B button is a panic stop for everything</li>
 * </ul>
 *
 * <p><b>Tip:</b> Before using this, test each Spark Max individually with the REV Hardware
 * Client over USB. That lets you verify CAN IDs and brushed/brushless mode without any code.
 * Then use this test mode to verify everything works together on the robot.
 */
public class TestMode {

  /** Max percent output for free-spinning motors in test mode. 20% is plenty for direction checks. */
  private static final double MAX_TEST_OUTPUT = 0.20;

  /** How many degrees the arm moves per bumper press. Small enough to be safe, big enough to see. */
  private static final double ARM_NUDGE_DEGREES = 5.0;

  /**
   * The motors that can be free-spun with the right stick. The intake arm is intentionally
   * excluded — it uses the bumper nudge buttons instead (see class javadoc for why).
   */
  private enum TestableMotor {
    LAUNCHER("Shooter Launcher"),
    FEEDER("Shooter Feeder"),
    INTAKE_ROLLER("Intake Roller");

    final String displayName;

    TestableMotor(String displayName) {
      this.displayName = displayName;
    }
  }

  private final ShooterSubsystem shooter;
  private final IntakeSubsystem intake;
  private final CommandXboxController controller;

  private TestableMotor selectedMotor = TestableMotor.LAUNCHER;

  // Track previous button states so we only act on the press edge (not every 20ms while held)
  private int lastPOV = -1;
  private boolean lastLeftBumper = false;
  private boolean lastRightBumper = false;

  public TestMode(ShooterSubsystem shooter, IntakeSubsystem intake,
      CommandXboxController controller) {
    this.shooter = shooter;
    this.intake = intake;
    this.controller = controller;
  }

  /**
   * Called once when entering test mode. Stops everything and resets selection to the first motor.
   */
  public void init() {
    selectedMotor = TestableMotor.LAUNCHER;
    stopAll();

    // Show instructions on the dashboard so students know what to do
    SmartDashboard.putString("Test/Selected Motor", selectedMotor.displayName);
    SmartDashboard.putNumber("Test/Motor Output", 0);
    SmartDashboard.putNumber("Test/Arm Angle", intake.getArmAngle());
    SmartDashboard.putString("Test/Instructions",
        "DPad Up/Down=select motor | Right stick=spin | LB/RB=nudge arm | B=stop all");
  }

  /**
   * Called every 20ms while in test mode. Reads the controller and drives the selected motor.
   */
  public void periodic() {
    handleMotorSelection();
    handleMotorSpin();
    handleArmNudge();
    handleEmergencyStop();
    updateDashboard();
  }

  /** Stop all motors. Call this when leaving test mode. */
  public void stopAll() {
    shooter.stop();
    intake.stop();
  }

  // ---------------------------------------------------------------------------
  //  Private helpers
  // ---------------------------------------------------------------------------

  /** D-pad up/down cycles through the free-spin motor list. */
  private void handleMotorSelection() {
    int pov = controller.getHID().getPOV();

    // Only act on the transition (edge detection) — not every cycle while held
    if (pov != lastPOV) {
      if (pov == 0) { // D-pad up
        selectedMotor = previous(selectedMotor);
      } else if (pov == 180) { // D-pad down
        selectedMotor = next(selectedMotor);
      }
    }
    lastPOV = pov;
  }

  /** Right stick Y spins the selected motor at up to MAX_TEST_OUTPUT. */
  private void handleMotorSpin() {
    // Negate because pushing the stick forward gives a negative value
    double stickValue = -controller.getRightY();
    double output = stickValue * MAX_TEST_OUTPUT;

    // Zero all free-spin motors first, then set only the selected one.
    // This ensures releasing the stick stops the motor immediately.
    shooter.setLauncherPercent(0);
    shooter.setFeederPercent(0);
    intake.setRollerPercent(0);

    switch (selectedMotor) {
      case LAUNCHER:
        shooter.setLauncherPercent(output);
        break;
      case FEEDER:
        shooter.setFeederPercent(output);
        break;
      case INTAKE_ROLLER:
        intake.setRollerPercent(output);
        break;
    }
  }

  /** Bumpers nudge the intake arm in small increments using PID position control. */
  private void handleArmNudge() {
    boolean leftBumper = controller.getHID().getLeftBumperButton();
    boolean rightBumper = controller.getHID().getRightBumperButton();

    // Edge detection — only nudge on the press, not every 20ms while held
    if (rightBumper && !lastRightBumper) {
      intake.nudgeArmTarget(ARM_NUDGE_DEGREES);  // toward deployed
    }
    if (leftBumper && !lastLeftBumper) {
      intake.nudgeArmTarget(-ARM_NUDGE_DEGREES);  // toward stowed
    }

    lastLeftBumper = leftBumper;
    lastRightBumper = rightBumper;
  }

  /** B button panic-stops everything. */
  private void handleEmergencyStop() {
    if (controller.getHID().getBButton()) {
      stopAll();
    }
  }

  /** Push test mode telemetry to the dashboard. */
  private void updateDashboard() {
    SmartDashboard.putString("Test/Selected Motor", selectedMotor.displayName);
    SmartDashboard.putNumber("Test/Arm Angle", intake.getArmAngle());
  }

  /** Get the next motor in the list, wrapping around at the end. */
  private static TestableMotor next(TestableMotor current) {
    TestableMotor[] values = TestableMotor.values();
    return values[(current.ordinal() + 1) % values.length];
  }

  /** Get the previous motor in the list, wrapping around at the start. */
  private static TestableMotor previous(TestableMotor current) {
    TestableMotor[] values = TestableMotor.values();
    return values[(current.ordinal() - 1 + values.length) % values.length];
  }
}
