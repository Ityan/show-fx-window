import com.sun.javafx.application.PlatformImpl;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jnativehook.keyboard.NativeKeyEvent.*;

public class Main extends Application implements NativeKeyListener {
    public Main() throws AWTException {
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Stage stage;
    private final Robot robot = new Robot();
    private final List<Integer> keys = Arrays.asList(VC_ALT_L, VC_SHIFT_L, VC_V);
    private List<Integer> expecting = new ArrayList<>(keys);
    private boolean isShowEvent = false;

    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(false);
        enableJnh();
        this.stage = primaryStage;
        if (stage.getStyle() != StageStyle.TRANSPARENT) {
            stage.initStyle(StageStyle.TRANSPARENT);
        }
        stage.focusedProperty().addListener((ov, t, t1) -> {
            if (t && stage.isShowing()) {
                stage.hide();
            }
        });
        StackPane root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
        stage.setScene(new Scene(root, 300, 250));
    }

    private void enableJnh() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException e) {
            throw new IllegalStateException(e);
        }
        Logger logger = Logger
            .getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);
    }

    public void nativeKeyPressed(NativeKeyEvent ke) {
        expecting.remove(new Integer(ke.getKeyCode()));
        if (expecting.isEmpty()) {
            isShowEvent = true;
        }
    }

    public void nativeKeyReleased(NativeKeyEvent ke) {
        if (keys.contains(ke.getKeyCode()) && !expecting.contains(ke.getKeyCode())) {
            expecting.add(ke.getKeyCode());
            if (isShowEvent && keys.size() == expecting.size()) {
                PlatformImpl.runAndWait(() -> {
//                    Point point = MouseInfo.getPointerInfo().getLocation();
//                    stage.setX(point.x);
//                    stage.setY(point.y);
                    stage.show();
                    stage.toFront();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    robot.mouseMove((int) stage.getX() + 150, (int) stage.getY() + 150) ;
                    robot.mousePress(MouseEvent.BUTTON1_MASK);
                    robot.mouseRelease(MouseEvent.BUTTON1_MASK);
                });
                this.expecting = new ArrayList<>(keys);
                isShowEvent = false;
            }
        }
    }

    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }
}
