class MainClass {

    public static void main(def args) {
		
        println("Printing arguments ");
        for(String arguments : args) {
            println (arguments);
        }
		String workingDir = args[0];
		String buildNumber = args[1];
		outPutURL = workingDir+buildNumber.html
		
		Matcher.run("C:\\ATlogs.txt",outPutURL, Utils.STARTING_PATTERN,Utils.FINISHING_PATTERN,
        Utils.DATE_FORMATTER,Utils.DATE_PATTERN);
    }

} 