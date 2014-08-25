package com.netapp.snap2cloud.services.netapp.cdot;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netapp.nmsdk.client.ApiRunner;
import com.netapp.nmsdk.ontap.api.exports.ExportPolicyCreateRequest;
import com.netapp.nmsdk.ontap.api.exports.ExportPolicyGetIterRequest;
import com.netapp.nmsdk.ontap.api.exports.ExportPolicyInfo;
import com.netapp.nmsdk.ontap.api.exports.ExportRuleCreateRequest;
import com.netapp.nmsdk.ontap.api.exports.ExportRuleGetIterRequest;
import com.netapp.nmsdk.ontap.api.exports.ExportRuleInfo;
import com.netapp.nmsdk.ontap.api.snapshot.SnapshotGetIterRequest;
import com.netapp.nmsdk.ontap.api.snapshot.SnapshotInfo;
import com.netapp.nmsdk.ontap.api.snapshot.SnapshotDeleteRequest;
import com.netapp.nmsdk.ontap.api.volume.VolumeAttributes;
import com.netapp.nmsdk.ontap.api.volume.VolumeCloneCreateRequest;
import com.netapp.nmsdk.ontap.api.volume.VolumeDestroyRequest;
import com.netapp.nmsdk.ontap.api.volume.VolumeExportAttributes;
import com.netapp.nmsdk.ontap.api.volume.VolumeGetIterRequest;
import com.netapp.nmsdk.ontap.api.volume.VolumeIdAttributes;
import com.netapp.nmsdk.ontap.api.volume.VolumeModifyIterRequest;
import com.netapp.nmsdk.ontap.api.volume.VolumeOfflineRequest;
import com.netapp.nmsdk.ontap.api.volume.VolumeUnmountRequest;

