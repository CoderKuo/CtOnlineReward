package cn.ctcraft.ctonlinereward;

import cn.ctcraft.ctonlinereward.service.RewardStatus;

public class RewardEntity {
    private String rewardID;
    private RewardStatus status;

    public RewardEntity(String rewardID, RewardStatus status) {
        this.rewardID = rewardID;
        this.status = status;
    }

    public RewardEntity() {
    }

    public String getRewardID() {
        return rewardID;
    }

    public void setRewardID(String rewardID) {
        this.rewardID = rewardID;
    }

    public RewardStatus getStatus() {
        return status;
    }

    public void setStatus(RewardStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "RewardEntity{" +
                "rewardID='" + rewardID + '\'' +
                ", status=" + status +
                '}';
    }
}
