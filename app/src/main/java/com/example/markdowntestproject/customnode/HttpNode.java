package com.example.markdowntestproject.customnode;

import org.commonmark.node.CustomNode;

public class HttpNode extends CustomNode {

    private String url;

    public HttpNode(String url) {
        this.url = url;
    }

    public String getUrl(){
        return  url;
    }
}

