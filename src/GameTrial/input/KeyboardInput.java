package GameTrial.input;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyboardInput extends KeyAdapter implements FocusListener {
    private final InputState inputState;

    private boolean isTurningLeft, isTurningRight;
    private boolean isSpaceDown;
    private boolean wasLeftDoorDown, wasRightDoorDown, wasFlashlightOn;

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
    public void focusGained(FocusEvent event) {
    }

    private void updateKey(int keyCode, boolean isDown) {
    if (keyCode == KeyEvent.VK_Q) {
        if (isDown && !wasLeftDoorDown) {
            inputState.leftDoorClosed = !inputState.leftDoorClosed;
        }
        wasLeftDoorDown = isDown;
    }

    if (keyCode == KeyEvent.VK_E) {
        if (isDown && !wasRightDoorDown) {
            inputState.rightDoorClosed = !inputState.rightDoorClosed;
        }
        wasRightDoorDown = isDown;
    }

    if (keyCode == KeyEvent.VK_F) {
        inputState.flashlightOn = isDown;
    }

    if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_9) {
        if (isDown) {
            inputState.requestedCamera = keyCode - KeyEvent.VK_1;
        }
    }

    if (keyCode == KeyEvent.VK_SPACE) {
        if (isDown && !isSpaceDown) {
            inputState.tabletTogglePressed = true;
        }
        isSpaceDown = isDown;
    }

    if (keyCode == KeyEvent.VK_LEFT) {
        isTurningLeft  = isDown;
    }

    if (keyCode == KeyEvent.VK_RIGHT) {
        isTurningRight = isDown;
    }

    inputState.turn = (isTurningRight ? 1 : 0) - (isTurningLeft ? 1 : 0);
    }

    @Override
    public void focusLost(FocusEvent event) {
        isTurningLeft = false;
        isTurningRight = false;
        isSpaceDown = false;
        wasLeftDoorDown = false;
        wasRightDoorDown = false;
        wasFlashlightOn = false;
        inputState.leftDoorClosed = false;
        inputState.rightDoorClosed = false;
        inputState.turn = 0;
    }
}