<!doctype html><html><head>
    <link rel="stylesheet" href="output/dep/bootstrap.min-3.3.4.css" type="text/css"/>
    <link rel="stylesheet" href="output/dep/bootstrap-theme.min-3.3.4.css" type="text/css"/>
    <link rel="stylesheet" href="output/dep/app.css" type="text/css"/>
    <link rel="stylesheet" href="output/dep/toastr.min.css" type="text/css"/>

    <script src="output/dep/jquery-latest.min-1.11.1.js"></script>
    <script src="output/dep/d3.v3.min.js"></script>
    <script src="output/dep/d3-timeline-0.0.4.js"></script>
    <script src="output/dep/bootstrap.min-3.3.4.js"></script>
    <script src="output/dep/toastr.min.js"></script>
    <script type="text/javascript">
        window.onload = function () {
            toastr.options = {
                "closeButton": true,
                "debug": false,
                "newestOnTop": false,
                "progressBar": false,
                "positionClass": "toast-top-right",
                "preventDuplicates": false,
                "onclick": null,
                "showDuration": "300",
                "hideDuration": "1000",
                "timeOut": "5000",
                "extendedTimeOut": "1000",
                "showEasing": "swing",
                "hideEasing": "linear",
                "showMethod": "fadeIn",
                "hideMethod": "fadeOut"
            };
            var labelWithTotalTime = [];
            var labelTestData = [
                <% runs.each { suiteLabel, suite -> %>
                { label: '${suiteLabel}', times: [
                    <% suite.runs.each { testRun -> %>
                    {labelx: '<%=testRun.label%>',
                        "starting_time": <%= testRun.startTime?testRun.startTime.toInstant(java.time.ZoneOffset.UTC).toEpochMilli():0%>,
                        "ending_time": <%= testRun.endTime?testRun.endTime.toInstant(java.time.ZoneOffset.UTC).toEpochMilli():0%> },
                    <% } %>
                ] },
                <% } %>
            ];

            function copy(o) {
                var output, v, key;
                output = Array.isArray(o) ? [] : {};
                for (key in o) {
                    v = o[key];
                    output[key] = (typeof v === "object") ? copy(v) : v;
                }
                return output;
            }

            function calculateTotalTime(suites) {
                var newArr = copy(suites);
                for (var i = 0; i < newArr.length; i++) {
                    var totalTestTime = 0;
                    for (var x = 0; x < newArr[i].times.length; x++) {
                        var currentTestTime = (newArr[i].times[x].ending_time - newArr[i].times[x].starting_time);
                        totalTestTime = totalTestTime + currentTestTime;
                    }
                    newArr[i].title = newArr[i].label;
                    suites[i].title = newArr[i].label;
                    newArr[i].label = timeNowNoHours(totalTestTime);
                    suites[i].totalTestCaseTimeTook = timeNowNoHours(totalTestTime);
                }
                return newArr;
            }

            function timeNow(moment) {
                var d = new Date(moment),
                        h = (d.getHours() < 10 ? '0' : '') + d.getHours(),
                        m = (d.getMinutes() < 10 ? '0' : '') + d.getMinutes(),
                        s = (d.getSeconds() < 10 ? '0' : '') + d.getSeconds(),
                        mil = (d.getMilliseconds() < 100 ? (d.getMilliseconds() < 10 ? '0' : '') + '0' : '') + d.getMilliseconds();
                return h + ':' + m + ":" + s + "." + mil;
            }

            function timeNowNoHours(moment) {
                var d = new Date(moment + new Date().getTimezoneOffset() * 60000),
                        m = (d.getMinutes() < 10 ? '0' : '') + d.getMinutes(),
                        s = (d.getSeconds() < 10 ? '0' : '') + d.getSeconds(),
                        mil = (d.getMilliseconds() < 100 ? (d.getMilliseconds() < 10 ? '0' : '') + '0' : '') + d.getMilliseconds();
                return m + ':' + s + "." + mil;
            }

            function timesComparator(a, b) {

                a.totalTime = (a.ending_time - a.starting_time);
                b.totalTime = (b.ending_time - b.starting_time);

                return b.totalTime - a.totalTime;
            }

            function findFlakyTests(times) {
                var occurences = {};

                times.forEach(function (v, i) {
                    if (!occurences[v.labelx]) {
                        occurences[v.labelx] = [i];
                    } else {
                        occurences[v.labelx].push(i)
                    }
                });
                return occurences;
            }


            function toISO8601(minutesAndSeconds) {
                return new Date('1970-01-01T00:' + minutesAndSeconds)
            }

            function sortData() {
                labelWithTotalTime.sort(function (a, b) {
                    return toISO8601(a.label) - toISO8601(b.label);
                });
                labelTestData.sort(function (a, b) {
                    return toISO8601(a.totalTestCaseTimeTook) - toISO8601(b.totalTestCaseTimeTook);
                });
                reRenderChart(labelWithTotalTime, labelTestData);
                toastr["success"]("Sorted data in ascending order")
            }

            function sortDataDescending() {
                labelWithTotalTime.sort(function (a, b) {
                    return toISO8601(b.label) - toISO8601(a.label);
                });
                labelTestData.sort(function (a, b) {
                    return toISO8601(b.totalTestCaseTimeTook) - toISO8601(a.totalTestCaseTimeTook);
                });
                reRenderChart(labelWithTotalTime, labelTestData);
                toastr["success"]("Sorted data in descending order")
            }

            function reRenderChart(labelWithTotalTime, labelTestData) {
                document.getElementById("totalTime").innerHTML = "";
                document.getElementById("timeline").innerHTML = "";
                d3.select("#timeline").append("svg").attr("width", width).datum(labelTestData).call(chart);
                d3.select("#totalTime").append("svg").attr("width", width).datum(labelWithTotalTime).call(chart);
            }

            $("#ascendingOrder").on("click", sortData);
            $("#descendingOrder").on("click", sortDataDescending);

            var width = 900;
            var chart = d3.timeline()
                    .width(width)
                    .stack()
                    .margin({left: 0, right: 30, top: 0, bottom: 0})
                    .click(function (d, i, datum) {
                        jQuery("#testTitle").html(datum.title);
                        var totalTestTime = 0;
                        var testTimes = datum.times.slice();
                        testTimes.sort(timesComparator);
                        var testOccurences = findFlakyTests(testTimes);
                        var s = "<table class='table'><tr><th>Label</th><th>Start time</th><th>End time</th><th>Length (mm:ss.ms)</th></tr>", x = 0;
                        for (; x < testTimes.length; x++) {
                            var currentTestTime = (testTimes[x].ending_time - testTimes[x].starting_time);
                            totalTestTime = totalTestTime + currentTestTime;
                            var flaky = (testOccurences[testTimes[x].labelx].length > 1 ? "Flaky" : " ");
                            s += [
                                "<tr><td>",
                                flaky, " ", testTimes[x].labelx,
                                "</td><td>",
                                timeNow(testTimes[x].starting_time),
                                "</td><td>",
                                timeNow(testTimes[x].ending_time),
                                "</td><td>",
                                timeNowNoHours(currentTestTime),
                                "</td></tr>"
                            ].join("")
                        }
                        s += "</table>"
                        jQuery("#testTime").html(timeNowNoHours(totalTestTime));
                        jQuery("#runTimes").html(s);
                        jQuery('#myModal').modal({
                            keyboard: false
                        });
                    })
                    .scroll(function (x, scale) {
                        jQuery("#scrolled_date").text(scale.invert(x) + " to " + scale.invert(x + width));
                    });

            d3.select("#timeline").append("svg").attr("width", width).datum(labelTestData).call(chart);
            labelWithTotalTime = calculateTotalTime(labelTestData);
            d3.select("#totalTime").append("svg").attr("width", width).datum(labelWithTotalTime).call(chart);
        };

    </script>
