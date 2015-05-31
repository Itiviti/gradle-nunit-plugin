package com.ullink

import com.ullink.gradle.nunit.NUnit
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import static org.junit.Assert.assertTrue

class NUnitPluginTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Project project

    @Before
    public void init() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'nunit'
    }

     @Test
    public void nunitPluginAddsNUnitTaskToProject() {
        assertTrue(project.tasks.nunit instanceof NUnit)
    }

    @Test
    public void execution_cleanhelp_works() {
        project.apply plugin: 'base'
        project.nunit {
            testAssemblies = ['/help']
        }

        project.tasks.clean.execute()
        project.tasks.nunit.execute()
    }

     @Test
    public void execution_help_works() {
        project.nunit {
            testAssemblies = ['/help']
        }

        project.tasks.nunit.execute()
    }

    @Test
    public void execution_noProject_throwsGradleException() {
        expectedException.expect(GradleException.class);
        expectedException.expectMessage("Execution failed for task ':nunit'.")

        project.tasks.nunit.execute()
    }
}
