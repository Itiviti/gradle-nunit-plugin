package com.ullink

import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class NUnitTest extends Specification {
    def "test input correctly parses lists and comma delimited strings"() {
        given:
            def nunit = getNUnitTask()
        expect:
            nunit.getTestInputAsList('A,B,C') == ['A', 'B', 'C']
            nunit.getTestInputAsList('A') == ['A']
            nunit.getTestInputAsList(['A', 'B', 'C']) == ['A', 'B', 'C']
            nunit.getTestInputAsList(['A']) == ['A']
    }

    def "test version generates correct fixed download version"(String version, String result) {
        given:
            def nunit = getNUnitTask()
        when:
            nunit.nunitVersion = version
        then:
            nunit.getFixedDownloadVersion() == result
        where:
            version | result
            "2.0.0" | "2.0.0"
            "3.5.0" | "3.5"
            "3.9.0" | "v3.9"
            "3.11.1" | "v3.11.1"
    }

    def "test input is parsed correctly as List in corner cases"() {
        given:
            def nunit = getNUnitTask()
        when:
            def emptyStringList = nunit.getTestInputAsList('')
            def emptyListList = nunit.getTestInputAsList([])
            def nullList = nunit.getTestInputAsList(null)
        then:
            emptyStringList instanceof List
            !emptyStringList
            emptyListList instanceof List
            !emptyListList
            nullList instanceof List
            !nullList
    }

    def "test input is parsed correctly when using nunit 2"() {
        given:
            def nunit = getNUnitTask()
            nunit.nunitVersion = '2.0.0'
        expect:
            nunit.getTestInputsAsString('A,B,C') == 'A,B,C'
            nunit.getTestInputsAsString('A') == 'A'
            nunit.getTestInputsAsString(['A', 'B', 'C']) == 'A,B,C'
            nunit.getTestInputsAsString(['A']) == 'A'
            nunit.getTestInputsAsString('') == ''
            nunit.getTestInputsAsString([]) == ''
            nunit.getTestInputsAsString(null) == ''
    }

    def "test input is parsed correctly when using nunit 3"() {
        given:
            def nunit = getNUnitTask()
        when: "nunit 3 is used"
            nunit.nunitVersion = '3.0.0'
        then:
            nunit.getTestInputsAsString('A,B,C') == 'A,B,C'
            nunit.getTestInputsAsString('A') == 'A'
            nunit.getTestInputsAsString(['A', 'B', 'C']) == 'A or B or C'
            nunit.getTestInputsAsString(['A']) == 'A'
            nunit.getTestInputsAsString('') == ''
            nunit.getTestInputsAsString([]) == ''
            nunit.getTestInputsAsString(null) == ''
    }

    def "a single run in parallel generates one report file"() {
        given:
            def nunit = getNUnitTask()
        when:
            nunit.reportFileName = 'TestResult.xml'
            nunit.parallelForks = true
            nunit.run = 'Test1'
            nunit.reportFolder = './'
        then:
            nunit.getTestReportPath() == new File(nunit.project.projectDir, "TestResult.xml")
    }

    def "a single run generates only one report file"() {
        given:
            def nunit = getNUnitTask()
        when:
            nunit.reportFileName = 'TestResult.xml'
            nunit.parallelForks = false
            nunit.run = 'Test1'
            nunit.reportFolder = './'
        then:
            nunit.getTestReportPath() == new File(nunit.project.projectDir, "TestResult.xml")
    }

    def "multiple runs generate only one report file"() {
        given:
            def nunit = getNUnitTask()
        when:
            nunit.reportFileName = 'TestResult.xml'
            nunit.parallelForks = false
            nunit.run = ['Test1', 'Test2']
            nunit.reportFolder = './'
        then:
            nunit.getTestReportPath() == new File(nunit.project.projectDir, "TestResult.xml")
    }

    def "multiple runs in parallel generate only one report file"() {
        given:
            def nunit = getNUnitTask()
        when:
            nunit.reportFileName = 'TestResult.xml'
            nunit.parallelForks = true
            nunit.run = ['Test1', 'Test2']
            nunit.reportFolder = './'
        then:
            nunit.getTestReportPath() == new File(nunit.project.projectDir, "TestResult.xml")
    }

    def "command args contain runs when using nunit 2"() {
        given:
            def nunit = getNUnitTask()
            nunit.nunitVersion = '2.0.0'
            nunit.run = ['Test1', 'Test2']
        when:
            def commandArgs = nunit.getCommandArgs()
        then:
            // list contains does not work with GString
            commandArgs.find { it == '-run:Test1,Test2' }
    }

    def "command args contain tests when using nunit 2"() {
        given:
            def nunit = getNUnitTask()
            nunit.nunitVersion = '2.0.0'
            nunit.test = ['Test1', 'Test2']
        when:
            def commandArgs = nunit.getCommandArgs()
        then:
            commandArgs.find { it == '-run:Test1,Test2' }
    }

    def "command args contain where when using nunit 3"() {
        given:
            def nunit = getNUnitTask()
            nunit.nunitVersion = '3.0.0'
            nunit.where = [ 'test == \'Test1\'', 'test == \'Test2\'']
        when:
            def commandArgs = nunit.getCommandArgs()
        then:
            commandArgs.find { it == '-where:test == \'Test1\' or test == \'Test2\'' }
    }

    def "command args contain test when using nunit 3"() {
        given:
            def nunit = getNUnitTask()
            nunit.nunitVersion = '3.0.0'
            nunit.test = ['Test1', 'Test2']
        when:
            def commandArgs = nunit.getCommandArgs()
        then:
            commandArgs.find { it == '-where:test == \'Test1\' or test == \'Test2\'' }
    }

    def "command args contain runs when using nunit 3"() {
        given:
            def nunit = getNUnitTask()
            nunit.nunitVersion = '3.0.0'
            nunit.run = ['Test1', 'Test2']
        when:
            def commandArgs = nunit.getCommandArgs()
        then:
            commandArgs.find {it == '-where:test == \'Test1\' or test == \'Test2\'' }
    }

    def "command args contain include when using nunit 2"() {
        given:
            def nunit = getNUnitTask()
            nunit.nunitVersion = '2.0.0'
            nunit.include = 'foo'
        when:
            def commandArgs = nunit.getCommandArgs()
        then:
            commandArgs.find { it == '-include:foo' }
    }

    def "exception is thrown when calling include on nunit 3"() {
        given:
            def nunit = getNUnitTask()
            nunit.nunitVersion = '3.0.0'
        when:
            nunit.include = 'foo'
        then:
            thrown GradleException
    }

    def "command args contain shadow copy attribute on nunit 3"() {
        given:
            def nunit = getNUnitTask()
            nunit.nunitVersion = '3.0.0'
            nunit.shadowCopy = true
        when:
            def commandArgs = nunit.getCommandArgs()
        then:
            commandArgs.find { it == '-shadowcopy' }
    }

    def "command args contain noshadow attribute on nunit 2"() {
        given:
            def nunit = getNUnitTask()
            nunit.nunitVersion = '2.0.0'
            nunit.shadowCopy = false
        when:
            def commandArgs = nunit.getCommandArgs()
        then:
            commandArgs.find { it == '-noshadow' }
    }

    def "command args contain all labels"() {
        given:
            def nunit = getNUnitTask()
            nunit.nunitVersion = '3.0.2'
            nunit.labels = 'All'
        when:
            def commandArgs = nunit.getCommandArgs()
        then:
            commandArgs.find { it == '-labels:All' }
    }

    def "command args contain result format"() {
        given:
            def nunit = getNUnitTask()
            nunit.nunitVersion = '3.0.1'
            nunit.resultFormat = 'nunit3'
        when:
            def commandArgs = nunit.getCommandArgs()

        then:
            commandArgs.find { it =~ /-result:.*TestResult\.xml;format=nunit3/ }
    }

    def "running with default command modifier sets path and build args"() {
        when:
            def nunit = getNUnitTask()

            def cmdLine = nunit.getCommandLine('input', 'path')
        then:
            cmdLine == [nunit.getNunitExec().absolutePath, *nunit.buildCommandArgs('input', 'path')]
    }

    def "running with custom command modifier sets path and build args with custom arguments"() {
        when:
            def nunit = getNUnitTask()

            nunit.nunitCommandModifier = { path, args ->
                ['dotMemoryUnit.exe', path, 'custom-args', *args]
            }

            def cmdLine = nunit.getCommandLine('input', 'path')
        then:
            cmdLine == [
                    'dotMemoryUnit.exe',
                    nunit.getNunitExec().absolutePath,
                    'custom-args',
                    *nunit.buildCommandArgs('input', 'path')
            ]
    }

    def getNUnitTask() {
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'nunit'
        return project.tasks.nunit
    }
}
