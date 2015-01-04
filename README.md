gradle-nunit-plugin
===================

A gradle plugin for launching NUnit tests

The plugin is named 'nunit'. It creates a task 'nunit' that may be configured as follows:

    nunit {
        // optional - defaults to '2.6.3'
        nunitVersion
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
    }