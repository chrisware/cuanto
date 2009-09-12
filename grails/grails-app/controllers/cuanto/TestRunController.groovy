/*
 Copyright (c) 2008 thePlatform, Inc.

This file is part of Cuanto, a test results repository and analysis program.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package cuanto

import grails.converters.JSON
import java.text.SimpleDateFormat

class TestRunController {
	def parsingService
	def dataService
	def testOutcomeService
	def testRunService
	def testCaseFormatterRegistry

	// the delete, save, update and submit actions only accept POST requests
	static def allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', submit: 'POST', create: 'POST',
		submitFile: 'POST']

	SimpleDateFormat dateFormat = new SimpleDateFormat(Defaults.dateFormat)

	def index = { redirect(action: 'list', controller: 'project', params: params) }

	def delete = {
		def testRun = TestRun.get(params.id)
		def myJson = [:]
		if (testRun) {
			dataService.deleteTestRun(testRun)
		} else {
			response.status = response.SC_NOT_FOUND
			myJson.error = "Test Run ${params.id} was not found"
		}
		render myJson as JSON
	}

	def edit = {
		def testRun = TestRun.get(params.id)

		if (!testRun) {
			flash.message = "TestRun not found with id ${params.id}"
			redirect(controller: project, action: select)
		}
		else {
			return [testRun: testRun]
		}
	}


	def update = {
		def testRun = TestRun.get(params.id)
		def myJson = [:]
		if (testRun) {
			testRun = testRunService.update(testRun, params)
			if (testRun.hasErrors()) {
				myJson.error = "Error updating test run";
			}
		}
		else {
			myJson.error = "TestRun not found with id ${params.id}"
			response.status = response.SC_NOT_FOUND
		}
		render myJson as JSON
	}

	def updateNote = {
		def testRun = TestRun.get(params.id)
		def myJson = [:]
		if (!params.keySet().contains("note")) {
			myJson.error = "The note field was missing"
		} else if (testRun) {
			testRunService.updateNote(testRun, params.note) 
		} else {
			myJson.error = "The test run id field was missing or invalid."
		}
		withFormat {
			json {
				if (myJson.error) {
					response.status = response.SC_NOT_FOUND
					render myJson as JSON
				} else {
					render myJson as JSON
				}
			}
		}
	}

	def submitFile = {
		// get the testRun id from the header (CuantoClient) or the GET param (manual file upload)
		def testRunId = Long.valueOf(request.getHeader("Cuanto-TestRun-Id") ?: params.testRunId)
        def project = TestRun.get(testRunId).project
        if (project.testType.name == 'Manual')
            throw new IllegalAccessException("You may not submit testrun files for a manual project.")
        
		for (fileName in request.getFileNames()) {
			def multipartFileRequest = request.getFile(fileName)
			parsingService.parseFileFromStream(multipartFileRequest.getInputStream(), testRunId)
		}
		testRunService.calculateTestRunStats(TestRun.get(testRunId))
		render ""
	}


	def submitSingleTest = {
		def testRunId = Long.valueOf(request.getHeader("Cuanto-TestRun-Id"))
		def testOutcome = parsingService.parseTestOutcome(request.getInputStream(), testRunId)
		//def myJson = [:]
		//myJson["testOutcomeId"] = testOutcome.id
		//render myJson as JSON
		render testOutcome.id
	}

	def outcomes = {
		def queryParams = [:]

		def possibleQueryParams = ["sort", "order", "max", "offset", "filter"]
		possibleQueryParams.each {possibleParam ->
			if (params.containsKey(possibleParam)) {
				queryParams[possibleParam] = params[possibleParam]
			}
		}

		def outs
		def totalCount
		def recordStartIndex
		def testRun = TestRun.get(params.id)

		if (!testRun) {
			redirect controller: 'project', action: 'list'
		} else {
			if (params.containsKey("recordStartIndex")) {
				recordStartIndex = Integer.valueOf(params.recordStartIndex)
			} else if (params.containsKey("offset")) {
				recordStartIndex = Integer.valueOf(params.offset)
			} else {
				recordStartIndex = 0
			}

			def filter = params.filter

			if (params.qry) {
				totalCount = testRunService.countTestOutcomesBySearch(params)
				outs = testRunService.searchTestOutcomes(params)
			} else if (filter?.equalsIgnoreCase("allFailures") || filter?.equalsIgnoreCase("unanalyzedFailures")) {
				outs = testRunService.getOutcomesForTestRun(testRun, queryParams)
				totalCount = dataService.countFailuresForTestRun(testRun)
			} else if (filter?.equalsIgnoreCase("newFailures")) {
				outs = testRunService.getNewFailures(testRun, queryParams)
				totalCount = testRunService.countNewFailuresForTestRun(testRun)
			} else if (params.outcome) {
				outs = [dataService.getTestOutcome(params.outcome)]
				recordStartIndex = (Integer.valueOf(params.recordStartIndex))
				totalCount = params.totalCount
			} else {
				outs = testRunService.getOutcomesForTestRun(testRun, queryParams)
				totalCount = testRunService.countOutcomes(testRun)
			}
		}
		withFormat {
			json {
				def formatter = testOutcomeService.getTestCaseFormatter(params.tcFormat)
				def jsonOutcomes = []
				outs.each {outcome ->
					jsonOutcomes += [testCase: [name:formatter.getTestName(outcome.testCase.packageName,
						outcome.testCase.testName),	id:outcome.testCase.id],
						result: outcome.testResult.name, analysisState: outcome.analysisState?.name,
						duration: outcome.duration, owner: outcome.owner,
						bug: [title: outcome.bug?.title, url: outcome.bug?.url], note: outcome.note, id: outcome.id]
				}

				def myJson = ['totalCount': totalCount, count: outs?.size(), testOutcomes: jsonOutcomes,
					'offset': recordStartIndex]
				render myJson as JSON
			}
		}
	}


	def outcomeCount = {
		render testOutcomeService.countOutcomes(params)
	}


	def statistics = {
		def testRun = TestRun.get(params.id)
		if (!testRun) {
			redirect(controller: 'project', action: 'list')
		}

		if (params.calculate) {
			testRunService.calculateTestRunStats(testRun)
		}
		
		withFormat {
			json {
				def myJson = [:]
				if (params.insertIndex) {
					myJson['recordInsertIndex'] = Integer.valueOf(params.insertIndex)
					myJson['insertIndex'] = Integer.valueOf(params.insertIndex)
				}

				myJson['results'] = []
				if (params.header) {
					if (!testRun.testRunStatistics) {
						testRunService.calculateTestRunStats(testRun)
					}
					myJson['results'] += testRun.testRunStatistics.toJsonMap()
				}

				render myJson as JSON
			}
			text {
				def testRunMap = [:]
				TestRunStats stats = testRun.testRunStatistics
				if (stats) {
					testRunMap.tests = stats.tests
					testRunMap.passed = stats.passed
					testRunMap.failed = stats.failed
				}
				render(view: 'get', model: ['testRunMap': testRunMap])
			}
		}
	}


	def get = {
		def testRun = TestRun.get(Long.valueOf(params.id))
		def testRunMap = testRun.toJSONWithDateFormat(dateFormat)
		withFormat {
			text {
				['testRunMap': testRunMap]
			}
			json {
				render testRunMap as JSON
			}
		}
	}


	def results = {
		def testRun = null
		if (params.id) {
			testRun = TestRun.get(params.id)
		} else if (params.projectKey) {
			testRun = dataService.getMostRecentTestRunForProjectKey(params.projectKey)
		}

		if (testRun) {
			def analysisStates = ["-None-"]
			analysisStates.addAll(AnalysisState.listOrderByName())

			def pieChartUrl = testRunService.getGoogleChartUrlForTestRunFailures(testRun)
			def bugSummary = testRunService.getBugSummary(testRun)
			def tcFormatList = testCaseFormatterRegistry.formatterList
			return ['testRun': testRun, 'filter': getDefaultFilter(testRun), 'filterList': getFilterList(),
				'testResultList': getJavascriptList(TestResult.listOrderByName()),
				'analysisStateList': getJavascriptList(analysisStates), 'pieChartUrl': pieChartUrl,
				'bugSummary': bugSummary, 'formatters': tcFormatList,
				'project': testRun?.project, 'tcFormat': tcFormatList[0].key]
		} else {
			redirect(controller: 'project', action: 'list')
		}
	}


	def failureChart = {
		if (params.id) {
			def testRun = TestRun.get(Long.valueOf(params.id))
			def pieChartUrl = testRunService.getGoogleChartUrlForTestRunFailures(testRun)
			render(template: "pieChart", model: ['pieChartUrl': pieChartUrl])
		} else {
			redirect(controller: 'project', action: 'list')
		}
	}

	def summaryTable = {
		if (params.id) {
			def testRun = TestRun.get(Long.valueOf(params.id))
			render(template: "summaryTable", model: ['testRun': testRun])
		} else {
			redirect(controller: 'project', action: 'list')
		}
	}

	def bugSummary = {
		if (params.id) {
			def testRun = TestRun.get(Long.valueOf(params.id))
			def bugSummary = testRunService.getBugSummary(testRun)
			render(template: 'bugSummary', model: ['bugSummary': bugSummary])
		} else {
			redirect(controller: 'project', action: 'list')
		}
	}

	private getDefaultFilter(testRun) {
		def filter
		if (testRun.testRunStatistics?.failed == 0 || testRun.project.testType.name == "Manual") {
			filter = "All Results"
		} else {
			filter = "All Failures"
		}
		return filter
	}


	private getFilterList() {
		def filterList = []
		filterList += [id: "allfailures", value: "All Failures"]
		filterList += [id: "unanalyzedfailures", value: "Unanalyzed Failures"]
		filterList += [id: "newfailures", value: "New Failures"]
		filterList += [id: "allresults", value: "All Results"]
		return filterList
	}


	def create = {
		if (!params.project) {
			response.status = 404 // todo is this the right response code? how about invalid request?
			render "project parameter is required"
		} else {
			try {
				def run = testRunService.createTestRun(params)
				render(view: "create", model: ['testRunId': run.id])
			} catch (CuantoException e) {
				response.status = 403
				render e.getMessage()
			}
		}
	}

	def createManual = {
		if (!params.id) {
			flash.message = "Unable to determine project for Test Case"
			redirect(controller: 'project', action: 'list')
		}
		def project = dataService.getProject(Long.valueOf(params.id))
		['project': project]
	}

	def manual = {
		if (!params.id) {
			flash.message = "Unable to determine project"
			redirect(controller: 'project', action: 'list')
		}
		def run = testRunService.createManualTestRun(params)
		flash.message = "Created Test Run ${run.id}."
		redirect(controller: 'project', action: 'history', id: params.id)
	}


	def getJavascriptList(libObj) {
		def javascriptList = "["
		libObj.eachWithIndex {item, idx ->
			javascriptList += "\"${item}\""
			if (idx < libObj.size() - 1) {
				javascriptList += ","
			}
		}
		javascriptList += "]"
		return javascriptList
	}
}
