package com.headwire.aem.tooling.intellij.ui;

import com.headwire.aem.tooling.intellij.explorer.SlingModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.popup.ListItemDescriptorAdapter;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.popup.list.GroupedItemsListRenderer;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenArchetype;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.headwire.aem.tooling.intellij.explorer.SlingModuleBuilder.ArchetypeTemplate;

public class SlingArchetypesStep extends ModuleWizardStep implements Disposable {

    private JPanel mainPanel;
    private JPanel archetypeListPanel;
    private JBList archetypeJBList;
    private JTextArea archetypeDescriptionField;
    private JScrollPane archetypeDescriptionScrollPane;

    private final SlingModuleBuilder moduleBuilder;
    private final List<SlingModuleBuilder.ArchetypeTemplate> mavenArchetypeList;

    public SlingArchetypesStep(SlingModuleBuilder builder, @NotNull List<ArchetypeTemplate> archetypeList) {
        moduleBuilder = builder;
        mavenArchetypeList = archetypeList;
        archetypeJBList.setModel(
            new CollectionListModel<ArchetypeTemplate>(
                mavenArchetypeList.isEmpty() ?
                    Arrays.asList(new NoArchetypeTemplate()) :
                    mavenArchetypeList
            )
        );
        archetypeJBList.addListSelectionListener(
            new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent listSelectionEvent) {
                    updateArchetypeDescription();
                }
            }
        );
        archetypeJBList.setCellRenderer(
            new GroupedItemsListRenderer(
                new ListItemDescriptorAdapter() {
                    @Nullable
                    @Override
                    public String getTextFor(Object value) {
                        if(value instanceof NoArchetypeTemplate) {
                            return ((NoArchetypeTemplate) value).getDescription();
                        } else {
                            ArchetypeTemplate archetype = (ArchetypeTemplate) value;
                            return archetype.getGroupId() + " : " + archetype.getArtifactId() + " : " + archetype.getVersion();
                        }
                    }

                    @Nullable
                    @Override
                    public Icon getIconFor(Object value) {
                        return null;
                    }

                    @Override
                    public boolean hasSeparatorAboveOf(Object value) {
                        if(value instanceof NoArchetypeTemplate) {
                            return false;
                        } else {
                            ArchetypeTemplate current = (ArchetypeTemplate) value;
                            int index = mavenArchetypeList.indexOf(current);
                            boolean separator = index == 0;
                            if (!separator) {
                                ArchetypeTemplate previous = mavenArchetypeList.get(index - 1);
                                separator = !previous.getGroupId().equals(current.getGroupId()) || !previous.getArtifactId().equals(current.getArtifactId());
                            }
                            return separator;
                        }
                    }

                    @Nullable
                    @Override
                    public String getCaptionAboveOf(Object value) {
                        if(value instanceof NoArchetypeTemplate) {
                            return "";
                        } else {
                            ArchetypeTemplate archetype = (ArchetypeTemplate) value;
                            return archetype.getGroupId() + " : " + archetype.getArtifactId();
                        }
                    }
                }
            )
        );
        //AS TODO: Setup the Table with the
        archetypeDescriptionField.setEditable(false);
        archetypeDescriptionField.setBackground(UIUtil.getPanelBackground());

        updateComponents();
    }

    public boolean validate()
        throws ConfigurationException
    {
        // If this no Archetype is selected then throw a Commit Step Exception
        if(mavenArchetypeList.isEmpty()) {
            throw new ConfigurationException("No Archetype available for Sling / AEM. To create a module please use another Builder like Maven.");
        }
        if(archetypeJBList.getSelectedValue() == null) {
            throw new ConfigurationException("No Archetype was selected. If this is for a Project select an Archetype. For a Module please use another Builder");
        }
        return true;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void updateComponents() {
    }

    @Nullable
    public MavenArchetype getSelectedArchetype() {
        MavenArchetype ret = null;
        if(!archetypeJBList.isSelectionEmpty()) {
            ret = ((ArchetypeTemplate) archetypeJBList.getSelectedValue()).getMavenArchetype();
        }
        return ret;
    }

    private void updateArchetypeDescription() {
        MavenArchetype sel = getSelectedArchetype();
        String desc = sel == null ? null : sel.description;
        if (StringUtil.isEmptyOrSpaces(desc)) {
            archetypeDescriptionScrollPane.setVisible(false);
        } else {
            archetypeDescriptionScrollPane.setVisible(true);
            archetypeDescriptionField.setText(desc);
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public JComponent getComponent() {
        return getMainPanel();
    }

    @Override
    public void updateDataModel() {
        MavenArchetype selectedArchetype = getSelectedArchetype();
        moduleBuilder.selectArchetype(  selectedArchetype);
    }

    public static class NoArchetypeTemplate extends ArchetypeTemplate {
        @Override
        public String getDescription() {
            return "There are no Archetypes for Modules";
        }
    }
}
