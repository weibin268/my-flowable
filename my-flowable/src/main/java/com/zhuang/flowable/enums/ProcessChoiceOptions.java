package com.zhuang.flowable.enums;

public enum ProcessChoiceOptions {

    SAVE("save", "保存"),
    SUBMIT("submit", "提交"),
    DELETE("delete", "刪除"),
    APPROVE("approve", "批准"),
    BACK("back", "退回"),
    REJECT("reject", "驳回"),
    AGREE("agree", "同意"),
    DISAGREE("disagree", "不同意");

    ProcessChoiceOptions(String value, String name) {
        this.value = value;
        this.name = name;
    }

    private String value;
    private String name;


    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public boolean equals(String valueOrName) {
        if (valueOrName.equals(this.value)) return true;
        if (valueOrName.equals(this.name)) return true;
        return false;
    }
}

