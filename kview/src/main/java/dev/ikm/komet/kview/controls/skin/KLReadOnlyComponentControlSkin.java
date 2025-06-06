package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.NodeUtils;
import dev.ikm.komet.kview.controls.*;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class KLReadOnlyComponentControlSkin extends KLReadOnlyBaseControlSkin<KLReadOnlyComponentControl> {

    private final HBox textContainer = new HBox();
    private final ImageView iconImageView = new ImageView();
    private final Label textLabel = new Label();

    /**
     * @param control The control for which this Skin should attach to.
     */
    public KLReadOnlyComponentControlSkin(KLReadOnlyComponentControl control) {
        super(control);

        mainContainer.getChildren().addAll(textContainer);

        textContainer.getChildren().addAll(iconImageView, promptTextLabel, textLabel);

        textLabel.textProperty().bind(Bindings.select(control, "value", "text"));
        iconImageView.imageProperty().bind(Bindings.select(control, "value", "icon"));

        textLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textLabel, Priority.ALWAYS);

        iconImageView.setFitWidth(16);
        iconImageView.setFitHeight(16);

        initTexts(control);

        setupContextMenu(control);

        // CSS
        textContainer.getStyleClass().add("text-container");
        textLabel.getStyleClass().add("text");
    }

    private void initTexts(KLReadOnlyComponentControl control) {
        updatePromptTextAndTextLabelVisibility(control);
        control.valueProperty().addListener(observable -> updatePromptTextAndTextLabelVisibility(control));
    }

    private void updatePromptTextAndTextLabelVisibility(KLReadOnlyComponentControl control) {
        boolean showPromptText = control.getValue() == null;

        NodeUtils.setShowing(promptTextLabel, showPromptText);
        NodeUtils.setShowing(textLabel, !showPromptText);
        NodeUtils.setShowing(iconImageView, !showPromptText);
    }

    private void setupContextMenu(KLReadOnlyComponentControl control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Component", KometIcon.IconValue.PENCIL, this::fireOnEditAction)
        );

        contextMenu.getItems().addAll(
                new SeparatorMenuItem(),
                createMenuItem("Remove", KometIcon.IconValue.TRASH, this::fireOnRemoveAction)
        );
    }
}