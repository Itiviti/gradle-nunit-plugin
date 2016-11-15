package com.ullink.gradle.nunit

import groovy.xml.XmlUtil

import java.nio.charset.StandardCharsets

class NUnitTestResultsMerger {
    void merge(List<File> files, File outputFile) {
        outputFile.write(merge(files.collect { it.text }), StandardCharsets.UTF_8.toString())
    }

    String merge(List<String> stringTestResults) {
        String xmlShell = '<test-results name="Merged results">' +
                '   <test-suite type="Test Project" executed="True" name="" asserts="0">' +
                '       <results/>' +
                '  </test-suite>' +
                '</test-results>'

        def testResults = stringTestResults.collect { new XmlParser().parseText(it) }
        def firstTestResult = testResults.first()

        def baseXml = new XmlParser().parseText(xmlShell)

        baseXml.children().add(0, firstTestResult.environment.first())
        baseXml.children().add(1, firstTestResult.'culture-info'.first())

        def mergedResultsNode = baseXml.'test-suite'.results.first()
        testResults.each { xml -> mergedResultsNode.append(xml.'test-suite') }

        def attributes = ['total', 'errors', 'failures', 'not-run', 'inconclusive', 'skipped', 'invalid', 'ignored'];
        attributes.each {
            baseXml['@' + it] = testResults.inject(0) { r, node -> r + Integer.valueOf(node['@' + it] ?: 0) }
        }

        baseXml.@date = firstTestResult.@date
        baseXml.@time = firstTestResult.@time

        def mergedTestSuite = baseXml.'test-suite'
        mergedTestSuite.@time = testResults.inject(0.0d) { r, node ->
            r + Double.valueOf(node.'test-suite'.first().@time)
        }
        mergedTestSuite.@result = testResults.inject('Success') { r, node ->
            r == 'Failure' ? r : node.'test-suite'.first().@result
        }

        return XmlUtil.serialize(baseXml)
    }
}
