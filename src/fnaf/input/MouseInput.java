package fnaf.input;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseInput extends MouseAdapter implements FocusListener {
    private static final double DEAD_ZONE_FRACTION = 0.20;

    private final PlayerInput playerInput;

    public MouseInput(PlayerInput playerInput) {
        this.playerInput = playerInput;
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        updateTurn(event);
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        updateTurn(event);
    }

    @Override
    public void mouseExited(MouseEvent event) {
        playerInput.mouseTurn = 0;
    }

    @Override
    public void focusLost(FocusEvent event) {
        playerInput.mouseTurn = 0;
    }

    @Override
    public void focusGained(FocusEvent event) {
    }

    private void updateTurn(MouseEvent event) {
        double centerX = event.getComponent().getWidth() / 2.0;
        double offset = (event.getX() - centerX) / centerX;
        offset = Math.max(-1.0, Math.min(1.0, offset));

        double distanceFromCenter = Math.abs(offset);
        if (distanceFromCenter < DEAD_ZONE_FRACTION) {
            playerInput.mouseTurn = 0;
            return;
        }
        double strength = (distanceFromCenter - DEAD_ZONE_FRACTION) / (1.0 - DEAD_ZONE_FRACTION);
        playerInput.mouseTurn = Math.signum(offset) * strength;
    }
}
