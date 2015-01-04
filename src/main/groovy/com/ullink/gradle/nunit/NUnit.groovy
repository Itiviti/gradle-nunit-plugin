package com.ullink.gradle.nunit

import org.gradle.api.GradleException
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.TaskAction

class NUnit extends ConventionTask {
    def nunitHome
    def nunitVersion
    List testAssemblies
    def verbosity
    boolean useX86 = false

    boolean ignoreFailures = false

    NUnit() {
        inputs.files {
            getTestAssemblies()
        }
        outputs.files {
            getTestReportPath()
        }
    }

    File nunitConsoleBinFile(String file) {
        new File(project.file(getNunitHome()), "bin/${file}")
    }

    File getNunitExec() {
        assert getNunitHome(), "You must install NUnit and set nunit.home property or NUNIT_HOME env variable"
        File nunitExec = new File(project.file(getNunitHome()), "bin/nunit-console${useX86 ? '-x86' : ''}.exe")
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
        def commandLine = [nunitExec] + buildCommandArgs()
        execute(commandLine)
    }

    def execute(commandLineArgs) {
        prepareExecute()
        def mbr = project.exec {
            commandLine = commandLineArgs
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
            commandLineArgs += '/trace=' + verb
        }
        commandLineArgs += '/xml:' + testReportPath
        getTestAssemblies().each {
            commandLineArgs += project.file(it)
        }
        commandLineArgs
    }
}
