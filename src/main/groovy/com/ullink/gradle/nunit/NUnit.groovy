package com.ullink.gradle.nunit

import org.gradle.api.GradleException
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.TaskAction

import static org.apache.tools.ant.taskdefs.condition.Os.*

class NUnit extends ConventionTask {
    def nunitHome
    def nunitVersion
    List testAssemblies
    def include
    def exclude
    def framework
    def verbosity
    boolean useX86 = false
    boolean noShadow = false

    boolean ignoreFailures = false

    NUnit() {
        inputs.files {
            getTestAssemblies()
        }
        outputs.files {
            getTestReportPath()
        }
    }

    File nunitBinFile(String file) {
        new File(project.file(getNunitHome()), "bin/${file}")
    }

    File getNunitExec() {
        assert getNunitHome(), "You must install NUnit and set nunit.home property or NUNIT_HOME env variable"
        File nunitExec = nunitBinFile("nunit-console${useX86 ? '-x86' : ''}.exe")
        assert nunitExec.isFile(), "You must install NUnit and set nunit.home property or NUNIT_HOME env variable"
        return nunitExec
    }

    File getOutputFolder() {
        new File(project.buildDir, 'nunit')
    }

    File getReportsFolder() {
        new File(outputFolder, 'reports')
    }

    File getTestReportPath() {
        new File(reportsFolder, 'TestResult.xml')
    }

    @TaskAction
    def build() {
        def cmdLine = [nunitExec.absolutePath, *buildCommandArgs()]
        if (!isFamily(FAMILY_WINDOWS)) {
            cmdLine = ["mono", *cmdLine]
        }
        execute(cmdLine)
    }

    def execute(commandLineExec) {
        prepareExecute()

        def mbr = project.exec {
            commandLine = commandLineExec
            ignoreExitValue = ignoreFailures
        }

        switch (mbr.exitValue) {
            case 0:
            case 16:
                break;
            case 1: // nunit test failed
                // ok & failure
                if (ignoreFailures) break;
            default:
                // nok
                throw new GradleException("${nunitExec} execution failed (ret=${mbr.exitValue})");
        }
    }

    def prepareExecute() {
        reportsFolder.mkdirs()
    }

    def buildCommandArgs() {
        def commandLineArgs = []

        String verb = verbosity
        if (!verb) {
            if (logger.debugEnabled) {
                verb = 'Verbose'
            } else if (logger.infoEnabled) {
                verb = 'Info'
            } else { // 'quiet'
                verb = 'Warning'
            }
        }
        if (verb) {
            commandLineArgs += '-trace=' + verb
        }
        if (exclude) {
            commandLineArgs += '-exclude:' + exclude
        }
        if (include) {
            commandLineArgs += '-include:' + include
        }
        if (framework) {
            commandLineArgs += '-framework:' + framework
        }
        if (noShadow) {
            commandLineArgs += '-noshadow'
        }
        commandLineArgs += '-xml:' + testReportPath
        getTestAssemblies().each {
            def file = project.file(it)
            if (file.exists() )
                commandLineArgs += file
            else
                commandLineArgs += it
        }
        commandLineArgs
    }
}
