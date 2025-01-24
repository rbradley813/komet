/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.kview.mvvm.view.helloworld;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.events.AxiomChangeEvent;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.propsheet.KometPropertySheet;
import dev.ikm.komet.framework.propsheet.SheetItem;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.AddFullyQualifiedNameEvent;
import dev.ikm.komet.kview.events.AddOtherNameToConceptEvent;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.events.CreateConceptEvent;
import dev.ikm.komet.kview.events.EditConceptEvent;
import dev.ikm.komet.kview.events.EditConceptFullyQualifiedNameEvent;
import dev.ikm.komet.kview.events.EditOtherNameConceptEvent;
import dev.ikm.komet.kview.events.OpenPropertiesPanelEvent;
import dev.ikm.komet.kview.fxutils.MenuHelper;
import dev.ikm.komet.kview.mvvm.model.DataModelHelper;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.view.stamp.StampEditController;
import dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.plugin.layer.IkmServiceManager;
import dev.ikm.plugins.identicon.api.IdenticonPlugin;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldDefinitionForEntity;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.carlfx.cognitive.loader.NamedVm;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.controlsfx.control.PopOver;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.ikm.komet.framework.events.FrameworkTopics.RULES_TOPIC;
import static dev.ikm.komet.kview.fxutils.MenuHelper.fireContextMenuEvent;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideIn;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideOut;
import static dev.ikm.komet.kview.fxutils.ViewportHelper.clipChildren;
import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.addToMembershipPattern;
import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.getMembershipPatterns;
import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.isInMembershipPattern;
import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.removeFromMembershipPattern;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel.AXIOM;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel.CONCEPT_STAMP_VIEW_MODEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel.CREATE;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel.CURRENT_ENTITY;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel.EDIT;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel.FULLY_QUALIFIED_NAME;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel.OTHER_NAMES;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel.MODULES_PROPERTY;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel.PATHS_PROPERTY;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.MODULE;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.PATH;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.STATUS;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.TIME;
import static dev.ikm.tinkar.terms.TinkarTerm.DEFINITION_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE;
import static dev.ikm.tinkar.terms.TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION;
import static dev.ikm.tinkar.terms.TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE;

public class HelloWorldController {
    private static final Logger LOG = LoggerFactory.getLogger(HelloWorldController.class);

    @FXML
    private Button closeConceptButton;

    /**
     * The outermost part of the details (may remove)
     */
    @FXML
    private BorderPane detailsOuterBorderPane;

    /**
     * The inner border pane contains all content.
     */
    @FXML
    private BorderPane detailsCenterBorderPane;

    //////////  Banner area /////////////////////
    @FXML
    private VBox identiconVbox;

    @FXML
    private Label fqnTitleText;

    @FXML
    private TextArea definitionTextArea;

    @FXML
    private TextField identifierText;

    private ViewProperties viewProperties;

    @InjectViewModel
    private ConceptViewModel conceptViewModel;
    private EvtBus eventBus;

    private UUID conceptTopic;

//    static {
//        IkmServiceManager.setPluginDirectory(Path.of("target/plugins"));
//    }

    public HelloWorldController() {
    }

    public HelloWorldController(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }

