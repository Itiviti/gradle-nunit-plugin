package com.ullink.gradle.nunit

import org.gradle.api.GradleException

class NUnit3Mixins {
    def resultFormat

    // Deprecated
    void setRun(def run) {
        logDeprecatedParameters('run', 'where')
        this.setTest(run)
    }

    def setTestInternal(def testWrapper, def whereWrapper, def input) {
        def isCollection = isACollection(input)
        def isCSV = isACommaSeparatedList(input)
        whereWrapper.value = isCollection || isCSV ? translateToWhereConditions(input, isCSV) : translateToWhereCondition(input)
    }

    Boolean isACollection(def input) {
        [Collection, Object[]].any { it.isAssignableFrom(input.getClass()) }
    }

    def translateToWhereConditions(def input, boolean isCSV) {
        return translateToWhereConditions(isCSV ? input.tokenize(',') : input)
    }

    def translateToWhereConditions(Collection<String> runs) {
        def conditions = []

        runs.each {
            conditions.add(translateToWhereCondition(it))
        }

        return conditions
    }

    def translateToWhereCondition(def input) {
        return "test == \'$input\'"
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

    def getRunActionInput() {
        return where.value
    }

    def combine(def input) {
        return input.join(' or ')
    }

    def toFileName(input) {
        UUID.randomUUID().toString()
    }

    def buildAdditionalCommandArgs(def whereCondition, def testReportPath) {
        def commandLineArgs = []

        if (this.useX86) {
            commandLineArgs += '-x86'
        }
        if (this.shadowCopy) {
            commandLineArgs += '-shadowcopy'
        }
        if (this.testList) {
            commandLineArgs += "-testlist:${this.testList}"
        }
        if (whereCondition) {
            commandLineArgs += "-where:$whereCondition"
        }
        String resultFormatArg = ''
        if(resultFormat) {
            resultFormatArg = ";format=$resultFormat"
        }
        commandLineArgs += "-result:$testReportPath$resultFormatArg"

        commandLineArgs
    }
}
