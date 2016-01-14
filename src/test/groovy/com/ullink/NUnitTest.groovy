package com.ullink

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
    public void getTestInputsAsString_works() {
        def nunit = getNUnitTask()
        assert nunit.getTestInputsAsString('A,B,C') == 'A,B,C'
        assert nunit.getTestInputsAsString('A') == 'A'
        assert nunit.getTestInputsAsString(['A', 'B', 'C']) == 'A,B,C'
        assert nunit.getTestInputsAsString(['A']) == 'A'
        assert nunit.getTestInputsAsString('') == ''
        assert nunit.getTestInputsAsString([]) == ''
        assert nunit.getTestInputsAsString(null) == ''
    }

    @Test
    public void whenSingleTest_notParallel_singleOutputFile(){
        def nunit = getNUnitTask()
        nunit.reportFileName = 'TestResult.xml'
        nunit.parallel_forks = true
        nunit.run = 'Test1'
        nunit.reportFolder = '.\\'

        assert nunit.getOutputFiles() == [new File("${nunit.project.projectDir}\\TestResult.xml")]
    }

    @Test
    public void whenSingleTest_Parallel_singleOutputFile(){
        def nunit = getNUnitTask()
        nunit.reportFileName = 'TestResult.xml'
        nunit.parallel_forks = true
        nunit.run = 'Test1'
        nunit.reportFolder = '.\\'

        assert nunit.getOutputFiles() == [new File("${nunit.project.projectDir}\\TestResult.xml")]
    }

    @Test
    public void whenMultipleTests_notParallel_singleOutputFile(){
        def nunit = getNUnitTask()
        nunit.reportFileName = 'TestResult.xml'
        nunit.parallel_forks = false
        nunit.run = ['Test1', 'Test2']
        nunit.reportFolder = '.\\'

        assert nunit.getOutputFiles() == [new File("${nunit.project.projectDir}\\TestResult.xml")]
    }

    @Test
    public void whenMultipleTests_Parallel_multipleOutputFiles(){
        def nunit = getNUnitTask()
        nunit.reportFileName = 'TestResult_<<TEST_RESULT>>.xml'
        nunit.parallel_forks = true
        nunit.run = ['Test1', 'Test2']
        nunit.reportFolder = '.\\'

        assert nunit.getOutputFiles() == [new File("${nunit.project.projectDir}\\TestResult_Test1.xml"),
                                          new File("${nunit.project.projectDir}\\TestResult_Test2.xml")]
    }

    def getNUnitTask() {
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'nunit'
        return project.tasks.nunit
    }
}
