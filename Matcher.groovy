
import groovy.transform.Synchronized

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

class Matcher {

    @Synchronized
    static
    def run(String urlOrFileLocation, String fileLocation, Pattern startingPattern,
            Pattern endingPattern, DateTimeFormatter formatter, Pattern timePattern) {
        def generator = new Generator()
        def at = urlOrFileLocation.startsWith("http") ? new URL(urlOrFileLocation).text : new File(urlOrFileLocation).text
        def lines = at.readLines()
        lines.each { line ->
            def matcherStart = line =~ startingPattern
            if (matcherStart) {
                def matchedQualifier = matcherStart[0] as List<String>
                generator.testStarted(matchedQualifier[1], matchedQualifier.size() > 2 ? matchedQualifier[2] : matchedQualifier[1],
                        extractTime(urlOrFileLocation, line, timePattern, formatter))
            } else {
                def matcherFinish = line =~ endingPattern
                if (matcherFinish) {
                    def matchedQualifier = matcherFinish[0] as List<String>
                    generator.testEnded(matchedQualifier[1], matchedQualifier.size() > 2 ? matchedQualifier[2] : matchedQualifier[1],
                            extractTime(urlOrFileLocation, line, timePattern, formatter))
                }
            }
        }
        generator.complete(fileLocation)
    }

    static
    def run(HashMap<String, String> urlOrFileLocations, String fileLocation,
            Pattern startingPattern, Pattern endingPattern, DateTimeFormatter formatter, Pattern timePattern, PrintStream printStream) {
        def generator = new Generator()

        urlOrFileLocations.each { k, v ->
            processUrlOrFile(generator, k, v, startingPattern, endingPattern, formatter, timePattern, printStream)
        }

        generator.complete(fileLocation)
    }

    private
    static processUrlOrFile(Generator generator, String at_name, String urlOrFileLocation,
                            Pattern startingPattern, Pattern endingPattern, DateTimeFormatter formatter, Pattern timePattern, PrintStream printStream) {
        def at = urlOrFileLocation.startsWith("http") ? new URL(urlOrFileLocation).text : new File(urlOrFileLocation).text
        def lines = at.readLines()
        lines.each { line ->
            def matcherStart = line =~ startingPattern
            if (matcherStart) {
                def matchedQualifier = matcherStart[0] as List<String>
                generator.testStarted(at_name + " > " + matchedQualifier[1], matchedQualifier.size() > 2 ? matchedQualifier[2] : matchedQualifier[1],
                        extractTime(at_name, line, timePattern, formatter), printStream)
            } else {
                def matcherFinish = line =~ endingPattern
                if (matcherFinish) {
                    def matchedQualifier = matcherFinish[0] as List<String>
                    generator.testEnded(at_name + " > " + matchedQualifier[1], matchedQualifier.size() > 2 ? matchedQualifier[2] : matchedQualifier[1],
                            extractTime(at_name, line, timePattern, formatter), printStream)
                }
            }
        }
    }

    private static LocalDateTime extractTime(String idOfTheSource, String line, Pattern timePattern, DateTimeFormatter formatter) {
        try {
            LocalDateTime.parse(((line =~ timePattern)[0] as List<String>)[1], formatter)
        } catch (Exception e) {
            throw new IllegalStateException("Parsing of $idOfTheSource failed with line=$line, timePattern=$timePattern, formatter=$formatter", e)
        }
    }
}