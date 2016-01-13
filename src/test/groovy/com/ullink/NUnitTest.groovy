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
    }

    def getNUnitTask() {
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'nunit'
        return project.tasks.nunit
    }
}
