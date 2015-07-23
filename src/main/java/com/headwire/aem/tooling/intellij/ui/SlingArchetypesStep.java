package com.headwire.aem.tooling.intellij.ui;

import com.headwire.aem.tooling.intellij.explorer.SlingModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.Disposable;
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

    public SlingArchetypesStep(SlingModuleBuilder builder, @NotNull List<SlingModuleBuilder.ArchetypeTemplate> archetypeList) {
        moduleBuilder = builder;
        mavenArchetypeList = archetypeList;
        archetypeJBList.setModel(
            new CollectionListModel<SlingModuleBuilder.ArchetypeTemplate>(mavenArchetypeList)
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
                        ArchetypeTemplate archetype = (ArchetypeTemplate) value;
                        return archetype.getGroupId() + " : " + archetype.getArtifactId() + " : " + archetype.getVersion();
                    }

                    @Nullable
                    @Override
                    public Icon getIconFor(Object value) {
                        return null;
                    }

                    @Override
                    public boolean hasSeparatorAboveOf(Object value) {
                        ArchetypeTemplate current = (ArchetypeTemplate) value;
                        int index = mavenArchetypeList.indexOf(current);
                        boolean separator = index == 0;
                        if(!separator) {
                            ArchetypeTemplate previous = mavenArchetypeList.get(index - 1);
                            separator = !previous.getGroupId().equals(current.getGroupId()) || !previous.getArtifactId().equals(current.getArtifactId());
                        }
                        return separator;
                    }

                    @Nullable
                    @Override
                    public String getCaptionAboveOf(Object value) {
                        ArchetypeTemplate archetype = (ArchetypeTemplate) value;
                        return archetype.getGroupId() + " : " + archetype.getArtifactId();
                    }
                }
            )
        );
        //AS TODO: Setup the Table with the
        archetypeDescriptionField.setEditable(false);
        archetypeDescriptionField.setBackground(UIUtil.getPanelBackground());

//        requestUpdate();
        updateComponents();
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

//    private static MavenArchetype getArchetypeInfoFromPathComponent(Object sel) {
//        return (MavenArchetype)((DefaultMutableTreeNode)sel).getUserObject();
//    }

    private void updateArchetypeDescription() {
        MavenArchetype sel = getSelectedArchetype();
        String desc = sel == null ? null : sel.description;
        if (StringUtil.isEmptyOrSpaces(desc)) {
            archetypeDescriptionScrollPane.setVisible(false);
        }
        else {
            archetypeDescriptionScrollPane.setVisible(true);
            archetypeDescriptionField.setText(desc);
        }
    }

//    @Nullable
//    private static TreePath findNodePath(MavenArchetype object, TreeModel model, Object parent) {
//        for (int i = 0; i < model.getChildCount(parent); i++) {
//            DefaultMutableTreeNode each = (DefaultMutableTreeNode)model.getChild(parent, i);
//            if (each.getUserObject().equals(object)) return new TreePath(each.getPath());
//
//            TreePath result = findNodePath(object, model, each);
//            if (result != null) return result;
//        }
//        return null;
//    }
//
//    private static TreeNode groupAndSortArchetypes(Set<MavenArchetype> archetypes) {
//        List<MavenArchetype> list = new ArrayList<MavenArchetype>(archetypes);
//
//        Collections.sort(list, new Comparator<MavenArchetype>() {
//            public int compare(MavenArchetype o1, MavenArchetype o2) {
//                String key1 = o1.groupId + ":" + o1.artifactId;
//                String key2 = o2.groupId + ":" + o2.artifactId;
//
//                int result = key1.compareToIgnoreCase(key2);
//                if (result != 0) return result;
//
//                return o2.version.compareToIgnoreCase(o1.version);
//            }
//        });
//
//        Map<String, List<MavenArchetype>> map = new TreeMap<String, List<MavenArchetype>>();
//
//        for (MavenArchetype each : list) {
//            String key = each.groupId + ":" + each.artifactId;
//            List<MavenArchetype> versions = map.get(key);
//            if (versions == null) {
//                versions = new ArrayList<MavenArchetype>();
//                map.put(key, versions);
//            }
//            versions.add(each);
//        }
//
//        DefaultMutableTreeNode result = new DefaultMutableTreeNode("root", true);
//        for (List<MavenArchetype> each : map.values()) {
//            MavenArchetype eachArchetype = each.get(0);
//            DefaultMutableTreeNode node = new DefaultMutableTreeNode(eachArchetype, true);
//            for (MavenArchetype eachVersion : each) {
//                DefaultMutableTreeNode versionNode = new DefaultMutableTreeNode(eachVersion, false);
//                node.add(versionNode);
//            }
//            result.add(node);
//        }
//
//        return result;
//    }

