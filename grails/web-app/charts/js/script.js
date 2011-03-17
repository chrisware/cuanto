//google.load('visualization', '1', {packages: ['corechart']});

var Cuanto = {
	API_BASE_URL: "../api/" /*"http://ftaps05-01-uvpr-uk-ci:8081/cuanto/api/"*/
}
	var ChartHelper = {
		drawDygraph: function(chart) {
			var dg = new Dygraph.GVizChart(
				this._createContainer()).draw(chart.data, {
				fillGraph:true,
				strokeWidth:1,
				drawPoints:false,
				pointSize:2,
				colors: ['green','green', 'grey'],
				rightGap:50,
				width:"1200px",
				height:"450px",
				showRoller: true//,
				//valueRange:[0,20000]
				//rollPeriod: Math.round(chart.data.D.length/20)
				//stepPlot: true
            });
			
			google.visualization.drawToolbar(container, components);			

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
			$("body").append("<div class='chart'><span id='" + id + "-title'></span><div id='" + id + "'></div></div>" );
			return document.getElementById(id);
		}
		
		
		
	};

/*
function TestRunDurationOverTime(options) {
	this.totalDurationForRun = [];
	
	this.init = function() {
		var url = Cuanto.API_BASE_URL + "getAllTestRuns?projectKey=" + options.projectKey;
		var myself = this;
		
		$.getJSON(url, function(response) {
			$(response.testRuns).each(function(){
				var tr = this;
				var url = Cuanto.API_BASE_URL + "getTestOutcomes?testCase=" + tr.id + "&sort=startDate&order=asc";
				myself.totalDurationForRun[tr.id]=0;
				$.getJSON(url, function(response) {
					$(response.testOutcomes).each(function(){
						var oc = this;
						console.log("duration: " + oc.duration)
						myself.totalDurationForRun[tr.id] += oc.duration;
					});
				});
			});
			console.log(myself.totalDurationForRun);
		});
	} 
}
*/

function PerformanceByTestCaseId(options) {
	this.divId;
	this.testName;  // used for the chart title
	//this.containerId = "_" + new Date().getTime(); //options.containerId;
	
	this.dataMC = new google.visualization.DataTable();
	this.dataMC.addColumn("string","User");
	this.dataMC.addColumn("datetime","Executed");
	this.dataMC.addColumn("number","Duration");
	
	this.dataDG = new google.visualization.DataTable();
	this.dataDG.addColumn("datetime","Executed");
	
	/*this.dataCC = new google.visualization.DataTable();
	this.dataCC.addColumn("datetime","Start Time");
	this.dataCC.addColumn("datetime","Start Time");
	this.dataCC.addColumn("datetime","End Time");*/
	
	this.init = function() {

        var urls = []
        $(options.testCases).each(function(idx){
            urls.push(Cuanto.API_BASE_URL + "getTestOutcomes?testCase=" + testCases[idx] + "&sort=startDate&order=desc&max=200");
        });

      //  console.log(urls[0]());

		var url1 = Cuanto.API_BASE_URL + "getTestOutcomes?testCase=143&sort=startDate&order=desc&max=200";
		/*var url2 = Cuanto.API_BASE_URL + "getTestOutcomes?testCase=73&sort=startDate&order=desc&max=200";
		var url3 = Cuanto.API_BASE_URL + "getTestOutcomes?testCase=180&sort=startDate&order=desc&max=200";*/
		
		var myself = this;
		

		// get the data from Cuanto
		//$.when($.getJSON(url1), $.getJSON(url2), $.getJSON(url3) ).then(function(response1, response2, response3){
        $.when( $.getJSON(urls[0]) ).then(function(response1){
			
			/* 
			startedAt | series1-duration | series2-duration | series3-duration
			12:03:11    4123
			12:03:12                       6525
            12:03:12                                          2302
				
				...etc.
			*/
			
			// add one column for each testCase request (these are the series)
			$(arguments).each(function(idx){
				myself.dataDG.addColumn("number","Response" + idx);
			});
			
			// add one row per testCase, being careful to put the duration in the correct column
			$(arguments).each(function(idx){
				$(this.testOutcomes).each(function(){
					var oc = this;
					var col = idx+1;

					myself.dataDG.setColumnLabel(col,oc.testCase.testName);  // use packageName to group by package, or testName to show individual. interesting...
					
					var startedAt = oc.dateExecuted ? new Date(oc.dateExecuted) : new Date(oc.dateCreated); // x-axis
					var duration = oc.duration; // y-axis

					// build a row with the correct number of gaps depending on which column (series) the duration is to go in
					var rowArrDG = [arguments.length+1];
					
					$(rowArrDG).each(function(){
						rowArrDG[this]=null;
					});
					
					rowArrDG[0] = startedAt; // first column is always the date
					rowArrDG[col] = duration; // put the duration in the correct column
					myself.dataDG.addRow(rowArrDG);
					
					var rowArrMC = [];
					rowArrMC[0] = oc.testCase.testName; 
					rowArrMC[1] = startedAt;
					rowArrMC[2] = duration; // put the duration in the correct column
					
					myself.dataMC.addRow(rowArrMC);
					
				});
				
				
			});

			//myself.drawDygraph();				
			
			
			/*var view1 = new google.visualization.DataView(myself.data);
			view1.setColumns([0,1,2]);
			
			var view2 = new google.visualization.DataView(myself.data);
			view2.setColumns([0,1,3]);
			
			var view3 = new google.visualization.DataView(myself.data);
			view3.setColumns([0,1,4]);
			
			var joinedData = google.visualization.data.join(view1, view2, "full", keys, dt1Columns, dt2Columns)
			*/
			
			//myself.drawDygraph();
			
			//myself.drawMotionChart();
			ChartHelper.drawDygraph({title: myself.testName, data: myself.dataDG});
			
			
			ChartHelper.drawMotionChart({title: myself.testName, data: myself.dataMC});			
			//ChartHelper.drawTable({divId:myself.divId, title: myself.testName, data: myself.dataMC});
				
		
		}, function(){
			//fail
		});
	};
}