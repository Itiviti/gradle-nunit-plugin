package com.ullink

import static groovy.test.GroovyAssert.shouldFail

import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

public class NUnitTest {

    @Test
    public void getTestInputAsList_works() {
        def nunit = getNUnitTask()
        assert nunit.getTestInputAsList('A,B,C') == ['A', 'B', 'C']
        assert nunit.getTestInputAsList('A') == ['A']
        assert nunit.getTestInputAsList(['A', 'B', 'C']) == ['A', 'B', 'C']
        assert nunit.getTestInputAsList(['A']) == ['A']

        def emptyStringList = nunit.getTestInputAsList('')
        assert (emptyStringList instanceof List)
        assert !emptyStringList

        def emptyListList = nunit.getTestInputAsList([])
        assert (emptyListList instanceof List)
        assert !emptyListList

        def nullList = nunit.getTestInputAsList(null)
        assert (nullList instanceof List)
        assert !nullList
    }

    @Test
    public void whenNUnit2_getTestInputsAsString_works() {
        def nunit = getNUnitTask()
        nunit.nunitVersion = '2.0.0'
        assert nunit.getTestInputsAsString('A,B,C') == 'A,B,C'
        assert nunit.getTestInputsAsString('A') == 'A'
        assert nunit.getTestInputsAsString(['A', 'B', 'C']) == 'A,B,C'
        assert nunit.getTestInputsAsString(['A']) == 'A'
        assert nunit.getTestInputsAsString('') == ''
        assert nunit.getTestInputsAsString([]) == ''
        assert nunit.getTestInputsAsString(null) == ''
    }

    @Test
    public void whenNUnit3_getTestInputsAsString_works() {
        def nunit = getNUnitTask()
        nunit.nunitVersion = '3.0.0'
        assert nunit.getTestInputsAsString('A,B,C') == 'A,B,C'
        assert nunit.getTestInputsAsString('A') == 'A'
        assert nunit.getTestInputsAsString(['A', 'B', 'C']) == 'A or B or C'
        assert nunit.getTestInputsAsString(['A']) == 'A'
        assert nunit.getTestInputsAsString('') == ''
        assert nunit.getTestInputsAsString([]) == ''
        assert nunit.getTestInputsAsString(null) == ''
    }

    @Test
    public void whenSingleTest_notParallel_singleTestReport(){
        def nunit = getNUnitTask()
        nunit.reportFileName = 'TestResult.xml'
        nunit.parallelForks = true
        nunit.run = 'Test1'
        nunit.reportFolder = './'

        assert nunit.getTestReportPath() == new File(nunit.project.projectDir, "TestResult.xml")
    }

    @Test
    public void whenSingleTest_parallel_singleTestReport(){
        def nunit = getNUnitTask()
        nunit.reportFileName = 'TestResult.xml'
        nunit.parallelForks = true
        nunit.run = 'Test1'
        nunit.reportFolder = './'

        assert nunit.getTestReportPath() == new File(nunit.project.projectDir, "TestResult.xml")
    }

    @Test
    public void whenMultipleTests_notParallel_singleTestReport(){
        def nunit = getNUnitTask()
        nunit.reportFileName = 'TestResult.xml'
        nunit.parallelForks = false
        nunit.run = ['Test1', 'Test2']
        nunit.reportFolder = './'

        assert nunit.getTestReportPath() == new File(nunit.project.projectDir, "TestResult.xml")
    }

    @Test
    public void whenMultipleTests_parallel_singleTestReport(){
        def nunit = getNUnitTask()
        nunit.reportFileName = 'TestResult.xml'
        nunit.parallelForks = true
        nunit.run = ['Test1', 'Test2']
        nunit.reportFolder = './'

        assert nunit.getTestReportPath() == new File(nunit.project.projectDir, "TestResult.xml")
    }

    @Test
    public void whenNUnit2_run_runSpecified() {
        def nunit = getNUnitTask()
        nunit.nunitVersion = '2.0.0'
        nunit.run = ['Test1', 'Test2']

        def commandArgs = nunit.getCommandArgs()

        // list contains does not work with GString
        assert commandArgs.find { it == '-run:Test1,Test2' }
    }

    @Test
    public void whenNUnit2_test_runSpecified() {
        def nunit = getNUnitTask()
        nunit.nunitVersion = '2.0.0'
        nunit.test = ['Test1', 'Test2']

        def commandArgs = nunit.getCommandArgs()

        assert commandArgs.find { it == '-run:Test1,Test2' }
    }

    @Test
    public void whenNUnit3_where_whereSpecified() {
        def nunit = getNUnitTask()
        nunit.nunitVersion = '3.0.0'
        nunit.where = [ 'test == \'Test1\'', 'test == \'Test2\'']

        def commandArgs = nunit.getCommandArgs()

        assert commandArgs.find { it == '-where:test == \'Test1\' or test == \'Test2\'' }
    }

    @Test
    public void whenNUnit3_test_whereSpecified() {
        def nunit = getNUnitTask()
        nunit.nunitVersion = '3.0.0'
        nunit.test = ['Test1', 'Test2']

        def commandArgs = nunit.getCommandArgs()

        assert commandArgs.find { it == '-where:test == \'Test1\' or test == \'Test2\'' }
    }

    @Test
    public void whenNUnit3_run_whereSpecified() {
        def nunit = getNUnitTask()
        nunit.nunitVersion = '3.0.0'
        nunit.run = ['Test1', 'Test2']

        def commandArgs = nunit.getCommandArgs()

        assert commandArgs.find {it == '-where:test == \'Test1\' or test == \'Test2\'' }
    }

    @Test
    public void whenNUnit2_include_shouldPass() {
        def nunit = getNUnitTask()
        nunit.nunitVersion = '2.0.0'
        nunit.include = 'foo'

        def commandArgs = nunit.getCommandArgs()

        assert commandArgs.find { it == '-include:foo' }
    }

    @Test
    public void whenNUnit3_include_shouldFail() {
        def nunit = getNUnitTask()
        nunit.nunitVersion = '3.0.0'

        shouldFail(GradleException) {
            nunit.include = 'foo'
        }
    }

    @Test
    public void whenNUnit3_shadowcopy_shadowcopy() {
        def nunit = getNUnitTask()
        nunit.nunitVersion = '3.0.0'
        nunit.shadowCopy = true

        def commandArgs = nunit.getCommandArgs()

        assert commandArgs.find { it == '-shadowcopy' }
    }

    @Test
    public void whenNUnit2_notShadowcopy_noshadow() {
        def nunit = getNUnitTask()
        nunit.nunitVersion = '2.0.0'
        nunit.shadowCopy = false

        def commandArgs = nunit.getCommandArgs()

        assert commandArgs.find { it == '-noshadow' }
    }

    @Test
    public void whenNUnit3_AllLabels_AlllabelsIsPassed() {
        def nunit = getNUnitTask()
        nunit.nunitVersion = '3.0.2'
        nunit.labels = 'All'

        def commandArgs = nunit.getCommandArgs()

        assert commandArgs.find { it == '-labels:All' }
    }

    @Test
    public void whenNUnit3_setResultFormat_resultFormatIsSet()
    {
        def nunit = getNUnitTask()
        nunit.nunitVersion = '3.0.1'
        nunit.resultFormat = 'nunit3'

        def commandArgs = nunit.getCommandArgs()

        assert commandArgs.find { it =~ /-result:.*TestResult\.xml;format=nunit3/ }
    }

    def getNUnitTask() {
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'nunit'
        return project.tasks.nunit
    }
}
