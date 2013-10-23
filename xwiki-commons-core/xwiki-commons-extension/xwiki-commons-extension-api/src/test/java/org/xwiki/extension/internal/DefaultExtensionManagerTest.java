/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.extension.internal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionManager;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.extension.version.Version;
import org.xwiki.extension.version.VersionConstraint;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

/**
 * Unit tests for {@link DefaultExtensionManager}.
 * 
 * @version $Id$
 * @since 5.3M1
 */
public class DefaultExtensionManagerTest
{
    @Rule
    public final MockitoComponentMockingRule<ExtensionManager> mocker =
        new MockitoComponentMockingRule<ExtensionManager>(DefaultExtensionManager.class);

    private CoreExtensionRepository coreExtensionRepository;

    private LocalExtensionRepository localExtensionRepository;

    private InstalledExtensionRepository installedExtensionRepository;

    @Before
    public void setUp() throws Exception
    {
        coreExtensionRepository = mocker.getInstance(CoreExtensionRepository.class);
        mockExtensionRepositoryDescriptor(coreExtensionRepository, "core");

        localExtensionRepository = mocker.getInstance(LocalExtensionRepository.class);
        mockExtensionRepositoryDescriptor(localExtensionRepository, "local");

        installedExtensionRepository = mocker.getInstance(InstalledExtensionRepository.class);
        mockExtensionRepositoryDescriptor(installedExtensionRepository, "installed");
    }

    private ExtensionRepositoryDescriptor mockExtensionRepositoryDescriptor(ExtensionRepository repository, String id)
    {
        ExtensionRepositoryDescriptor descriptor = mock(ExtensionRepositoryDescriptor.class, id);
        when(repository.getDescriptor()).thenReturn(descriptor);
        when(descriptor.getId()).thenReturn(id);
        return descriptor;
    }

    /**
     * Unit test for {@link ExtensionManager#resolveExtension(org.xwiki.extension.ExtensionDependency, String)}.
     */
    @Test
    public void resolveMissingExtensionDependencyOnNamespace() throws Exception
    {
        ExtensionDependency extensionDependency = mock(ExtensionDependency.class);

        doThrow(ResolveException.class).when(coreExtensionRepository).resolve(extensionDependency);
        doThrow(ResolveException.class).when(localExtensionRepository).resolve(extensionDependency);

        assertNull(mocker.getComponentUnderTest().resolveExtension(extensionDependency, "wiki:math"));
    }

    /**
     * Unit test for {@link ExtensionManager#resolveExtension(org.xwiki.extension.ExtensionDependency, String)}.
     */
    @Test
    public void resolveInstalledExtensionDependencyOnNamespace() throws Exception
    {
        String namespace = "wiki:math";
        String extensionId = "test:extension";

        ExtensionDependency extensionDependency = mock(ExtensionDependency.class);
        doThrow(ResolveException.class).when(coreExtensionRepository).resolve(extensionDependency);

        InstalledExtension extension = mock(InstalledExtension.class);
        when(extensionDependency.getId()).thenReturn(extensionId);
        when(installedExtensionRepository.getInstalledExtension(extensionId, namespace)).thenReturn(extension);

        Version version = mock(Version.class);
        when(extension.getId()).thenReturn(new ExtensionId(extensionId, version));

        VersionConstraint versionConstraint = mock(VersionConstraint.class);
        when(extensionDependency.getVersionConstraint()).thenReturn(versionConstraint);
        when(versionConstraint.containsVersion(version)).thenReturn(true);

        assertSame(extension, mocker.getComponentUnderTest().resolveExtension(extensionDependency, namespace));
    }
}
