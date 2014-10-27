package com.netapp.snap2cloud.model;

public class Storage {
    
    private String cluster;
    private String svm;
    private String port;
    private String userName;
    private String password;
    private boolean secure;
    private String volumeName;
    private String volumeCloneName;
    private String snapshotName;
    private boolean latestSnapshot;
    
    public String getSvm() {
        return svm;
    }
    
    public void setSvm(String svm) {
        this.svm = svm;
    }

    public String getVolumeName() {
        return volumeName;
    }

    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName;
    }

    public String getVolumeCloneName() {
        return volumeCloneName;
    }

    public void setVolumeCloneName(String volumeCloneName) {
        this.volumeCloneName = volumeCloneName;
    }

    public String getSnapshotName() {
        return snapshotName;
    }

    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isLatestSnapshot() {
        return latestSnapshot;
    }

    public void setLatestSnapshot(boolean latestSnapshot) {
        this.latestSnapshot = latestSnapshot;
    }

}
