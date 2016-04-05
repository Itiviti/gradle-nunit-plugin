package com.ullink.gradle.nunit

import groovy.xml.XmlUtil

import java.text.NumberFormat

class NUnitTestResultsMerger {

    private static final DefaultNumberFormat = NumberFormat.getInstance(new Locale('en', 'US'))

    String merge(List<String> stringTestResults, boolean takeCultureFromFile) {
        def testResults = stringTestResults.collect { new XmlParser().parseText(it) }
        def firstTestResult = testResults.first()
        def baseXml = new XmlParser().parseText(
                '<test-results name="Merged results">' +
                '   <test-suite type="Test Project" executed="True" name="" asserts="0">' +
                '       <results/>' +
                '  </test-suite>' +
                '</test-results>'
        )

        baseXml.children().add(0, firstTestResult.environment.first())
        def cultureInfo = firstTestResult.'culture-info'.first()

        NumberFormat inputNumberFormat = GetInputNumberFormat(takeCultureFromFile, cultureInfo)
        NumberFormat outputNumberFormat = DefaultNumberFormat

        cultureInfo.@'current-culture' = 'en-US'
        baseXml.children().add(1, cultureInfo)

        def mergedResultsNode = baseXml.'test-suite'.results.first()
        testResults.each { xml -> mergedResultsNode.append(xml.'test-suite') }

        def attributes = ['total', 'errors', 'failures', 'not-run', 'inconclusive', 'skipped', 'invalid', 'ignored'];
        attributes.each {
            baseXml['@' + it] = outputNumberFormat.format(testResults.inject(0) { r, node -> r + inputNumberFormat.parse(node['@' + it] ?: 0) })
        }

        baseXml.@date = firstTestResult.@date
        baseXml.@time = firstTestResult.@time

        def mergedTestSuite = baseXml.'test-suite'
        mergedTestSuite.@time = inputNumberFormat.format(testResults.inject(0.0d) { r, node ->
            r + inputNumberFormat.parse(node.'test-suite'.first().@time)
        })

        GetElementsWithTime(baseXml).each {
            it.@time = outputNumberFormat.format(inputNumberFormat.parse(it.@time))
        }

        mergedTestSuite.@result = testResults.inject('Success') { r, node ->
            r == 'Failure' ? r : node.'test-suite'.first().@result
        }

        return XmlUtil.serialize(baseXml)
    }

    private static List<Node> GetElementsWithTime(Node node) {
        def result = []
        node.children().each {
            if(it instanceof  Node){
                if (it.name() == 'test-suite'
                    || it.name() == 'test-case') {
                    result.add(it)
                }
                result.addAll(GetElementsWithTime(it))
            }
        }
        result
    }

    private static NumberFormat GetInputNumberFormat(boolean takeCultureFromFile, cultureInfo) {
        if (takeCultureFromFile) {
            def splitCurrentCulture = cultureInfo.@'current-culture'.split('-')
            return NumberFormat.getInstance(new Locale(
                    splitCurrentCulture[0], // language code
                    splitCurrentCulture[1]))// country code
        } else {
            return DefaultNumberFormat
        }
    }
}
