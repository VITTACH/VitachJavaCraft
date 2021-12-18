package com.vittach.jumpjack.ui.buttons;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.vittach.jumpjack.MainEngine;
import com.vittach.jumpjack.Preferences;
import com.vittach.jumpjack.framework.ColorImpl;
import com.vittach.jumpjack.framework.FontHandler;
import com.vittach.jumpjack.framework.ImageHandler;
import com.vittach.jumpjack.ui.InputListener;

/**
 * Created by ZHARIKOV VITALIY at 12.02.2016.
 */

public class ScreenButton extends InputListener {
    public FontHandler font = new FontHandler();
    private SpriteBatch spriteBatch = new SpriteBatch();
    public String message;
    private boolean hasBackground;
    public int textX;
    public int textY;
    public ColorImpl color = new ColorImpl(1, 1, 1);
    public ImageHandler background = new ImageHandler();
    public ImageHandler screen = new ImageHandler();
    public ImageHandler choice;
    public float x;
    public float y;
    
    private final Preferences prefInstance = Preferences.getInstance();
    private final MainEngine engineInstance = MainEngine.getInstance();

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if (choice != null) {
            if (touchDown(screenX, screenY, -1)) {
                if (hasBackground) {
                    screen.clear();
                    hasBackground = false;
                    screen.blit(choice);
                }
            } else if (!hasBackground) {
                screen.clear();
                screen.blit(background);
                hasBackground = true;
            }
        }
        return true;
    }

    @Override
    public boolean touchDragged(int xPosition, int yPosition, int pointer) {
        mouseMoved(xPosition, yPosition);
        return true;
    }

    public boolean touchDown(int xPosition, int yPosition) {
        return touchDown(xPosition, yPosition, 0);
    }

    public boolean touchDown(int xPosition, int yPosition, int id) {
        if (choice != null && !hasBackground && id >= 0) {
            screen.clear();
            screen.blit(background);
            hasBackground = true;
        }

        float scaleX = prefInstance.screenWidth / engineInstance.renderWidth;
        float scaleY = prefInstance.screenHeight / engineInstance.renderHeight;
        xPosition -= (prefInstance.displayWidth - prefInstance.screenWidth) / 2;
        yPosition -= (prefInstance.displayHeight - prefInstance.screenHeight) / 2;

        return xPosition >= x * scaleX
                && xPosition <= x * scaleX + background.getWidth() * scaleX
                && yPosition >= (prefInstance.screenHeight - y * scaleY) - background.getHeight() * scaleY
                && yPosition <= (prefInstance.screenHeight - y * scaleY);
    }

    public void show(Viewport viewport) {
        if (message != null) {
            screen.fontPrint(font, textX, textY, message, color);
        }

        Sprite sprite = screen.render();
        sprite.setPosition(x, y);

        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();
        sprite.draw(spriteBatch);
        spriteBatch.end();
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public int getWidth() {
        return background.getWidth();
    }

    public int getHeight() {
        return background.getHeight();
    }

    public void dispose() {
        spriteBatch.dispose();
        background.dispose();
        screen.dispose();
    }
}