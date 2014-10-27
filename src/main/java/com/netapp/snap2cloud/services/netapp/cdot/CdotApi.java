package com.netapp.snap2cloud.services.netapp.cdot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

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
import com.netapp.nmsdk.ontap.api.volume.VolumeSnapshotAttributes;
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
            snapshotInfo.setVolume(volume);

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
    
    public SnapshotInfo getLatestSnapshot(String volume) throws Exception {
        List<SnapshotInfo> snapshotList = this.getSnapshots(volume);
        
        TreeMap<Long, SnapshotInfo> sortedSnapshots = new TreeMap<Long, SnapshotInfo>();
        for (SnapshotInfo snapshot : snapshotList) {
            long epoch = this.getEpoch(snapshot.getAccessTime().toString());
            sortedSnapshots.put(epoch, snapshot);
        }

        return sortedSnapshots.get(sortedSnapshots.lastKey());
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
        LOGGER.info("NetApp volume clone " + clone + " of parent volume " + volume + " using snapshot " + snapshot);
        try {
            String junctionPath = "/" + clone;
            VolumeCloneCreateRequest volumeClone = new VolumeCloneCreateRequest();
            volumeClone.withVolume(clone).withParentSnapshot(snapshot).withJunctionActive(true).withJunctionPath(junctionPath).withParentVolume(volume);

            runner.run(volumeClone);
            LOGGER.info("NetApp volume clone " + clone + " of parent volume " + volume + " using snapshot " + snapshot + " completed successfully");
        } catch (Exception e) {
            LOGGER.error("NetApp volume clone " + clone + " of parent volume " + volume + " using snapshot " + snapshot + " failed");
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
    
    public void exportVolume(String svm, String volume, String policy, String host, String permission)
            throws Exception {

        LOGGER.info("NetApp volume export of volume " + volume + " using policy " + policy + " to host " + host + " with permission " + permission);
        try {
            if (!this.isPolicy(svm, policy)) {
                this.createPolicy(svm, policy);
            }
        } catch (Exception e) {
            LOGGER.error("NetApp policy check for poliy " + policy + " failed");
            throw new Exception(e.getMessage(), e);
        }
        
        try {
            if (!this.isRule(svm, policy, host)) {
                this.createRule(svm, policy, host, permission);
            }
        } catch (Exception e) {
            LOGGER.error("NetApp policy check for rule in policy " + policy + " failed");
            throw new Exception(e.getMessage(), e);
        }
        
        try {
            this.modifyVolume(svm, volume, policy);
        } catch (Exception e) {
            LOGGER.error("NetApp volume modify to apply policy " + policy + " failed");
            throw new Exception(e.getMessage(), e);
        }
        
        LOGGER.info("NetApp volume export of volume " + volume + " using policy " + policy + " to host " + host + " with permission " + permission + " completed successfully");
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
    
    public boolean createRule(String svm, String policy, String host, String permission) throws Exception {

        String rwSecurityFlavor = null;
        String roSecurityFlavor = null;
        String suSecurityFlavor = null;
        String anonUserId = "0";

        LOGGER.info("NetApp rule create for policy " + policy + " to host " + host + " using permission " + permission);
        
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
            ExportRuleCreateRequest ruleCreateRequest = new ExportRuleCreateRequest().withPolicyName(policy).withClientMatch(host)
                    .withRoRule(roSecurityFlavor).withRwRule(rwSecurityFlavor).withSuperUserSecurity(suSecurityFlavor).withAnonymousUserId(anonUserId);
            runner.run(ruleCreateRequest);

        } catch (Exception e) {
            LOGGER.error("NetApp rule create for policy " + policy + " to host " + host + " using permission " + permission + " failed");
            throw new Exception(e.getMessage(), e);
        }
        
        LOGGER.info("NetApp rule create for policy " + policy + " to host " + host + " using permission " + permission + " completed successfully");
        
        return true;
    }
    
    public boolean createPolicy(String svm, String policy) throws Exception {
        LOGGER.info("NetApp policy create for " + policy);
        try {
            
            ExportPolicyCreateRequest policyCreate = new ExportPolicyCreateRequest().withPolicyName(policy);
            runner.run(policyCreate);

        } catch (Exception e) {
            LOGGER.error("NetApp policy create for " + policy + " failed");
            throw new Exception(e.getMessage(), e);
        }      
        
        LOGGER.error("NetApp policy create for " + policy + " completed successfully");
        
        return true;
    }
    
    public boolean modifyVolume(String svm, String volume, String policy) throws Exception {
        LOGGER.info("NetApp volume modify for volume " + volume + " using policy " + policy);
        try {
            VolumeAttributes volumeQueryAttributes = new VolumeAttributes();
            VolumeAttributes volumeAttributes = new VolumeAttributes();

            VolumeIdAttributes volumeIdAttributes = new VolumeIdAttributes();
            volumeIdAttributes.setName(volume);

            VolumeSnapshotAttributes volumeSnapshotAttributes = new VolumeSnapshotAttributes();
            volumeSnapshotAttributes.setSnapdirAccessEnabled(false);
            
            VolumeExportAttributes volumeExportAttributes = new VolumeExportAttributes();
            volumeExportAttributes.setPolicy(policy);

            volumeQueryAttributes.setVolumeIdAttributes(volumeIdAttributes);
            volumeAttributes.setVolumeExportAttributes(volumeExportAttributes);
            volumeAttributes.setVolumeIdAttributes(volumeIdAttributes);
            volumeAttributes.setVolumeSnapshotAttributes(volumeSnapshotAttributes);
            
            VolumeModifyIterRequest modifyVolume = new VolumeModifyIterRequest().withQuery(volumeQueryAttributes).withAttributes(volumeAttributes);
            runner.run(modifyVolume);

        } catch (Exception e) {
            LOGGER.error("NetApp volume modify for volume " + volume + " using policy " + policy + " failed");
            throw new Exception(e.getMessage(), e);
        }   
        
        LOGGER.info("NetApp volume modify for volume " + volume + " using policy " + policy + " completed successfully");
        return true;
    }    

    public void unmountVolume(String svm, String volume)
            throws Exception {

        LOGGER.info("NetApp volume unmount for volume " + volume);
        try {
            VolumeUnmountRequest volumeOfflineRequest = new VolumeUnmountRequest().withVolumeName(volume).withForce(true);

            runner.run(volumeOfflineRequest);
        } catch (Exception e) {
            LOGGER.error("NetApp volume unmount for volume " + volume + " failed");
            throw new Exception(e.getMessage(), e);
        }      
        
        LOGGER.info("NetApp volume unmount for volume " + volume + " completed successfully");
    }

    public void offlineVolume(String svm, String volume)
            throws Exception {

        LOGGER.info("NetApp volume offline for volume " + volume);
        try {
            VolumeOfflineRequest volumeOfflineRequest = new VolumeOfflineRequest().withName(volume);

            runner.run(volumeOfflineRequest);
        } catch (Exception e) {
            LOGGER.error("NetApp volume offline for volume " + volume +  " failed");
            throw new Exception(e.getMessage(), e);
        }
        
        LOGGER.info("NetApp volume offline for volume " + volume + " completed successfully");
    }
    
    public void destroyVolume(String svm, String volume)
            throws Exception {

        LOGGER.info("NetApp volume destroy for volume " + volume);
        try {
            VolumeDestroyRequest volumeOfflineRequest = new VolumeDestroyRequest().withName(volume);

            runner.run(volumeOfflineRequest);
        } catch (Exception e) {
            LOGGER.error("NetApp volume unmount for volume " + volume + " failed");
            throw new Exception(e.getMessage(), e);
        }    
        
        LOGGER.info("NetApp volume unmount for volume " + volume + " completed successfully");
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
    
    public long getEpoch(String date) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
        
        Date parsedDate;
        try {
            parsedDate = df.parse(date);
        } catch (ParseException e) {
            throw new Exception(e.getMessage(), e);
        }
        
        return parsedDate.getTime();
    }
}
