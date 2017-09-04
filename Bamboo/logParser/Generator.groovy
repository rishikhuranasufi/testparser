import groovy.text.StreamingTemplateEngine
import groovy.transform.ToString

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class Generator {

    @ToString
    private static class SuiteRun {
        def String label
        def SingleTestRun[] runs
    }

    @ToString
    private static class SingleTestRun {
        def String label
        def LocalDateTime startTime
        def LocalDateTime endTime
    }

    private static def thresholdSec = 0

    private def context = new HashMap<String, HashMap<String, LocalDateTime>>()

    private def runs = new LinkedHashMap<String, SuiteRun>()

    void testStarted(String suiteName, String testName, LocalDateTime start) {
        //printStream.println("Starting $suiteName :: $testName")
		println("Starting $suiteName :: $testName");
        context.putIfAbsent(suiteName, new HashMap<String, LocalDateTime>())
        def entireSuite = context[suiteName]
        entireSuite[testName] = start
        runs.putIfAbsent(suiteName, new SuiteRun(label: suiteName, runs: []))
        runs[suiteName].runs += new SingleTestRun(label: testName, startTime: start)
    }

    void testEnded(String suiteName, String testName, LocalDateTime end) {
        //printStream.println("Ending $suiteName :: $testName")
		println("Ending $suiteName :: $testName");
        def testInfo = context[suiteName]
        def begin = testInfo.remove(testName)
        if (begin) {
            def timeTaken = begin.until(end, ChronoUnit.SECONDS)
            if (timeTaken > thresholdSec) {
                runs[suiteName].runs.find { it.label == testName && it.endTime == null }.endTime = end
            } else {
                runs[suiteName].runs = runs[suiteName].runs.findAll { it.label != testName }
            }
            context.remove(suiteName)
        }
    }

    def complete(String outputFile) {
        if (!context.isEmpty())
            System.err.println("Context wasn't cleared properly!" + context)
        this.class.getResourceAsStream("/template.gsp").withReader {
            def template = new StreamingTemplateEngine().createTemplate(it)
            new File(outputFile).text = template.make([runs: runs])
        }
    }
}
