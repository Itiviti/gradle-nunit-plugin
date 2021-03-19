package com.ullink.gradle.nunit

import groovy.xml.XmlUtil

import java.nio.charset.StandardCharsets

class NUnitTestResultsMerger {
    void merge(List<File> files, File outputFile) {
        outputFile.write(merge(files.collect { it.text }), StandardCharsets.UTF_8.toString())
    }

    String merge(List<String> stringTestResults) {
        def testResults = stringTestResults.collect { new XmlParser().parseText(it) }
        def isNUnit2ReportMode = testResults.first().name() == 'test-results'

        def mergedXml = isNUnit2ReportMode ? mergeNUnit2(testResults) : mergeNUnit3(testResults)
        return XmlUtil.serialize(mergedXml)
    }

    private double getTestDuration(List<groovy.util.Node> nodesList) {
        return nodesList.inject(0.0d) { r, node ->
            r + getTestDuration(node)
        }
    }

    private double getTestDuration(groovy.util.Node parentNode) {
        return parentNode.inject(0.0d, { duration, node ->
            def currentNodeDuration = 0.0d
            if (node.name() == 'test-suite' && node.@result == "Ignored")
                currentNodeDuration = getTestDuration(node.'results'.first())
            else if (node.name() == 'test-suite') {
                currentNodeDuration = Double.valueOf(node.@time ?: node.@duration)
            } else if (node.name() == 'test-case') {
                def time = node.@time
                currentNodeDuration = Double.valueOf(time ? time : "0")
            } else if (node.name() == 'results') {
                currentNodeDuration = getTestDuration(node.children())
            }
            return duration + currentNodeDuration
        })
    }

    Node mergeNUnit2(def testResults) {
        def firstTestResult = testResults.first()
        String xmlShell = '<test-results name="Merged results">' +
                '   <test-suite type="Test Project" executed="True" name="" asserts="0">' +
                '       <results/>' +
                '  </test-suite>' +
                '</test-results>'

        def baseXml = new XmlParser().parseText(xmlShell)

        baseXml.children().add(0, firstTestResult.environment.first())
        baseXml.children().add(1, firstTestResult.'culture-info'.first())

        def mergedResultsNode = baseXml.'test-suite'.results.first()
        testResults.each { xml -> mergedResultsNode.append(xml.'test-suite') }

        def attributes = ['total', 'errors', 'failures', 'not-run', 'inconclusive', 'skipped', 'invalid', 'ignored']
        attributes.each {
            baseXml['@' + it] = testResults.inject(0) { r, node -> r + Integer.valueOf(node['@' + it] ?: 0) }
        }

        baseXml.@date = firstTestResult.@date
        baseXml.@time = firstTestResult.@time

        def mergedTestSuite = baseXml.'test-suite'
        mergedTestSuite.@time = getTestDuration(testResults)
        mergedTestSuite.@result = testResults.inject('Success') { r, node ->
            r == 'Failure' ? r : node.'test-suite'.first().@result
        }

        return baseXml
    }

    Node mergeNUnit3(def testResults) {
        def firstTestResult = testResults.first()
        String xmlShell = '<test-run id="0" name="Merged results"></test-run>'

        def baseXml = new XmlParser().parseText(xmlShell)

        if (firstTestResult.environment.any()) {
            baseXml.children().add(0, firstTestResult.environment.first())
        }

        testResults.each { xml ->
            def testSuites = xml.'test-suite'
            testSuites.each { testSuite ->
                baseXml.append(testSuite)
            }
        }
        def attributes = ['total', 'errors', 'failures', 'not-run', 'inconclusive', 'skipped', 'invalid', 'ignored']
        attributes.each {
            baseXml['@' + it] = testResults.inject(0) { r, node -> r + Integer.valueOf(node['@' + it] ?: 0) }
        }

        def startTimeAttributeName = '@start-time'
        def endTimeAttributeName = '@end-time'
        baseXml[startTimeAttributeName] = testResults.sort { it[startTimeAttributeName] }.first()[startTimeAttributeName]
        baseXml[endTimeAttributeName] = testResults.sort { it[endTimeAttributeName] }.last()[endTimeAttributeName]

        def mergedTestSuite = baseXml.'test-suite'
        mergedTestSuite.@time = getTestDuration(testResults)
        mergedTestSuite.@result = testResults.inject('Success') { r, node ->
            r == 'Failure' ? r : node.'test-suite'.first().@result
        }

        return baseXml
    }
}
