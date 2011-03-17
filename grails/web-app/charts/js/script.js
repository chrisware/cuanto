var Cuanto = {
    API_BASE_URL: "../api/" /*"http://ftaps05-01-uvpr-uk-ci:8081/cuanto/api/"*/
}

var ChartHelper = {
    drawDygraph: function(chart) {
        var container = this._createContainer();
        $(container).prev("span").text(chart.title);
        var dg = new Dygraph.GVizChart(container).draw(chart.data, {
            fillGraph:true,
            strokeWidth:1,
            drawPoints:false,
            pointSize:2,
            colors: ['green','grey'],
            rightGap:50,
            width:"1200px",
            height:"450px",
            showRoller: true,
            rollPeriod: Math.round(chart.data.D.length/20)
            //stepPlot: true
        });

        //google.visualization.drawToolbar(container, components);

    },

    drawMotionChart: function (chart) {
        var mc = new google.visualization.MotionChart(this._createContainer());
        mc.draw(chart.data, {width: 1200, height:450});
    },


    drawTable: function (chart) {
        var table = new google.visualization.Table(document.getElementById('table_div'));
        table.draw(chart.data, {showRowNumber: true});
    },

    _createContainer: function () {
        var id = "chart-" + new Date().getTime();
        $("body").append("<div class='chart'><span id='" + id + "-title'></span><div id='" + id + "'></div></div>");
        return document.getElementById(id);
    }



};


function PerformanceByTestCaseId(options) {
    this.divId;
    this.testName;  // used for the chart title
    this.data = new google.visualization.DataTable();
    this.data.addColumn("datetime", "Executed");
    this.data.addColumn("number", "Duration");

    this.init = function() {
        var myself = this;
        var url1 = Cuanto.API_BASE_URL + "getTestOutcomes?testCase=" + options.testCases[0] + "&sort=startDate&order=desc&max=200";
        $.getJSON(url1, function(data) {
            $(data.testOutcomes).each(function() {
                var oc = this;
                myself.testName = oc.testCase.testName;
                myself.data.setColumnLabel(1, oc.testCase.testName);
                var startedAt = oc.dateExecuted ? new Date(oc.dateExecuted) : new Date(oc.dateCreated); // x-axis
                var duration = oc.duration; // y-axis
                myself.data.addRow([startedAt, duration]);
            });
            ChartHelper.drawDygraph({title: myself.testName, data: myself.data});
        });
    }
}