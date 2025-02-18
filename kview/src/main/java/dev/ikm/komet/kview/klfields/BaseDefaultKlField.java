package dev.ikm.komet.kview.klfields;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.component.version.field.KlField;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

public abstract class BaseDefaultKlField<T> implements KlField<T> {
    protected final ObservableField<T> observableStringField;
    protected final ObservableView observableView;

    protected ObjectProperty<Node> klWidget = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            Tooltip.install(get(), tooltip);
        }
    };

    protected final boolean isEditable;

    protected final Tooltip tooltip = new Tooltip();

    private final String title;

    public BaseDefaultKlField(ObservableField<T> observableStringField, ObservableView observableView, boolean isEditable) {
        this.observableStringField = observableStringField;
        this.observableView = observableView;

        this.isEditable = isEditable;

        title = field().field().meaning().description() + ":";

        tooltip.setText(observableView.getDescriptionTextOrNid(observableStringField.purposeNid()));
    }

    @Override
    public ObservableField<T> field() {
        return observableStringField;
    }

    // -- title
    public String getTitle() { return title; }

    // -- klwidget
    @Override
    public <SGN extends Node> SGN klWidget() {
        return (SGN) klWidget.get();
    }
    protected void setKlWidget(Node klWidget) { this.klWidget.set(klWidget); }
}