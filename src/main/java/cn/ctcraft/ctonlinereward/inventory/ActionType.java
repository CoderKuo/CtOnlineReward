package cn.ctcraft.ctonlinereward.inventory;

public enum ActionType {
    closeGUI,
    sound,
    Message,
    openGUI;

    public static ActionType getActionType(String actionContent){
        if (actionContent.contains("[closeGUI]")){
            return closeGUI;
        }
        if (actionContent.contains("[sound]")){
            return sound;
        }
        if (actionContent.contains("[Message]")){
            return Message;
        }
        if (actionContent.contains("openGUI")){
            return openGUI;
        }
        return null;
    }
}