    @FXML
    public void initialize() {
        conceptViewModel.getProperty("conceptPublicId").addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal instanceof PublicId publicId) {
                ServiceLoader<IdenticonPlugin> identiconPlugins = PluggableService.load(IdenticonPlugin.class);

                System.out.println("Found " + identiconPlugins.stream().count() + " Identicon Plugins:");
                identiconPlugins.stream()
                        .map(ServiceLoader.Provider::get)
                        .forEach(javaFxIdenticonPlugin -> System.out.println(javaFxIdenticonPlugin.getName()));

                identiconPlugins.stream()
                        .map(ServiceLoader.Provider::get)
                        .forEach(javaFxIdenticonPlugin -> {
                            java.awt.Image image = javaFxIdenticonPlugin.getIdenticonImage(5,5, publicId, (seed) -> seed.idString());
                            ImageView imageView = new ImageView(SwingFXUtils.toFXImage((BufferedImage) image, null));
                            imageView.setFitHeight(32);
                            imageView.setFitWidth(32);
                            identiconVbox.getChildren().add(imageView);
                        });
            }
        });
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    public ValidationViewModel getConceptViewModel() {
        return conceptViewModel;
    }

    private Consumer<HelloWorldController> onCloseConceptWindow;
    public void setOnCloseConceptWindow(Consumer<HelloWorldController> onClose) {
        this.onCloseConceptWindow = onClose;
    }

    @FXML
    void closeConceptWindow(ActionEvent event) {
        LOG.info("Cleanup occurring: Closing Window with concept: " + fqnTitleText.getText());
        if (this.onCloseConceptWindow != null) {
            onCloseConceptWindow.accept(this);
        }
        Pane parent = (Pane) detailsOuterBorderPane.getParent();
        parent.getChildren().remove(detailsOuterBorderPane);
    }

    public void updateModel(final ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }

    public void updateView() {
        EntityFacade entityFacade = conceptViewModel.getPropertyValue(CURRENT_ENTITY);
        StampViewModel stampViewModel = new StampViewModel();
        if (entityFacade != null) { // edit concept
            getConceptViewModel().setPropertyValue(MODE, EDIT);
            if (conceptViewModel.getPropertyValue(CONCEPT_STAMP_VIEW_MODEL) == null) {

                // add a new stamp view model to the concept view model
                stampViewModel.setPropertyValue(MODE, EDIT)
                        .setPropertyValues(MODULES_PROPERTY, stampViewModel.findAllModules(viewProperties), true)
                        .setPropertyValues(PATHS_PROPERTY, stampViewModel.findAllPaths(viewProperties), true);

                conceptViewModel.setPropertyValue(CONCEPT_STAMP_VIEW_MODEL,stampViewModel);
            }

            // TODO: Ability to change Concept record. but for now user can edit stamp but not affect Concept version.
            EntityVersion latestVersion = viewProperties.calculator().latest(entityFacade).get();
            StampEntity stamp = latestVersion.stamp();
            updateStampViewModel(EDIT, stamp);
        } else { // create concept
            getConceptViewModel().setPropertyValue(MODE, CREATE);
            stampViewModel.setPropertyValue(MODE, CREATE);
        }
        conceptViewModel.setPropertyValue(CONCEPT_STAMP_VIEW_MODEL,stampViewModel);

        // Display info for top banner area
        updateConceptBanner();
    }

    /**
     * Responsible for populating the top banner area of the concept view panel.
     */
    public void updateConceptBanner() {
        // do not update ui should be blank
        if (getConceptViewModel().getPropertyValue(MODE) == CREATE) {
            return;
        }

        EntityFacade entityFacade = conceptViewModel.getPropertyValue(CURRENT_ENTITY);
        // TODO do a null check on the entityFacade
        // Title (FQN of concept)
        final ViewCalculator viewCalculator = viewProperties.calculator();
        String conceptNameStr = viewCalculator.getFullyQualifiedDescriptionTextWithFallbackOrNid(entityFacade);
        fqnTitleText.setText(conceptNameStr);

        // Definition description text
        definitionTextArea.setText(viewCalculator.getDefinitionDescriptionText(entityFacade.nid()).orElse(""));

        // Public ID (UUID)
        List<String> idList = entityFacade.publicId().asUuidList().stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        idList.addAll(DataModelHelper.getIdsToAppend(viewCalculator, entityFacade.toProxy()));
        String idStr = String.join(", ", idList);
        identifierText.setText(idStr);

        // Identicon
        Image identicon = Identicon.generateIdenticonImage(entityFacade.publicId());
//        identiconImageView.setImage(identicon);
    }

    private void updateStampViewModel(String mode, StampEntity stamp) {
        ValidationViewModel stampViewModel = conceptViewModel.getPropertyValue(CONCEPT_STAMP_VIEW_MODEL);
        if (conceptViewModel.getPropertyValue(CONCEPT_STAMP_VIEW_MODEL) != null) {
            stampViewModel.setPropertyValue(MODE, mode)
                    .setPropertyValue(STATUS, stamp.state())
                    .setPropertyValue(MODULE, stamp.moduleNid())
                    .setPropertyValue(PATH, stamp.pathNid())
                    .setPropertyValue(TIME, stamp.time())
                    .save(true);
        }
    }

    public void clearView() {
//        identiconImageView.setImage(null);
        //fqnTitleText.setText(""); // Defaults to 'Concept Name'. It's what is specified in Scene builder
        definitionTextArea.setText("");
        identifierText.setText("");
    }

    public void setConceptTopic(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }
}
