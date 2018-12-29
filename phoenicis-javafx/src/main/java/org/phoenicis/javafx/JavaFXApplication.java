/*
 * Copyright (C) 2015-2017 PÂRIS Quentin
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.phoenicis.javafx;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.phoenicis.javafx.controller.MainController;
import org.phoenicis.multithreading.ControlledThreadPoolExecutorServiceCloser;
import org.phoenicis.repository.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.phoenicis.configuration.localisation.Localisation.tr;

public class JavaFXApplication extends Application {
    private final Logger LOGGER = LoggerFactory.getLogger(JavaFXApplication.class);

    private double splashWidth;
    private double splashHeight;
    private VBox splashLayout;
    private ProgressIndicator loadProgress;
    private Label progressText;

    public static void main(String[] args) {
        try {
            Application.launch(JavaFXApplication.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        final Image splashImage = new Image(getClass().getResourceAsStream("views/common/splash.png"));

        // use a reasonable splash screen size on all resolutions
        final double splashImageAspectRatio = splashImage.getWidth() / splashImage.getHeight();
        splashWidth = Screen.getPrimary().getBounds().getWidth() / 3;
        splashHeight = splashWidth * (1 / splashImageAspectRatio);

        BackgroundSize backgroundSize = new BackgroundSize(splashWidth, splashHeight, false, false, true, true);
        BackgroundImage backgroundImage = new BackgroundImage(splashImage, BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                backgroundSize);
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        loadProgress = new ProgressIndicator();
        progressText = new Label(tr("Loading..."));
        progressText.setFont(new Font(12));
        progressText.setPadding(new Insets(20, 5, 10, 5));
        progressText.setBackground(Background.EMPTY);
        progressText.setAlignment(Pos.CENTER);
        splashLayout = new VBox(spacer, loadProgress, progressText);
        splashLayout.setPrefSize(splashWidth, splashHeight);
        splashLayout.setAlignment(Pos.CENTER);
        splashLayout.setBackground(new Background(backgroundImage));
        splashLayout.setEffect(new DropShadow());
    }

    @Override
    public void start(final Stage initStage) {

        final Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() {
                final int numLoadSteps = 2;
                int loadStep = 0;

                // load fonts
                loadStep++;
                List<String> requiredFonts = Arrays.asList(
                        "views/common/mavenpro/MavenPro-Medium.ttf",
                        "views/common/roboto/Roboto-Medium.ttf",
                        "views/common/roboto/Roboto-Light.ttf",
                        "views/common/roboto/Roboto-Bold.ttf");

                updateMessage(tr("Loading fonts..."));
                LOGGER.debug("Loading fonts...");
                for (int i = 0; i < requiredFonts.size(); i++) {
                    Font.loadFont(getClass().getResource(requiredFonts.get(i)).toExternalForm(), 12);
                    updateMessage(tr("Loading font {0} of {1}...", i + 1, requiredFonts.size()));
                    LOGGER.debug(String.format("Loading font %s...", requiredFonts.get(i)));
                }
                updateProgress(loadStep, numLoadSteps);
                updateMessage(tr("All fonts loaded"));
                LOGGER.debug("All fonts loaded");

                // load repository
                loadStep++;
                updateMessage(tr("Loading repository..."));
                LOGGER.debug("Loading repository...");
                ConfigurableApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                        AppConfigurationNoUi.class);
                RepositoryManager repositoryManager = applicationContext.getBean(RepositoryManager.class);
                repositoryManager.forceSynchronousUpdate();
                updateProgress(loadStep, numLoadSteps);
                updateMessage(tr("Repository loaded"));
                LOGGER.debug("Repository loaded");

                return null;
            }
        };

        showSplash(initStage, loadTask, this::showMainStage);
        new Thread(loadTask).start();
    }

    /**
     * shows splash as long as task is in progress
     *
     * @param initStage stage to show the splash
     * @param task pre-loading task
     * @param initCompletionHandler handler which is called once task is finished
     */
    private void showSplash(final Stage initStage, Task<?> task, InitCompletionHandler initCompletionHandler) {
        progressText.textProperty().bind(task.messageProperty());
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                initStage.toFront();
                initStage.close();

                initCompletionHandler.complete();
            } else if (newState == Worker.State.CANCELLED || newState == Worker.State.FAILED) {
                throw new LoadingException("Loading failed");
            }
        });

        final Scene splashScene = new Scene(splashLayout, Color.TRANSPARENT);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - splashWidth / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - splashHeight / 2);
        initStage.initStyle(StageStyle.TRANSPARENT);
        initStage.setAlwaysOnTop(true);
        initStage.show();
    }

    /**
     * shows the main JavaFX stage
     * (call if pre-loading finished)
     */
    private void showMainStage() {
        Stage mainStage = new Stage(StageStyle.DECORATED);
        mainStage.getIcons().add(new Image(getClass().getResourceAsStream("views/common/phoenicis.png")));
        mainStage.setTitle("Phoenicis");

        ConfigurableApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                AppConfiguration.class);

        final MainController mainController = applicationContext.getBean(MainController.class);
        mainController.show();
        mainController.setOnClose(() -> {
            applicationContext.getBean(ControlledThreadPoolExecutorServiceCloser.class).setCloseImmediately(true);
            applicationContext.close();
        });
        mainStage.toFront();
    }

    /**
     * called if pre-loading task is finished
     */
    public interface InitCompletionHandler {
        void complete();
    }

}
