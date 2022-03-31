package com.example.markdowntestproject.customnode;

import org.commonmark.node.CustomNode;

public class ProjectNode extends CustomNode {

    private String destination;

    public ProjectNode(String destination) {
        this.destination = destination;
    }

    public String getDestination(){
        return  destination;
    }
}