package com.ullink.gradle.nunit

import org.gradle.api.GradleException

class NUnit3Mixins {

    def where
    def test
    def testList

    def useX86
    def shadowCopy

    def methodMissing(String name, args) {
        def obsoleteParameters = [
            run: 'test',
            runList: 'testList',
            include: 'where',
            exclude: 'where'
        ]
        methodMissing(obsoleteParameters, name, args)
    }

    static void methodMissing(def obsoleteParameterMap, String name, args) {
        if (name.startsWith('set')) {

            // variables missing in NUnit 3
            def variableName = name.substring(3, 1).toLowerCase()
            if (name.length > 3) {
                 variableName += name.substring(4)
            }

            // Obsolete variables
            if (obsoleteParameterMap[variableName]) {
                throwOnObsoleteParameter variableName obsoleteParameterMap[variableName]
            }
        }
        throw new MissingMethodException(name, NUnit3Mixins.class, args)
    }

    static void throwOnObsoleteParameter(def oldName, def newName) {
        throw new GradleException("'$oldName' option isn't supported, use '$newName' option instead")
    }

    File getNunitExec() {
        File nunitExec = this.nunitBinFile('nunit3-console.exe')
        assert nunitExec.isFile(), "You must install NUnit and set nunit.home property or NUNIT_HOME env variable"
        return nunitExec
    }

    def buildAdditionalCommandArgs(def test, def testReportPath) {
        def commandLineArgs = []

        if (useX86) {
            commandLineArgs += '-x86'
        }
        if (where) {
            commandLineArgs += "-where:$where"
        }
        if (shadowCopy) {
            commandLineArgs += '-shadowcopy'
        }
        if (testList) {
            commandLineArgs += "-testlist:$testList"
        }
        if (test) {
            commandLineArgs += "-test:$test"
        }
        commandLineArgs += "-result:$testReportPath"

        commandLineArgs
    }
}