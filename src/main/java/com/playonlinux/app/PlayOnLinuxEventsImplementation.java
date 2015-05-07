package com.playonlinux.app;

import com.playonlinux.domain.PlayOnLinuxError;
import com.playonlinux.injection.Component;
import com.playonlinux.injection.Inject;
import com.playonlinux.ui.api.EventHandler;
import com.playonlinux.domain.Script;
import com.playonlinux.ui.api.InstalledApplications;
import com.playonlinux.ui.api.VirtualDrives;

import java.io.File;

@Component
public class PlayOnLinuxEventsImplementation implements EventHandler {

    @Inject
    static PlayOnLinuxBackgroundServicesManager playOnLinuxBackgroundServicesManager;

    private InstalledApplications installedApplications;
    private PlayOnLinuxVirtualDrivesImplementation virtualDrives;

    public void runLocalScript(File scriptToRun) {
        Script playonlinuxScript = new Script(scriptToRun);
        playOnLinuxBackgroundServicesManager.register(playonlinuxScript);
    }

    @Override
    public InstalledApplications getInstalledApplications() throws PlayOnLinuxError {
        if(installedApplications == null) {
            installedApplications = new PlayOnLinuxInstalledApplicationImplementation();
        }
        return this.installedApplications;
    }

    @Override
    public VirtualDrives getInstalledVirtualDrives() throws PlayOnLinuxError {
        if(virtualDrives == null) {
            virtualDrives = new PlayOnLinuxVirtualDrivesImplementation();
        }

        return this.virtualDrives;
    }
}
