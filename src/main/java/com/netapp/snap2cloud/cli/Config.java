package com.netapp.snap2cloud.cli;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.netapp.snap2cloud.model.Host;
import com.netapp.snap2cloud.model.Hyperscaler;
import com.netapp.snap2cloud.model.Storage;

public class Config {
    
    public Map<String, String> getConfig(String configPath) throws Exception {
        Map<String, String> config = new HashMap<String, String>();
        Properties properties = new Properties();
        InputStream is = new FileInputStream(configPath);

        properties.load(is);

        for (String key : properties.stringPropertyNames()) {
            config.put(key, properties.get(key).toString());
        }
        
        return config;
      }
    
    public Storage getStorageDetails(Map<String, String> configMap) {
        
        Storage storageModel = new Storage();
        storageModel.setCluster(configMap.get("NTAP_CLUSTER_NAME"));
        storageModel.setPort(configMap.get("NTAP_PORT"));
        storageModel.setUserName(configMap.get("NTAP_USERNAME"));
        storageModel.setPassword(configMap.get("NTAP_PASSWORD"));
        storageModel.setSecure(Boolean.valueOf(configMap.get("NTAP_HTTPS_ENABLE")));
        storageModel.setSnapshotName(configMap.get("NTAP_SNAPSHOT_NAME"));
        storageModel.setSvm(configMap.get("NTAP_SVM"));
        storageModel.setVolumeName(configMap.get("NTAP_VOLUME_NAME"));
        storageModel.setVolumeCloneName(configMap.get("NTAP_VOLUME_CLONE_NAME"));
        storageModel.setLatestSnapshot(Boolean.valueOf(configMap.get("NTAP_USE_LATEST_SNAPSHOT")));
        
        return storageModel;
    }
    
    public Host getHostDetails(Map<String, String> configMap) {
        
        Host hostModel = new Host();
        hostModel.setExportHostorNetwork(configMap.get("HOST_EXPORT_CIDR"));
        hostModel.setExportPermission(configMap.get("HOST_EXPORT_PERMISSION"));
        hostModel.setExportPolicyName(configMap.get("HOST_EXPORT_POLICY_NAME"));
        hostModel.setMountCmd(configMap.get("HOST_MOUNT_CMD"));
        hostModel.setUmountCmd(configMap.get("HOST_UMOUNT_CMD"));
        hostModel.setMountPath(configMap.get("HOST_MOUNT_PATH"));
        
        return hostModel;
    }
    
    public Hyperscaler getHyperscalerDetails(Map<String, String> configMap) {
        
        Hyperscaler hyperscalerModel = new Hyperscaler();
        hyperscalerModel.setS3BucketName(configMap.get("HYPERSCALER_BUCKET_NAME"));
        hyperscalerModel.setBackupTimestamp(Boolean.valueOf(configMap.get("HYPERSCALER_BACKUP_TIMESTAMP_ENABLE")));
        hyperscalerModel.setRetention(Integer.valueOf(configMap.get("HYPERSCALER_BACKUP_RETENTION")));
        
        return hyperscalerModel;
    }
}
