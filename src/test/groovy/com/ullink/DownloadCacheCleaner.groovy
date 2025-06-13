package com.ullink

import org.gradle.internal.impldep.org.apache.commons.io.FileUtils

import java.nio.file.Paths

class DownloadCacheCleaner {

    private DownloadCacheCleaner(){}

    static void clear() {
        def cachesDir = Paths.get(System.getProperty("user.home"), ".gradle", "caches", "nunit").toFile()
        if (cachesDir.exists()) {
            println "Cleaning up caches directory: $cachesDir"
            cachesDir.eachDir { File dir ->
                println "Deleting directory: $dir"
                FileUtils.deleteDirectory(dir)
            }
        } else {
            println "Caches directory does not exist: $cachesDir, no cleanup needed"
        }
    }

}
