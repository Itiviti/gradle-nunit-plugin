gradle-nunit-plugin [![Build status](https://ci.appveyor.com/api/projects/status/riwqs7bua948ncvw?svg=true)](https://ci.appveyor.com/project/gluck/gradle-nunit-plugin) [![Build Status](https://travis-ci.org/Ullink/gradle-nunit-plugin.svg?branch=master)](https://travis-ci.org/Ullink/gradle-nunit-plugin)
===================

A gradle plugin for launching NUnit tests

It is compatible with the new plugin mechanism and can be used with:

    plugins {
      id 'com.ullink.nunit' version '1.0'
    }

Or, when using Gradle lower than 2.1:

    buildscript {
        repositories {
          mavenCentral()
        }

        dependencies {
            classpath "com.ullink.gradle:gradle-nunit-plugin:1.0"
        }
    }

It creates a task 'nunit' that may be configured as follows:

    nunit {
        // optional - defaults to '2.6.4'
        nunitVersion
        // optional - defaults to 'https://github.com/nunit/nunitv2/releases/download'
        nunitDownloadUrl
        // optional - defaults to NUNIT_HOME env variable if set or to a downloaded version of NUnit fitting the
        // specified nunitVersion
        nunitHome
        // mandatory - the assemblies containing the tests to be run
        testAssemblies
        // optional - if set, specifies the /trace argument of nunit-console
        verbosity
        // optional - defaults to FALSE and determines the nunit-console.exe used (-x86 one if TRUE)
        useX86
        // optional - defaults to FALSE and termines the behavior of the task if the nunit-console.exe program exits
        // abnormally
        ignoreFailures = false

        // Mapped NUnit-Console Command Line Options
        exclude 'Database'
        include 'BaseLine'
        framework 'net-1.1'
        noShadow = true
    }

# License

All these plugins are licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) with no warranty (expressed or implied) for any purpose.
