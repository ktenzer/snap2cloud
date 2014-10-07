package com.netapp.snap2cloud.model;

public class Host {

    private String exportPolicyName;
    private String exportHostorNetwork;
    private String exportPermission;
    private String mountPath;
    private String mountCmd;
    private String umountCmd;
    
    public String getExportPolicyName() {
        return exportPolicyName;
    }
    
    public void setExportPolicyName(String exportPolicyName) {
        this.exportPolicyName = exportPolicyName;
    }

    public String getExportHostorNetwork() {
        return exportHostorNetwork;
    }

    public void setExportHostorNetwork(String exportHostorNetwork) {
        this.exportHostorNetwork = exportHostorNetwork;
    }

    public String getExportPermission() {
        return exportPermission;
    }

    public void setExportPermission(String exportPermission) {
        this.exportPermission = exportPermission;
    }

    public String getMountPath() {
        return mountPath;
    }

    public void setMountPath(String mountPath) {
        this.mountPath = mountPath;
    }

    public String getMountCmd() {
        return mountCmd;
    }

    public void setMountCmd(String mountCmd) {
        this.mountCmd = mountCmd;
    }

    public String getUmountCmd() {
        return umountCmd;
    }

    public void setUmountCmd(String umountCmd) {
        this.umountCmd = umountCmd;
    }
    
}