public class CdotApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(NtapConn.class);
    
    private String svm;
    private ApiRunner runner;
    
    public CdotApi (String svm, NtapConnModel connection) {
        this.svm = svm;    
        this.runner = NtapConn.getRunner(svm, connection);
    }

    public List<SnapshotInfo> getSnapshots(String volume) throws Exception {
        LOGGER.info("NetApp snapshot list of volume " + volume);
        try {
            SnapshotInfo snapshotInfo = new SnapshotInfo();
            snapshotInfo.setVserver(svm);

            SnapshotGetIterRequest snapshotGetIterRequest = new SnapshotGetIterRequest();
            snapshotGetIterRequest.withQuery(snapshotInfo);

            List<SnapshotInfo> snapshotList = runner.run(snapshotGetIterRequest).getAttributesList();
            
            LOGGER.info("NetApp snapshot list of volume " + volume + " completed successfully");
            return snapshotList;      
        } catch (Exception e) {
            LOGGER.info("NetApp snapshot list of volume " + volume + " failed");
            throw new Exception(e.getMessage(), e);
        }
    }
    
    public void deleteSnapshot(String volume, String snapshot) throws Exception{
        LOGGER.info("NetApp snapshot delete of snapshot " + snapshot + " on volume " + volume);
        try {
        SnapshotDeleteRequest snapshotDelete = new SnapshotDeleteRequest();
            snapshotDelete.withVolume(volume).withSnapshot(snapshot);
            
            runner.run(snapshotDelete);
            
            LOGGER.info("NetApp snapshot delete of snapshot " + snapshot + " on volume " + volume + " completed successfully");
        } catch (Exception e) {
            LOGGER.error("NetApp snapshot delete of snapshot " + snapshot + " on volume " + volume + " failed");
            throw new Exception(e.getMessage(), e);
        }       
    }
    
    public void cloneVolumeFromSnapshot(String volume, String snapshot, String clone) throws Exception {
        LOGGER.info("NetApp volume clone " + clone + " of parent volume " + volume + " using snapshot" + snapshot);
        try {
            String junctionPath = "/" + volume;
            VolumeCloneCreateRequest volumeClone = new VolumeCloneCreateRequest();
            volumeClone.withVolume(clone).withParentSnapshot(snapshot).withJunctionActive(true).withJunctionPath(junctionPath).withParentVolume(volume);

            runner.run(volumeClone);
            LOGGER.info("NetApp volume clone " + clone + " of parent volume " + volume + " using snapshot" + snapshot + " completed successfully");
        } catch (Exception e) {
            LOGGER.error("NetApp volume clone " + clone + " of parent volume " + volume + " using snapshot" + snapshot + " failed");
            throw new Exception(e.getMessage(), e);
        }
    }
    
    public void deleteVolume(String volume) throws Exception {
        LOGGER.info("NetApp volume delete of volume " + volume);
        //unmount volume
        try {
            unmountVolume(svm, volume);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new Exception(e.getMessage()); 
        }
        
        // offline volume if online
        try {
            if (this.isVolumeOnline(svm, volume)) {
                offlineVolume(svm, volume);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new Exception(e.getMessage()); 
        }
        
        // destroy volume
        try {
            destroyVolume(svm, volume);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new Exception(e.getMessage()); 
        }
        
        LOGGER.info("NetApp volume delete of volume " + volume + " completed successfully");
    }
    
    public void exportVolume(String svm, String containerName, String policyName, String hostName, String permission)
            throws Exception {

        try {
            if (!this.isPolicy(svm, policyName)) {
                this.createPolicy(svm, policyName);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
        
        try {
            if (!this.isRule(svm, policyName, hostName)) {
                this.createRule(svm, policyName, hostName, permission);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
        
        try {
            this.modifyVolume(svm, containerName, policyName);
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }
    
    public boolean isRule(String svm, String policyName, String hostName) throws Exception {
        boolean isRule = true;
        try {
            ExportRuleInfo ruleInfo = new ExportRuleInfo();
            ruleInfo.setPolicyName(policyName);
            ruleInfo.setClientMatch(hostName);
            
            ExportRuleGetIterRequest ruleGetRequest = new ExportRuleGetIterRequest().withQuery(ruleInfo);
            List<ExportRuleInfo> ruleList = runner.run(ruleGetRequest).getAttributesList();

            if (ruleList == null) {
                isRule = false;
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }    
        
        return isRule;
    }
    
    public boolean isPolicy(String svm, String policyName) throws Exception {

        boolean isPolicy = true;
        try {
            ExportPolicyInfo policyInfo = new ExportPolicyInfo();
            policyInfo.setPolicyName(policyName);
            
            ExportPolicyGetIterRequest ruleGetRequest = new ExportPolicyGetIterRequest().withQuery(policyInfo);
            List<ExportPolicyInfo> policyInfoList = runner.run(ruleGetRequest).getAttributesList();

            if (policyInfoList == null) {
                isPolicy = false;
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }     
        
        return isPolicy;
    }
    
    public boolean createRule(String svm, String policyName, String hostName, String permission) throws Exception {

        String rwSecurityFlavor = null;
        String roSecurityFlavor = null;
        String suSecurityFlavor = null;
        String anonUserId = "0";

        if (permission.toString().equalsIgnoreCase("read_only")) {
            rwSecurityFlavor = "any";
            roSecurityFlavor = "never";
            suSecurityFlavor = "never";
        } else if (permission.toString().equalsIgnoreCase("read_write")) {
            rwSecurityFlavor = "any";
            roSecurityFlavor = "any";
            suSecurityFlavor = "never";
        } else if (permission.toString().equalsIgnoreCase("root")) {
            rwSecurityFlavor = "any";
            roSecurityFlavor = "any";
            suSecurityFlavor = "any";
        } else {
            throw new Exception("Incorrect permission " + permission.toString() + " was given");
        }

        try {
            ExportRuleCreateRequest ruleCreateRequest = new ExportRuleCreateRequest().withPolicyName(policyName).withClientMatch(hostName)
                    .withRoRule(roSecurityFlavor).withRwRule(rwSecurityFlavor).withSuperUserSecurity(suSecurityFlavor).withAnonymousUserId(anonUserId);
            runner.run(ruleCreateRequest);

        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
        
        return true;
    }
    
    public boolean createPolicy(String svm, String policyName) throws Exception {
        try {
            
            ExportPolicyCreateRequest policyCreate = new ExportPolicyCreateRequest().withPolicyName(policyName);
            runner.run(policyCreate);

        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }      
        
        return true;
    }
    
    public boolean modifyVolume(String svm, String volumeName, String policyName) throws Exception {
        try {
            VolumeAttributes volumeQueryAttributes = new VolumeAttributes();
            VolumeAttributes volumeAttributes = new VolumeAttributes();

            VolumeIdAttributes volumeIdAttributes = new VolumeIdAttributes();
            volumeIdAttributes.setName(volumeName);
            
            VolumeExportAttributes volumeExportAttributes = new VolumeExportAttributes();
            volumeExportAttributes.setPolicy(policyName);

            volumeQueryAttributes.setVolumeIdAttributes(volumeIdAttributes);
            volumeAttributes.setVolumeExportAttributes(volumeExportAttributes);
            volumeAttributes.setVolumeIdAttributes(volumeIdAttributes);
            
            VolumeModifyIterRequest modifyVolume = new VolumeModifyIterRequest().withQuery(volumeQueryAttributes).withAttributes(volumeAttributes);
            runner.run(modifyVolume);

        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }   
        
        return true;
    }    

    public void unmountVolume(String svm, String volumeName)
            throws Exception {

        try {
            VolumeUnmountRequest volumeOfflineRequest = new VolumeUnmountRequest().withVolumeName(volumeName).withForce(true);

            runner.run(volumeOfflineRequest);
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }      
    }

    public void offlineVolume(String svm, String volumeName)
            throws Exception {

        try {
            VolumeOfflineRequest volumeOfflineRequest = new VolumeOfflineRequest().withName(volumeName);

            runner.run(volumeOfflineRequest);
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }      
    }
    
    public void destroyVolume(String svm, String volumeName)
            throws Exception {

        try {
            VolumeDestroyRequest volumeOfflineRequest = new VolumeDestroyRequest().withName(volumeName);

            runner.run(volumeOfflineRequest);
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }      
    }
    
    public boolean isVolumeOnline(String svm, String volumeName)
            throws Exception {

        boolean isOnline = true;
        try {
            VolumeAttributes volumeQueryAttributes = new VolumeAttributes();

            VolumeIdAttributes volumeIdAttributes = new VolumeIdAttributes();
            volumeIdAttributes.setName(volumeName);
            
            volumeQueryAttributes.setVolumeIdAttributes(volumeIdAttributes);
            
            VolumeGetIterRequest volumeGetIterRequest = new VolumeGetIterRequest().withQuery(volumeQueryAttributes);

            List<VolumeAttributes> volumeList = runner.run(volumeGetIterRequest).getAttributesList();
            
            for (VolumeAttributes volume : volumeList) {
                if (! volume.getVolumeStateAttributes().getState().equalsIgnoreCase("online")) {
                    isOnline = false;
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }      
        
        return isOnline;
    }
}
