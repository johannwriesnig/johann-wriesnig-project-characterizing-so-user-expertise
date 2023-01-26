package com.wriesnig.expertise.git.metrics;

import java.io.File;

public abstract class CyclomaticComplexity {
    File root;

    public CyclomaticComplexity(File root){
        this.root = root;
    }

    abstract public double getProjectComplexity();
}
