package com.bayoumi.controllers.settings.azkar;

import com.bayoumi.controllers.settings.SettingsInterface;
import com.bayoumi.models.AbsoluteZekr;
import com.bayoumi.models.AzkarSettings;
import com.bayoumi.models.NotificationSettings;
import com.bayoumi.util.Logger;
import com.bayoumi.util.gui.HelperMethods;
import com.bayoumi.util.gui.IntegerSpinner;
import com.bayoumi.util.gui.notfication.Notification;
import com.bayoumi.util.time.ArabicNumeralDiscrimination;
import com.bayoumi.util.validation.SingleInstance;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXSlider;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.octicons.OctIcon;
import de.jensd.fx.glyphs.octicons.OctIconView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class AzkarSettingsController implements Initializable, SettingsInterface {

    private FontAwesomeIconView pauseIcon;
    private FontAwesomeIconView playIcon;
    private AzkarSettings azkarSettings;
    private NotificationSettings notificationSettings;
    private JFXButton currentFrequency;
    @FXML
    private VBox periodBox;
    @FXML
    private JFXComboBox<Pos> posComboBox;
    @FXML
    private JFXButton highFrequency;
    @FXML
    private JFXButton midFrequency;
    @FXML
    private JFXButton lowFrequency;
    @FXML
    private JFXButton rearFrequency;
    @FXML
    private Spinner<Integer> azkarPeriod;
    @FXML
    private Label minPlurality;
    @FXML
    private Spinner<Integer> azkarPeriod_hour;
    @FXML
    private Label hourPlurality;
    @FXML
    private JFXCheckBox stopAzkar;
    @FXML
    private JFXComboBox<String> azkarAlarmComboBox;

    @FXML
    private JFXButton playButton;
    @FXML
    private JFXButton showZekrButton;
    @FXML
    private OctIconView volume;
    @FXML
    private JFXSlider volumeSlider;
    private MediaPlayer MEDIA_PLAYER;
    @FXML
    private HBox volumeBox;
    private double previousValue = 50;
    private boolean isMuted = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playIcon = new FontAwesomeIconView(FontAwesomeIcon.PLAY);
        playIcon.setStyle("-fx-fill: -fx-secondary;");
        playIcon.setGlyphSize(30);
        pauseIcon = new FontAwesomeIconView(FontAwesomeIcon.PAUSE);
        pauseIcon.setGlyphSize(30);
        pauseIcon.setStyle("-fx-fill: -fx-secondary;");

        periodBox.disableProperty().bind(stopAzkar.selectedProperty());
        // init Spinner Values
        IntegerSpinner.init(azkarPeriod);
        IntegerSpinner.init(azkarPeriod_hour);
        azkarPeriod.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 1));
        azkarPeriod_hour.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        azkarPeriod.valueProperty().addListener((observable, oldValue, newValue) -> minPlurality.setText(ArabicNumeralDiscrimination.minutesArabicPlurality(Integer.parseInt(azkarPeriod.getEditor().getText()))));
        azkarPeriod_hour.valueProperty().addListener((observable, oldValue, newValue) -> hourPlurality.setText(ArabicNumeralDiscrimination.hoursArabicPlurality(Integer.parseInt(azkarPeriod_hour.getEditor().getText()))));
        azkarPeriod.setOnKeyReleased(event -> minPlurality.setText(ArabicNumeralDiscrimination.minutesArabicPlurality(Integer.parseInt(azkarPeriod.getEditor().getText()))));
        azkarPeriod_hour.setOnKeyReleased(event -> hourPlurality.setText(ArabicNumeralDiscrimination.hoursArabicPlurality(Integer.parseInt(azkarPeriod_hour.getEditor().getText()))));

        // init Saved data form DB
        azkarSettings = new AzkarSettings();
        azkarAlarmComboBox.setValue(azkarSettings.getAudioName());
        azkarAlarmComboBox.setItems(AzkarSettings.getAudioList());
        playButton.setDisable(azkarAlarmComboBox.getValue().equals("بدون صوت"));
        azkarAlarmComboBox.setOnAction(event -> {
            playButton.setDisable(azkarAlarmComboBox.getValue().equals("بدون صوت"));
            volumeBox.setDisable(azkarAlarmComboBox.getValue().equals("بدون صوت"));
            if (MEDIA_PLAYER != null && MEDIA_PLAYER.getStatus().equals(MediaPlayer.Status.PLAYING)) {
                MEDIA_PLAYER.stop();
                playButton.setGraphic(playIcon);
                playButton.setPadding(new Insets(5, 14, 5, 8));
            }
        });
        volumeSlider.setValue(azkarSettings.getVolume());
        // init volume
        volumeBox.setDisable(azkarAlarmComboBox.getValue().equals("بدون صوت"));
        // volume config
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            azkarSettings.setVolume((int) volumeSlider.getValue());
            playButton.requestFocus();
            previousValue = (double) oldValue;
            // multiply duration by percentage calculated by
            // slider position
            if (volumeSlider.getValue() > 0) {
                volume.setIcon(OctIcon.UNMUTE);
            } else if (volumeSlider.getValue() == 0) {
                volume.setIcon(OctIcon.MUTE);
            }
            if (null != MEDIA_PLAYER) {
                MEDIA_PLAYER.setVolume(azkarSettings.getVolume() / 100.0);
            }
        });

        notificationSettings = new NotificationSettings();
        posComboBox.setItems(FXCollections.observableArrayList(Pos.TOP_RIGHT, Pos.BOTTOM_RIGHT, Pos.TOP_LEFT, Pos.BOTTOM_LEFT, Pos.CENTER));
        posComboBox.setValue(notificationSettings.getPosition());
        // TODO Check for ENG Language
        posComboBox.setConverter(NotificationSettings.posArabicConverter());

        azkarPeriod.getValueFactory().setValue(azkarSettings.getHighPeriod() % 60);
        azkarPeriod_hour.getValueFactory().setValue(azkarSettings.getHighPeriod() / 60);

        stopAzkar.setSelected(azkarSettings.isStopped());
        currentFrequency = highFrequency;
        currentFrequency.getStyleClass().add("frequency-btn-selected");
    }

    @FXML
    private void play() {
        if (MEDIA_PLAYER != null && MEDIA_PLAYER.getStatus().equals(MediaPlayer.Status.PLAYING)) {
            MEDIA_PLAYER.stop();
            playButton.setGraphic(playIcon);
        } else {
            String fileName = azkarAlarmComboBox.getValue();
            System.out.println(fileName);
            if (!fileName.equals("بدون صوت")) {
                MEDIA_PLAYER = new MediaPlayer(new Media(new File("jarFiles/audio/" + fileName).toURI().toString()));
                MEDIA_PLAYER.setVolume(azkarSettings.getVolume() / 100.0);
                MEDIA_PLAYER.play();
                // playing
                playButton.setGraphic(pauseIcon);
                playButton.setPadding(new Insets(5, 11, 5, 11));
                MEDIA_PLAYER.setOnEndOfMedia(() -> playButton.setGraphic(playIcon));
            }
        }
    }

    @FXML
    private void highFrequencyAction() {
        toggleFrequencyBTN(highFrequency);
        azkarPeriod.getValueFactory().setValue(azkarSettings.getHighPeriod() % 60);
        azkarPeriod_hour.getValueFactory().setValue(azkarSettings.getHighPeriod() / 60);
    }

    @FXML
    private void lowFrequencyAction() {
        toggleFrequencyBTN(lowFrequency);
        azkarPeriod.getValueFactory().setValue(azkarSettings.getLowPeriod() % 60);
        azkarPeriod_hour.getValueFactory().setValue(azkarSettings.getLowPeriod() / 60);
    }

    @FXML
    private void midFrequencyAction() {
        toggleFrequencyBTN(midFrequency);
        azkarPeriod.getValueFactory().setValue(azkarSettings.getMidPeriod() % 60);
        azkarPeriod_hour.getValueFactory().setValue(azkarSettings.getMidPeriod() / 60);
    }

    @FXML
    private void rearFrequencyAction() {
        toggleFrequencyBTN(rearFrequency);
        azkarPeriod.getValueFactory().setValue(azkarSettings.getRearPeriod() % 60);
        azkarPeriod_hour.getValueFactory().setValue(azkarSettings.getRearPeriod() / 60);
    }

    private void toggleFrequencyBTN(JFXButton b) {
        azkarPeriod.getValueFactory().setValue(Integer.valueOf(azkarPeriod.getEditor().getText()));
        azkarPeriod_hour.getValueFactory().setValue(Integer.valueOf(azkarPeriod_hour.getEditor().getText()));
        if (azkarPeriod.getEditor().getText().equals("0") && azkarPeriod_hour.getEditor().getText().equals("0")) {
            azkarPeriod.getValueFactory().setValue(1);
        }
        // save data
        if (currentFrequency.equals(highFrequency)) {
            azkarSettings.setHighPeriod(azkarPeriod.getValueFactory().getValue() + azkarPeriod_hour.getValueFactory().getValue() * 60);
        } else if (currentFrequency.equals(midFrequency)) {
            azkarSettings.setMidPeriod(azkarPeriod.getValueFactory().getValue() + azkarPeriod_hour.getValueFactory().getValue() * 60);
        } else if (currentFrequency.equals(lowFrequency)) {
            azkarSettings.setLowPeriod(azkarPeriod.getValueFactory().getValue() + azkarPeriod_hour.getValueFactory().getValue() * 60);
        } else if (currentFrequency.equals(rearFrequency)) {
            azkarSettings.setRearPeriod(azkarPeriod.getValueFactory().getValue() + azkarPeriod_hour.getValueFactory().getValue() * 60);
        }
        // toggle style to selected button
        currentFrequency.getStyleClass().remove("frequency-btn-selected");
        currentFrequency = b;
        currentFrequency.getStyleClass().add("frequency-btn-selected");
    }

    @FXML
    private void goToAzkar() {
        try {
            Stage stage = new Stage();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/com/bayoumi/views/azkar/absolute/AbsoluteAzkar.fxml"))));
            stage.initOwner(SingleInstance.getInstance().getCurrentStage());
            HelperMethods.SetIcon(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            Logger.error(null, e, getClass().getName() + ".goToAzkar()");
        }
    }

    @Override
    public void saveToDB() {
        try {
            highFrequency.fire();
            azkarSettings.setAudioName(azkarAlarmComboBox.getValue());
            azkarSettings.setStopped(stopAzkar.isSelected());
            azkarSettings.setVolume((int) volumeSlider.getValue());
            azkarSettings.save();
            notificationSettings.setPosition(posComboBox.getValue());
            notificationSettings.save();
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.error(null, ex, getClass().getName() + ".saveToDB()");
        }
    }


    @FXML
    private void showZekr() {
        if (AbsoluteZekr.absoluteZekrObservableList.isEmpty()) {
            return;
        }
        // save alarm sound & pos in case of selecting new sound
        azkarSettings.setAudioName(azkarAlarmComboBox.getValue());
        azkarSettings.saveAlarmSound();
        notificationSettings.setPosition(posComboBox.getValue());
        notificationSettings.save();
        Platform.runLater(()
                -> {
            try {
                Notification.createControlsFX(
                        AbsoluteZekr.absoluteZekrObservableList.get(
                                new Random().nextInt(AbsoluteZekr.absoluteZekrObservableList.size())).getText(),
                        new Image("/com/bayoumi/images/Kaaba.png"), null,10);
            } catch (Exception ex) {
                Logger.error("createControlsFX", ex, getClass().getName() + ".runLater => createControlsFX()");
                ex.printStackTrace();
            }
        });
//        showZekrButton.setDisable(true);
//        PauseTransition pause = new PauseTransition(Duration.seconds(10));
//        pause.setOnFinished(event -> showZekrButton.setDisable(false));
//        pause.play();
    }

    @FXML
    private void muteUnmute() {
        if (isMuted) {
            // umMute
            isMuted = false;
            volumeSlider.setValue(previousValue);
        } else {
            // mute
            isMuted = true;
            previousValue = volumeSlider.getValue();
            volumeSlider.setValue(0);
        }
    }

}
