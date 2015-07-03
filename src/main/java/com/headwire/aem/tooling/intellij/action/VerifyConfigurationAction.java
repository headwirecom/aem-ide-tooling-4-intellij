package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.eclipse.ProjectUtil;
import com.headwire.aem.tooling.intellij.eclipse.ServerUtil;
import com.headwire.aem.tooling.intellij.eclipse.stub.CoreException;
import com.headwire.aem.tooling.intellij.eclipse.stub.IServer;
import com.headwire.aem.tooling.intellij.eclipse.stub.NullProgressMonitor;
import com.headwire.aem.tooling.intellij.explorer.ServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.util.Constants;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataContextWrapper;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.sling.ide.filter.Filter;
import org.apache.sling.ide.filter.FilterResult;
import org.apache.sling.ide.transport.Repository;
import org.apache.sling.ide.transport.ResourceProxy;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

/**
 * Created by schaefa on 6/12/15.
 */
public class VerifyConfigurationAction extends AbstractProjectAction {

    public static final String VERIFY_CONTENT_WITH_WARNINGS = "VerifyContentWithWarnings";

    public VerifyConfigurationAction() {
        super("verify.configuration.action");
    }

    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext) {
        DataContext wrappedDataContext = SimpleDataContext.getSimpleContext(VERIFY_CONTENT_WITH_WARNINGS, true, dataContext);
        doVerify(project, wrappedDataContext);
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext) {
        ServerConnectionManager serverConnectionManager = ServiceManager.getService(project, ServerConnectionManager.class);
        return serverConnectionManager != null && serverConnectionManager.isConfigurationSelected();
    }

    public void doVerify(final Project project, final DataContext dataContext) {
        int ret = Messages.OK;
        ServerTreeSelectionHandler selectionHandler = getSelectionHandler(project);
        ServerConnectionManager serverConnectionManager = ServiceManager.getService(project, ServerConnectionManager.class);
        MessageManager messageManager = getMessageManager(project);
        if(selectionHandler != null && serverConnectionManager != null && messageManager != null) {
            ServerConfiguration source = selectionHandler.getCurrentConfiguration();
            if(source != null) {
                // Before we can verify we need to ensure the Configuration is properly bound to Maven
                serverConnectionManager.checkBinding(source);
                // Verify each Module to see if all prerequisites are met
                for(ServerConfiguration.Module module: source.getModuleList()) {
                    if(module.isSlingPackage()) {
                        // Check if the Filter is available for Content Modules
                        Filter filter = null;
                        try {
                            filter = ProjectUtil.loadFilter(module);
                            if(filter == null) {
                                ret = messageManager.showAlertWithOptions(NotificationType.ERROR, "server.configuration.filter.file.not.found", module.getName());
                                module.setStatus(ServerConfiguration.SynchronizationStatus.compromised);
                                if(ret == Messages.CANCEL) {
                                    return;
                                }
                            }
                        } catch(CoreException e) {
                            ret = messageManager.showAlertWithOptions(NotificationType.ERROR, "server.configuration.filter.file.failure", module.getName(), e.getMessage());
                            module.setStatus(ServerConfiguration.SynchronizationStatus.compromised);
                            if(ret == Messages.CANCEL) {
                                return;
                            }
                        }
                        // Check if the Content Modules have a Content Resource
                        List<String> resourceList = serverConnectionManager.findContentResources(module);
                        if(resourceList.isEmpty()) {
                            ret = messageManager.showAlertWithOptions(NotificationType.ERROR, "server.configuration.content.folder.not.", module.getName());
                            module.setStatus(ServerConfiguration.SynchronizationStatus.compromised);
                            if(ret == Messages.CANCEL) {
                                return;
                            }
                        }
                        // Check if Content Module Folders all have a .content.xml
                        Object temp = dataContext.getData(VERIFY_CONTENT_WITH_WARNINGS);
                        boolean verifyWithWarnings = !(temp instanceof Boolean) || ((Boolean) temp);
                        if(verifyWithWarnings && filter != null) {
                            Repository repository = ServerConnectionManager.obtainRepository(module.getParent(), messageManager);
                            if(repository != null) {
                                // Get the Content Root /jcr_root)
                                for(String contentPath : resourceList) {
                                    VirtualFile rootFile = project.getProjectFile().getFileSystem().findFileByPath(contentPath);
                                    if(rootFile != null) {
                                        // Loop over all folders and check if .content.xml file is there
                                        ret = checkFolderContent(repository, messageManager, serverConnectionManager, module, null, rootFile, filter);
                                        if(ret == Messages.CANCEL) {
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private int checkFolderContent(Repository repository, MessageManager messageManager, ServerConnectionManager serverConnectionManager, ServerConfiguration.Module module, File rootDirectory, VirtualFile parentDirectory, Filter filter) {
        int ret = Messages.OK;
        // Loop over all files in the given folder
        for(VirtualFile child: parentDirectory.getChildren()) {
            // We only need to check Folders
            if(child.isDirectory()) {
                // If the Root Directory is null then just started -> Create Root Directory File and work on its children
                if(rootDirectory == null) {
                    rootDirectory = new File(parentDirectory.getPath());
                } else {
                    // Get Relative Paths
                    String relativeParentPath = parentDirectory.getPath().substring(rootDirectory.getPath().length());
                    String relativeChildPath = child.getPath().substring(rootDirectory.getPath().length());
                    List<ResourceProxy> childNodes = null;
                    FilterResult filterResult = null;
                    if(filter != null) {
                        // Check if the Resource is part of the Filter
                        filterResult = filter.filter(rootDirectory, relativeChildPath);
                    }
                    // We don't need to check anything if it is part of one of the filter entries
                    if(filterResult != FilterResult.ALLOW) {
                        if(filterResult == FilterResult.DENY) {
                            // File is not part of a filter entry so it will never be deployed
                            ret = messageManager.showAlertWithOptions(NotificationType.ERROR, "server.configuration.content.folder.filtered.out", relativeChildPath, module.getName());
                            module.setStatus(ServerConfiguration.SynchronizationStatus.compromised);
                            if(ret == Messages.CANCEL) {
                                return ret;
                            }
                        } else if(child.findChild(Constants.CONTENT_FILE_NAME) == null) {
                            boolean isOk = false;
                            // .content.xml file not found in the filter -> check if that folder exists on the Server
                            if(repository != null && childNodes == null) {
                                childNodes = serverConnectionManager.getChildrenNodes(repository, relativeParentPath);
                                for(ResourceProxy childNode: childNodes) {
                                    String path = childNode.getPath();
                                    boolean found = path.equals(relativeChildPath);
                                    if(found) {
                                        isOk = true;
                                        break;
                                    }
                                }
                            }
                            if(!isOk) {
                                ret = messageManager.showAlertWithOptions(NotificationType.ERROR, "server.configuration.content.folder.configuration.not.found", relativeChildPath, module.getName());
                                module.setStatus(ServerConfiguration.SynchronizationStatus.compromised);
                                if(ret == Messages.CANCEL) {
                                    return ret;
                                }
                            }
                        }
                    }
                }
                // Check the children of the current folder
                ret = checkFolderContent(repository, messageManager, serverConnectionManager, module, rootDirectory, child, filter);
                if(ret == Messages.CANCEL) {
                    return ret;
                }
            }
        }
        return ret;
    }
}
