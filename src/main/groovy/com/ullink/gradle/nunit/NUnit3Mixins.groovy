package com.ullink.gradle.nunit

import org.gradle.api.GradleException

class NUnit3Mixins {

    def where

    // Deprecated
    void setRun(def run) {
        logDeprecatedParameters('run', 'test')
        this.setTest(run)
    }

    // Deprecated
    void setRunList(def runList) {
        logDeprecatedParameters('runList', 'testList')
        this.setTestList(runList)
    }

    void logDeprecatedParameters(def variableName, def newVariableName) {
        this.getLogger().warn("'$variableName' option has been deprecated, please use '$newVariableName' option instead")
    }

    // Obsolete
    void setInclude(def obj) {
        throwOnObsoleteParameters('include', 'where')
    }

    // Obsolete
    void setExclude(def obj) {
        throwOnObsoleteParameters('exclude', 'where')
    }

    void throwOnObsoleteParameters(def variableName, def newVariableName) {
        throw new GradleException("'$variableName' option isn't supported, please use '$newVariableName' option instead")
    }

    File getNunitExec() {
        File nunitExec = this.nunitBinFile('nunit3-console.exe')
        assert nunitExec.isFile(), "You must install NUnit and set nunit.home property or NUNIT_HOME env variable"
        return nunitExec
    }

    def buildAdditionalCommandArgs(def test, def testReportPath) {
        def commandLineArgs = []

        if (this.useX86) {
            commandLineArgs += '-x86'
        }
        if (where) {
            commandLineArgs += "-where:$where"
        }
        if (this.shadowCopy) {
            commandLineArgs += '-shadowcopy'
        }
        if (this.testList) {
            commandLineArgs += "-testlist:${this.testList}"
        }
        if (test) {
            commandLineArgs += "-test:${test}"
        }
        commandLineArgs += "-result:$testReportPath"

        commandLineArgs
    }
}