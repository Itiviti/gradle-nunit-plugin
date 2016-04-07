package com.ullink.gradle.nunit
import org.gradle.api.Plugin
import org.gradle.api.Project

class NUnitBasePlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'de.undercouch.download'
        project.tasks.withType(NUnit).whenTaskAdded { NUnit task ->
            applyNunitConventions(task, project)
            task.metaClass.mixin NUnit2Mixins
        }
    }

    def applyNunitConventions(NUnit task, Project project) {
        task.conventionMapping.map "nunitDownloadUrl", { "https://github.com/nunit/${task.isV3 ? 'nunit' : 'nunitv2'}/releases/download" }
        task.conventionMapping.map "nunitVersion", { '2.6.4' }
        task.conventionMapping.map "nunitHome", {
            if (System.getenv()['NUNIT_HOME']) {
                return System.getenv()['NUNIT_HOME']
            }
        }
        if (project.plugins.hasPlugin('msbuild')) {
            task.dependsOn project.tasks.msbuild
            task.conventionMapping.map "testAssemblies", {
                project.tasks.msbuild.projects.findAll {
                    it.key =~ 'test' && it.value.properties.TargetPath
                }
                .collect {
                    it.value.getProjectPropertyPath('TargetPath')
                }
            }
        }
    }
}
