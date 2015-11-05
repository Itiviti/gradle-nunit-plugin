package com.ullink.gradle.nunit

import org.junit.Before

import static org.junit.Assert.*
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class NUnitPluginTest {

    private Project project

    @Before
    public void init() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'nunit'
    }

    @Test
    public void nunitPluginAddsNUnitTasksToProject() {        
        assertTrue(project.tasks.nunit instanceof NUnit)
    }

    @Test
    public void testExecute() {      
        project.nunit {
            testAssemblies = ['TestA.dll']
            customReportFolder = 'c:\\temp\\reports'
        } 

        assertEquals('c:\\temp\\reports', project.tasks.nunit.reportsFolder.absolutePath)
        project.tasks.nunit.outputs.files.each( { assertEquals('C:\\temp\\reports\\TestResult.xml', it.absolutePath) })
    }

}