</head>

<body>
<br>

<div class="container-fluid">
    <div class="row" style="outline: 1px solid #65cfd4;">
        <div class="col-md-6">
            <img src="/plugin/logParser/img/BW_logo.png">
        </div>
        <div class="col-md-6 pull-right" style="text-align: right;">
            <h4>Log Parser</h4>
        </div>
    </div>
</div>

<div class="container-fluid">
    <div class="row">
        <div class="col-md-6">
            <div id="tableHeading"><h6>Class Name</h6></div>
            <div id="timeline" style="margin-top: -13px;">
            </div>
        </div>
        <div class="col-md-6">
            <div id="tableHeading1">
                <img id="ascendingOrder" src="/plugin/logParser/img/ascending.png" width="12" height="12" style="cursor: pointer;">
                Time taken
                <img id="descendingOrder" src="/plugin/logParser/img/descending.png" width="12" height="12" style="cursor: pointer;">
            </div>
            <div id="totalTime">
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel">Run information</h4>
            </div>
            <div class="modal-body">
                <h3>Title of the test</h3>
                <p id="testTitle"></p>
                <h3>Total running time of the test ( Minutes:Seconds.millis )</h3>
                <p id="testTime"></p>
                <h3>Run times</h3>
                <p id="runTimes"></p>
                <p id="runTable"></p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>
</body>
</html>