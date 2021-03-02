package cn.ctcraft.ctonlinereward.pojo;

import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RewardData implements Serializable {
    private List<ItemStack> rewardList = new ArrayList<>();

    public RewardData(List<ItemStack> rewardList) {
        this.rewardList = rewardList;
    }

    public List<ItemStack> getRewardList() {
        return rewardList;
    }

    public void setRewardList(List<ItemStack> rewardList) {
        this.rewardList = rewardList;
    }

    @Override
    public String toString() {
        return "RewardData{" +
                "rewardList=" + rewardList +
                '}';
    }
}
