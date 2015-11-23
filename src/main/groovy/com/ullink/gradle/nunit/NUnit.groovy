package com.ullink.gradle.nunit

import org.bouncycastle.math.raw.Nat
import org.gradle.api.GradleException
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.TaskAction

import static org.apache.tools.ant.taskdefs.condition.Os.*

class NUnit extends ConventionTask {
    def nunitHome
    def nunitVersion
    def nunitDownloadUrl
    List testAssemblies
    def include
    def exclude
    def where
    def framework
    def verbosity
    def config
    def timeout
    def runList
    def run
    def testList
    def test
    def reportFolder
    boolean useX86 = false
    boolean shadowCopy = false

    boolean ignoreFailures = false

    NUnit() {
        conventionMapping.map "reportFolder", { new File(outputFolder, 'reports') }
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

    boolean getIsV3() {
        getNunitVersion().startsWith("3.")
    }

    File getNunitExec() {
        assert getNunitHome(), "You must install NUnit and set nunit.home property or NUNIT_HOME env variable"
        File nunitExec = isV3
            ? nunitBinFile('nunit3-console.exe')
            : nunitBinFile("nunit-console${useX86 ? '-x86' : ''}.exe")
        assert nunitExec.isFile(), "You must install NUnit and set nunit.home property or NUNIT_HOME env variable"
        return nunitExec
    }

    File getOutputFolder() {
        new File(project.buildDir, 'nunit')
    }

    File getReportFolderImpl() {
        project.file(getReportFolder())
    }

    File getTestReportPath() {
        new File(getReportFolderImpl(), 'TestResult.xml')
    }

    @TaskAction
    def build() {
        def cmdLine = [nunitExec.absolutePath, *buildCommandArgs()]
        if (!isFamily(FAMILY_WINDOWS)) {
            cmdLine = ["mono", *cmdLine]
        }
        execute(cmdLine)
    }

    // Return values of nunit v2 and v3 are defined in
    // https://github.com/nunit/nunitv2/blob/master/src/ConsoleRunner/nunit-console/ConsoleUi.cs and
    // https://github.com/nunit/nunit/blob/master/src/NUnitConsole/nunit-console/ConsoleRunner.cs
    def execute(commandLineExec) {
        prepareExecute()

        def mbr = project.exec {
            commandLine = commandLineExec
            ignoreExitValue = ignoreFailures
        }

        int exitValue = mbr.exitValue
        if (exitValue == 0) {
            return
        }

        boolean anyTestFailing = exitValue > 0
        if (anyTestFailing && ignoreFailures) {
            return
        }

        throw new GradleException("${nunitExec} execution failed (ret=${mbr.exitValue})");
    }

    def prepareExecute() {
        getReportFolderImpl().mkdirs()
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
            commandLineArgs += "-trace=$verb"
        }
        if (isV3) {
            if (useX86) {
                commandLineArgs += '-x86'
            }
        }
        if (isV3) {
            if (include || exclude) {
                throw new GradleException("'include'/'exclude' options aren't supported on NUnit v3, use 'where' option instead")
            }
            if (where) {
                commandLineArgs += "-where:$where"
            }
        } else {
            if (where) {
                throw new GradleException("'where' isn't supported on NUnit v2, you need to set the NUnit version to v3+")
            }
            if (exclude) {
                commandLineArgs += "-exclude:$exclude"
            }
            if (include) {
                commandLineArgs += "-include:$include"
            }
        }
        if (framework) {
            commandLineArgs += "-framework:$framework"
        }
        if (isV3) {
            if (shadowCopy) {
                commandLineArgs += '-shadowcopy'
            }
        } else {
            if (!shadowCopy) {
                commandLineArgs += '-noshadow'
            }
        }
        // Maintain backward compatibility with old (nunit 2.x) gradle files.
        if (!testList && runList) {
            testList = runList
        }
        if (!test && run) {
            test = run
        }

        if (testList) {
            if (isV3) {
                commandLineArgs += "-testlist:$testList"
            } else {
                commandLineArgs += "-runList:$testList"
            }
        }
        if (test) {
            if (isV3) {
                commandLineArgs += "-test:$test"
            } else {
                commandLineArgs += "-run:$test"
            }
        }
        if (config){
            commandLineArgs += "-config:$config"
        }
        if (timeout){
            commandLineArgs += "-timeout:$timeout"
        }
        if (isV3) {
            commandLineArgs += "-out:$testReportPath"
        } else {
            commandLineArgs += "-xml:$testReportPath"
        }
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
