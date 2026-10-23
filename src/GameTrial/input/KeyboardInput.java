package GameTrial.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyboardInput extends KeyAdapter {
    private final InputState inputState;

    private boolean isMovingForward, isMovingBackward, isTurningLeft, isTurningRight;

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

    private void updateKey(int keyCode, boolean isDown) {
        if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) isMovingForward = isDown;
        if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) isMovingBackward = isDown;
        if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) isTurningLeft = isDown;
        if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) isTurningRight = isDown;

        inputState.forward = (isMovingForward ? 1 : 0) - (isMovingBackward ? 1 : 0);
        inputState.turn = (isTurningRight ? 1 : 0) - (isTurningLeft ? 1 : 0);
    }
}