//    public void requestUpdate() {
//
//        MavenArchetype selectedArch = getSelectedArchetype();
//        if (selectedArch == null) {
//            selectedArch = moduleBuilder.getArchetype();
//        }
//        if (selectedArch != null) myUseArchetypeCheckBox.setSelected(true);
//
//        if (myArchetypesTree.getRowCount() == 0) updateArchetypesList(selectedArch);
//    }

//    public void updateArchetypesList(final MavenArchetype selected) {
//        ApplicationManager.getApplication().assertIsDispatchThread();
//
//        myLoadingIcon.setBackground(myArchetypesTree.getBackground());
//
//        ((CardLayout)myArchetypesPanel.getLayout()).show(myArchetypesPanel, "loading");
//
//        final Object currentUpdaterMarker = new Object();
//        myCurrentUpdaterMarker = currentUpdaterMarker;
//
//        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
//            public void run() {
//                final Set<MavenArchetype> archetypes = MavenIndicesManager.getInstance().getArchetypes();
//
//                //noinspection SSBasedInspection
//                SwingUtilities.invokeLater(new Runnable() {
//                    public void run() {
//                        if (currentUpdaterMarker != myCurrentUpdaterMarker) return; // Other updater has been run.
//
//                        ((CardLayout) myArchetypesPanel.getLayout()).show(myArchetypesPanel, "archetypes");
//
//                        TreeNode root = groupAndSortArchetypes(archetypes);
//                        TreeModel model = new DefaultTreeModel(root);
//                        myArchetypesTree.setModel(model);
//
//                        if (selected != null) {
//                            TreePath path = findNodePath(selected, model, model.getRoot());
//                            if (path != null) {
//                                myArchetypesTree.expandPath(path.getParentPath());
//                                TreeUtil.selectPath(myArchetypesTree, path, true);
//                            }
//                        }
//
//                        updateArchetypeDescription();
//                    }
//                });
//            }
//        });
//    }

//    public boolean isSkipUpdateUI() {
//        return skipUpdateUI;
//    }

//    private void archetypeMayBeChanged() {
//        MavenArchetype selectedArchetype = getSelectedArchetype();
//        if (((moduleBuilder.getArchetype() == null) != (selectedArchetype == null))) {
//            moduleBuilder.setArchetype(selectedArchetype);
//            skipUpdateUI = true;
//            try {
//                if (myStep != null) {
//                    myStep.fireStateChanged();
//                }
//            }
//            finally {
//                skipUpdateUI = false;
//            }
//        }
//    }

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

//    private static class MyRenderer extends ColoredTreeCellRenderer {
//        public void customizeCellRenderer(JTree tree,
//                                          Object value,
//                                          boolean selected,
//                                          boolean expanded,
//                                          boolean leaf,
//                                          int row,
//                                          boolean hasFocus) {
//            Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
//            if (!(userObject instanceof MavenArchetype)) return;
//
//            MavenArchetype info = (MavenArchetype)userObject;
//
//            if (leaf) {
//                append(info.artifactId, SimpleTextAttributes.GRAY_ATTRIBUTES);
//                append(":" + info.version, SimpleTextAttributes.REGULAR_ATTRIBUTES);
//            }
//            else {
//                append(info.groupId + ":", SimpleTextAttributes.GRAY_ATTRIBUTES);
//                append(info.artifactId, SimpleTextAttributes.REGULAR_ATTRIBUTES);
//            }
//        }
//    }

}
