package fnaf.input;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyboardInput extends KeyAdapter implements FocusListener {
    private final PlayerInput playerInput;

    private boolean isTurningLeft, isTurningRight;
    private boolean isSpaceDown;
    private boolean wasLeftDoorDown, wasRightDoorDown;
    private boolean wasMinimapToggleDown;

    public KeyboardInput(PlayerInput playerInput) {
        this.playerInput = playerInput;
    }

    @Override
    public void keyPressed(KeyEvent event) {
        handleKeyInput(event.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent event) {
        handleKeyInput(event.getKeyCode(), false);
    }

    @Override
    public void focusGained(FocusEvent event) {
    }

    private void handleKeyInput(int keyCode, boolean isDown) {
    if (keyCode == KeyEvent.VK_Q) {
        if (isDown && !wasLeftDoorDown) {
            playerInput.leftDoorClosed = !playerInput.leftDoorClosed;
        }
        wasLeftDoorDown = isDown;
    }

    if (keyCode == KeyEvent.VK_E) {
        if (isDown && !wasRightDoorDown) {
            playerInput.rightDoorClosed = !playerInput.rightDoorClosed;
        }
        wasRightDoorDown = isDown;
    }

    if (keyCode == KeyEvent.VK_M) {
        if (isDown && !wasMinimapToggleDown) {
            playerInput.toggleMinimapDebug = true;
        }
        wasMinimapToggleDown = isDown;
    }

    if (isDown) {
        int selectedCameraIndex = -1;
        if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_9) {
            selectedCameraIndex = keyCode - KeyEvent.VK_1;
        } else if (keyCode == KeyEvent.VK_0) {
            selectedCameraIndex = 9;   // CAM 6 - Kitchen
        } else if (keyCode == KeyEvent.VK_MINUS) {
            selectedCameraIndex = 10;  // CAM 7 - Restrooms
        }
        if (selectedCameraIndex >= 0) {
            playerInput.selectedCamera = selectedCameraIndex;
        }
    }

    if (keyCode == KeyEvent.VK_SPACE) {
        if (isDown && !isSpaceDown) {
            playerInput.tabletTogglePressed = true;
        }
        isSpaceDown = isDown;
    }

    if (keyCode == KeyEvent.VK_LEFT) {
        isTurningLeft  = isDown;
    }

    if (keyCode == KeyEvent.VK_RIGHT) {
        isTurningRight = isDown;
    }

    playerInput.turn = (isTurningRight ? 1 : 0) - (isTurningLeft ? 1 : 0);
    }

    @Override
    public void focusLost(FocusEvent event) {
        isTurningLeft = false;
        isTurningRight = false;
        isSpaceDown = false;
        wasLeftDoorDown = false;
        wasRightDoorDown = false;
        wasMinimapToggleDown = false;
        playerInput.leftDoorClosed = false;
        playerInput.rightDoorClosed = false;
        playerInput.turn = 0;
    }
}
