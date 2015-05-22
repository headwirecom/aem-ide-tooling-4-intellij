package com.headwire.aem.tooling.intellij.eclipse.stub;

import org.apache.commons.io.IOUtils;
import org.apache.sling.ide.eclipse.core.internal.Activator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by schaefa on 5/18/15.
 */
public class JarBuilder {

    public InputStream buildJar(final IFolder sourceDir) throws CoreException {

        ByteArrayOutputStream store = new ByteArrayOutputStream();

        JarOutputStream zos = null;
        InputStream manifestInput = null;
        try {
            IResource manifestResource = sourceDir.findMember(JarFile.MANIFEST_NAME);
            if (manifestResource == null || manifestResource.getType() != IResource.FILE) {
                throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "No file named "
                    + JarFile.MANIFEST_NAME + " found under " + sourceDir));
            }

            manifestInput = ((IFile) manifestResource).getContents();

            Manifest manifest = new Manifest(manifestInput);

            zos = new JarOutputStream(store);
            zos.setLevel(Deflater.NO_COMPRESSION);
            // manifest first
            final ZipEntry anEntry = new ZipEntry(JarFile.MANIFEST_NAME);
            zos.putNextEntry(anEntry);
            manifest.write(zos);
            zos.closeEntry();
            zipDir(sourceDir, zos, "");
        } catch (IOException e) {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
        } finally {
            IOUtils.closeQuietly(zos);
            IOUtils.closeQuietly(manifestInput);
        }

        return new ByteArrayInputStream(store.toByteArray());
    }

    private void zipDir(final IFolder sourceDir, final ZipOutputStream zos, final String path) throws CoreException,
        IOException {

        for (final IResource f : sourceDir.members()) {
            if (f.getType() == IResource.FOLDER) {
                final String prefix = path + f.getName() + "/";
                zos.putNextEntry(new ZipEntry(prefix));
                zipDir((IFolder) f, zos, prefix);
            } else if (f.getType() == IResource.FILE) {
                final String entry = path + f.getName();
                if (JarFile.MANIFEST_NAME.equals(entry)) {
                    continue;
                }
                final InputStream fis = ((IFile) f).getContents();
                try {
                    final byte[] readBuffer = new byte[8192];
                    int bytesIn = 0;
                    final ZipEntry anEntry = new ZipEntry(entry);
                    zos.putNextEntry(anEntry);
                    while ((bytesIn = fis.read(readBuffer)) != -1) {
                        zos.write(readBuffer, 0, bytesIn);
                    }
                } finally {
                    IOUtils.closeQuietly(fis);
                }
            }
        }
    }
}
