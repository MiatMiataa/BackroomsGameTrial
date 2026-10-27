package GameTrial.input;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyboardInput extends KeyAdapter implements FocusListener {
    private final InputState inputState;

    private boolean isMovingForward, isMovingBackward, isTurningLeft, isTurningRight, isStrafingLeft, isStrafingRight;

    public KeyboardInput(InputState inputState) {
        this.inputState = inputState;
    }

    @Override
    public void keyPressed(KeyEvent event) {
        updateKey(event.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent event) {
        updateKey(event.getKeyCode(), false);
    }

    @Override
    public void focusLost(FocusEvent event) {
        isMovingForward = false;
        isMovingBackward = false;
        isTurningLeft = false;
        isTurningRight = false;
        isStrafingLeft = false;
        isStrafingRight = false;
        inputState.forward = 0;
        inputState.turn = 0;
    }

    @Override
    public void focusGained(FocusEvent event) {
    }

    private void updateKey(int keyCode, boolean isDown) {
        if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) isMovingForward = isDown;
        if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) isMovingBackward = isDown;
        if (keyCode == KeyEvent.VK_LEFT) isTurningLeft = isDown;
        if (keyCode == KeyEvent.VK_RIGHT) isTurningRight = isDown;
        if (keyCode == KeyEvent.VK_A) isStrafingLeft = isDown;
        if (keyCode == KeyEvent.VK_D) isStrafingRight = isDown;
        
        inputState.forward = (isMovingForward ? 1 : 0) - (isMovingBackward ? 1 : 0);
        inputState.turn = (isTurningRight ? 1 : 0) - (isTurningLeft ? 1 : 0);
        inputState.strafe = (isStrafingRight ? 1 : 0) - (isStrafingLeft ? 1 : 0);
    }
}