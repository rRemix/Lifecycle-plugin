package com.remix.lifecycle

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * created by Remix on 2019-09-06
 */
public class LifecyclePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        println "========LifecyclePlugin========"

        def android = project.android as AppExtension
        android.registerTransform(new LifecycleTransform())
    }
}
