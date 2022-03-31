package com.example.markdowntestproject.customnode;

import org.commonmark.node.CustomNode;

public class MentionNode extends CustomNode {

    private String mention;

    public MentionNode(String mention) {
        this.mention = mention;
    }

    public String getMention(){
        return  mention;
    }
}
